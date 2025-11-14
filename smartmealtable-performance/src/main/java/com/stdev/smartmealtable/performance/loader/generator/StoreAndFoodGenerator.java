package com.stdev.smartmealtable.performance.loader.generator;

import com.stdev.smartmealtable.performance.config.PerformanceLoadProperties.FoodProperties;
import com.stdev.smartmealtable.performance.loader.result.DataLoadResult;
import com.stdev.smartmealtable.performance.loader.util.RunTag;
import com.stdev.smartmealtable.performance.loader.util.WorkChunk;
import com.stdev.smartmealtable.performance.loader.util.WorkPartitioner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates store and food rows using multi-threaded batch writes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StoreAndFoodGenerator {

    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_STORE_SQL = """
            INSERT INTO store (
                external_id, name, address, lot_number_address,
                latitude, longitude, phone_number, description,
                average_price, review_count, view_count, favorite_count,
                store_type, image_url, registered_at, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
            """;

    private static final String INSERT_FOOD_SQL = """
            INSERT INTO food (
                store_id, category_id, food_name, price,
                description, image_url, is_main, display_order,
                registered_dt, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
            """;

    public StoreDataSet generate(String runId, FoodProperties props, List<Long> categoryIds,
                                 int threadCount, int batchSize) {
        if (categoryIds.isEmpty()) {
            throw new IllegalStateException("At least one category is required before generating stores/foods");
        }

        StoreInsertResult storeInsertResult = insertStores(runId, props, categoryIds, threadCount, batchSize);
        List<StoreSummary> stores = fetchStores(runId, categoryIds);
        FoodInsertResult foodInsertResult = insertFoods(runId, props, stores, categoryIds, threadCount, batchSize);
        return new StoreDataSet(stores, storeInsertResult.summary(), foodInsertResult.summary());
    }

    private StoreInsertResult insertStores(String runId, FoodProperties props, List<Long> categoryIds,
                                           int threadCount, int batchSize) {
        Instant start = Instant.now();
        List<WorkChunk> chunks = WorkPartitioner.partition(props.getStores(), threadCount);
        if (chunks.isEmpty()) {
            return new StoreInsertResult(0, new DataLoadResult("store", 0, Duration.ZERO));
        }

        ExecutorService executor = Executors.newFixedThreadPool(chunks.size());
        try {
            List<Future<Long>> futures = new ArrayList<>();
            for (WorkChunk chunk : chunks) {
                futures.add(executor.submit(new StoreInsertTask(chunk, batchSize, runId, categoryIds)));
            }
            long inserted = waitAndSum(futures, "store");
            Duration took = Duration.between(start, Instant.now());
            DataLoadResult summary = new DataLoadResult("store", inserted, took);
            log.info("[store] inserted={} rows in {} ms", inserted, took.toMillis());
            return new StoreInsertResult(inserted, summary);
        } finally {
            executor.shutdown();
        }
    }

    private FoodInsertResult insertFoods(String runId, FoodProperties props, List<StoreSummary> stores,
                                         List<Long> categoryIds, int threadCount, int batchSize) {
        Instant start = Instant.now();
        if (stores.isEmpty()) {
            return new FoodInsertResult(0, new DataLoadResult("food", 0, Duration.ZERO));
        }

        List<List<StoreSummary>> partitions = partitionStores(stores, threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(partitions.size());
        try {
            List<Future<Long>> futures = new ArrayList<>();
            for (List<StoreSummary> partition : partitions) {
                futures.add(executor.submit(new FoodInsertTask(partition, props, batchSize, runId, categoryIds)));
            }
            long inserted = waitAndSum(futures, "food");
            Duration took = Duration.between(start, Instant.now());
            DataLoadResult summary = new DataLoadResult("food", inserted, took);
            log.info("[food] inserted={} rows in {} ms", inserted, took.toMillis());
            return new FoodInsertResult(inserted, summary);
        } finally {
            executor.shutdown();
        }
    }

    private long waitAndSum(List<Future<Long>> futures, String label) {
        long inserted = 0;
        for (Future<Long> future : futures) {
            try {
                inserted += future.get();
            } catch (Exception ex) {
                throw new IllegalStateException(label + " insert task failed", ex);
            }
        }
        return inserted;
    }

    private List<StoreSummary> fetchStores(String runId, List<Long> categoryIds) {
        return jdbcTemplate.query(
                "SELECT store_id, external_id, name FROM store WHERE external_id LIKE ? ORDER BY store_id",
                ps -> ps.setString(1, RunTag.storePattern(runId)),
                (rs, rowNum) -> {
                    long storeId = rs.getLong("store_id");
                    String externalId = rs.getString("external_id");
                    long sequence = RunTag.extractSequence(externalId);
                    long categoryId = categoryIds.get((int) Math.floorMod(sequence, categoryIds.size()));
                    return new StoreSummary(storeId, rs.getString("name"), categoryId);
                }
        );
    }

    private static List<List<StoreSummary>> partitionStores(List<StoreSummary> stores, int partitions) {
        if (stores.isEmpty() || partitions <= 1) {
            return List.of(stores);
        }
        int size = stores.size();
        int chunkSize = Math.max(1, size / partitions);
        List<List<StoreSummary>> result = new ArrayList<>();
        for (int i = 0; i < size; i += chunkSize) {
            int end = Math.min(size, i + chunkSize);
            result.add(stores.subList(i, end));
        }
        return result;
    }

    private class StoreInsertTask implements Callable<Long> {
        private final WorkChunk chunk;
        private final int batchSize;
        private final String runId;
        private final List<Long> categoryIds;

        private StoreInsertTask(WorkChunk chunk, int batchSize, String runId, List<Long> categoryIds) {
            this.chunk = chunk;
            this.batchSize = batchSize;
            this.runId = runId;
            this.categoryIds = categoryIds;
        }

        @Override
        public Long call() {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            List<Object[]> batch = new ArrayList<>(batchSize);
            long inserted = 0;
            for (long offset = 0; offset < chunk.size(); offset++) {
                long sequence = chunk.startInclusive() + offset;
                batch.add(buildStoreRow(sequence, random));
                if (batch.size() == batchSize) {
                    jdbcTemplate.batchUpdate(INSERT_STORE_SQL, batch);
                    inserted += batch.size();
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                jdbcTemplate.batchUpdate(INSERT_STORE_SQL, batch);
                inserted += batch.size();
            }
            return inserted;
        }

        private Object[] buildStoreRow(long sequence, ThreadLocalRandom random) {
            double latitude = 37.0 + random.nextDouble(-0.8, 0.8);
            double longitude = 127.0 + random.nextDouble(-0.8, 0.8);
            String name = "Perf Store " + sequence;
            String address = "Seoul Performance-gu Data-ro " + (100 + random.nextInt(900));
            Timestamp registeredAt = Timestamp.valueOf(LocalDateTime.now().minusDays(random.nextInt(365)));
            return new Object[]{
                    RunTag.storeExternalId(runId, sequence),
                    name,
                    address,
                    address,
                    latitude,
                    longitude,
                    String.format("010-%04d-%04d", random.nextInt(10000), random.nextInt(10000)),
                    "Performance seed store",
                    5000 + random.nextInt(20000),
                    random.nextInt(5000),
                    random.nextInt(10_000, 200_000),
                    random.nextInt(1_000),
                    random.nextBoolean() ? "RESTAURANT" : "CAMPUS_RESTAURANT",
                    "https://img.smartmealtable/perf/" + sequence,
                    registeredAt
            };
        }
    }

    private class FoodInsertTask implements Callable<Long> {
        private final List<StoreSummary> stores;
        private final FoodProperties props;
        private final int batchSize;
        private final String runId;
        private final List<Long> categoryIds;

        private FoodInsertTask(List<StoreSummary> stores, FoodProperties props, int batchSize,
                               String runId, List<Long> categoryIds) {
            this.stores = stores;
            this.props = props;
            this.batchSize = batchSize;
            this.runId = runId;
            this.categoryIds = categoryIds;
        }

        @Override
        public Long call() {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            List<Object[]> batch = new ArrayList<>(batchSize);
            long inserted = 0;
            for (StoreSummary store : stores) {
                for (int i = 0; i < props.getFoodsPerStore(); i++) {
                    batch.add(buildFoodRow(store, i, random));
                    if (batch.size() == batchSize) {
                        jdbcTemplate.batchUpdate(INSERT_FOOD_SQL, batch);
                        inserted += batch.size();
                        batch.clear();
                    }
                }
            }
            if (!batch.isEmpty()) {
                jdbcTemplate.batchUpdate(INSERT_FOOD_SQL, batch);
                inserted += batch.size();
            }
            return inserted;
        }

        private Object[] buildFoodRow(StoreSummary store, int menuIndex, ThreadLocalRandom random) {
            long categoryId = random.nextBoolean() ? store.primaryCategoryId() :
                    categoryIds.get(random.nextInt(categoryIds.size()));
            int price = random.nextInt(props.getMinPrice(), props.getMaxPrice());
            Timestamp registered = Timestamp.valueOf(LocalDateTime.now().minusDays(random.nextInt(180)));
            return new Object[]{
                store.storeId(),
                categoryId,
                RunTag.foodName(runId, store.storeId(), menuIndex),
                price,
                "Performance dataset menu",
                "https://img.smartmealtable/perf/menu/" + store.storeId() + '-' + menuIndex,
                random.nextBoolean(),
                menuIndex,
                registered
            };
        }
    }

    public record StoreSummary(long storeId, String name, long primaryCategoryId) {}

    private record StoreInsertResult(long inserted, DataLoadResult summary) {}

    private record FoodInsertResult(long inserted, DataLoadResult summary) {}

    public record StoreDataSet(List<StoreSummary> stores, DataLoadResult storeSummary, DataLoadResult foodSummary) {}
}

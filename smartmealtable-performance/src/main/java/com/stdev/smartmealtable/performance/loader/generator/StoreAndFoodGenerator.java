package com.stdev.smartmealtable.performance.loader.generator;

import com.stdev.smartmealtable.performance.config.PerformanceLoadProperties.FoodProperties;
import com.stdev.smartmealtable.performance.loader.result.DataLoadResult;
import com.stdev.smartmealtable.performance.loader.util.KeywordGenerator;
import com.stdev.smartmealtable.performance.loader.util.KeywordGenerator.Keyword;
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
import java.util.stream.Collectors;

/**
 * Generates store and food rows using multi-threaded batch writes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StoreAndFoodGenerator {

    private final JdbcTemplate jdbcTemplate;

    private static final String[] STORE_AREAS = {
            "강남", "홍대", "판교", "광화문", "압구정", "해운대", "서면", "분당", "일산", "대전 둔산",
            "대구 동성로", "제주 애월", "수원 인계", "춘천 명동", "전주 한옥"
    };
    private static final String[] STORE_THEMES = {
            "로컬", "트렌디", "힙한", "클래식", "프리미엄", "웰빙", "감성", "푸드랩", "스페셜티", "셀럽"
    };
    private static final String[] STORE_TYPES_DESCRIPTORS = {
            "테이블", "주방", "다이닝", "키친", "식당", "카페", "포차", "그릴", "바", "라운지"
    };
    private static final String[] STORE_STORY_SNIPPETS = {
            "제철 재료만 고집하는 공간", "야외 테라스가 인기인 매장", "지역 농가와 협업하는 식당",
            "셰프의 시그니처 코스를 즐길 수 있는 곳", "스터디와 모임이 끊이지 않는 스폿",
            "로스터리 바와 주방이 함께 있는 복합 공간", "비건과 글루텐 프리 메뉴를 확대 중",
            "지역 청년 셰프들이 운영하는 실험적 공간", "장인 정신이 녹아 있는 메뉴 구성"
    };
    private static final String[] STREET_SUFFIXES = {
            "로", "대로", "길", "거리", "대로길", "중앙로", "문화로"
    };
    private static final String[] FOOD_MODIFIERS = {
            "직화", "트러플", "마라", "허브", "수비드", "크리스피", "비건", "스파이시",
            "저온숙성", "버터갈릭", "시즈널", "노아일", "로우칼로리", "바질", "깊은맛"
    };
    private static final String[] FOOD_BASES = {
            "덮밥", "라멘", "파스타", "스테이크", "샐러드", "비빔밥", "볶음밥", "샌드위치", "타코",
            "커리", "수프", "플랫브레드", "만두", "쌀국수", "버거", "피자"
    };
    private static final String[] FOOD_STORY_SNIPPETS = {
            "직접 배양한 스타터로 만든 메뉴", "발효 소스를 사용해 감칠맛을 더한 요리",
            "로스팅한 채소 토핑을 듬뿍 올린 메뉴", "셰프의 시그니처 소스를 곁들인 요리",
            "현지 조리법을 현대적으로 재해석한 메뉴", "주문 즉시 조리해 신선함을 살린 요리",
            "식물성 원료만 사용한 메뉴", "고소한 버터와 허브향이 조화를 이루는 요리"
    };

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

    private static final String INSERT_STORE_KEYWORD_SQL = """
            INSERT INTO store_search_keyword (store_id, keyword, keyword_prefix, keyword_type)
            VALUES (?, ?, ?, ?)
            """;

    private static final String INSERT_FOOD_KEYWORD_SQL = """
            INSERT INTO food_search_keyword (food_id, store_id, keyword, keyword_prefix, keyword_type)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String KEYWORD_TYPE = "NAME_SUBSTRING";
    private static final int KEYWORD_BATCH_SIZE = 500;

    public StoreDataSet generate(String runId, FoodProperties props, List<Long> categoryIds,
                                 int threadCount, int batchSize) {
        if (categoryIds.isEmpty()) {
            throw new IllegalStateException("At least one category is required before generating stores/foods");
        }

        StoreInsertResult storeInsertResult = insertStores(runId, props, categoryIds, threadCount, batchSize);
        List<StoreSummary> stores = fetchStores(runId, categoryIds);
        populateStoreSearchKeywords(stores);
        FoodInsertResult foodInsertResult = insertFoods(runId, props, stores, categoryIds, threadCount, batchSize);
        populateFoodSearchKeywords(runId);
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

    private void populateStoreSearchKeywords(List<StoreSummary> stores) {
        if (stores.isEmpty()) {
            return;
        }

        deleteStoreKeywords(stores);

        List<Object[]> batch = new ArrayList<>(KEYWORD_BATCH_SIZE);
        java.util.concurrent.atomic.AtomicLong inserted = new java.util.concurrent.atomic.AtomicLong();
        for (StoreSummary store : stores) {
            List<Keyword> keywords = KeywordGenerator.generate(store.name());
            for (Keyword keyword : keywords) {
                batch.add(new Object[]{store.storeId(), keyword.keyword(), keyword.prefix(), KEYWORD_TYPE});
                if (batch.size() == KEYWORD_BATCH_SIZE) {
                    jdbcTemplate.batchUpdate(INSERT_STORE_KEYWORD_SQL, batch);
                    inserted.addAndGet(batch.size());
                    batch.clear();
                }
            }
        }

        if (!batch.isEmpty()) {
            jdbcTemplate.batchUpdate(INSERT_STORE_KEYWORD_SQL, batch);
            inserted.addAndGet(batch.size());
        }

        log.info("[store_keyword] inserted {} rows", inserted.get());
    }

    private void deleteStoreKeywords(List<StoreSummary> stores) {
        List<Long> storeIds = stores.stream()
                .map(StoreSummary::storeId)
                .toList();
        chunk(storeIds, 500).forEach(idChunk -> {
            String placeholders = idChunk.stream().map(id -> "?").collect(Collectors.joining(","));
            String sql = "DELETE FROM store_search_keyword WHERE store_id IN (" + placeholders + ")";
            jdbcTemplate.update(sql, idChunk.toArray());
        });
    }

    private void populateFoodSearchKeywords(String runId) {
        String pattern = RunTag.storePattern(runId);
        jdbcTemplate.update("""
                DELETE FROM food_search_keyword
                WHERE store_id IN (
                    SELECT store_id FROM store WHERE external_id LIKE ?
                )
                """, pattern);

        List<Object[]> batch = new ArrayList<>(KEYWORD_BATCH_SIZE);
        java.util.concurrent.atomic.AtomicLong inserted = new java.util.concurrent.atomic.AtomicLong();
        jdbcTemplate.query(
                """
                        SELECT f.food_id, f.store_id, f.food_name
                        FROM food f
                        INNER JOIN store s ON f.store_id = s.store_id
                        WHERE s.external_id LIKE ?
                        """,
                ps -> ps.setString(1, pattern),
                rs -> {
                    while (rs.next()) {
                        long foodId = rs.getLong("food_id");
                        long storeId = rs.getLong("store_id");
                        String name = rs.getString("food_name");
                        for (Keyword keyword : KeywordGenerator.generate(name)) {
                            batch.add(new Object[]{foodId, storeId, keyword.keyword(), keyword.prefix(), KEYWORD_TYPE});
                            if (batch.size() == KEYWORD_BATCH_SIZE) {
                                jdbcTemplate.batchUpdate(INSERT_FOOD_KEYWORD_SQL, batch);
                                inserted.addAndGet(batch.size());
                                batch.clear();
                            }
                        }
                    }
                    return null;
                }
        );

        if (!batch.isEmpty()) {
            jdbcTemplate.batchUpdate(INSERT_FOOD_KEYWORD_SQL, batch);
            inserted.addAndGet(batch.size());
        }

        log.info("[food_keyword] inserted {} rows", inserted.get());
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
            String name = randomStoreName(sequence, random);
            String address = randomAddress(random);
            Timestamp registeredAt = Timestamp.valueOf(LocalDateTime.now().minusDays(random.nextInt(365)));
            return new Object[]{
                    RunTag.storeExternalId(runId, sequence),
                    name,
                    address,
                    address,
                    latitude,
                    longitude,
                    String.format("010-%04d-%04d", random.nextInt(10000), random.nextInt(10000)),
                    randomStoreDescription(random),
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
            String foodName = randomFoodName(random);
            return new Object[]{
                store.storeId(),
                categoryId,
                foodName,
                price,
                randomFoodDescription(random),
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

    private static String pick(String[] source, ThreadLocalRandom random) {
        return source[random.nextInt(source.length)];
    }

    private static <T> List<List<T>> chunk(List<T> source, int chunkSize) {
        if (source.isEmpty()) {
            return List.of();
        }
        List<List<T>> result = new ArrayList<>();
        int size = Math.max(1, chunkSize);
        for (int i = 0; i < source.size(); i += size) {
            int end = Math.min(source.size(), i + size);
            result.add(source.subList(i, end));
        }
        return result;
    }

    private String randomStoreName(long sequence, ThreadLocalRandom random) {
        String area = pick(STORE_AREAS, random);
        String theme = pick(STORE_THEMES, random);
        String type = pick(STORE_TYPES_DESCRIPTORS, random);
        long branch = (sequence % 50) + 1;
        return String.format("%s %s%s %d호", area, theme, type, branch);
    }

    private String randomAddress(ThreadLocalRandom random) {
        String area = pick(STORE_AREAS, random).replace(" ", "");
        String suffix = pick(STREET_SUFFIXES, random);
        int number = 50 + random.nextInt(950);
        return String.format("%s %s %d", area, suffix, number);
    }

    private String randomStoreDescription(ThreadLocalRandom random) {
        return pick(STORE_STORY_SNIPPETS, random);
    }

    private String randomFoodName(ThreadLocalRandom random) {
        String modifier = pick(FOOD_MODIFIERS, random);
        String base = pick(FOOD_BASES, random);
        return modifier + ' ' + base;
    }

    private String randomFoodDescription(ThreadLocalRandom random) {
        return pick(FOOD_STORY_SNIPPETS, random);
    }
}

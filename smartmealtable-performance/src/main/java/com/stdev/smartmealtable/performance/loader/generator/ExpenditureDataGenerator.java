package com.stdev.smartmealtable.performance.loader.generator;

import com.stdev.smartmealtable.performance.config.PerformanceLoadProperties.ExpenditureProperties;
import com.stdev.smartmealtable.performance.loader.generator.StoreAndFoodGenerator.StoreSummary;
import com.stdev.smartmealtable.performance.loader.result.DataLoadResult;
import com.stdev.smartmealtable.performance.loader.util.RunTag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.Time;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates expenditure and optional expenditure_item rows.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExpenditureDataGenerator {

    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_EXPENDITURE_SQL = """
            INSERT INTO expenditure (
                member_id, store_id, store_name, amount, discount,
                expended_date, expended_time, category_id, meal_type, memo,
                created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
            """;

    private static final String INSERT_EXPENDITURE_ITEM_SQL = """
            INSERT INTO expenditure_item (
                expenditure_id, food_id, food_name, order_price, order_quantity,
                created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, NOW(), NOW())
            """;

    public ExpenditureGenerationResult generate(String runId,
                                                 List<Long> memberIds,
                                                 List<StoreSummary> stores,
                                                 List<Long> categoryIds,
                                                 ExpenditureProperties props,
                                                 int threadCount,
                                                 int batchSize) {
        if (memberIds.isEmpty()) {
            DataLoadResult empty = new DataLoadResult("expenditure", 0, Duration.ZERO);
            return new ExpenditureGenerationResult(empty, new DataLoadResult("expenditure_item", 0, Duration.ZERO));
        }
        if (stores.isEmpty()) {
            throw new IllegalStateException("No store rows detected. Generate stores before expenditures.");
        }

        Instant start = Instant.now();
        List<List<Long>> partitions = partition(memberIds, threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(partitions.size());
        AtomicLong sequence = new AtomicLong();
        try {
            List<Future<Long>> futures = new ArrayList<>();
            for (List<Long> partition : partitions) {
                futures.add(executor.submit(new ExpenditureInsertTask(
                        partition, stores, categoryIds, props, batchSize, runId, sequence)));
            }
            long inserted = waitAndSum(futures, "expenditure");
            Duration took = Duration.between(start, Instant.now());
            DataLoadResult summary = new DataLoadResult("expenditure", inserted, took);
            log.info("[expenditure] inserted={} rows in {} ms", inserted, took.toMillis());

            DataLoadResult itemSummary = props.isGenerateItems()
                    ? insertItems(runId, props)
                    : new DataLoadResult("expenditure_item", 0, Duration.ZERO);
            return new ExpenditureGenerationResult(summary, itemSummary);
        } finally {
            executor.shutdown();
        }
    }

    private DataLoadResult insertItems(String runId, ExpenditureProperties props) {
        Instant start = Instant.now();
        List<Long> expenditureIds = jdbcTemplate.query(
                "SELECT expenditure_id FROM expenditure WHERE memo LIKE ? ORDER BY expenditure_id",
                ps -> ps.setString(1, RunTag.expenditurePattern(runId)),
                (rs, row) -> rs.getLong(1)
        );
        if (expenditureIds.isEmpty()) {
            return new DataLoadResult("expenditure_item", 0, Duration.ZERO);
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<Object[]> batch = new ArrayList<>(1_000);
        long inserted = 0;
        for (Long expId : expenditureIds) {
            int items = random.nextInt(1, props.getMaxItemsPerReceipt() + 1);
            for (int i = 0; i < items; i++) {
                int price = random.nextInt(Math.max(1, props.getMinAmount() / 4), props.getMaxAmount() / 2 + 1);
                batch.add(new Object[]{
                        expId,
                        null,
                        "PERF_ITEM_" + expId + '_' + i,
                        price,
                        random.nextInt(1, 5)
                });
                if (batch.size() == 1_000) {
                    jdbcTemplate.batchUpdate(INSERT_EXPENDITURE_ITEM_SQL, batch);
                    inserted += batch.size();
                    batch.clear();
                }
            }
        }
        if (!batch.isEmpty()) {
            jdbcTemplate.batchUpdate(INSERT_EXPENDITURE_ITEM_SQL, batch);
            inserted += batch.size();
        }
        Duration took = Duration.between(start, Instant.now());
        log.info("[expenditure_item] inserted={} rows in {} ms", inserted, took.toMillis());
        return new DataLoadResult("expenditure_item", inserted, took);
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

    private static List<List<Long>> partition(List<Long> ids, int partitions) {
        if (ids.isEmpty()) {
            return List.of(List.of());
        }
        int chunk = Math.max(1, ids.size() / Math.max(1, partitions));
        List<List<Long>> result = new ArrayList<>();
        for (int i = 0; i < ids.size(); i += chunk) {
            int end = Math.min(ids.size(), i + chunk);
            result.add(ids.subList(i, end));
        }
        return result;
    }

    private class ExpenditureInsertTask implements Callable<Long> {
        private final List<Long> memberIds;
        private final List<StoreSummary> stores;
        private final List<Long> categoryIds;
        private final ExpenditureProperties props;
        private final int batchSize;
        private final String runId;
        private final AtomicLong sequence;

        private ExpenditureInsertTask(List<Long> memberIds,
                                      List<StoreSummary> stores,
                                      List<Long> categoryIds,
                                      ExpenditureProperties props,
                                      int batchSize,
                                      String runId,
                                      AtomicLong sequence) {
            this.memberIds = memberIds;
            this.stores = stores;
            this.categoryIds = categoryIds;
            this.props = props;
            this.batchSize = batchSize;
            this.runId = runId;
            this.sequence = sequence;
        }

        @Override
        public Long call() {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            List<Object[]> batch = new ArrayList<>(batchSize);
            long inserted = 0;
            LocalDate end = LocalDate.now();
            LocalDate start = end.minusMonths(props.getMonthsSpan());
            for (Long memberId : memberIds) {
                for (int i = 0; i < props.getRecordsPerMember(); i++) {
                    batch.add(buildRow(memberId, random, start, end));
                    if (batch.size() == batchSize) {
                        jdbcTemplate.batchUpdate(INSERT_EXPENDITURE_SQL, batch);
                        inserted += batch.size();
                        batch.clear();
                    }
                }
            }
            if (!batch.isEmpty()) {
                jdbcTemplate.batchUpdate(INSERT_EXPENDITURE_SQL, batch);
                inserted += batch.size();
            }
            return inserted;
        }

        private Object[] buildRow(Long memberId, ThreadLocalRandom random, LocalDate start, LocalDate end) {
            StoreSummary store = stores.get(random.nextInt(stores.size()));
            long daysRange = Math.max(1, ChronoUnit.DAYS.between(start, end) + 1);
            LocalDate date = start.plusDays(random.nextLong(daysRange));
            LocalTime time = LocalTime.of(random.nextInt(8, 23), random.nextInt(0, 60));
            int amount = random.nextInt(props.getMinAmount(), props.getMaxAmount());
            int discount = random.nextInt(0, props.getMaxDiscount() + 1);
            long memoSeq = sequence.incrementAndGet();
            Long category = categoryIds.get(random.nextInt(categoryIds.size()));
            String mealType = switch (random.nextInt(4)) {
                case 0 -> "BREAKFAST";
                case 1 -> "LUNCH";
                case 2 -> "DINNER";
                default -> "OTHER";
            };
            return new Object[]{
                    memberId,
                    store.storeId(),
                    store.name(),
                    amount,
                    discount,
                    Date.valueOf(date),
                    Time.valueOf(time),
                    category,
                    mealType,
                    RunTag.expenditureMemo(runId, memoSeq)
            };
        }
    }

    public record ExpenditureGenerationResult(DataLoadResult summary, DataLoadResult itemSummary) {}
}

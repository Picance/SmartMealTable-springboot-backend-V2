package com.stdev.smartmealtable.performance.loader.generator;

import com.stdev.smartmealtable.performance.config.PerformanceLoadProperties.BudgetProperties;
import com.stdev.smartmealtable.performance.loader.result.DataLoadResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates monthly/daily/meal budget rows.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BudgetDataGenerator {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    private static final String INSERT_MONTHLY_SQL = """
            INSERT INTO monthly_budget (
                member_id, monthly_food_budget, monthly_used_amount, budget_month,
                created_at, updated_at
            ) VALUES (?, ?, ?, ?, NOW(), NOW())
            """;

    private static final String INSERT_DAILY_SQL = """
            INSERT INTO daily_budget (
                member_id, daily_food_budget, daily_used_amount, budget_date,
                created_at, updated_at
            ) VALUES (?, ?, ?, ?, NOW(), NOW())
            """;

    private static final Map<String, Double> MEAL_RATIOS = new LinkedHashMap<>() {{
        put("BREAKFAST", 0.25d);
        put("LUNCH", 0.4d);
        put("DINNER", 0.3d);
        put("OTHER", 0.05d);
    }};

    public BudgetGenerationResult generate(List<Long> memberIds, BudgetProperties props,
                                           int threadCount, int batchSize) {
        if (memberIds.isEmpty()) {
            DataLoadResult empty = new DataLoadResult("budget", 0, Duration.ZERO);
            return new BudgetGenerationResult(empty, empty, empty);
        }

        List<List<Long>> partitions = partition(memberIds, threadCount);
        List<YearMonth> months = buildMonths(props.getMonthlyMonths());
        List<LocalDate> days = buildDays(props.getDailyDays());

        MonthlyInsertResult monthlyInsertResult = insertMonthlyBudgets(partitions, months, props, batchSize);
        DailyInsertResult dailyInsertResult = insertDailyBudgets(partitions, days, props, batchSize);
        DataLoadResult mealSummary = props.isGenerateMealBudgets()
                ? expandMealBudgets(memberIds)
                : new DataLoadResult("meal_budget", 0, Duration.ZERO);

        return new BudgetGenerationResult(monthlyInsertResult.summary(), dailyInsertResult.summary(), mealSummary);
    }

    private MonthlyInsertResult insertMonthlyBudgets(List<List<Long>> partitions, List<YearMonth> months,
                                                     BudgetProperties props, int batchSize) {
        Instant start = Instant.now();
        ExecutorService executor = Executors.newFixedThreadPool(partitions.size());
        try {
            List<Future<Long>> futures = new ArrayList<>();
            for (List<Long> partition : partitions) {
                futures.add(executor.submit(new MonthlyInsertTask(partition, months, props, batchSize)));
            }
            long inserted = waitAndSum(futures, "monthly_budget");
            Duration took = Duration.between(start, Instant.now());
            DataLoadResult summary = new DataLoadResult("monthly_budget", inserted, took);
            log.info("[monthly_budget] inserted={} rows in {} ms", inserted, took.toMillis());
            return new MonthlyInsertResult(summary);
        } finally {
            executor.shutdown();
        }
    }

    private DailyInsertResult insertDailyBudgets(List<List<Long>> partitions, List<LocalDate> days,
                                                 BudgetProperties props, int batchSize) {
        Instant start = Instant.now();
        ExecutorService executor = Executors.newFixedThreadPool(partitions.size());
        try {
            List<Future<Long>> futures = new ArrayList<>();
            for (List<Long> partition : partitions) {
                futures.add(executor.submit(new DailyInsertTask(partition, days, props, batchSize)));
            }
            long inserted = waitAndSum(futures, "daily_budget");
            Duration took = Duration.between(start, Instant.now());
            DataLoadResult summary = new DataLoadResult("daily_budget", inserted, took);
            log.info("[daily_budget] inserted={} rows in {} ms", inserted, took.toMillis());
            return new DailyInsertResult(summary);
        } finally {
            executor.shutdown();
        }
    }

    private DataLoadResult expandMealBudgets(List<Long> memberIds) {
        Instant start = Instant.now();
        long totalInserted = 0;
        for (Map.Entry<String, Double> entry : MEAL_RATIOS.entrySet()) {
            totalInserted += insertMealBudgetsForType(memberIds, entry.getKey(), entry.getValue());
        }
        Duration took = Duration.between(start, Instant.now());
        log.info("[meal_budget] inserted={} rows in {} ms", totalInserted, took.toMillis());
        return new DataLoadResult("meal_budget", totalInserted, took);
    }

    private long insertMealBudgetsForType(List<Long> memberIds, String mealType, double ratio) {
        long inserted = 0;
        for (List<Long> chunk : partition(memberIds, 500)) {
            if (chunk.isEmpty()) {
                continue;
            }
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("memberIds", chunk);
            params.addValue("ratio", ratio);
            params.addValue("mealType", mealType);
            inserted += namedJdbcTemplate.update("""
                    INSERT INTO meal_budget (
                        daily_budget_id, meal_budget, meal_type, used_amount, budget_date, created_at, updated_at
                    )
                    SELECT db.budget_id,
                           LEAST(GREATEST(500, ROUND(db.daily_food_budget * :ratio)), db.daily_food_budget),
                           :mealType,
                           0,
                           db.budget_date,
                           NOW(),
                           NOW()
                    FROM daily_budget db
                    WHERE db.member_id IN (:memberIds)
                    """, params);
        }
        return inserted;
    }

    private static List<List<Long>> partition(List<Long> source, int partitions) {
        if (source.isEmpty()) {
            return List.of(List.of());
        }
        int size = Math.max(1, partitions);
        int chunk = Math.max(1, source.size() / size);
        List<List<Long>> result = new ArrayList<>();
        for (int i = 0; i < source.size(); i += chunk) {
            int end = Math.min(source.size(), i + chunk);
            result.add(source.subList(i, end));
        }
        return result;
    }

    private static List<YearMonth> buildMonths(int months) {
        List<YearMonth> result = new ArrayList<>();
        YearMonth now = YearMonth.now();
        for (int i = months - 1; i >= 0; i--) {
            result.add(now.minusMonths(i));
        }
        return result;
    }

    private static List<LocalDate> buildDays(int days) {
        List<LocalDate> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            result.add(today.minusDays(i));
        }
        return result;
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

    private class MonthlyInsertTask implements Callable<Long> {
        private final List<Long> memberIds;
        private final List<YearMonth> months;
        private final BudgetProperties props;
        private final int batchSize;

        private MonthlyInsertTask(List<Long> memberIds, List<YearMonth> months,
                                  BudgetProperties props, int batchSize) {
            this.memberIds = memberIds;
            this.months = months;
            this.props = props;
            this.batchSize = batchSize;
        }

        @Override
        public Long call() {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            List<Object[]> batch = new ArrayList<>(batchSize);
            long inserted = 0;
            for (Long memberId : memberIds) {
                for (YearMonth month : months) {
                    int budget = random.nextInt(props.getMinMonthlyBudget(), props.getMaxMonthlyBudget());
                    int used = random.nextInt(0, Math.max(1, budget));
                    batch.add(new Object[]{memberId, budget, used, month.toString()});
                    if (batch.size() == batchSize) {
                        jdbcTemplate.batchUpdate(INSERT_MONTHLY_SQL, batch);
                        inserted += batch.size();
                        batch.clear();
                    }
                }
            }
            if (!batch.isEmpty()) {
                jdbcTemplate.batchUpdate(INSERT_MONTHLY_SQL, batch);
                inserted += batch.size();
            }
            return inserted;
        }
    }

    private class DailyInsertTask implements Callable<Long> {
        private final List<Long> memberIds;
        private final List<LocalDate> days;
        private final BudgetProperties props;
        private final int batchSize;

        private DailyInsertTask(List<Long> memberIds, List<LocalDate> days,
                                BudgetProperties props, int batchSize) {
            this.memberIds = memberIds;
            this.days = days;
            this.props = props;
            this.batchSize = batchSize;
        }

        @Override
        public Long call() {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            List<Object[]> batch = new ArrayList<>(batchSize);
            long inserted = 0;
            for (Long memberId : memberIds) {
                for (LocalDate day : days) {
                    int budget = random.nextInt(props.getMinDailyBudget(), props.getMaxDailyBudget());
                    int used = random.nextInt(0, Math.max(1, budget));
                    batch.add(new Object[]{memberId, budget, used, day});
                    if (batch.size() == batchSize) {
                        jdbcTemplate.batchUpdate(INSERT_DAILY_SQL, batch);
                        inserted += batch.size();
                        batch.clear();
                    }
                }
            }
            if (!batch.isEmpty()) {
                jdbcTemplate.batchUpdate(INSERT_DAILY_SQL, batch);
                inserted += batch.size();
            }
            return inserted;
        }
    }

    private record MonthlyInsertResult(DataLoadResult summary) {}

    private record DailyInsertResult(DataLoadResult summary) {}

    public record BudgetGenerationResult(DataLoadResult monthlySummary,
                                         DataLoadResult dailySummary,
                                         DataLoadResult mealSummary) {}
}

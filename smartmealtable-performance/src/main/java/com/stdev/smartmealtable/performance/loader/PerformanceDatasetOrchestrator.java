package com.stdev.smartmealtable.performance.loader;

import com.stdev.smartmealtable.performance.config.PerformanceLoadProperties;
import com.stdev.smartmealtable.performance.loader.generator.BudgetDataGenerator;
import com.stdev.smartmealtable.performance.loader.generator.BudgetDataGenerator.BudgetGenerationResult;
import com.stdev.smartmealtable.performance.loader.generator.CategoryBootstrapper;
import com.stdev.smartmealtable.performance.loader.generator.CategoryBootstrapper.CategoryBootstrapResult;
import com.stdev.smartmealtable.performance.loader.generator.ExpenditureDataGenerator;
import com.stdev.smartmealtable.performance.loader.generator.ExpenditureDataGenerator.ExpenditureGenerationResult;
import com.stdev.smartmealtable.performance.loader.generator.MemberDataGenerator;
import com.stdev.smartmealtable.performance.loader.generator.MemberDataGenerator.MemberGenerationResult;
import com.stdev.smartmealtable.performance.loader.generator.StoreAndFoodGenerator;
import com.stdev.smartmealtable.performance.loader.generator.StoreAndFoodGenerator.StoreDataSet;
import com.stdev.smartmealtable.performance.loader.result.DataLoadResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Coordinates every generator step so the CLI produces a full dataset in one shot.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PerformanceDatasetOrchestrator {

    private final PerformanceLoadProperties properties;
    private final CategoryBootstrapper categoryBootstrapper;
    private final MemberDataGenerator memberDataGenerator;
    private final StoreAndFoodGenerator storeAndFoodGenerator;
    private final BudgetDataGenerator budgetDataGenerator;
    private final ExpenditureDataGenerator expenditureDataGenerator;

    private static final DateTimeFormatter RUN_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public void execute() {
        String runId = resolveRunId();
        log.info("===== SmartMealTable synthetic data loader start runId={} =====", runId);
        Instant start = Instant.now();
        List<DataLoadResult> summaries = new ArrayList<>();

        Instant categoryStart = Instant.now();
        CategoryBootstrapResult categoryResult = categoryBootstrapper.ensureMinimumCategories(
                runId,
                properties.getCategory().getMinimumCount()
        );
        summaries.add(new DataLoadResult(
                "category_bootstrap",
                categoryResult.inserted(),
                Duration.between(categoryStart, Instant.now())
        ));

        MemberGenerationResult memberResult = memberDataGenerator.generate(
                runId,
                properties.getMember(),
                properties.getThreadCount(),
                properties.getBatchSize()
        );
        summaries.add(memberResult.summary());

        StoreDataSet storeDataSet = storeAndFoodGenerator.generate(
                runId,
                properties.getFood(),
                categoryResult.categoryIds(),
                properties.getThreadCount(),
                properties.getBatchSize()
        );
        summaries.add(storeDataSet.storeSummary());
        summaries.add(storeDataSet.foodSummary());

        BudgetGenerationResult budgetResult = budgetDataGenerator.generate(
                memberResult.memberIds(),
                properties.getBudget(),
                properties.getThreadCount(),
                properties.getBatchSize()
        );
        summaries.add(budgetResult.monthlySummary());
        summaries.add(budgetResult.dailySummary());
        if (budgetResult.mealSummary().rows() > 0) {
            summaries.add(budgetResult.mealSummary());
        }

        ExpenditureGenerationResult expenditureResult = expenditureDataGenerator.generate(
                runId,
                memberResult.memberIds(),
                storeDataSet.stores(),
                categoryResult.categoryIds(),
                properties.getExpenditure(),
                properties.getThreadCount(),
                properties.getBatchSize()
        );
        summaries.add(expenditureResult.summary());
        if (expenditureResult.itemSummary().rows() > 0) {
            summaries.add(expenditureResult.itemSummary());
        }

        logSummary(runId, summaries, Duration.between(start, Instant.now()));
    }

    private String resolveRunId() {
        return properties.getRunId() != null && !properties.getRunId().isBlank()
                ? properties.getRunId()
                : RUN_ID_FORMAT.format(LocalDateTime.now());
    }

    private void logSummary(String runId, List<DataLoadResult> results, Duration totalDuration) {
        StringBuilder table = new StringBuilder(System.lineSeparator());
        table.append(String.format("%-24s %-12s %-12s%n", "dataset", "rows", "ms"));
        table.append("-----------------------------------------------").append(System.lineSeparator());
        for (DataLoadResult result : results) {
            table.append(String.format(
                    "%-24s %-12d %-12d%n",
                    result.dataset(),
                    result.rows(),
                    result.duration().toMillis()
            ));
        }
        log.info("Completed runId={} in {} ms{}", runId, totalDuration.toMillis(), table);
    }
}

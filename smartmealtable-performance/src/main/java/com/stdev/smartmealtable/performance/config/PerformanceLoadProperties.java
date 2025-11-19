package com.stdev.smartmealtable.performance.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties that control how much synthetic data is generated for performance tests.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "performance.loader")
public class PerformanceLoadProperties {

    /**
     * Optional external run identifier. When not provided, a timestamp-based value is generated for each execution.
     */
    private String runId;

    /** Number of worker threads used for each dataset writer. */
    @Min(1)
    @Max(128)
    private int threadCount = 8;

    /** Number of rows grouped inside a single JDBC batch call. */
    @Min(100)
    @Max(10_000)
    private int batchSize = 1_000;

    @NotNull
    private MemberProperties member = new MemberProperties();

    @NotNull
    private CategoryProperties category = new CategoryProperties();

    @NotNull
    private FoodProperties food = new FoodProperties();

    @NotNull
    private BudgetProperties budget = new BudgetProperties();

    @NotNull
    private ExpenditureProperties expenditure = new ExpenditureProperties();

    @Getter
    @Setter
    public static class MemberProperties {
        /** Total number of synthetic members to create for a run. */
        @Min(1)
        private long count = 50_000;

        /** Default recommendation type stored on the member record. */
        private String recommendationType = "BALANCED";
    }

    @Getter
    @Setter
    public static class CategoryProperties {
        /** Minimum number of category rows that must exist before generating stores/foods. */
        @Min(8)
        private int minimumCount = 32;
    }

    @Getter
    @Setter
    public static class FoodProperties {
        /** How many new store rows should be created. */
        @Min(1)
        private long stores = 5_000;

        /** How many menus are registered per store. */
        @Min(1)
        private int foodsPerStore = 120;

        /** Minimum random price assigned to menu rows. */
        @Min(500)
        private int minPrice = 4_000;

        /** Maximum random price assigned to menu rows. */
        @Min(1_000)
        private int maxPrice = 20_000;
    }

    @Getter
    @Setter
    public static class BudgetProperties {
        /** Number of past months to seed inside monthly budget table. */
        @Min(1)
        private int monthlyMonths = 12;

        /** Number of sequential days (ending today) to seed inside daily budget table. */
        @Min(7)
        private int dailyDays = 120;

        @Min(50_000)
        private int minMonthlyBudget = 200_000;

        @Min(50_000)
        private int maxMonthlyBudget = 600_000;

        @Min(1_000)
        private int minDailyBudget = 10_000;

        @Min(1_000)
        private int maxDailyBudget = 35_000;

        /** When true the loader also expands the daily budgets into meal_budget rows. */
        private boolean generateMealBudgets = true;
    }

    @Getter
    @Setter
    public static class ExpenditureProperties {
        /** Number of expenditure rows per member. */
        @Min(1)
        private int recordsPerMember = 80;

        /** How many months worth of history should be covered when generating expenditures. */
        @Min(1)
        private int monthsSpan = 9;

        @Min(1_000)
        private int minAmount = 3_000;

        @Min(2_000)
        private int maxAmount = 45_000;

        @Min(0)
        private int maxDiscount = 5_000;

        /** Number of random line items created for each expenditure entry. */
        @Min(0)
        @Max(10)
        private int maxItemsPerReceipt = 3;

        /** When false the loader skips generating expenditure_item rows. */
        private boolean generateItems = false;
    }
}

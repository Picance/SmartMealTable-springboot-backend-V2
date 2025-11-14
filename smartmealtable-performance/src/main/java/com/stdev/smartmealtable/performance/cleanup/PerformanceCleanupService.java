package com.stdev.smartmealtable.performance.cleanup;

import com.stdev.smartmealtable.performance.loader.util.RunTag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Removes synthetic data created by the performance loader for a specific run id.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PerformanceCleanupService {

    private final JdbcTemplate jdbcTemplate;

    public void cleanup(String runId) {
        if (runId == null || runId.isBlank()) {
            throw new IllegalArgumentException("cleanup-run-id must be provided");
        }
        log.info("Cleaning up synthetic data for runId={}", runId);

        String memberPattern = RunTag.memberPattern(runId);
        String storePattern = RunTag.storePattern(runId);
        String foodPattern = RunTag.foodPattern(runId);
        String expenditurePattern = RunTag.expenditurePattern(runId);
        String categoryPattern = RunTag.categoryPattern(runId);

        int expItems = jdbcTemplate.update("""
                DELETE FROM expenditure_item
                WHERE expenditure_id IN (
                    SELECT e.expenditure_id FROM expenditure e WHERE e.memo LIKE ?
                )
                """, expenditurePattern);

        int expenditures = jdbcTemplate.update(
                "DELETE FROM expenditure WHERE memo LIKE ?",
                expenditurePattern
        );

        int mealBudgets = jdbcTemplate.update("""
                DELETE FROM meal_budget
                WHERE daily_budget_id IN (
                    SELECT db.budget_id FROM daily_budget db
                    WHERE db.member_id IN (
                        SELECT member_id FROM member WHERE nickname LIKE ?
                    )
                )
                """, memberPattern);

        int dailyBudgets = jdbcTemplate.update(
                "DELETE FROM daily_budget WHERE member_id IN (SELECT member_id FROM member WHERE nickname LIKE ?)",
                memberPattern
        );

        int monthlyBudgets = jdbcTemplate.update(
                "DELETE FROM monthly_budget WHERE member_id IN (SELECT member_id FROM member WHERE nickname LIKE ?)",
                memberPattern
        );

        int foods = jdbcTemplate.update(
                "DELETE FROM food WHERE food_name LIKE ?",
                foodPattern
        );

        int stores = jdbcTemplate.update(
                "DELETE FROM store WHERE external_id LIKE ?",
                storePattern
        );

        int members = jdbcTemplate.update(
                "DELETE FROM member WHERE nickname LIKE ?",
                memberPattern
        );

        int categories = jdbcTemplate.update(
                "DELETE FROM category WHERE name LIKE ?",
                categoryPattern
        );

        log.info("Cleanup summary - expenditure_item:{} expenditure:{} meal_budget:{} daily_budget:{} monthly_budget:{} food:{} store:{} member:{} category:{}",
                expItems, expenditures, mealBudgets, dailyBudgets, monthlyBudgets, foods, stores, members, categories);
    }
}

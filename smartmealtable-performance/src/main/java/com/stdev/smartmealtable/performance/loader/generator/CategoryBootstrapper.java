package com.stdev.smartmealtable.performance.loader.generator;

import com.stdev.smartmealtable.performance.loader.util.RunTag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Ensures that the category master table contains enough rows to be referenced by synthetic stores/foods.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CategoryBootstrapper {

    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL =
            "INSERT INTO category (name, created_at, updated_at) VALUES (?, NOW(), NOW())";

    public CategoryBootstrapResult ensureMinimumCategories(String runId, int minimumCount) {
        Integer existing = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM category", Integer.class);
        int currentCount = existing != null ? existing : 0;
        int need = Math.max(0, minimumCount - currentCount);
        if (need > 0) {
            log.info("[category] existing={}, inserting={} to reach minimum {}", currentCount, need, minimumCount);
            for (int i = 0; i < need; i++) {
                String name = RunTag.categoryName(runId, i);
                jdbcTemplate.update(INSERT_SQL, ps -> ps.setString(1, name));
            }
        } else {
            log.info("[category] existing count {} satisfies minimum {}", currentCount, minimumCount);
        }

        List<Long> categoryIds = jdbcTemplate.query(
                "SELECT category_id FROM category ORDER BY category_id",
                (rs, rowNum) -> rs.getLong(1)
        );
        return new CategoryBootstrapResult(categoryIds, need);
    }

    public record CategoryBootstrapResult(List<Long> categoryIds, long inserted) {
    }
}

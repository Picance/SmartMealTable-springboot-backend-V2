package com.stdev.smartmealtable.performance.loader.result;

import java.time.Duration;

/**
 * Summary of a single dataset writing phase.
 */
public record DataLoadResult(
        String dataset,
        long rows,
        Duration duration
) {
}

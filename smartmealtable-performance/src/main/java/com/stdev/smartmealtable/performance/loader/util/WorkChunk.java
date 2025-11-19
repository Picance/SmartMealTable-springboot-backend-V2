package com.stdev.smartmealtable.performance.loader.util;

/**
 * Represents a contiguous range of work assigned to a single thread.
 */
public record WorkChunk(long startInclusive, long size) {

    public long endExclusive() {
        return startInclusive + size;
    }
}

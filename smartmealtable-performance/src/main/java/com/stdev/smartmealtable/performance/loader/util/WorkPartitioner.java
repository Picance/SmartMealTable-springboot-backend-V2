package com.stdev.smartmealtable.performance.loader.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility for splitting a number of logical records into thread-friendly chunks.
 */
public final class WorkPartitioner {

    private WorkPartitioner() {
    }

    public static List<WorkChunk> partition(long total, int maxChunks) {
        List<WorkChunk> chunks = new ArrayList<>();
        if (total <= 0 || maxChunks <= 0) {
            return chunks;
        }

        long chunkCount = Math.min(total, maxChunks);
        long baseSize = total / chunkCount;
        long remainder = total % chunkCount;

        long cursor = 0;
        for (int i = 0; i < chunkCount; i++) {
            long size = baseSize + (i < remainder ? 1 : 0);
            if (size <= 0) {
                continue;
            }
            chunks.add(new WorkChunk(cursor, size));
            cursor += size;
        }
        return chunks;
    }
}

package com.stdev.smartmealtable.performance.loader.generator;

import com.stdev.smartmealtable.performance.config.PerformanceLoadProperties.MemberProperties;
import com.stdev.smartmealtable.performance.loader.result.DataLoadResult;
import com.stdev.smartmealtable.performance.loader.util.RunTag;
import com.stdev.smartmealtable.performance.loader.util.WorkChunk;
import com.stdev.smartmealtable.performance.loader.util.WorkPartitioner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Generates synthetic member rows that other datasets can reference.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MemberDataGenerator {

    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL =
            "INSERT INTO member (group_id, nickname, profile_image_url, recommendation_type, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, NOW(), NOW())";

    public MemberGenerationResult generate(String runId, MemberProperties props, int threadCount, int batchSize) {
        Instant start = Instant.now();
        List<WorkChunk> chunks = WorkPartitioner.partition(props.getCount(), threadCount);
        if (chunks.isEmpty()) {
            return new MemberGenerationResult(List.of(), new DataLoadResult("member", 0, Duration.ZERO));
        }

        ExecutorService executor = Executors.newFixedThreadPool(chunks.size());
        try {
            List<Future<Long>> futures = new ArrayList<>();
            for (WorkChunk chunk : chunks) {
                futures.add(executor.submit(new MemberInsertTask(chunk, batchSize, runId, props.getRecommendationType())));
            }

            long inserted = 0;
            for (Future<Long> future : futures) {
                try {
                    inserted += future.get();
                } catch (Exception ex) {
                    throw new IllegalStateException("Member insert task failed", ex);
                }
            }

            List<Long> memberIds = jdbcTemplate.query(
                    "SELECT member_id FROM member WHERE nickname LIKE ? ORDER BY member_id",
                    ps -> ps.setString(1, RunTag.memberPattern(runId)),
                    (rs, rowNum) -> rs.getLong(1)
            );

            Duration took = Duration.between(start, Instant.now());
            DataLoadResult result = new DataLoadResult("member", inserted, took);
            log.info("[member] inserted={} rows in {} ms", inserted, took.toMillis());
            return new MemberGenerationResult(memberIds, result);
        } finally {
            executor.shutdown();
        }
    }

    private class MemberInsertTask implements Callable<Long> {

        private final WorkChunk chunk;
        private final int batchSize;
        private final String runId;
        private final String recommendationType;

        private MemberInsertTask(WorkChunk chunk, int batchSize, String runId, String recommendationType) {
            this.chunk = chunk;
            this.batchSize = batchSize;
            this.runId = runId;
            this.recommendationType = (recommendationType == null || recommendationType.isBlank())
                    ? "BALANCED"
                    : recommendationType.trim().toUpperCase();
        }

        @Override
        public Long call() {
            List<Object[]> batch = new ArrayList<>(batchSize);
            long inserted = 0;
            for (long offset = 0; offset < chunk.size(); offset++) {
                long sequence = chunk.startInclusive() + offset;
                Object[] params = new Object[]{
                        null,
                        RunTag.memberNickname(runId, sequence),
                        null,
                        recommendationType
                };
                batch.add(params);

                if (batch.size() == batchSize) {
                    jdbcTemplate.batchUpdate(INSERT_SQL, batch);
                    inserted += batch.size();
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                jdbcTemplate.batchUpdate(INSERT_SQL, batch);
                inserted += batch.size();
            }
            return inserted;
        }
    }

    public record MemberGenerationResult(List<Long> memberIds, DataLoadResult summary) {
    }
}

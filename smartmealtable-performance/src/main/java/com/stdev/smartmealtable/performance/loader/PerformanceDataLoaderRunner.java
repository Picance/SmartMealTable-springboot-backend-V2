package com.stdev.smartmealtable.performance.loader;

import com.stdev.smartmealtable.performance.cleanup.PerformanceCleanupService;
import com.stdev.smartmealtable.performance.jwt.JwtCliService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Entry-point runner that interprets CLI options and dispatches to the appropriate mode.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PerformanceDataLoaderRunner implements ApplicationRunner {

    private final PerformanceDatasetOrchestrator datasetOrchestrator;
    private final JwtCliService jwtCliService;
    private final PerformanceCleanupService cleanupService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (args.containsOption("jwt-member-id")) {
            Long memberId = parseLongOption(args, "jwt-member-id");
            jwtCliService.printToken(memberId);
            return;
        }

        if (args.containsOption("cleanup-run-id")) {
            String runId = args.getOptionValues("cleanup-run-id").get(0);
            cleanupService.cleanup(runId);
            return;
        }

        datasetOrchestrator.execute();
    }

    private Long parseLongOption(ApplicationArguments args, String name) {
        try {
            return Long.parseLong(args.getOptionValues(name).get(0));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid value for --" + name + ", expected a number", ex);
        }
    }
}

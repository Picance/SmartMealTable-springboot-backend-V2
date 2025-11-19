package com.stdev.smartmealtable.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 비동기 실행 환경 설정
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "searchKeywordEventExecutor")
    public Executor searchKeywordEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("search-event-");
        executor.setAwaitTerminationSeconds(5);
        executor.initialize();
        return executor;
    }
}

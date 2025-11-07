package com.stdev.smartmealtable.api.config;

import com.stdev.smartmealtable.api.budget.service.DailyBudgetQueryService;
import com.stdev.smartmealtable.api.budget.service.MonthlyBudgetQueryService;
import com.stdev.smartmealtable.api.expenditure.service.CreateExpenditureService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * 테스트용 Service Mock 설정 (Expenditure, Budget)
 */
@TestConfiguration
public class MockExpenditureServiceConfig {
    
    @Bean
    @Primary
    public CreateExpenditureService createExpenditureServiceMock() {
        return Mockito.mock(CreateExpenditureService.class);
    }
    
    @Bean
    @Primary
    public DailyBudgetQueryService dailyBudgetQueryServiceMock() {
        return Mockito.mock(DailyBudgetQueryService.class);
    }
    
    @Bean
    @Primary
    public MonthlyBudgetQueryService monthlyBudgetQueryServiceMock() {
        return Mockito.mock(MonthlyBudgetQueryService.class);
    }
}

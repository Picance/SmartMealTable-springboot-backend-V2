package com.stdev.smartmealtable.admin.statistics.service;

import com.stdev.smartmealtable.domain.statistics.ExpenditureStatistics;
import com.stdev.smartmealtable.domain.statistics.StatisticsRepository;
import com.stdev.smartmealtable.domain.statistics.StoreStatistics;
import com.stdev.smartmealtable.domain.statistics.UserStatistics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 통계 조회 Application Service
 * 읽기 전용 트랜잭션으로 통계 데이터 조회
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsApplicationService {

    private final StatisticsRepository statisticsRepository;

    /**
     * 사용자 통계 조회
     *
     * @return 사용자 통계 정보
     */
    public UserStatistics getUserStatistics() {
        return statisticsRepository.getUserStatistics();
    }

    /**
     * 지출 통계 조회
     *
     * @return 지출 통계 정보
     */
    public ExpenditureStatistics getExpenditureStatistics() {
        return statisticsRepository.getExpenditureStatistics();
    }

    /**
     * 음식점 통계 조회
     *
     * @return 음식점 통계 정보
     */
    public StoreStatistics getStoreStatistics() {
        return statisticsRepository.getStoreStatistics();
    }
}

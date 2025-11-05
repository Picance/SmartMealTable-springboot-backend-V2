package com.stdev.smartmealtable.domain.statistics;

/**
 * 통계 조회 Repository
 * 순수 Java 인터페이스 - Spring Data 의존성 없음 (POJO 원칙)
 */
public interface StatisticsRepository {

    /**
     * 사용자 통계 조회
     *
     * @return 사용자 관련 통계 정보
     */
    UserStatistics getUserStatistics();

    /**
     * 지출 통계 조회
     *
     * @return 지출 관련 통계 정보
     */
    ExpenditureStatistics getExpenditureStatistics();

    /**
     * 음식점 통계 조회
     *
     * @return 음식점 관련 통계 정보
     */
    StoreStatistics getStoreStatistics();
}

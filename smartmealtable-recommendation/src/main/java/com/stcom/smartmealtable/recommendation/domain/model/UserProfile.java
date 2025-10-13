package com.stcom.smartmealtable.recommendation.domain.model;

import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * 추천 시스템을 위한 사용자 프로필
 * 
 * <p>사용자의 선호도, 지출 내역, 위치 정보 등을 포함합니다.</p>
 */
@Getter
@Builder
public class UserProfile {
    
    /**
     * 회원 ID
     */
    private final Long memberId;
    
    /**
     * 추천 유형 (절약형, 모험형, 균형형)
     */
    private final RecommendationType recommendationType;
    
    /**
     * 현재 위도
     */
    private final BigDecimal currentLatitude;
    
    /**
     * 현재 경도
     */
    private final BigDecimal currentLongitude;
    
    /**
     * 카테고리별 선호도 (categoryId -> weight)
     * weight: 100 (좋아요), 0 (보통), -100 (싫어요)
     */
    private final Map<Long, Integer> categoryPreferences;
    
    /**
     * 최근 지출 내역 (날짜별)
     */
    private final Map<LocalDate, ExpenditureRecord> recentExpenditures;
    
    /**
     * 가게별 마지막 방문 날짜
     */
    private final Map<Long, LocalDate> storeLastVisitDates;
    
    /**
     * 특정 카테고리에 대한 선호도 조회
     * 
     * @param categoryId 카테고리 ID
     * @return 선호도 가중치 (100, 0, -100), 없으면 0
     */
    public Integer getCategoryPreference(Long categoryId) {
        return categoryPreferences.getOrDefault(categoryId, 0);
    }
    
    /**
     * 최근 N일간의 지출 내역 조회
     * 
     * @param days 조회할 일수
     * @return 지출 내역 맵 (날짜 -> 지출 레코드)
     */
    public Map<LocalDate, ExpenditureRecord> getRecentExpenditures(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return recentExpenditures.entrySet().stream()
                .filter(entry -> !entry.getKey().isBefore(startDate))
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }
    
    /**
     * 특정 가게의 마지막 방문 날짜 조회
     * 
     * @param storeId 가게 ID
     * @return 마지막 방문 날짜 (없으면 null)
     */
    public LocalDate getLastVisitDate(Long storeId) {
        return storeLastVisitDates.get(storeId);
    }
}

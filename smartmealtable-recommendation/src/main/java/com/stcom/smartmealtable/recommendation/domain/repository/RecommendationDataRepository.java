package com.stcom.smartmealtable.recommendation.domain.repository;

import com.stcom.smartmealtable.recommendation.domain.model.UserProfile;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 추천 시스템 데이터 Repository 인터페이스
 * 
 * <p>추천 알고리즘에 필요한 집계 데이터를 조회합니다.</p>
 * <p>Storage 계층에서 구현됩니다.</p>
 */
public interface RecommendationDataRepository {

    /**
     * 사용자 프로필 로드
     * 
     * <p>추천에 필요한 모든 사용자 정보를 조회합니다:</p>
     * <ul>
     *   <li>회원 기본 정보 (추천 타입 포함)</li>
     *   <li>카테고리 선호도 (좋아요: 100, 중립: 0, 싫어요: -100)</li>
     *   <li>최근 6개월 지출 내역</li>
     *   <li>가게 방문 이력 (마지막 방문 날짜)</li>
     *   <li>기본 주소 (위도/경도)</li>
     * </ul>
     * 
     * @param memberId 회원 ID
     * @return UserProfile 도메인 객체
     * @throws IllegalArgumentException memberId에 해당하는 회원이 없는 경우
     */
    UserProfile loadUserProfile(Long memberId);

    /**
     * 반경 내 가게 목록 조회 (필터링 포함)
     * 
     * <p>다음 조건으로 가게를 필터링합니다:</p>
     * <ul>
     *   <li>위치 기반 반경 필터 (Haversine distance)</li>
     *   <li>삭제되지 않은 가게만 조회</li>
     *   <li>영업시간 필터 (옵션)</li>
     *   <li>불호 카테고리 제외 (옵션)</li>
     *   <li>가게 타입 필터 (옵션)</li>
     * </ul>
     * 
     * @param latitude 사용자 위도
     * @param longitude 사용자 경도
     * @param radiusKm 검색 반경 (km)
     * @param excludedCategoryIds 제외할 카테고리 ID 목록 (불호 카테고리)
     * @param isOpenOnly 영업 중만 조회 여부
     * @return 필터링된 가게 목록
     */
    List<Store> findStoresInRadius(
            BigDecimal latitude,
            BigDecimal longitude,
            double radiusKm,
            List<Long> excludedCategoryIds,
            boolean isOpenOnly,
            StoreType storeType
    );

    /**
     * 반경 내 가게 목록 조회 (검색어 포함)
     * 
     * <p>다음 조건으로 가게를 필터링합니다:</p>
     * <ul>
     *   <li>위치 기반 반경 필터 (Haversine distance)</li>
     *   <li>삭제되지 않은 가게만 조회</li>
     *   <li>검색어로 가게명 또는 음식명 필터링 (옵션)</li>
     *   <li>영업시간 필터 (옵션)</li>
     *   <li>불호 카테고리 제외 (옵션)</li>
     *   <li>가게 타입 필터 (옵션)</li>
     * </ul>
     * 
     * @param latitude 사용자 위도
     * @param longitude 사용자 경도
     * @param radiusKm 검색 반경 (km)
     * @param keyword 검색어 (가게명 또는 음식명, null 허용)
     * @param excludedCategoryIds 제외할 카테고리 ID 목록 (불호 카테고리)
     * @param isOpenOnly 영업 중만 조회 여부
     * @return 필터링된 가게 목록
     */
    List<Store> findStoresInRadiusWithKeyword(
            BigDecimal latitude,
            BigDecimal longitude,
            double radiusKm,
            String keyword,
            List<Long> excludedCategoryIds,
            boolean isOpenOnly,
            StoreType storeType
    );

    /**
     * 여러 가게의 즐겨찾기 수 조회
     * 
     * @param storeIds 가게 ID 목록
     * @return 가게 ID -> 즐겨찾기 수 맵
     */
    Map<Long, Long> countFavoritesByStoreIds(List<Long> storeIds);
}

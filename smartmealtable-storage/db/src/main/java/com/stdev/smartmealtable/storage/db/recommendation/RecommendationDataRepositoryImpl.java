package com.stdev.smartmealtable.storage.db.recommendation;

import com.stcom.smartmealtable.recommendation.domain.model.ExpenditureRecord;
import com.stcom.smartmealtable.recommendation.domain.model.UserProfile;
import com.stcom.smartmealtable.recommendation.domain.repository.RecommendationDataRepository;
import com.stdev.smartmealtable.domain.expenditure.Expenditure;
import com.stdev.smartmealtable.domain.expenditure.ExpenditureRepository;
import com.stdev.smartmealtable.domain.favorite.FavoriteRepository;
import com.stdev.smartmealtable.domain.member.entity.AddressHistory;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.AddressHistoryRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.preference.Preference;
import com.stdev.smartmealtable.domain.preference.PreferenceRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.domain.store.StoreType;
import com.stdev.smartmealtable.storage.db.store.StoreQueryDslRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 추천 시스템 데이터 Repository 구현체
 * 
 * <p>여러 Repository를 조합하여 추천에 필요한 데이터를 조회합니다.</p>
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RecommendationDataRepositoryImpl implements RecommendationDataRepository {

    private final MemberRepository memberRepository;
    private final PreferenceRepository preferenceRepository;
    private final ExpenditureRepository expenditureRepository;
    private final AddressHistoryRepository addressHistoryRepository;
    private final StoreRepository storeRepository;
    private final FavoriteRepository favoriteRepository;
    private final StoreQueryDslRepository storeQueryDslRepository;

    @Override
    public UserProfile loadUserProfile(Long memberId) {
        log.debug("사용자 프로필 로드 시작 - memberId: {}", memberId);

        // 1. 회원 기본 정보 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. memberId: " + memberId));

        // 2. 카테고리 선호도 조회
        List<Preference> preferences = preferenceRepository.findByMemberId(memberId);
        Map<Long, Integer> categoryPreferences = preferences.stream()
                .collect(Collectors.toMap(
                        Preference::getCategoryId,
                        Preference::getWeight
                ));

        // 3. 최근 6개월 지출 내역 조회
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        LocalDate today = LocalDate.now();
        List<Expenditure> expenditures = expenditureRepository.findByMemberIdAndDateRange(
                memberId, 
                sixMonthsAgo, 
                today
        );

        // 지출 내역을 날짜 기준으로 맵 변환
        Map<LocalDate, ExpenditureRecord> recentExpenditures = expenditures.stream()
                .collect(Collectors.toMap(
                        Expenditure::getExpendedDate,
                        exp -> ExpenditureRecord.builder()
                                .expendedAt(exp.getExpendedDate())
                                .amount(exp.getAmount())
                                .categoryId(exp.getCategoryId())
                                .storeId(null) // TODO: Store ID 매핑 필요
                                .build(),
                        // 같은 날짜에 여러 지출이 있으면 첫 번째 것 사용
                        (existing, replacement) -> existing
                ));

        // 4. 가게 방문 이력 (마지막 방문 날짜)
        // TODO: 가게별 마지막 방문 날짜 조회 로직 추가
        // 현재는 지출 내역에서 가게명 기반으로 추출 (storeId가 없어서 임시 구현)
        Map<Long, LocalDate> storeLastVisitDates = new HashMap<>();

        // 5. 기본 주소 조회 (위도/경도)
        AddressHistory primaryAddress = addressHistoryRepository.findPrimaryByMemberId(memberId)
                .orElse(null);

        BigDecimal defaultLatitude = primaryAddress != null && primaryAddress.getAddress() != null && primaryAddress.getAddress().getLatitude() != null
                ? BigDecimal.valueOf(primaryAddress.getAddress().getLatitude())
                : BigDecimal.valueOf(37.5665); // 서울 시청 기본값

        BigDecimal defaultLongitude = primaryAddress != null && primaryAddress.getAddress() != null && primaryAddress.getAddress().getLongitude() != null
                ? BigDecimal.valueOf(primaryAddress.getAddress().getLongitude())
                : BigDecimal.valueOf(126.9780); // 서울 시청 기본값

        // 6. 추천 타입 (기본값: BALANCED)
        RecommendationType recommendationType = member.getRecommendationType() != null
                ? member.getRecommendationType()
                : RecommendationType.BALANCED;

        UserProfile userProfile = UserProfile.builder()
                .memberId(memberId)
                .recommendationType(recommendationType)
                .currentLatitude(defaultLatitude)
                .currentLongitude(defaultLongitude)
                .categoryPreferences(categoryPreferences)
                .recentExpenditures(recentExpenditures)
                .storeLastVisitDates(storeLastVisitDates)
                .build();

        log.debug("사용자 프로필 로드 완료 - userProfile: {}", userProfile);
        return userProfile;
    }

    @Override
    public List<Store> findStoresInRadius(
            BigDecimal latitude,
            BigDecimal longitude,
            double radiusKm,
            List<Long> excludedCategoryIds,
            boolean isOpenOnly,
            StoreType storeType
    ) {
        return findStoresInRadiusWithKeyword(
                latitude,
                longitude,
                radiusKm,
                null, // keyword 없음
                excludedCategoryIds,
                isOpenOnly,
                storeType
        );
    }

    @Override
    public List<Store> findStoresInRadiusWithKeyword(
            BigDecimal latitude,
            BigDecimal longitude,
            double radiusKm,
            String keyword,
            List<Long> excludedCategoryIds,
            boolean isOpenOnly,
            StoreType storeType
    ) {
        log.debug("반경 내 가게 조회 (검색어 포함) - lat: {}, lon: {}, radius: {}km, keyword: {}, excludedCategories: {}, openOnly: {}",
                latitude, longitude, radiusKm, keyword, excludedCategoryIds, isOpenOnly);

        // QueryDSL을 활용한 복잡한 쿼리 실행
        StoreRepository.StoreSearchResult result = storeQueryDslRepository.searchStores(
                keyword,        // 검색어 (가게명 또는 음식명)
                latitude,
                longitude,
                radiusKm,
                null,           // categoryId
                isOpenOnly,
                storeType,
                "DISTANCE",     // sortBy
                0,              // page
                1000            // size (충분히 큰 값)
        );

        List<Store> stores = result.stores().stream()
                .map(storeWithDistance -> storeWithDistance.store())
                .collect(Collectors.toList());

        // excludedCategoryIds 필터링 (메모리에서 처리)
        // 주 카테고리(첫 번째)를 확인하여 필터링
        if (excludedCategoryIds != null && !excludedCategoryIds.isEmpty()) {
            stores = stores.stream()
                    .filter(store -> {
                        Long primaryCategoryId = store.getCategoryIds() != null && !store.getCategoryIds().isEmpty()
                                ? store.getCategoryIds().get(0)
                                : null;
                        return primaryCategoryId == null || !excludedCategoryIds.contains(primaryCategoryId);
                    })
                    .collect(Collectors.toList());
        }

        log.debug("반경 내 가게 조회 완료 - {} 건", stores.size());
        return stores;
    }

    @Override
    public Map<Long, Long> countFavoritesByStoreIds(List<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return Map.of();
        }

        // 각 가게별 즐겨찾기 수 조회
        Map<Long, Long> favoriteCounts = new HashMap<>();
        for (Long storeId : storeIds) {
            // TODO: FavoriteRepository에 countByStoreId 메서드 추가 필요
            // 현재는 0으로 반환 (임시)
            favoriteCounts.put(storeId, 0L);
        }

        return favoriteCounts;
    }
}

package com.stdev.smartmealtable.api.recommendation.service;

import com.stcom.smartmealtable.recommendation.domain.model.RecommendationResult;
import com.stcom.smartmealtable.recommendation.domain.model.ScoreDetail;
import com.stcom.smartmealtable.recommendation.domain.model.UserProfile;
import com.stcom.smartmealtable.recommendation.domain.repository.RecommendationDataRepository;
import com.stcom.smartmealtable.recommendation.domain.service.RecommendationDomainService;
import com.stdev.smartmealtable.api.recommendation.dto.RecommendationRequestDto;
import com.stdev.smartmealtable.api.recommendation.dto.ScoreDetailResponseDto;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 추천 Application Service
 * 
 * <p>추천 시스템의 유즈케이스를 처리합니다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationApplicationService {

    private final RecommendationDomainService recommendationDomainService;
    private final RecommendationDataRepository recommendationDataRepository;
    private final StoreRepository storeRepository;
    private final MemberRepository memberRepository;

    /**
     * 추천 목록 조회
     * 
     * @param memberId 회원 ID
     * @param request 추천 요청
     * @return 추천 결과 목록
     */
    public List<RecommendationResult> getRecommendations(
            Long memberId,
            RecommendationRequestDto request
    ) {
        log.info("추천 목록 조회 시작 - memberId: {}, useCursor: {}, lastId: {}, limit: {}, page: {}, size: {}", 
                memberId, request.useCursorPagination(), request.getLastId(), request.getLimit(), request.getPage(), request.getSize());

        // 1. 사용자 프로필 조회 (Repository 사용)
        UserProfile userProfile = recommendationDataRepository.loadUserProfile(memberId);
        
        // 2. 요청에서 위도/경도가 제공된 경우 프로필 오버라이드
        if (request.getLatitude() != null && request.getLongitude() != null) {
            userProfile = UserProfile.builder()
                    .memberId(userProfile.getMemberId())
                    .recommendationType(userProfile.getRecommendationType())
                    .currentLatitude(request.getLatitude())
                    .currentLongitude(request.getLongitude())
                    .categoryPreferences(userProfile.getCategoryPreferences())
                    .recentExpenditures(userProfile.getRecentExpenditures())
                    .storeLastVisitDates(userProfile.getStoreLastVisitDates())
                    .build();
        }

        // 3. 가게 목록 필터링 (위치, 반경, 불호 카테고리 등)
        List<Long> excludedCategoryIds = getExcludedCategoryIds(userProfile);
        
        // 검색어가 있는 경우와 없는 경우 분기
        List<Store> filteredStores;
        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            // 검색어가 있는 경우: 검색어를 포함하는 메서드 사용
            filteredStores = recommendationDataRepository.findStoresInRadiusWithKeyword(
                    userProfile.getCurrentLatitude(),
                    userProfile.getCurrentLongitude(),
                    request.getRadius(),
                    request.getKeyword().trim(),
                    excludedCategoryIds,
                    request.getOpenNow() != null ? request.getOpenNow() : false
            );
            log.debug("검색어 '{}'로 필터링된 가게 수: {}", request.getKeyword(), filteredStores.size());
        } else {
            // 검색어가 없는 경우: 기존 메서드 사용
            filteredStores = recommendationDataRepository.findStoresInRadius(
                    userProfile.getCurrentLatitude(),
                    userProfile.getCurrentLongitude(),
                    request.getRadius(),
                    excludedCategoryIds,
                    request.getOpenNow() != null ? request.getOpenNow() : false
            );
            log.debug("필터링된 가게 수: {}", filteredStores.size());
        }

        // 4. 추천 점수 계산
        List<RecommendationResult> results = recommendationDomainService.calculateRecommendations(
                filteredStores,
                userProfile
        );

        // 5. 정렬
        List<RecommendationResult> sortedResults = sortResults(results, request.getSortBy());

        // 6. 페이징 (커서 또는 오프셋)
        if (request.useCursorPagination()) {
            return paginateByCursor(sortedResults, request.getLastId(), request.getLimit());
        } else {
            return paginateByOffset(sortedResults, request.getPage(), request.getSize());
        }
    }

    /**
     * 불호 카테고리 ID 목록 추출
     */
    private List<Long> getExcludedCategoryIds(UserProfile userProfile) {
        return userProfile.getCategoryPreferences().entrySet().stream()
                .filter(entry -> entry.getValue() == -100) // 싫어요 (-100)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 결과 정렬
     */
    private List<RecommendationResult> sortResults(
            List<RecommendationResult> results,
            RecommendationRequestDto.SortBy sortBy
    ) {
        return results.stream()
                .sorted(getComparator(sortBy))
                .collect(Collectors.toList());
    }

    /**
     * 정렬 기준에 따른 Comparator 생성
     */
    private Comparator<RecommendationResult> getComparator(RecommendationRequestDto.SortBy sortBy) {
        return switch (sortBy) {
            case SCORE -> Comparator.comparing(RecommendationResult::getFinalScore).reversed();
            case DISTANCE -> Comparator.comparing(RecommendationResult::getDistance);
            case REVIEW -> Comparator.comparing(RecommendationResult::getReviewCount).reversed();
            case PRICE_LOW -> Comparator.comparing(RecommendationResult::getAveragePrice);
            case PRICE_HIGH -> Comparator.comparing(RecommendationResult::getAveragePrice).reversed();
            default -> Comparator.comparing(RecommendationResult::getFinalScore).reversed();
        };
    }

    /**
     * 커서 기반 페이징 처리
     *
     * <p>마지막 항목 ID를 기준으로 다음 데이터를 조회합니다 (무한 스크롤용).</p>
     *
     * @param results 정렬된 결과 목록
     * @param lastId 마지막 항목의 ID (null이면 처음부터 시작)
     * @param limit 조회할 항목 수
     * @return 커서 기반 페이징된 결과 (limit + 1개를 조회하여 hasMore 판단)
     */
    private List<RecommendationResult> paginateByCursor(
            List<RecommendationResult> results,
            Long lastId,
            Integer limit
    ) {
        // lastId 이후의 항목들을 찾기 위해 시작 인덱스 결정
        int startIndex = 0;
        if (lastId != null) {
            // lastId와 일치하는 항목을 찾고 그 다음부터 시작
            for (int i = 0; i < results.size(); i++) {
                if (results.get(i).getStoreId().equals(lastId)) {
                    startIndex = i + 1;
                    break;
                }
            }
        }

        // limit + 1개를 조회하여 hasMore 판단 (클라이언트가 hasMore로 판단)
        int endIndex = Math.min(startIndex + limit, results.size());
        
        if (startIndex >= results.size()) {
            return Collections.emptyList();
        }

        return results.subList(startIndex, endIndex);
    }

    /**
     * 오프셋 기반 페이징 처리 (하위 호환성)
     *
     * @param results 정렬된 결과 목록
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 오프셋 기반 페이징된 결과
     */
    private List<RecommendationResult> paginateByOffset(
            List<RecommendationResult> results,
            int page,
            int size
    ) {
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, results.size());
        
        if (fromIndex >= results.size()) {
            return Collections.emptyList();
        }
        
        return results.subList(fromIndex, toIndex);
    }

    /**
     * 페이징 처리 (하위 호환성 - 구 메서드)
     */
    private List<RecommendationResult> paginateResults(
            List<RecommendationResult> results,
            int page,
            int size
    ) {
        return paginateByOffset(results, page, size);
    }

    /**
     * 특정 가게의 점수 상세 조회 (DTO 반환)
     * 
     * @param memberId 회원 ID
     * @param storeId 가게 ID
     * @param latitude 현재 위도 (선택)
     * @param longitude 현재 경도 (선택)
     * @return 점수 상세 정보 DTO
     */
    public ScoreDetailResponseDto getScoreDetailResponse(
            Long memberId,
            Long storeId,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        log.info("점수 상세 조회 (DTO) 시작 - memberId: {}, storeId: {}", memberId, storeId);

        // 1. 가게 조회
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다. storeId: " + storeId));

        // 2. 사용자 프로필 조회
        UserProfile userProfile = recommendationDataRepository.loadUserProfile(memberId);
        
        // 3. 위도/경도 오버라이드
        if (latitude != null && longitude != null) {
            userProfile = UserProfile.builder()
                    .memberId(userProfile.getMemberId())
                    .recommendationType(userProfile.getRecommendationType())
                    .currentLatitude(latitude)
                    .currentLongitude(longitude)
                    .categoryPreferences(userProfile.getCategoryPreferences())
                    .recentExpenditures(userProfile.getRecentExpenditures())
                    .storeLastVisitDates(userProfile.getStoreLastVisitDates())
                    .build();
        }

        // 4. 단일 가게에 대한 추천 점수 계산
        List<RecommendationResult> results = recommendationDomainService.calculateRecommendations(
                List.of(store),
                userProfile
        );

        if (results.isEmpty()) {
            throw new IllegalStateException("추천 점수 계산 실패");
        }

        RecommendationResult result = results.get(0);
        
        // 5. 거리 계산
        double distance = calculateDistance(
                userProfile.getCurrentLatitude(),
                userProfile.getCurrentLongitude(),
                store.getLatitude(),
                store.getLongitude()
        );
        
        // 6. DTO로 변환하여 반환
        return ScoreDetailResponseDto.from(
                result.getScoreDetail(),
                store.getStoreId(),
                store.getName(),
                distance
        );
    }

    /**
     * 특정 가게의 점수 상세 조회
     * 
     * @param memberId 회원 ID
     * @param storeId 가게 ID
     * @param latitude 현재 위도 (선택)
     * @param longitude 현재 경도 (선택)
     * @return 점수 상세 정보
     */
    public ScoreDetail getScoreDetail(
            Long memberId,
            Long storeId,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        log.info("점수 상세 조회 시작 - memberId: {}, storeId: {}", memberId, storeId);

        // 1. 가게 조회
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다. storeId: " + storeId));

        // 2. 사용자 프로필 조회
        UserProfile userProfile = recommendationDataRepository.loadUserProfile(memberId);
        
        // 3. 위도/경도 오버라이드
        if (latitude != null && longitude != null) {
            userProfile = UserProfile.builder()
                    .memberId(userProfile.getMemberId())
                    .recommendationType(userProfile.getRecommendationType())
                    .currentLatitude(latitude)
                    .currentLongitude(longitude)
                    .categoryPreferences(userProfile.getCategoryPreferences())
                    .recentExpenditures(userProfile.getRecentExpenditures())
                    .storeLastVisitDates(userProfile.getStoreLastVisitDates())
                    .build();
        }

        // 4. 단일 가게에 대한 추천 점수 계산
        List<RecommendationResult> results = recommendationDomainService.calculateRecommendations(
                List.of(store),
                userProfile
        );

        if (results.isEmpty()) {
            throw new IllegalStateException("추천 점수 계산 실패");
        }

        RecommendationResult result = results.get(0);
        
        // 5. ScoreDetail 반환
        return result.getScoreDetail();
    }

    /**
     * 추천 유형 변경
     * 
     * @param memberId 회원 ID
     * @param newType 새로운 추천 유형
     * @return 변경된 회원 정보
     */
    @Transactional
    public Member updateRecommendationType(Long memberId, RecommendationType newType) {
        log.info("추천 유형 변경 시작 - memberId: {}, newType: {}", memberId, newType);

        // 1. 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. memberId: " + memberId));

        // 2. 추천 유형 변경 (도메인 로직)
        member.changeRecommendationType(newType);

        // 3. 저장
        Member updatedMember = memberRepository.save(member);

        log.info("추천 유형 변경 완료 - memberId: {}, newType: {}", memberId, newType);
        
        return updatedMember;
    }

    /**
     * Haversine 거리 계산
     */
    private double calculateDistance(
            BigDecimal lat1, BigDecimal lon1,
            BigDecimal lat2, BigDecimal lon2
    ) {
        final int EARTH_RADIUS = 6371; // km

        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1.doubleValue())) *
                   Math.cos(Math.toRadians(lat2.doubleValue())) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * Mock 가게 데이터 생성 (테스트용)
     */
    private List<Store> createMockStores() {
        // TODO: 실제로는 DB에서 조회
        return List.of(
                Store.builder()
                        .storeId(1L)
                        .name("맛있는 한식당")
                        .categoryIds(List.of(1L))
                        .averagePrice(8000)
                        .reviewCount(150)
                        .viewCount(500)
                        .latitude(BigDecimal.valueOf(37.5665))
                        .longitude(BigDecimal.valueOf(126.9780))
                        .build(),
                Store.builder()
                        .storeId(2L)
                        .name("저렴한 분식집")
                        .categoryIds(List.of(3L))
                        .averagePrice(5000)
                        .reviewCount(80)
                        .viewCount(300)
                        .latitude(BigDecimal.valueOf(37.5670))
                        .longitude(BigDecimal.valueOf(126.9785))
                        .build()
        );
    }
}

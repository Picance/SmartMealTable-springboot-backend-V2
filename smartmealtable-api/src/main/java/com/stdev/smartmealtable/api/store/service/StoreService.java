package com.stdev.smartmealtable.api.store.service;

import com.stdev.smartmealtable.api.store.dto.BudgetComparison;
import com.stdev.smartmealtable.api.store.dto.GetFoodDetailResponse;
import com.stdev.smartmealtable.api.store.dto.StoreAutocompleteResponse;
import com.stdev.smartmealtable.api.store.dto.StoreDetailResponse;
import com.stdev.smartmealtable.api.store.dto.StoreListRequest;
import com.stdev.smartmealtable.api.store.dto.StoreListResponse;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.member.entity.AddressHistory;
import com.stdev.smartmealtable.domain.member.repository.AddressHistoryRepository;
import com.stdev.smartmealtable.domain.store.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 가게 관리 Application Service
 * - 가게 목록 조회 (위치 기반 필터링, 정렬)
 * - 가게 상세 조회 (조회 이력 기록, 조회수 증가)
 * - 가게 자동완성 검색
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {
    
    private final StoreRepository storeRepository;
    private final StoreOpeningHourRepository storeOpeningHourRepository;
    private final StoreTemporaryClosureRepository storeTemporaryClosureRepository;
    private final StoreViewHistoryRepository storeViewHistoryRepository;
    private final AddressHistoryRepository addressHistoryRepository;
    private final FoodRepository foodRepository;
    
    /**
     * 가게 목록 조회
     * - 사용자의 기본 주소를 기준으로 거리 계산
     * - 다양한 필터 및 정렬 옵션 지원
     */
    public StoreListResponse getStores(Long memberId, StoreListRequest request) {
        // 사용자의 기본 주소 조회
        AddressHistory primaryAddress = addressHistoryRepository.findPrimaryByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorType.ADDRESS_NOT_FOUND));
        
        // 가게 검색
        StoreRepository.StoreSearchResult searchResult = storeRepository.searchStores(
                request.keyword(),
                BigDecimal.valueOf(primaryAddress.getAddress().getLatitude()),
                BigDecimal.valueOf(primaryAddress.getAddress().getLongitude()),
                request.radius(),
                request.categoryId(),
                request.isOpen(),
                request.storeType(),
                request.sortBy(),
                request.page(),
                request.size()
        );
        
        return StoreListResponse.from(searchResult.stores(), searchResult.totalCount(), request.page(), request.size());
    }
    
    /**
     * 가게 상세 조회
     * - 조회 이력 기록
     * - 조회수 증가
     * - 메뉴 정보 포함
     */
    @Transactional
    public StoreDetailResponse getStoreDetail(Long memberId, Long storeId) {
        // 가게 조회
        Store store = storeRepository.findByIdAndDeletedAtIsNull(storeId)
                .orElseThrow(() -> new BusinessException(ErrorType.STORE_NOT_FOUND));
        
        // 조회 이력 기록
        StoreViewHistory viewHistory = storeViewHistoryRepository.createViewHistory(storeId, memberId);
        storeViewHistoryRepository.save(viewHistory);
        
        // 조회수 증가
        store.incrementViewCount();
        storeRepository.save(store);
        
        // 영업시간 조회
        List<StoreOpeningHour> openingHours = storeOpeningHourRepository.findByStoreId(storeId);
        
        // 임시 휴무 조회
        List<StoreTemporaryClosure> temporaryClosures = storeTemporaryClosureRepository.findByStoreId(storeId);
        
        // 메뉴(음식) 조회
        var foods = foodRepository.findByStoreId(storeId);
        
        // TODO: 즐겨찾기 여부 조회 추가 필요
        
        return StoreDetailResponse.from(store, openingHours, temporaryClosures, foods);
    }
    
    /**
     * 메뉴 상세 조회
     * - 메뉴 정보, 판매 가게 정보, 사용자 예산 비교 정보 반환
     */
    public GetFoodDetailResponse getFoodDetail(Long memberId, Long foodId) {
        // 음식 조회
        var food = foodRepository.findById(foodId)
                .orElseThrow(() -> new BusinessException(ErrorType.FOOD_NOT_FOUND));
        
        // 가게 조회
        var store = storeRepository.findById(food.getStoreId())
                .orElseThrow(() -> new BusinessException(ErrorType.STORE_NOT_FOUND));
        
        // TODO: 사용자의 현재 끼니별 예산 정보 조회
        // 현재는 null로 처리 (나중에 DailyBudgetRepository 활용)
        Integer userMealBudget = null;
        
        return GetFoodDetailResponse.from(food, store, userMealBudget);
    }
    
    /**
     * 가게 자동완성 검색
     */
    public List<StoreAutocompleteResponse> autocomplete(String keyword, Integer limit) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        
        int searchLimit = (limit != null && limit > 0) ? limit : 10;
        List<Store> stores = storeRepository.searchByKeywordForAutocomplete(keyword, searchLimit);
        
        return stores.stream()
                .map(StoreAutocompleteResponse::from)
                .toList();
    }
}

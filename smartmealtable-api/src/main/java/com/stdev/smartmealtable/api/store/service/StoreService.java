package com.stdev.smartmealtable.api.store.service;

import com.stdev.smartmealtable.api.store.dto.GetFoodDetailResponse;
import com.stdev.smartmealtable.api.store.dto.GetStoreFoodsResponse;
import com.stdev.smartmealtable.api.store.dto.StoreAutocompleteResponse;
import com.stdev.smartmealtable.api.store.dto.StoreDetailResponse;
import com.stdev.smartmealtable.api.store.dto.StoreListRequest;
import com.stdev.smartmealtable.api.store.dto.StoreListResponse;
import com.stdev.smartmealtable.domain.favorite.FavoriteRepository;
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
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
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
    private final StoreImageRepository storeImageRepository;
    private final StoreOpeningHourRepository storeOpeningHourRepository;
    private final StoreTemporaryClosureRepository storeTemporaryClosureRepository;
    private final StoreViewHistoryRepository storeViewHistoryRepository;
    private final AddressHistoryRepository addressHistoryRepository;
    private final FoodRepository foodRepository;
    private final FavoriteRepository favoriteRepository;
    private final Clock clock;
    
    /**
     * 가게 목록 조회
     * - 사용자의 기본 주소를 기준으로 거리 계산
     * - 다양한 필터 및 정렬 옵션 지원
     * - 커서 기반 페이징 및 오프셋 기반 페이징 모두 지원
     */
    public StoreListResponse getStores(Long memberId, StoreListRequest request) {
        // 사용자의 기본 주소 조회
        AddressHistory primaryAddress = addressHistoryRepository.findPrimaryByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorType.ADDRESS_NOT_FOUND));
        
        // 페이징 모드에 따라 처리
        if (request.useCursorPagination()) {
            return paginateByCursor(memberId, request, primaryAddress);
        } else {
            return paginateByOffset(memberId, request, primaryAddress);
        }
    }

    /**
     * 커서 기반 페이징으로 가게 목록 조회
     */
    private StoreListResponse paginateByCursor(Long memberId, StoreListRequest request, AddressHistory primaryAddress) {
        // 커서 기반 페이징: limit + 1개를 조회하여 hasMore 여부 결정
        int queryLimit = request.limit() + 1;

        StoreRepository.StoreSearchResult searchResult = storeRepository.searchStores(
                request.keyword(),
                BigDecimal.valueOf(primaryAddress.getAddress().getLatitude()),
                BigDecimal.valueOf(primaryAddress.getAddress().getLongitude()),
                request.radius(),
                request.categoryId(),
                request.isOpen(),
                request.storeType(),
                request.sortBy(),
                0, // 커서 기반에서는 page 무시
                queryLimit
        );

        // lastId 위치 이후의 데이터만 반환
        List<StoreWithDistance> resultStores = new java.util.ArrayList<>(searchResult.stores());
        if (request.lastId() != null) {
            int startIndex = 0;
            for (int i = 0; i < resultStores.size(); i++) {
                if (resultStores.get(i).store().getStoreId().equals(request.lastId())) {
                    startIndex = i + 1;
                    break;
                }
            }
            resultStores = resultStores.subList(startIndex, resultStores.size());
        }

        // limit개만 클라이언트에게 반환 (hasMore 결정용 +1개는 제외)
        int limit = request.limit();
        List<StoreWithDistance> returnStores = resultStores.stream()
                .limit(limit)
                .toList();

        return StoreListResponse.ofCursor(returnStores, searchResult.totalCount(), limit, limit);
    }


    /**
     * 오프셋 기반 페이징으로 가게 목록 조회 (기존 방식)
     */
    private StoreListResponse paginateByOffset(Long memberId, StoreListRequest request, AddressHistory primaryAddress) {
        StoreRepository.StoreSearchResult searchResult = storeRepository.searchStores(
                request.keyword(),
                BigDecimal.valueOf(primaryAddress.getAddress().getLatitude()),
                BigDecimal.valueOf(primaryAddress.getAddress().getLongitude()),
                request.radius(),
                request.categoryId(),
                request.isOpen(),
                request.storeType(),
                request.sortBy(),
                request.getEffectivePage(),
                request.size()
        );

        return StoreListResponse.from(searchResult.stores(), searchResult.totalCount(), request.getEffectivePage(), request.size());
    }

    
    /**
     * 가게 상세 조회
     * - 조회 이력 기록
     * - 조회수 증가
     * - 메뉴 정보 포함 (isMain, displayOrder 기준 정렬)
     * - 이미지 정보 포함 (isMain 우선, displayOrder 순 정렬)
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
        
        // 가게 이미지 조회 (isMain 우선, displayOrder 순 정렬)
        List<StoreImage> images = storeImageRepository.findByStoreId(storeId);
        
        // 영업시간 조회
        List<StoreOpeningHour> openingHours = storeOpeningHourRepository.findByStoreId(storeId);
        
        // 임시 휴무 조회
        List<StoreTemporaryClosure> temporaryClosures = storeTemporaryClosureRepository.findByStoreId(storeId);
        
        // 메뉴(음식) 조회 및 정렬 (isMain 우선, displayOrder 순)
        var foods = foodRepository.findByStoreId(storeId).stream()
                .sorted((f1, f2) -> {
                    // isMain 우선 정렬 (true가 먼저)
                    int mainCompare = Boolean.compare(
                            f2.getIsMain() != null && f2.getIsMain(),
                            f1.getIsMain() != null && f1.getIsMain()
                    );
                    if (mainCompare != 0) {
                        return mainCompare;
                    }
                    // displayOrder 오름차순 정렬 (null은 맨 뒤)
                    if (f1.getDisplayOrder() == null && f2.getDisplayOrder() == null) {
                        return 0;
                    }
                    if (f1.getDisplayOrder() == null) {
                        return 1;
                    }
                    if (f2.getDisplayOrder() == null) {
                        return -1;
                    }
                    return Integer.compare(f1.getDisplayOrder(), f2.getDisplayOrder());
                })
                .toList();
        
        boolean isFavorite = favoriteRepository.existsByMemberIdAndStoreId(memberId, storeId);
        boolean isOpen = isStoreOpenNow(openingHours, temporaryClosures);
        
        return StoreDetailResponse.from(store, images, openingHours, temporaryClosures, foods, isFavorite, isOpen);
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
     * 가게별 메뉴 목록 조회
     * - 특정 가게의 메뉴 목록만 조회
     * - 정렬 옵션: displayOrder (기본값), price, registeredDt, isMain
     * 
     * @param storeId 가게 ID
     * @param sort 정렬 기준 (displayOrder,asc/desc, price,asc/desc, registeredDt,desc, isMain,desc)
     */
    public GetStoreFoodsResponse getStoreFoods(Long storeId, String sort) {
        // 가게 조회
        Store store = storeRepository.findByIdAndDeletedAtIsNull(storeId)
                .orElseThrow(() -> new BusinessException(ErrorType.STORE_NOT_FOUND));
        
        // 메뉴 조회
        var foods = foodRepository.findByStoreId(storeId);
        
        // 정렬 처리
        var sortedFoods = sortFoods(foods, sort);
        
        return GetStoreFoodsResponse.of(storeId, store.getName(), sortedFoods);
    }
    
    /**
     * 메뉴 정렬 헬퍼 메서드
     */
    private List<com.stdev.smartmealtable.domain.food.Food> sortFoods(
            List<com.stdev.smartmealtable.domain.food.Food> foods, 
            String sort
    ) {
        if (sort == null || sort.isBlank()) {
            sort = "displayOrder,asc"; // 기본값
        }
        
        String[] parts = sort.split(",");
        String field = parts[0];
        String direction = parts.length > 1 ? parts[1] : "asc";
        final boolean ascending = "asc".equalsIgnoreCase(direction);
        
        return foods.stream()
                .sorted((f1, f2) -> {
                    if ("isMain".equals(field)) {
                        boolean f1Main = f1.getIsMain() != null && f1.getIsMain();
                        boolean f2Main = f2.getIsMain() != null && f2.getIsMain();
                        int mainCompare = Boolean.compare(f1Main, f2Main);
                        if (!ascending) {
                            mainCompare = -mainCompare;
                        }
                        if (mainCompare != 0) {
                            return mainCompare;
                        }
                        Integer d1 = f1.getDisplayOrder();
                        Integer d2 = f2.getDisplayOrder();
                        if (d1 == null && d2 == null) {
                            return 0;
                        }
                        if (d1 == null) {
                            return 1;
                        }
                        if (d2 == null) {
                            return -1;
                        }
                        return Integer.compare(d1, d2);
                    }

                    int compare = switch (field) {
                        case "price" -> {
                            Integer p1 = f1.getAveragePrice();
                            Integer p2 = f2.getAveragePrice();
                            if (p1 == null && p2 == null) yield 0;
                            if (p1 == null) yield 1;
                            if (p2 == null) yield -1;
                            yield Integer.compare(p1, p2);
                        }
                        case "registeredDt" -> {
                            var r1 = f1.getRegisteredDt();
                            var r2 = f2.getRegisteredDt();
                            if (r1 == null && r2 == null) yield 0;
                            if (r1 == null) yield 1;
                            if (r2 == null) yield -1;
                            yield r1.compareTo(r2);
                        }
                        default -> { // displayOrder
                            Integer d1 = f1.getDisplayOrder();
                            Integer d2 = f2.getDisplayOrder();
                            if (d1 == null && d2 == null) yield 0;
                            if (d1 == null) yield 1;
                            if (d2 == null) yield -1;
                            yield Integer.compare(d1, d2);
                        }
                    };
                    return ascending ? compare : -compare;
                })
                .toList();
    }

    private boolean isStoreOpenNow(List<StoreOpeningHour> openingHours, List<StoreTemporaryClosure> temporaryClosures) {
        LocalDateTime now = LocalDateTime.now(clock);
        if (isTemporarilyClosed(now, temporaryClosures)) {
            return false;
        }
        return isWithinOpeningHours(now, openingHours);
    }

    private boolean isWithinOpeningHours(LocalDateTime now, List<StoreOpeningHour> openingHours) {
        if (openingHours == null || openingHours.isEmpty()) {
            return false;
        }
        DayOfWeek today = now.getDayOfWeek();
        LocalTime currentTime = now.toLocalTime();

        return openingHours.stream()
                .filter(hour -> hour.dayOfWeek() == today)
                .findFirst()
                .map(hour -> isOpenDuringHour(hour, currentTime))
                .orElse(false);
    }

    private boolean isOpenDuringHour(StoreOpeningHour openingHour, LocalTime currentTime) {
        if (openingHour.isHoliday()) {
            return false;
        }

        LocalTime openTime = parseTime(openingHour.openTime());
        LocalTime closeTime = parseTime(openingHour.closeTime());
        if (openTime == null || closeTime == null || openTime.equals(closeTime)) {
            return false;
        }

        LocalTime breakStart = parseTime(openingHour.breakStartTime());
        LocalTime breakEnd = parseTime(openingHour.breakEndTime());
        if (breakStart != null && breakEnd != null && isTimeInRange(currentTime, breakStart, breakEnd)) {
            return false;
        }

        return isTimeInRange(currentTime, openTime, closeTime);
    }

    private boolean isTemporarilyClosed(LocalDateTime now, List<StoreTemporaryClosure> temporaryClosures) {
        if (temporaryClosures == null || temporaryClosures.isEmpty()) {
            return false;
        }

        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();

        return temporaryClosures.stream()
                .filter(closure -> closure.closureDate() != null && closure.closureDate().isEqual(today))
                .anyMatch(closure -> {
                    LocalTime start = closure.startTime() != null ? closure.startTime() : LocalTime.MIN;
                    LocalTime end = closure.endTime() != null ? closure.endTime() : LocalTime.MAX;
                    return isTimeInRange(currentTime, start, end);
                });
    }

    private boolean isTimeInRange(LocalTime target, LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            return false;
        }
        if (end.equals(start)) {
            return false;
        }

        if (end.isBefore(start)) {
            // overnight range (e.g., 21:00 ~ 03:00)
            return !target.isBefore(start) || target.isBefore(end);
        }

        return !target.isBefore(start) && target.isBefore(end);
    }

    private LocalTime parseTime(String timeValue) {
        if (timeValue == null || timeValue.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(timeValue);
        } catch (DateTimeParseException ex) {
            return null;
        }
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

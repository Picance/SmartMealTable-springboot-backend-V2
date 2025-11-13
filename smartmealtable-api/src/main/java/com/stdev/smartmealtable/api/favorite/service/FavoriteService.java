package com.stdev.smartmealtable.api.favorite.service;

import com.stdev.smartmealtable.api.favorite.dto.*;
import com.stdev.smartmealtable.api.home.service.BusinessHoursService;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.favorite.Favorite;
import com.stdev.smartmealtable.domain.favorite.FavoriteRepository;
import com.stdev.smartmealtable.domain.member.entity.AddressHistory;
import com.stdev.smartmealtable.domain.member.repository.AddressHistoryRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.support.location.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 즐겨찾기 Application Service
 * 유즈케이스에 집중하며 도메인 서비스를 조합하여 비즈니스 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FavoriteService {
    
    private final FavoriteRepository favoriteRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final AddressHistoryRepository addressHistoryRepository;
    private final BusinessHoursService businessHoursService;
    private final DistanceCalculator distanceCalculator;
    
    /**
     * 즐겨찾기 추가
     * [REQ-FAV-101] 중복 검사를 수행하여 동일 가게의 중복 등록을 방지
     * 
     * @param memberId 회원 ID
     * @param request 즐겨찾기 추가 요청
     * @return 추가된 즐겨찾기 정보
     */
    @Transactional
    public AddFavoriteResponse addFavorite(Long memberId, AddFavoriteRequest request) {
        Long storeId = request.getStoreId();
        
        // 1. 가게 존재 여부 확인
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorType.STORE_NOT_FOUND));
        
        // 2. 중복 체크
        if (favoriteRepository.existsByMemberIdAndStoreId(memberId, storeId)) {
            throw new BusinessException(ErrorType.FAVORITE_ALREADY_EXISTS);
        }
        
        // 3. 새로운 우선순위 계산 (최대값 + 1)
        Long maxPriority = favoriteRepository.findMaxPriorityByMemberId(memberId);
        Long newPriority = maxPriority + 1;
        
        // 4. 즐겨찾기 생성 및 저장
        Favorite favorite = Favorite.create(memberId, storeId, newPriority);
        Favorite savedFavorite = favoriteRepository.save(favorite);
        
        return AddFavoriteResponse.builder()
                .favoriteId(savedFavorite.getFavoriteId())
                .storeId(savedFavorite.getStoreId())
                .priority(savedFavorite.getPriority())
                .favoritedAt(savedFavorite.getFavoritedAt())
                .build();
    }
    
    /**
     * 즐겨찾기 목록 조회 (커서 기반)
     *
     * @param memberId 회원 ID
     * @param request 조회 조건
     * @return 즐겨찾기 목록 + 커서 정보
     */
    public GetFavoritesResponse getFavorites(Long memberId, GetFavoritesRequest request) {
        if (request.isUnsupportedSort()) {
            throw new BusinessException(ErrorType.INVALID_INPUT);
        }

        AddressHistory primaryAddress = addressHistoryRepository.findPrimaryByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorType.ADDRESS_NOT_FOUND));

        List<Favorite> favorites = favoriteRepository.findByMemberIdOrderByPriorityAsc(memberId);
        if (favorites.isEmpty()) {
            return GetFavoritesResponse.builder()
                    .favorites(List.of())
                    .totalCount(0)
                    .openCount(0)
                    .size(request.pageSize())
                    .hasNext(false)
                    .nextCursor(null)
                    .build();
        }

        Map<Long, Store> storeMap = loadStores(favorites);
        if (storeMap.isEmpty()) {
            return GetFavoritesResponse.builder()
                    .favorites(List.of())
                    .totalCount(0)
                    .openCount(0)
                    .size(request.pageSize())
                    .hasNext(false)
                    .nextCursor(null)
                    .build();
        }

        Map<Long, Category> categoryMap = loadCategories(storeMap);
        List<FavoriteView> views = buildFavoriteViews(favorites, storeMap, categoryMap, primaryAddress);

        // 카테고리 필터 적용
        List<FavoriteView> categoryFiltered = filterByCategory(views, request.categoryId());

        int openCount = (int) categoryFiltered.stream()
                .filter(FavoriteView::isOpenNow)
                .count();

        List<FavoriteView> openFiltered = request.openOnly()
                ? categoryFiltered.stream().filter(FavoriteView::isOpenNow).toList()
                : categoryFiltered;

        List<FavoriteView> sorted = sortViews(openFiltered, request.sortKey());
        CursorResult cursorResult = applyCursor(sorted, request.cursor(), request.pageSize());

        List<FavoriteStoreDto> responseDtos = cursorResult.items().stream()
                .map(this::toDto)
                .toList();

        return GetFavoritesResponse.builder()
                .favorites(responseDtos)
                .totalCount(openFiltered.size())
                .openCount(openCount)
                .size(request.pageSize())
                .hasNext(cursorResult.hasNext())
                .nextCursor(cursorResult.nextCursor())
                .build();
    }

    private Map<Long, Store> loadStores(List<Favorite> favorites) {
        List<Long> storeIds = favorites.stream()
                .map(Favorite::getStoreId)
                .distinct()
                .toList();

        if (storeIds.isEmpty()) {
            return Map.of();
        }

        return storeRepository.findByIdIn(storeIds).stream()
                .collect(Collectors.toMap(Store::getStoreId, Function.identity()));
    }

    private Map<Long, Category> loadCategories(Map<Long, Store> storeMap) {
        List<Long> categoryIds = storeMap.values().stream()
                .flatMap(store -> store.getCategoryIds().stream())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (categoryIds.isEmpty()) {
            return Map.of();
        }

        return categoryRepository.findByIdIn(categoryIds).stream()
                .collect(Collectors.toMap(Category::getCategoryId, Function.identity()));
    }

    private List<FavoriteView> buildFavoriteViews(
            List<Favorite> favorites,
            Map<Long, Store> storeMap,
            Map<Long, Category> categoryMap,
            AddressHistory primaryAddress
    ) {
        Double userLat = primaryAddress.getAddress() != null ? primaryAddress.getAddress().getLatitude() : null;
        Double userLon = primaryAddress.getAddress() != null ? primaryAddress.getAddress().getLongitude() : null;

        List<FavoriteView> views = new ArrayList<>();
        for (Favorite favorite : favorites) {
            Store store = storeMap.get(favorite.getStoreId());
            if (store == null) {
                log.debug("Skipping favorite {} because store {} not found", favorite.getFavoriteId(), favorite.getStoreId());
                continue;
            }

            views.add(buildView(favorite, store, categoryMap, userLat, userLon));
        }
        return views;
    }

    private FavoriteView buildView(
            Favorite favorite,
            Store store,
            Map<Long, Category> categoryMap,
            Double userLat,
            Double userLon
    ) {
        List<Long> categoryIds = store.getCategoryIds() != null ? store.getCategoryIds() : List.of();
        Long primaryCategoryId = !categoryIds.isEmpty() ? categoryIds.get(0) : null;
        String categoryName = primaryCategoryId != null && categoryMap.containsKey(primaryCategoryId)
                ? categoryMap.get(primaryCategoryId).getName()
                : "";

        Double distance = calculateDistance(userLat, userLon, store);
        boolean isOpenNow = businessHoursService.getOperationStatus(store.getStoreId()).isOpen();

        return new FavoriteView(
                favorite,
                store,
                categoryIds,
                primaryCategoryId,
                categoryName,
                distance,
                isOpenNow
        );
    }

    private List<FavoriteView> filterByCategory(List<FavoriteView> views, Long categoryId) {
        if (categoryId == null) {
            return views;
        }

        return views.stream()
                .filter(view -> view.categoryIds().contains(categoryId))
                .toList();
    }

    private List<FavoriteView> sortViews(List<FavoriteView> views, String sortKey) {
        if (views.isEmpty()) {
            return views;
        }

        List<FavoriteView> sorted = new ArrayList<>(views);
        sorted.sort(buildComparator(sortKey));
        return sorted;
    }

    private Comparator<FavoriteView> buildComparator(String sortKey) {
        Comparator<FavoriteView> comparator;
        switch (sortKey) {
            case "name" -> comparator = Comparator.comparing(
                    (FavoriteView view) -> view.store().getName(),
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            );
            case "reviewcount" -> comparator = Comparator.comparingInt(
                    (FavoriteView view) -> safeInt(view.store().getReviewCount())
            ).reversed();
            case "distance" -> comparator = Comparator.comparing(
                    FavoriteView::distance,
                    Comparator.nullsLast(Double::compareTo)
            );
            case "createdat" -> comparator = Comparator.comparing(
                    (FavoriteView view) -> view.favorite().getFavoritedAt(),
                    Comparator.nullsLast(Comparator.naturalOrder())
            ).reversed();
            case "priority" -> comparator = Comparator.comparing(
                    (FavoriteView view) -> view.favorite().getPriority(),
                    Comparator.nullsLast(Long::compareTo)
            );
            default -> comparator = Comparator.comparing(
                    (FavoriteView view) -> view.favorite().getPriority(),
                    Comparator.nullsLast(Long::compareTo)
            );
        }

        return comparator.thenComparing((FavoriteView view) -> view.favorite().getFavoriteId());
    }

    private CursorResult applyCursor(List<FavoriteView> sorted, Long cursor, int size) {
        if (sorted.isEmpty()) {
            return new CursorResult(List.of(), false, null);
        }

        int startIndex = 0;
        if (cursor != null) {
            for (int i = 0; i < sorted.size(); i++) {
                if (Objects.equals(sorted.get(i).favorite().getFavoriteId(), cursor)) {
                    startIndex = i + 1;
                    break;
                }
            }
            if (startIndex >= sorted.size()) {
                return new CursorResult(List.of(), false, null);
            }
        }

        int endIndex = Math.min(sorted.size(), startIndex + size);
        List<FavoriteView> slice = new ArrayList<>(sorted.subList(startIndex, endIndex));
        boolean hasNext = endIndex < sorted.size();
        Long nextCursor = hasNext && !slice.isEmpty()
                ? slice.get(slice.size() - 1).favorite().getFavoriteId()
                : null;

        return new CursorResult(slice, hasNext, nextCursor);
    }

    private FavoriteStoreDto toDto(FavoriteView view) {
        return FavoriteStoreDto.builder()
                .favoriteId(view.favorite().getFavoriteId())
                .storeId(view.store().getStoreId())
                .storeName(view.store().getName())
                .categoryId(view.primaryCategoryId())
                .categoryName(view.primaryCategoryName())
                .reviewCount(view.store().getReviewCount())
                .averagePrice(view.store().getAveragePrice())
                .address(view.store().getAddress())
                .distance(view.distance())
                .imageUrl(view.store().getImageUrl())
                .displayOrder(view.favorite().getPriority())
                .isOpenNow(view.isOpenNow())
                .createdAt(view.favorite().getFavoritedAt())
                .build();
    }

    private Double calculateDistance(Double userLat, Double userLon, Store store) {
        if (userLat == null || userLon == null || store.getLatitude() == null || store.getLongitude() == null) {
            return null;
        }
        try {
            BigDecimal distanceKm = distanceCalculator.calculateDistanceKm(
                    userLat,
                    userLon,
                    store.getLatitude().doubleValue(),
                    store.getLongitude().doubleValue()
            );
            return distanceKm.doubleValue();
        } catch (IllegalArgumentException e) {
            log.debug("Failed to calculate distance for store {}", store.getStoreId(), e);
            return null;
        }
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private record FavoriteView(
            Favorite favorite,
            Store store,
            List<Long> categoryIds,
            Long primaryCategoryId,
            String primaryCategoryName,
            Double distance,
            boolean isOpenNow
    ) {
    }

    private record CursorResult(
            List<FavoriteView> items,
            boolean hasNext,
            Long nextCursor
    ) {
    }
    
    /**
     * 즐겨찾기 순서 변경
     * [REQ-FAV-201] 드래그 앤 드롭으로 순서를 변경하고 서버에 저장
     * 
     * @param memberId 회원 ID
     * @param request 순서 변경 요청
     * @return 변경 결과
     */
    @Transactional
    public ReorderFavoritesResponse reorderFavorites(Long memberId, ReorderFavoritesRequest request) {
        List<ReorderFavoritesRequest.FavoriteOrderDto> orderList = request.getFavoriteOrders();
        
        if (orderList == null || orderList.isEmpty()) {
            throw new BusinessException(ErrorType.INVALID_INPUT);
        }
        
        // 1. 즐겨찾기 조회 및 권한 확인
        List<Long> favoriteIds = orderList.stream()
                .map(ReorderFavoritesRequest.FavoriteOrderDto::getFavoriteId)
                .collect(Collectors.toList());
        
        Map<Long, Favorite> favoriteMap = favoriteIds.stream()
                .map(favoriteRepository::findById)
                .filter(opt -> opt.isPresent())
                .map(opt -> opt.get())
                .peek(favorite -> {
                    // 권한 확인: 본인의 즐겨찾기만 수정 가능
                    if (!favorite.getMemberId().equals(memberId)) {
                        throw new BusinessException(ErrorType.FORBIDDEN_ACCESS);
                    }
                })
                .collect(Collectors.toMap(Favorite::getFavoriteId, favorite -> favorite));
        
        // 2. 우선순위 변경
        List<Favorite> updatedFavorites = orderList.stream()
                .map(orderDto -> {
                    Favorite favorite = favoriteMap.get(orderDto.getFavoriteId());
                    if (favorite != null) {
                        favorite.changePriority(orderDto.getPriority());
                    }
                    return favorite;
                })
                .filter(favorite -> favorite != null)
                .collect(Collectors.toList());
        
        // 3. 일괄 저장
        favoriteRepository.saveAll(updatedFavorites);
        
        return ReorderFavoritesResponse.builder()
                .updatedCount(updatedFavorites.size())
                .message("즐겨찾기 순서가 성공적으로 변경되었습니다.")
                .build();
    }
    
    /**
     * 즐겨찾기 삭제
     * [REQ-FAV-301] 삭제 재확인 후 즐겨찾기에서 제거
     * 
     * @param memberId 회원 ID
     * @param favoriteId 즐겨찾기 ID
     * @return 삭제 결과
     */
    @Transactional
    public DeleteFavoriteResponse deleteFavorite(Long memberId, Long favoriteId) {
        // 1. 즐겨찾기 조회
        Favorite favorite = favoriteRepository.findById(favoriteId)
                .orElseThrow(() -> new BusinessException(ErrorType.FAVORITE_NOT_FOUND));
        
        // 2. 권한 확인: 본인의 즐겨찾기만 삭제 가능
        if (!favorite.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorType.FORBIDDEN_ACCESS);
        }
        
        // 3. 삭제
        favoriteRepository.delete(favorite);
        
        return DeleteFavoriteResponse.builder()
                .favoriteId(favoriteId)
                .message("즐겨찾기가 성공적으로 삭제되었습니다.")
                .build();
    }
}

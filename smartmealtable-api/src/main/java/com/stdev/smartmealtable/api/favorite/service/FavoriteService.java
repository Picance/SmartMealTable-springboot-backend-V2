package com.stdev.smartmealtable.api.favorite.service;

import com.stdev.smartmealtable.api.favorite.dto.*;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.favorite.Favorite;
import com.stdev.smartmealtable.domain.favorite.FavoriteRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 즐겨찾기 Application Service
 * 유즈케이스에 집중하며 도메인 서비스를 조합하여 비즈니스 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {
    
    private final FavoriteRepository favoriteRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    
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
     * 즐겨찾기 목록 조회
     * [REQ-FAV-102] 저장된 순서대로 가게 목록을 표시
     * 
     * @param memberId 회원 ID
     * @return 즐겨찾기 목록
     */
    public GetFavoritesResponse getFavorites(Long memberId) {
        // 1. 즐겨찾기 목록 조회 (우선순위 오름차순)
        List<Favorite> favorites = favoriteRepository.findByMemberIdOrderByPriorityAsc(memberId);
        
        if (favorites.isEmpty()) {
            return GetFavoritesResponse.builder()
                    .favorites(List.of())
                    .totalCount(0)
                    .build();
        }
        
        // 2. 가게 정보 조회
        List<Long> storeIds = favorites.stream()
                .map(Favorite::getStoreId)
                .collect(Collectors.toList());
        
        Map<Long, Store> storeMap = storeIds.stream()
                .map(storeRepository::findById)
                .filter(opt -> opt.isPresent())
                .map(opt -> opt.get())
                .collect(Collectors.toMap(Store::getStoreId, store -> store));
        
        // 3. 카테고리 정보 조회
        List<Long> categoryIds = storeMap.values().stream()
                .map(store -> store.getCategoryIds().isEmpty() ? null : store.getCategoryIds().get(0))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, Category> categoryMap = categoryIds.stream()
                .map(categoryRepository::findById)
                .filter(opt -> opt.isPresent())
                .map(opt -> opt.get())
                .collect(Collectors.toMap(Category::getCategoryId, category -> category));
        
        // 4. DTO 변환
        List<FavoriteStoreDto> favoriteStoreDtos = favorites.stream()
                .map(favorite -> {
                    Store store = storeMap.get(favorite.getStoreId());
                    if (store == null) {
                        return null;
                    }
                    
                    Long primaryCategoryId = store.getCategoryIds().isEmpty() ? null : store.getCategoryIds().get(0);
                    Category category = primaryCategoryId != null ? categoryMap.get(primaryCategoryId) : null;
                    String categoryName = category != null ? category.getName() : "";
                    
                    return FavoriteStoreDto.builder()
                            .favoriteId(favorite.getFavoriteId())
                            .storeId(store.getStoreId())
                            .storeName(store.getName())
                            .categoryName(categoryName)
                            .reviewCount(store.getReviewCount())
                            .averagePrice(store.getAveragePrice())
                            .address(store.getAddress())
                            .imageUrl(store.getImageUrl())
                            .priority(favorite.getPriority())
                            .favoritedAt(favorite.getFavoritedAt())
                            .build();
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
        
        return GetFavoritesResponse.builder()
                .favorites(favoriteStoreDtos)
                .totalCount(favoriteStoreDtos.size())
                .build();
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

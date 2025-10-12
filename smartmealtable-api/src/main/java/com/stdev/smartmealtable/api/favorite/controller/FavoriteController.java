package com.stdev.smartmealtable.api.favorite.controller;

import com.stdev.smartmealtable.api.favorite.dto.*;
import com.stdev.smartmealtable.api.favorite.service.FavoriteService;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import com.stdev.smartmealtable.core.auth.AuthUser;
import com.stdev.smartmealtable.core.auth.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 즐겨찾기 Controller
 * 즐겨찾기 추가, 조회, 순서 변경, 삭제 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    
    private final FavoriteService favoriteService;
    
    /**
     * 즐겨찾기 추가
     * POST /api/v1/favorites
     * 
     * @param user 인증된 사용자 정보
     * @param request 즐겨찾기 추가 요청
     * @return 추가된 즐겨찾기 정보
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AddFavoriteResponse> addFavorite(
            @AuthUser AuthenticatedUser user,
            @RequestBody AddFavoriteRequest request) {
        AddFavoriteResponse response = favoriteService.addFavorite(user.memberId(), request);
        return ApiResponse.success(response);
    }
    
    /**
     * 즐겨찾기 목록 조회
     * GET /api/v1/favorites
     * 
     * @param user 인증된 사용자 정보
     * @return 즐겨찾기 목록
     */
    @GetMapping
    public ApiResponse<GetFavoritesResponse> getFavorites(@AuthUser AuthenticatedUser user) {
        GetFavoritesResponse response = favoriteService.getFavorites(user.memberId());
        return ApiResponse.success(response);
    }
    
    /**
     * 즐겨찾기 순서 변경
     * PUT /api/v1/favorites/reorder
     * 
     * @param user 인증된 사용자 정보
     * @param request 순서 변경 요청
     * @return 변경 결과
     */
    @PutMapping("/reorder")
    public ApiResponse<ReorderFavoritesResponse> reorderFavorites(
            @AuthUser AuthenticatedUser user,
            @RequestBody ReorderFavoritesRequest request) {
        ReorderFavoritesResponse response = favoriteService.reorderFavorites(user.memberId(), request);
        return ApiResponse.success(response);
    }
    
    /**
     * 즐겨찾기 삭제
     * DELETE /api/v1/favorites/{favoriteId}
     * 
     * @param user 인증된 사용자 정보
     * @param favoriteId 즐겨찾기 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/{favoriteId}")
    public ApiResponse<DeleteFavoriteResponse> deleteFavorite(
            @AuthUser AuthenticatedUser user,
            @PathVariable Long favoriteId) {
        DeleteFavoriteResponse response = favoriteService.deleteFavorite(user.memberId(), favoriteId);
        return ApiResponse.success(response);
    }
}

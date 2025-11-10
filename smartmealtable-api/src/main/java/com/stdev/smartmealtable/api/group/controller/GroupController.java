package com.stdev.smartmealtable.api.group.controller;

import com.stdev.smartmealtable.api.group.controller.dto.GroupResponse;
import com.stdev.smartmealtable.api.group.service.GroupAutocompleteService;
import com.stdev.smartmealtable.api.group.service.SearchGroupsService;
import com.stdev.smartmealtable.api.group.service.dto.GroupAutocompleteResponse;
import com.stdev.smartmealtable.api.group.service.dto.SearchGroupsServiceResponse;
import com.stdev.smartmealtable.api.group.service.dto.TrendingKeywordsResponse;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import com.stdev.smartmealtable.core.api.response.PageInfo;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 그룹 관리 Controller
 * 온보딩 시 소속 선택을 위한 그룹 검색 API 제공
 */
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Slf4j
@Validated
public class GroupController {

    private final SearchGroupsService searchGroupsService;
    private final GroupAutocompleteService groupAutocompleteService;

    /**
     * 그룹 목록 조회 (검색 + 페이징)
     * GET /api/v1/groups?type=UNIVERSITY&name=서울&page=0&size=20
     *
     * @param type 그룹 타입 (optional): UNIVERSITY, COMPANY, OTHER
     * @param name 그룹 이름 검색어 (optional)
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 20)
     * @return 그룹 목록과 페이징 정보
     */
    @GetMapping
    public ResponseEntity<ApiResponse<GroupListResponse>> searchGroups(
            @RequestParam(required = false) GroupType type,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("그룹 검색 API 호출 - type: {}, name: {}, page: {}, size: {}", type, name, page, size);

        SearchGroupsServiceResponse serviceResponse = searchGroupsService.searchGroups(type, name, page, size);

        // Response DTO 변환
        List<GroupResponse> groups = serviceResponse.groups().stream()
                .map(g -> new GroupResponse(
                        g.groupId(),
                        g.name(),
                        g.type(),
                        g.address()
                ))
                .collect(Collectors.toList());

        int totalCount = serviceResponse.totalCount();
        int totalPages = (int) Math.ceil((double) totalCount / size);

        PageInfo pageInfo = new PageInfo(
                page,
                size,
                totalCount,
                totalPages,
                page >= totalPages - 1
        );

        GroupListResponse response = new GroupListResponse(groups, pageInfo);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 그룹 목록 응답 DTO
     */
    public record GroupListResponse(
            List<GroupResponse> content,
            PageInfo pageInfo
    ) {
    }
    
    // ==================== 검색 기능 강화 API ====================
    
    /**
     * 그룹 자동완성
     * GET /api/v1/groups/autocomplete?keyword=서울&limit=10
     *
     * @param keyword 검색 키워드 (필수, 1-50자)
     * @param limit 결과 개수 제한 (기본값: 10, 최대: 20)
     * @return 자동완성 제안 목록
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<ApiResponse<GroupAutocompleteResponse>> autocomplete(
            @RequestParam @Size(min = 1, max = 50, message = "검색 키워드는 1-50자 이내여야 합니다.") String keyword,
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) int limit
    ) {
        log.info("그룹 자동완성 API 호출 - keyword: {}, limit: {}", keyword, limit);

        GroupAutocompleteResponse response = groupAutocompleteService.autocomplete(keyword, limit);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 인기 검색어 조회
     * GET /api/v1/groups/trending?limit=10
     *
     * @param limit 결과 개수 (기본값: 10, 최대: 20)
     * @return 인기 검색어 목록
     */
    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<TrendingKeywordsResponse>> getTrendingKeywords(
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) int limit
    ) {
        log.info("인기 검색어 조회 API 호출 - limit: {}", limit);
        
        TrendingKeywordsResponse response = groupAutocompleteService.getTrendingKeywords(limit);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

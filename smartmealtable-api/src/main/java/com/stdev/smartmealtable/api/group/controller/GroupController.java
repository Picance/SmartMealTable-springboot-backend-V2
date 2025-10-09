package com.stdev.smartmealtable.api.group.controller;

import com.stdev.smartmealtable.api.group.controller.dto.GroupResponse;
import com.stdev.smartmealtable.api.group.service.SearchGroupsService;
import com.stdev.smartmealtable.api.group.service.dto.SearchGroupsServiceResponse;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import com.stdev.smartmealtable.core.api.response.PageInfo;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
public class GroupController {

    private final SearchGroupsService searchGroupsService;

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
}

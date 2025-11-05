package com.stdev.smartmealtable.admin.group.controller;

import com.stdev.smartmealtable.admin.group.controller.request.CreateGroupRequest;
import com.stdev.smartmealtable.admin.group.controller.request.UpdateGroupRequest;
import com.stdev.smartmealtable.admin.group.controller.response.GroupListResponse;
import com.stdev.smartmealtable.admin.group.controller.response.GroupResponse;
import com.stdev.smartmealtable.admin.group.service.GroupApplicationService;
import com.stdev.smartmealtable.admin.group.service.dto.*;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 그룹 관리 API Controller (ADMIN)
 * 
 * <p>관리자가 그룹(학교/회사) 마스터 데이터를 관리하는 API를 제공합니다.</p>
 */
@RestController
@RequestMapping("/api/v1/admin/groups")
@RequiredArgsConstructor
@Validated
@Slf4j
public class GroupController {

    private final GroupApplicationService groupApplicationService;

    /**
     * 그룹 목록 조회 (페이징, 검색)
     *
     * @param type 그룹 타입 필터 (선택)
     * @param name 검색할 그룹 이름 (선택)
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 20)
     * @return 그룹 목록
     */
    @GetMapping
    public ApiResponse<GroupListResponse> getGroups(
            @RequestParam(required = false) GroupType type,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size
    ) {
        GroupListServiceRequest serviceRequest = GroupListServiceRequest.of(type, name, page, size);
        GroupListServiceResponse serviceResponse = groupApplicationService.getGroups(serviceRequest);
        
        GroupListResponse response = GroupListResponse.from(serviceResponse);
        return ApiResponse.success(response);
    }

    /**
     * 그룹 상세 조회
     *
     * @param groupId 그룹 ID
     * @return 그룹 상세 정보
     */
    @GetMapping("/{groupId}")
    public ApiResponse<GroupResponse> getGroup(
            @PathVariable Long groupId
    ) {
        GroupServiceResponse serviceResponse = groupApplicationService.getGroup(groupId);
        
        GroupResponse response = GroupResponse.from(serviceResponse);
        return ApiResponse.success(response);
    }

    /**
     * 그룹 생성
     *
     * @param request 그룹 생성 요청
     * @return 생성된 그룹 정보 (201 Created)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GroupResponse> createGroup(
            @Valid @RequestBody CreateGroupRequest request
    ) {
        CreateGroupServiceRequest serviceRequest = CreateGroupServiceRequest.of(
                request.name(),
                request.type(),
                request.address()
        );
        GroupServiceResponse serviceResponse = groupApplicationService.createGroup(serviceRequest);
        
        GroupResponse response = GroupResponse.from(serviceResponse);
        return ApiResponse.success(response);
    }

    /**
     * 그룹 수정
     *
     * @param groupId 그룹 ID
     * @param request 그룹 수정 요청
     * @return 수정된 그룹 정보
     */
    @PutMapping("/{groupId}")
    public ApiResponse<GroupResponse> updateGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody UpdateGroupRequest request
    ) {
        UpdateGroupServiceRequest serviceRequest = UpdateGroupServiceRequest.of(
                request.name(),
                request.type(),
                request.address()
        );
        GroupServiceResponse serviceResponse = groupApplicationService.updateGroup(groupId, serviceRequest);
        
        GroupResponse response = GroupResponse.from(serviceResponse);
        return ApiResponse.success(response);
    }

    /**
     * 그룹 삭제
     *
     * @param groupId 그룹 ID
     * @return 204 No Content
     */
    @DeleteMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroup(
            @PathVariable Long groupId
    ) {
        groupApplicationService.deleteGroup(groupId);
    }
}

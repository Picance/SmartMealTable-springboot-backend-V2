package com.stdev.smartmealtable.admin.group.service.dto;

import com.stdev.smartmealtable.domain.member.entity.GroupPageResult;

import java.util.List;

/**
 * 그룹 목록 조회 Service Response DTO
 */
public record GroupListServiceResponse(
        List<GroupServiceResponse> groups,
        PageInfo pageInfo
) {
    public static GroupListServiceResponse from(GroupPageResult pageResult) {
        List<GroupServiceResponse> groups = pageResult.content().stream()
                .map(GroupServiceResponse::from)
                .toList();
        
        PageInfo pageInfo = new PageInfo(
                pageResult.pageNumber(),
                pageResult.pageSize(),
                pageResult.totalElements(),
                pageResult.totalPages(),
                pageResult.hasNext()
        );
        
        return new GroupListServiceResponse(groups, pageInfo);
    }
    
    public record PageInfo(
            int pageNumber,
            int pageSize,
            long totalElements,
            int totalPages,
            boolean hasNext
    ) {}
}

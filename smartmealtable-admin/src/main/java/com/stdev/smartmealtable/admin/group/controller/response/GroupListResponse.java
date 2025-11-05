package com.stdev.smartmealtable.admin.group.controller.response;

import com.stdev.smartmealtable.admin.group.service.dto.GroupListServiceResponse;
import com.stdev.smartmealtable.admin.group.service.dto.GroupServiceResponse;

import java.util.List;

/**
 * 그룹 목록 Controller Response DTO
 */
public record GroupListResponse(
        List<GroupSummary> groups,
        PageInfo pageInfo
) {
    public static GroupListResponse from(GroupListServiceResponse serviceResponse) {
        List<GroupSummary> groups = serviceResponse.groups().stream()
                .map(GroupSummary::from)
                .toList();
        
        GroupListServiceResponse.PageInfo servicePage = serviceResponse.pageInfo();
        PageInfo pageInfo = new PageInfo(
                servicePage.pageNumber(),
                servicePage.pageSize(),
                servicePage.totalElements(),
                servicePage.totalPages(),
                servicePage.hasNext()
        );
        
        return new GroupListResponse(groups, pageInfo);
    }
    
    /**
     * 그룹 요약 정보
     */
    public record GroupSummary(
            Long groupId,
            String name,
            String type,
            String address
    ) {
        public static GroupSummary from(GroupServiceResponse serviceResponse) {
            return new GroupSummary(
                    serviceResponse.groupId(),
                    serviceResponse.name(),
                    serviceResponse.type().name(),
                    serviceResponse.address()
            );
        }
    }
    
    /**
     * 페이징 정보
     */
    public record PageInfo(
            int pageNumber,
            int pageSize,
            long totalElements,
            int totalPages,
            boolean hasNext
    ) {}
}

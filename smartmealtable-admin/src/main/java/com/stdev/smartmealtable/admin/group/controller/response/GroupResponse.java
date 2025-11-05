package com.stdev.smartmealtable.admin.group.controller.response;

import com.stdev.smartmealtable.admin.group.service.dto.GroupServiceResponse;
import com.stdev.smartmealtable.domain.member.entity.GroupType;

/**
 * 그룹 상세 Controller Response DTO
 */
public record GroupResponse(
        Long groupId,
        String name,
        GroupType type,
        String address
) {
    public static GroupResponse from(GroupServiceResponse serviceResponse) {
        return new GroupResponse(
                serviceResponse.groupId(),
                serviceResponse.name(),
                serviceResponse.type(),
                serviceResponse.address()
        );
    }
}

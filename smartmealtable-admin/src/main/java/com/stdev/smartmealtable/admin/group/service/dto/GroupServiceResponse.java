package com.stdev.smartmealtable.admin.group.service.dto;

import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;

/**
 * 그룹 상세 Service Response DTO
 */
public record GroupServiceResponse(
        Long groupId,
        String name,
        GroupType type,
        String address
) {
    public static GroupServiceResponse from(Group group) {
        return new GroupServiceResponse(
                group.getGroupId(),
                group.getName(),
                group.getType(),
                group.getAddress() != null ? group.getAddress().getFullAddress() : null
        );
    }
}

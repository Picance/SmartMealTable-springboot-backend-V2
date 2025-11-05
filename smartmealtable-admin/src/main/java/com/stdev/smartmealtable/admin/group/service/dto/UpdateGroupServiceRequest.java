package com.stdev.smartmealtable.admin.group.service.dto;

import com.stdev.smartmealtable.domain.member.entity.GroupType;

/**
 * 그룹 수정 Service Request DTO
 */
public record UpdateGroupServiceRequest(
        String name,
        GroupType type,
        String address
) {
    public static UpdateGroupServiceRequest of(String name, GroupType type, String address) {
        return new UpdateGroupServiceRequest(name, type, address);
    }
}

package com.stdev.smartmealtable.admin.group.service.dto;

import com.stdev.smartmealtable.domain.member.entity.GroupType;

/**
 * 그룹 생성 Service Request DTO
 */
public record CreateGroupServiceRequest(
        String name,
        GroupType type,
        String address
) {
    public static CreateGroupServiceRequest of(String name, GroupType type, String address) {
        return new CreateGroupServiceRequest(name, type, address);
    }
}

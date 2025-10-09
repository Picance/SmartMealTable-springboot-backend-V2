package com.stdev.smartmealtable.api.group.controller.dto;

import com.stdev.smartmealtable.domain.member.entity.GroupType;

/**
 * 그룹 조회 응답 DTO
 */
public record GroupResponse(
        Long groupId,
        String name,
        GroupType type,
        String address
) {
}

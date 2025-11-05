package com.stdev.smartmealtable.admin.group.service.dto;

import com.stdev.smartmealtable.domain.member.entity.GroupType;

/**
 * 그룹 목록 조회 Service Request DTO
 */
public record GroupListServiceRequest(
        GroupType type,
        String name,
        int page,
        int size
) {
    public static GroupListServiceRequest of(GroupType type, String name, Integer page, Integer size) {
        return new GroupListServiceRequest(
                type,
                name,
                page != null ? page : 0,
                size != null ? size : 20
        );
    }
}

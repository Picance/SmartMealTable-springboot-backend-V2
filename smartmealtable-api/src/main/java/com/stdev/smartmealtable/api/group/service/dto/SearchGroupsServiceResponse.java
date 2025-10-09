package com.stdev.smartmealtable.api.group.service.dto;

import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;

import java.util.List;

/**
 * 그룹 검색 서비스 응답 DTO
 */
public record SearchGroupsServiceResponse(
        List<GroupInfo> groups,
        int totalCount
) {
    public record GroupInfo(
            Long groupId,
            String name,
            GroupType type,
            String address
    ) {
        public static GroupInfo from(Group group) {
            return new GroupInfo(
                    group.getGroupId(),
                    group.getName(),
                    group.getType(),
                    group.getAddress()
            );
        }
    }
}

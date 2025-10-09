package com.stdev.smartmealtable.domain.member.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 그룹 도메인 엔티티
 * 사용자의 소속 집단 (대학교, 회사 등)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Group {

    private Long groupId;
    private String name;
    private GroupType type;
    private String address;

    public static Group create(String name, GroupType type, String address) {
        Group group = new Group();
        group.name = name;
        group.type = type;
        group.address = address;
        return group;
    }

    // 재구성 팩토리 메서드 (Storage에서 사용)
    public static Group reconstitute(Long groupId, String name, GroupType type, String address) {
        Group group = new Group();
        group.groupId = groupId;
        group.name = name;
        group.type = type;
        group.address = address;
        return group;
    }
}

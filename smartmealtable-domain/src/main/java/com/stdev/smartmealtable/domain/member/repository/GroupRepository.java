package com.stdev.smartmealtable.domain.member.repository;

import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;

import java.util.List;
import java.util.Optional;

/**
 * Group Repository 인터페이스
 * Storage 계층에서 구현
 */
public interface GroupRepository {

    /**
     * 그룹 저장
     */
    Group save(Group group);

    /**
     * 그룹 조회 by ID
     */
    Optional<Group> findById(Long groupId);

    /**
     * 그룹 목록 조회 by Type
     */
    List<Group> findByType(GroupType type);

    /**
     * 그룹 이름으로 검색
     */
    List<Group> findByNameContaining(String name);

    /**
     * 그룹 검색 (타입과 이름 조건으로 검색)
     * @param type 그룹 타입 (optional)
     * @param name 그룹 이름 검색어 (optional)
     * @return 검색된 그룹 목록
     */
    List<Group> searchGroups(GroupType type, String name);
}

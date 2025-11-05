package com.stdev.smartmealtable.domain.member.repository;

import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupPageResult;
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
    
    // ==================== ADMIN API 전용 메서드 ====================
    
    /**
     * 그룹 목록 조회 (페이징, 검색)
     * @param type 그룹 타입 필터 (optional)
     * @param name 그룹 이름 검색어 (optional)
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 페이징된 그룹 목록
     */
    GroupPageResult searchByTypeAndName(GroupType type, String name, int page, int size);
    
    /**
     * 그룹 이름 중복 체크 (생성 시)
     * @param name 체크할 그룹 이름
     * @return 중복 여부
     */
    boolean existsByName(String name);
    
    /**
     * 그룹 이름 중복 체크 (수정 시 - 자기 자신 제외)
     * @param name 체크할 그룹 이름
     * @param groupId 제외할 그룹 ID (자기 자신)
     * @return 중복 여부
     */
    boolean existsByNameAndIdNot(String name, Long groupId);
    
    /**
     * 그룹에 속한 회원이 있는지 확인
     * @param groupId 그룹 ID
     * @return 회원 존재 여부
     */
    boolean hasMembers(Long groupId);
    
    /**
     * 그룹 삭제 (물리적 삭제)
     * @param groupId 삭제할 그룹 ID
     */
    void deleteById(Long groupId);
}

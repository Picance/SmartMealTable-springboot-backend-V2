package com.stdev.smartmealtable.storage.db.member.repository;

import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupPageResult;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.storage.db.member.entity.GroupJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Group Repository 구현체
 */
@Repository
public class GroupRepositoryImpl implements GroupRepository {

    private final GroupJpaRepository jpaRepository;

    public GroupRepositoryImpl(GroupJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Group save(Group group) {
        GroupJpaEntity jpaEntity = GroupJpaEntity.from(group);
        GroupJpaEntity saved = jpaRepository.save(jpaEntity);
        return saved.toDomainWithDetails();
    }

    @Override
    public Optional<Group> findById(Long groupId) {
        return jpaRepository.findById(groupId)
                .map(GroupJpaEntity::toDomainWithDetails);
    }

    @Override
    public List<Group> findByType(GroupType type) {
        return jpaRepository.findByType(type).stream()
                .map(GroupJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> findByNameContaining(String name) {
        return jpaRepository.findByNameContaining(name).stream()
                .map(GroupJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> searchGroups(GroupType type, String name) {
        // 페이징 없이 모든 결과를 반환하도록 임시 구현
        // Application 레이어에서 페이징 처리
        Page<GroupJpaEntity> page = jpaRepository.searchGroups(
                type, 
                name, 
                Pageable.unpaged()
        );
        return page.getContent().stream()
                .map(GroupJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    // ==================== ADMIN API 전용 메서드 ====================
    
    @Override
    public GroupPageResult searchByTypeAndName(GroupType type, String name, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<GroupJpaEntity> jpaPage = jpaRepository.searchGroups(type, name, pageRequest);
        
        List<Group> groups = jpaPage.getContent().stream()
                .map(GroupJpaEntity::toDomain)
                .collect(Collectors.toList());
        
        return GroupPageResult.of(
                groups,
                jpaPage.getNumber(),
                jpaPage.getSize(),
                jpaPage.getTotalElements()
        );
    }
    
    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }
    
    @Override
    public boolean existsByNameAndIdNot(String name, Long groupId) {
        return jpaRepository.existsByNameAndGroupIdNot(name, groupId);
    }
    
    @Override
    public boolean hasMembers(Long groupId) {
        return jpaRepository.countMembersByGroupId(groupId) > 0;
    }
    
    @Override
    public void deleteById(Long groupId) {
        jpaRepository.deleteById(groupId);
    }
    
    // ==================== 검색 기능 강화 메서드 ====================
    
    @Override
    public List<Group> findByNameStartsWith(String prefix) {
        return jpaRepository.findByNameStartingWith(prefix).stream()
                .map(GroupJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Group> findAllByIdIn(List<Long> groupIds) {
        return jpaRepository.findByGroupIdIn(groupIds).stream()
                .map(GroupJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public long count() {
        return jpaRepository.count();
    }
    
    @Override
    public List<Group> findAll(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<GroupJpaEntity> jpaPage = jpaRepository.findAll(pageRequest);
        
        return jpaPage.getContent().stream()
                .map(GroupJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
}

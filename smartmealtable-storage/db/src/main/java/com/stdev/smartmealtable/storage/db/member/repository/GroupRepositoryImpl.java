package com.stdev.smartmealtable.storage.db.member.repository;

import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.storage.db.member.entity.GroupJpaEntity;
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
        return saved.toDomain();
    }

    @Override
    public Optional<Group> findById(Long groupId) {
        return jpaRepository.findById(groupId)
                .map(GroupJpaEntity::toDomain);
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
}

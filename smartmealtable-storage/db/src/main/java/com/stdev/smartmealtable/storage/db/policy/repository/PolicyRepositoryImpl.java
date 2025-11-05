package com.stdev.smartmealtable.storage.db.policy.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stdev.smartmealtable.domain.policy.entity.Policy;
import com.stdev.smartmealtable.domain.policy.entity.PolicyPageResult;
import com.stdev.smartmealtable.domain.policy.repository.PolicyRepository;
import com.stdev.smartmealtable.storage.db.policy.PolicyJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.stdev.smartmealtable.storage.db.policy.QPolicyJpaEntity.policyJpaEntity;
import static com.stdev.smartmealtable.storage.db.policy.QPolicyAgreementJpaEntity.policyAgreementJpaEntity;

/**
 * Policy Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class PolicyRepositoryImpl implements PolicyRepository {

    private final PolicyJpaRepository jpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Policy save(Policy policy) {
        PolicyJpaEntity jpaEntity = PolicyJpaEntity.from(policy);
        PolicyJpaEntity saved = jpaRepository.save(jpaEntity);
        return saved.toDomain();
    }

    @Override
    public Optional<Policy> findById(Long policyId) {
        return jpaRepository.findById(policyId)
                .map(PolicyJpaEntity::toDomain);
    }

    @Override
    public List<Policy> findAllActive() {
        return jpaRepository.findAllActive().stream()
                .map(PolicyJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Policy> findAll() {
        return jpaRepository.findAll().stream()
                .map(PolicyJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public PolicyPageResult searchByTitle(String title, Boolean isActive, int page, int size) {
        // Null-safe 조건 조합
        BooleanExpression titleCondition = titleContains(title);
        BooleanExpression activeCondition = isActiveEquals(isActive);
        
        BooleanExpression condition = titleCondition;
        if (activeCondition != null) {
            condition = (condition != null) ? condition.and(activeCondition) : activeCondition;
        }

        // 전체 개수 조회
        Long total = queryFactory
                .select(policyJpaEntity.count())
                .from(policyJpaEntity)
                .where(condition)
                .fetchOne();

        long totalElements = (total != null) ? total : 0;

        // 페이징 데이터 조회
        List<PolicyJpaEntity> entities = queryFactory
                .selectFrom(policyJpaEntity)
                .where(condition)
                .orderBy(policyJpaEntity.createdAt.desc())
                .offset((long) page * size)
                .limit(size)
                .fetch();

        List<Policy> content = entities.stream()
                .map(PolicyJpaEntity::toDomain)
                .collect(Collectors.toList());

        return PolicyPageResult.of(content, page, size, totalElements);
    }

    @Override
    public boolean existsByTitle(String title) {
        Integer count = queryFactory
                .selectOne()
                .from(policyJpaEntity)
                .where(policyJpaEntity.title.eq(title))
                .fetchFirst();
        return count != null;
    }

    @Override
    public boolean existsByTitleAndIdNot(String title, Long policyId) {
        Integer count = queryFactory
                .selectOne()
                .from(policyJpaEntity)
                .where(
                        policyJpaEntity.title.eq(title),
                        policyJpaEntity.policyId.ne(policyId)
                )
                .fetchFirst();
        return count != null;
    }

    @Override
    public void deleteById(Long policyId) {
        jpaRepository.deleteById(policyId);
    }

    @Override
    public boolean hasAgreements(Long policyId) {
        Integer count = queryFactory
                .selectOne()
                .from(policyAgreementJpaEntity)
                .where(policyAgreementJpaEntity.policyId.eq(policyId))
                .fetchFirst();
        return count != null;
    }

    /**
     * 제목 검색 조건
     */
    private BooleanExpression titleContains(String title) {
        return StringUtils.hasText(title) ?
                policyJpaEntity.title.containsIgnoreCase(title) : null;
    }

    /**
     * 활성 여부 조건
     */
    private BooleanExpression isActiveEquals(Boolean isActive) {
        return isActive != null ?
                policyJpaEntity.isActive.eq(isActive) : null;
    }
}

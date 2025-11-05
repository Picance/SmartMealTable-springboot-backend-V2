package com.stdev.smartmealtable.storage.db.statistics;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stdev.smartmealtable.domain.statistics.ExpenditureStatistics;
import com.stdev.smartmealtable.domain.statistics.StatisticsRepository;
import com.stdev.smartmealtable.domain.statistics.StoreStatistics;
import com.stdev.smartmealtable.domain.statistics.UserStatistics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.stdev.smartmealtable.storage.db.category.QCategoryJpaEntity.categoryJpaEntity;
import static com.stdev.smartmealtable.storage.db.expenditure.QExpenditureJpaEntity.expenditureJpaEntity;
import static com.stdev.smartmealtable.storage.db.food.QFoodJpaEntity.foodJpaEntity;
import static com.stdev.smartmealtable.storage.db.member.entity.QGroupJpaEntity.groupJpaEntity;
import static com.stdev.smartmealtable.storage.db.member.entity.QMemberAuthenticationJpaEntity.memberAuthenticationJpaEntity;
import static com.stdev.smartmealtable.storage.db.member.entity.QMemberJpaEntity.memberJpaEntity;
import static com.stdev.smartmealtable.storage.db.member.entity.QSocialAccountJpaEntity.socialAccountJpaEntity;
import static com.stdev.smartmealtable.storage.db.store.QStoreJpaEntity.storeJpaEntity;

/**
 * 통계 Repository 구현체
 * QueryDSL 기반 복잡한 집계 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class StatisticsRepositoryImpl implements StatisticsRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public UserStatistics getUserStatistics() {
        // 1. 전체 회원 수 (삭제되지 않은)
        Long totalMembers = queryFactory
                .select(memberAuthenticationJpaEntity.count())
                .from(memberAuthenticationJpaEntity)
                .where(memberAuthenticationJpaEntity.deletedAt.isNull())
                .fetchOne();

        // 2. 소셜 로그인 회원 수 (social_account 테이블에 존재)
        Long socialLoginMembers = queryFactory
                .select(memberAuthenticationJpaEntity.memberAuthenticationId.countDistinct())
                .from(memberAuthenticationJpaEntity)
                .innerJoin(socialAccountJpaEntity)
                .on(memberAuthenticationJpaEntity.memberAuthenticationId.eq(socialAccountJpaEntity.memberAuthenticationId))
                .where(memberAuthenticationJpaEntity.deletedAt.isNull())
                .fetchOne();

        // 3. 이메일 로그인 회원 수 (hashed_password가 있는)
        Long emailLoginMembers = queryFactory
                .select(memberAuthenticationJpaEntity.count())
                .from(memberAuthenticationJpaEntity)
                .where(
                        memberAuthenticationJpaEntity.deletedAt.isNull(),
                        memberAuthenticationJpaEntity.hashedPassword.isNotNull()
                )
                .fetchOne();

        // 4. 탈퇴 회원 수
        Long deletedMembers = queryFactory
                .select(memberAuthenticationJpaEntity.count())
                .from(memberAuthenticationJpaEntity)
                .where(memberAuthenticationJpaEntity.deletedAt.isNotNull())
                .fetchOne();

        // 5. 그룹별 회원 분포 (UNIVERSITY, COMPANY, OTHER)
        List<Tuple> groupDistribution = queryFactory
                .select(
                        groupJpaEntity.type,
                        memberJpaEntity.count()
                )
                .from(memberJpaEntity)
                .innerJoin(memberAuthenticationJpaEntity)
                .on(memberJpaEntity.memberId.eq(memberAuthenticationJpaEntity.memberId))
                .leftJoin(groupJpaEntity)
                .on(memberJpaEntity.groupId.eq(groupJpaEntity.groupId))
                .where(memberAuthenticationJpaEntity.deletedAt.isNull())
                .groupBy(groupJpaEntity.type)
                .fetch();

        Map<String, Long> membersByGroupType = groupDistribution.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(groupJpaEntity.type) != null ? tuple.get(groupJpaEntity.type).name() : "NONE",
                        tuple -> tuple.get(memberJpaEntity.count())
                ));

        return new UserStatistics(
                totalMembers != null ? totalMembers : 0L,
                socialLoginMembers != null ? socialLoginMembers : 0L,
                emailLoginMembers != null ? emailLoginMembers : 0L,
                deletedMembers != null ? deletedMembers : 0L,
                membersByGroupType
        );
    }

    @Override
    public ExpenditureStatistics getExpenditureStatistics() {
        // 1. 총 지출액 및 평균 지출액 (삭제되지 않은 지출만)
        Tuple totals = queryFactory
                .select(
                        expenditureJpaEntity.amount.sum().longValue(),
                        expenditureJpaEntity.amount.avg(),
                        expenditureJpaEntity.count()
                )
                .from(expenditureJpaEntity)
                .where(expenditureJpaEntity.deleted.isFalse())
                .fetchOne();

        Long totalAmountLong = totals != null && totals.get(expenditureJpaEntity.amount.sum().longValue()) != null
                ? totals.get(expenditureJpaEntity.amount.sum().longValue())
                : 0L;
        BigDecimal totalAmount = BigDecimal.valueOf(totalAmountLong);

        Double avgAmountDouble = totals != null ? totals.get(expenditureJpaEntity.amount.avg()) : null;
        BigDecimal averageAmount = avgAmountDouble != null
                ? BigDecimal.valueOf(avgAmountDouble).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Long totalCount = totals != null && totals.get(expenditureJpaEntity.count()) != null
                ? totals.get(expenditureJpaEntity.count())
                : 0L;

        // 2. 카테고리별 지출 TOP 5
        List<Tuple> topCategories = queryFactory
                .select(
                        categoryJpaEntity.name,
                        expenditureJpaEntity.amount.sum().longValue()
                )
                .from(expenditureJpaEntity)
                .innerJoin(categoryJpaEntity)
                .on(expenditureJpaEntity.categoryId.eq(categoryJpaEntity.categoryId))
                .where(expenditureJpaEntity.deleted.isFalse())
                .groupBy(categoryJpaEntity.categoryId, categoryJpaEntity.name)
                .orderBy(expenditureJpaEntity.amount.sum().desc())
                .limit(5)
                .fetch();

        Map<String, BigDecimal> topCategoriesByAmount = topCategories.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(categoryJpaEntity.name),
                        tuple -> BigDecimal.valueOf(tuple.get(expenditureJpaEntity.amount.sum().longValue()))
                ));

        // 3. 식사 시간대별 지출 분포
        List<Tuple> mealTypeDistribution = queryFactory
                .select(
                        expenditureJpaEntity.mealType,
                        expenditureJpaEntity.count()
                )
                .from(expenditureJpaEntity)
                .where(expenditureJpaEntity.deleted.isFalse())
                .groupBy(expenditureJpaEntity.mealType)
                .fetch();

        Map<String, Long> expendituresByMealType = mealTypeDistribution.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(expenditureJpaEntity.mealType) != null ? tuple.get(expenditureJpaEntity.mealType).name() : "UNKNOWN",
                        tuple -> tuple.get(expenditureJpaEntity.count())
                ));

        // 4. 1인당 평균 지출액 (전체 지출액 / 활성 회원 수)
        Long activeMemberCount = queryFactory
                .select(memberAuthenticationJpaEntity.count())
                .from(memberAuthenticationJpaEntity)
                .where(memberAuthenticationJpaEntity.deletedAt.isNull())
                .fetchOne();

        BigDecimal averageAmountPerMember = activeMemberCount != null && activeMemberCount > 0
                ? totalAmount.divide(BigDecimal.valueOf(activeMemberCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new ExpenditureStatistics(
                totalAmount,
                averageAmount,
                totalCount,
                topCategoriesByAmount,
                expendituresByMealType,
                averageAmountPerMember
        );
    }

    @Override
    public StoreStatistics getStoreStatistics() {
        // 1. 총 음식점 수 (삭제되지 않은)
        Long totalStores = queryFactory
                .select(storeJpaEntity.count())
                .from(storeJpaEntity)
                .where(storeJpaEntity.deletedAt.isNull())
                .fetchOne();

        // 2. 카테고리별 음식점 수
        List<Tuple> storesByCategory = queryFactory
                .select(
                        categoryJpaEntity.name,
                        storeJpaEntity.count()
                )
                .from(storeJpaEntity)
                .innerJoin(categoryJpaEntity)
                .on(storeJpaEntity.categoryId.eq(categoryJpaEntity.categoryId))
                .where(storeJpaEntity.deletedAt.isNull())
                .groupBy(categoryJpaEntity.categoryId, categoryJpaEntity.name)
                .fetch();

        Map<String, Long> storesByCategoryMap = storesByCategory.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(categoryJpaEntity.name),
                        tuple -> tuple.get(storeJpaEntity.count())
                ));

        // 3. 음식점 타입별 분포
        List<Tuple> storesByType = queryFactory
                .select(
                        storeJpaEntity.storeType,
                        storeJpaEntity.count()
                )
                .from(storeJpaEntity)
                .where(storeJpaEntity.deletedAt.isNull())
                .groupBy(storeJpaEntity.storeType)
                .fetch();

        Map<String, Long> storesByTypeMap = storesByType.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(storeJpaEntity.storeType) != null ? tuple.get(storeJpaEntity.storeType).name() : "UNKNOWN",
                        tuple -> tuple.get(storeJpaEntity.count())
                ));

        // 4. 총 메뉴 수
        Long totalFoods = queryFactory
                .select(foodJpaEntity.count())
                .from(foodJpaEntity)
                .fetchOne();

        // 5. 음식점당 평균 메뉴 수
        double averageFoodsPerStore = totalStores != null && totalStores > 0
                ? (totalFoods != null ? totalFoods : 0L) / (double) totalStores
                : 0.0;

        // 6. 조회수 상위 TOP 10
        List<Tuple> topByViews = queryFactory
                .select(
                        storeJpaEntity.storeId,
                        storeJpaEntity.name,
                        storeJpaEntity.viewCount
                )
                .from(storeJpaEntity)
                .where(storeJpaEntity.deletedAt.isNull())
                .orderBy(storeJpaEntity.viewCount.desc())
                .limit(10)
                .fetch();

        List<StoreStatistics.TopStore> topStoresByViews = topByViews.stream()
                .map(tuple -> new StoreStatistics.TopStore(
                        tuple.get(storeJpaEntity.storeId),
                        tuple.get(storeJpaEntity.name),
                        tuple.get(storeJpaEntity.viewCount) != null ? tuple.get(storeJpaEntity.viewCount) : 0L
                ))
                .collect(Collectors.toList());

        // 7. 리뷰 수 상위 TOP 10
        List<Tuple> topByReviews = queryFactory
                .select(
                        storeJpaEntity.storeId,
                        storeJpaEntity.name,
                        storeJpaEntity.reviewCount
                )
                .from(storeJpaEntity)
                .where(storeJpaEntity.deletedAt.isNull())
                .orderBy(storeJpaEntity.reviewCount.desc())
                .limit(10)
                .fetch();

        List<StoreStatistics.TopStore> topStoresByReviews = topByReviews.stream()
                .map(tuple -> new StoreStatistics.TopStore(
                        tuple.get(storeJpaEntity.storeId),
                        tuple.get(storeJpaEntity.name),
                        tuple.get(storeJpaEntity.reviewCount) != null ? tuple.get(storeJpaEntity.reviewCount) : 0L
                ))
                .collect(Collectors.toList());

        // 8. 즐겨찾기 수 상위 TOP 10
        List<Tuple> topByFavorites = queryFactory
                .select(
                        storeJpaEntity.storeId,
                        storeJpaEntity.name,
                        storeJpaEntity.favoriteCount
                )
                .from(storeJpaEntity)
                .where(storeJpaEntity.deletedAt.isNull())
                .orderBy(storeJpaEntity.favoriteCount.desc())
                .limit(10)
                .fetch();

        List<StoreStatistics.TopStore> topStoresByFavorites = topByFavorites.stream()
                .map(tuple -> new StoreStatistics.TopStore(
                        tuple.get(storeJpaEntity.storeId),
                        tuple.get(storeJpaEntity.name),
                        tuple.get(storeJpaEntity.favoriteCount) != null ? tuple.get(storeJpaEntity.favoriteCount) : 0L
                ))
                .collect(Collectors.toList());

        return new StoreStatistics(
                totalStores != null ? totalStores : 0L,
                storesByCategoryMap,
                storesByTypeMap,
                totalFoods != null ? totalFoods : 0L,
                Math.round(averageFoodsPerStore * 100.0) / 100.0,
                topStoresByViews,
                topStoresByReviews,
                topStoresByFavorites
        );
    }
}

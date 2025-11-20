package com.stdev.smartmealtable.storage.db.favorite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 즐겨찾기 JPA Repository
 */
public interface FavoriteJpaRepository extends JpaRepository<FavoriteEntity, Long> {
    
    /**
     * 회원의 모든 즐겨찾기 조회 (우선순위 오름차순 정렬)
     * 
     * @param memberId 회원 ID
     * @return 즐겨찾기 엔티티 목록
     */
    @Query("SELECT f FROM FavoriteEntity f WHERE f.memberId = :memberId ORDER BY f.priority ASC")
    List<FavoriteEntity> findByMemberIdOrderByPriorityAsc(@Param("memberId") Long memberId);
    
    /**
     * 회원과 가게로 즐겨찾기 조회
     * 
     * @param memberId 회원 ID
     * @param storeId 가게 ID
     * @return Optional FavoriteEntity
     */
    @Query("SELECT f FROM FavoriteEntity f WHERE f.memberId = :memberId AND f.storeId = :storeId")
    Optional<FavoriteEntity> findByMemberIdAndStoreId(@Param("memberId") Long memberId, @Param("storeId") Long storeId);
    
    /**
     * 회원의 즐겨찾기 개수 조회
     * 
     * @param memberId 회원 ID
     * @return 즐겨찾기 개수
     */
    @Query("SELECT COUNT(f) FROM FavoriteEntity f WHERE f.memberId = :memberId")
    long countByMemberId(@Param("memberId") Long memberId);
    
    /**
     * 회원의 최대 우선순위 값 조회
     * 
     * @param memberId 회원 ID
     * @return 최대 우선순위 값 (즐겨찾기가 없으면 null)
     */
    @Query("SELECT MAX(f.priority) FROM FavoriteEntity f WHERE f.memberId = :memberId")
    Long findMaxPriorityByMemberId(@Param("memberId") Long memberId);
    
    /**
     * 즐겨찾기 존재 여부 확인
     * 
     * @param memberId 회원 ID
     * @param storeId 가게 ID
     * @return 존재 여부
     */
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN TRUE ELSE FALSE END FROM FavoriteEntity f WHERE f.memberId = :memberId AND f.storeId = :storeId")
    boolean existsByMemberIdAndStoreId(@Param("memberId") Long memberId, @Param("storeId") Long storeId);

    /**
     * 회원이 즐겨찾기에 추가한 가게 ID 목록 조회 (필터링)
     *
     * @param memberId 회원 ID
     * @param storeIds 확인할 가게 ID 목록
     * @return 즐겨찾기된 가게 ID 목록
     */
    @Query("SELECT f.storeId FROM FavoriteEntity f WHERE f.memberId = :memberId AND f.storeId IN :storeIds")
    List<Long> findStoreIdsByMemberIdAndStoreIdIn(@Param("memberId") Long memberId, @Param("storeIds") List<Long> storeIds);
}

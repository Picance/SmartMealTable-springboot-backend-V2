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
    List<FavoriteEntity> findByMemberIdOrderByPriorityAsc(Long memberId);
    
    /**
     * 회원과 가게로 즐겨찾기 조회
     * 
     * @param memberId 회원 ID
     * @param storeId 가게 ID
     * @return Optional FavoriteEntity
     */
    Optional<FavoriteEntity> findByMemberIdAndStoreId(Long memberId, Long storeId);
    
    /**
     * 회원의 즐겨찾기 개수 조회
     * 
     * @param memberId 회원 ID
     * @return 즐겨찾기 개수
     */
    long countByMemberId(Long memberId);
    
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
    boolean existsByMemberIdAndStoreId(Long memberId, Long storeId);
}

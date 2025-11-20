package com.stdev.smartmealtable.domain.favorite;

import java.util.List;
import java.util.Optional;

/**
 * 즐겨찾기 Repository 인터페이스
 */
public interface FavoriteRepository {
    
    /**
     * 즐겨찾기 저장
     * 
     * @param favorite 저장할 즐겨찾기 엔티티
     * @return 저장된 즐겨찾기 엔티티
     */
    Favorite save(Favorite favorite);
    
    /**
     * 즐겨찾기 ID로 조회
     * 
     * @param favoriteId 즐겨찾기 ID
     * @return Optional Favorite
     */
    Optional<Favorite> findById(Long favoriteId);
    
    /**
     * 회원의 모든 즐겨찾기 조회 (우선순위 순으로 정렬)
     * 
     * @param memberId 회원 ID
     * @return 즐겨찾기 목록 (priority 오름차순)
     */
    List<Favorite> findByMemberIdOrderByPriorityAsc(Long memberId);
    
    /**
     * 회원과 가게로 즐겨찾기 조회 (중복 체크용)
     * 
     * @param memberId 회원 ID
     * @param storeId 가게 ID
     * @return Optional Favorite
     */
    Optional<Favorite> findByMemberIdAndStoreId(Long memberId, Long storeId);
    
    /**
     * 회원의 즐겨찾기 개수 조회
     * 
     * @param memberId 회원 ID
     * @return 즐겨찾기 개수
     */
    long countByMemberId(Long memberId);
    
    /**
     * 회원의 최대 우선순위 값 조회
     * 즐겨찾기가 없으면 0 반환
     * 
     * @param memberId 회원 ID
     * @return 최대 우선순위 값
     */
    Long findMaxPriorityByMemberId(Long memberId);
    
    /**
     * 즐겨찾기 삭제
     * 
     * @param favorite 삭제할 즐겨찾기 엔티티
     */
    void delete(Favorite favorite);
    
    /**
     * 여러 즐겨찾기 일괄 저장
     * 순서 변경 시 사용
     * 
     * @param favorites 저장할 즐겨찾기 목록
     * @return 저장된 즐겨찾기 목록
     */
    List<Favorite> saveAll(List<Favorite> favorites);
    
    /**
     * 회원의 즐겨찾기 여부 확인
     * 
     * @param memberId 회원 ID
     * @param storeId 가게 ID
     * @return 즐겨찾기 여부
     */
    boolean existsByMemberIdAndStoreId(Long memberId, Long storeId);

    /**
     * 회원이 즐겨찾기에 추가한 가게 ID 목록 조회
     *
     * @param memberId 회원 ID
     * @param storeIds 확인할 가게 ID 목록
     * @return 즐겨찾기에 포함된 가게 ID 목록
     */
    List<Long> findStoreIdsByMemberIdAndStoreIdIn(Long memberId, List<Long> storeIds);
}

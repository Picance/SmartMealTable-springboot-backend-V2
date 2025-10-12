package com.stdev.smartmealtable.domain.favorite;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 즐겨찾기 도메인 엔티티
 * 사용자가 선호하는 음식점을 저장 및 관리합니다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Favorite {
    
    /**
     * 즐겨찾기 고유 식별자
     */
    private Long favoriteId;
    
    /**
     * 회원 ID (외부 참조)
     */
    private Long memberId;
    
    /**
     * 가게 ID (외부 참조)
     */
    private Long storeId;
    
    /**
     * 즐겨찾기 순서 (표시 순서)
     * 사용자가 드래그 앤 드롭으로 순서를 변경할 수 있음
     */
    private Long priority;
    
    /**
     * 즐겨찾기 등록 시각 (비즈니스 필드)
     * 시스템이 자동으로 기록
     */
    private LocalDateTime favoritedAt;
    
    /**
     * 정적 팩토리 메서드 - 새 즐겨찾기 생성
     * 
     * @param memberId 회원 ID
     * @param storeId 가게 ID
     * @param priority 즐겨찾기 순서
     * @return 생성된 Favorite 엔티티
     */
    public static Favorite create(Long memberId, Long storeId, Long priority) {
        return Favorite.builder()
                .memberId(memberId)
                .storeId(storeId)
                .priority(priority)
                .favoritedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 즐겨찾기 순서 변경
     * 
     * @param newPriority 새로운 우선순위 값
     */
    public void changePriority(Long newPriority) {
        this.priority = newPriority;
    }
}

package com.stdev.smartmealtable.domain.member.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 도메인 엔티티 (순수 Java 객체)
 * JPA 연관관계 매핑 없이 논리적 FK만 사용
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    private Long memberId;
    private Long groupId;  // 논리 FK
    private String nickname;
    private String profileImageUrl;
    private RecommendationType recommendationType;

    // 생성 팩토리 메서드
    public static Member create(Long groupId, String nickname, String profileImageUrl, RecommendationType recommendationType) {
        Member member = new Member();
        member.groupId = groupId;
        member.nickname = nickname;
        member.profileImageUrl = profileImageUrl;
        member.recommendationType = recommendationType;
        return member;
    }

    // 재구성 팩토리 메서드 (Storage에서 사용)
    public static Member reconstitute(
            Long memberId, 
            Long groupId, 
            String nickname,
            String profileImageUrl,
            RecommendationType recommendationType
    ) {
        Member member = new Member();
        member.memberId = memberId;
        member.groupId = groupId;
        member.nickname = nickname;
        member.profileImageUrl = profileImageUrl;
        member.recommendationType = recommendationType;
        return member;
    }

    // 도메인 로직: 닉네임 변경
    public void changeNickname(String newNickname) {
        validateNickname(newNickname);
        this.nickname = newNickname;
    }

    // 도메인 로직: 프로필 이미지 변경
    public void changeProfileImage(String newProfileImageUrl) {
        this.profileImageUrl = newProfileImageUrl;
    }

    // 도메인 로직: 추천 유형 변경
    public void changeRecommendationType(RecommendationType type) {
        if (type == null) {
            throw new IllegalArgumentException("추천 유형은 필수입니다.");
        }
        this.recommendationType = type;
    }
    
    // 도메인 로직: 그룹 변경
    public void changeGroup(Long newGroupId) {
        if (newGroupId == null) {
            throw new IllegalArgumentException("그룹 ID는 필수입니다.");
        }
        this.groupId = newGroupId;
    }
    
    // 도메인 로직: 온보딩 완료 여부 확인
    public boolean isOnboardingComplete() {
        // 그룹이 설정되어 있으면 온보딩 완료로 간주
        // 추후 온보딩 단계가 늘어나면 더 복잡한 조건으로 변경 가능
        return this.groupId != null;
    }

    // 비즈니스 규칙 검증
    private void validateNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }
        if (nickname.length() < 2 || nickname.length() > 50) {
            throw new IllegalArgumentException("닉네임은 2-50자 사이여야 합니다.");
        }
    }
}

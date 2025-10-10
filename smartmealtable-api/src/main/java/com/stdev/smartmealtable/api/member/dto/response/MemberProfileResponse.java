package com.stdev.smartmealtable.api.member.dto.response;

import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 내 프로필 조회 응답 DTO
 */
@Getter
@Builder
public class MemberProfileResponse {

    private Long memberId;
    private String nickname;
    private String email;
    private String name;
    private RecommendationType recommendationType;
    private GroupInfo group;
    private List<SocialAccountInfo> socialAccounts;
    private LocalDateTime passwordExpiresAt;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    public static class GroupInfo {
        private Long groupId;
        private String name;
        private GroupType type;
    }

    @Getter
    @Builder
    public static class SocialAccountInfo {
        private String provider;
        private LocalDateTime connectedAt;
    }
}

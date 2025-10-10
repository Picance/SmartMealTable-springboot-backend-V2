package com.stdev.smartmealtable.api.member.dto.response;

import com.stdev.smartmealtable.domain.member.entity.GroupType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 프로필 수정 응답 DTO
 */
@Getter
@Builder
public class UpdateProfileResponse {

    private Long memberId;
    private String nickname;
    private GroupInfo group;
    private LocalDateTime updatedAt;

    @Getter
    @Builder
    public static class GroupInfo {
        private Long groupId;
        private String name;
        private GroupType type;
    }
}

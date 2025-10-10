package com.stdev.smartmealtable.api.member.service.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 프로필 수정 Service 요청 DTO
 */
@Getter
@Builder
public class UpdateProfileServiceRequest {

    private Long memberId;
    private String nickname;
    private Long groupId;

    public static UpdateProfileServiceRequest of(Long memberId, String nickname, Long groupId) {
        return UpdateProfileServiceRequest.builder()
                .memberId(memberId)
                .nickname(nickname)
                .groupId(groupId)
                .build();
    }
}

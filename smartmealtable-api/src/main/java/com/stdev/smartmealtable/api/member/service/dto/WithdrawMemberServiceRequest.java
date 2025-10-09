package com.stdev.smartmealtable.api.member.service.dto;

/**
 * 회원 탈퇴 서비스 요청 DTO
 */
public record WithdrawMemberServiceRequest(
        Long memberId,
        String password,
        String reason
) {
}

package com.stdev.smartmealtable.api.member.dto.request;

import com.stdev.smartmealtable.api.member.service.dto.WithdrawMemberServiceRequest;
import jakarta.validation.constraints.NotBlank;

/**
 * 회원 탈퇴 요청 DTO
 */
public record WithdrawMemberRequest(
        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password,
        
        String reason
) {
    public WithdrawMemberServiceRequest toServiceRequest(Long memberId) {
        return new WithdrawMemberServiceRequest(memberId, password, reason);
    }
}

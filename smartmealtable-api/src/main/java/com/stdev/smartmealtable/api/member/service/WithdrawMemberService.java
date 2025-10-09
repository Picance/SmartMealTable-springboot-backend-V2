package com.stdev.smartmealtable.api.member.service;

import com.stdev.smartmealtable.api.member.service.dto.WithdrawMemberServiceRequest;
import com.stdev.smartmealtable.domain.member.service.MemberDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 탈퇴 Application Service
 * 유즈케이스 orchestration에 집중
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WithdrawMemberService {

    private final MemberDomainService memberDomainService;

    /**
     * 회원 탈퇴 (Soft Delete)
     */
    @Transactional
    public void withdrawMember(WithdrawMemberServiceRequest request) {
        // Domain Service를 통한 회원 탈퇴
        memberDomainService.withdrawMember(
                request.memberId(),
                request.password(),
                request.reason()
        );
    }
}

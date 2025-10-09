package com.stdev.smartmealtable.api.member.service;

import com.stdev.smartmealtable.api.member.service.dto.WithdrawMemberServiceRequest;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.AuthenticationException;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 탈퇴 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WithdrawMemberService {

    private final MemberAuthenticationRepository memberAuthenticationRepository;

    /**
     * 회원 탈퇴 (Soft Delete)
     */
    @Transactional
    public void withdrawMember(WithdrawMemberServiceRequest request) {
        // 1. 회원 인증 정보 조회
        MemberAuthentication authentication = memberAuthenticationRepository.findByMemberId(request.memberId())
                .orElseThrow(() -> new BusinessException(ErrorType.MEMBER_NOT_FOUND));

        // 2. 비밀번호 검증
        if (!authentication.verifyPassword(request.password())) {
            throw new AuthenticationException(ErrorType.INVALID_CURRENT_PASSWORD);
        }

        // 3. 회원 탈퇴 (Soft Delete)
        authentication.withdraw();
        
        // 4. 탈퇴 사유 로깅 (선택사항)
        if (request.reason() != null && !request.reason().isBlank()) {
            log.info("Member withdrawal - ID: {}, Reason: {}", request.memberId(), request.reason());
        }
    }
}

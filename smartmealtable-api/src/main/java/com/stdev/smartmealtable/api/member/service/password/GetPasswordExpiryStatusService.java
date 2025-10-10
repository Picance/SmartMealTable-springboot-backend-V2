package com.stdev.smartmealtable.api.member.service.password;

import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 비밀번호 만료 상태 조회 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPasswordExpiryStatusService {

    private final MemberAuthenticationRepository memberAuthenticationRepository;

    public PasswordExpiryStatusResponse getPasswordExpiryStatus(Long memberId) {
        MemberAuthentication memberAuth = memberAuthenticationRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorType.MEMBER_NOT_FOUND));

        return PasswordExpiryStatusResponse.of(
                memberAuth.getPasswordChangedAt(),
                memberAuth.getPasswordExpiresAt()
        );
    }
}

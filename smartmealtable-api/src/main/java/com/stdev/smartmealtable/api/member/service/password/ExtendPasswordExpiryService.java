package com.stdev.smartmealtable.api.member.service.password;

import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 비밀번호 만료일 연장 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ExtendPasswordExpiryService {

    private final MemberAuthenticationRepository memberAuthenticationRepository;

    public ExtendPasswordExpiryResponse extendPasswordExpiry(Long memberId) {
        MemberAuthentication memberAuth = memberAuthenticationRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorType.MEMBER_NOT_FOUND));

        // Domain 로직 호출
        memberAuth.extendPasswordExpiry();

        // 저장
        memberAuthenticationRepository.save(memberAuth);

        return ExtendPasswordExpiryResponse.of(memberAuth.getPasswordExpiresAt());
    }
}

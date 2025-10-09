package com.stdev.smartmealtable.api.member.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.stdev.smartmealtable.api.member.service.dto.ChangePasswordServiceRequest;
import com.stdev.smartmealtable.api.member.service.dto.ChangePasswordServiceResponse;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.AuthenticationException;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 비밀번호 변경 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChangePasswordService {

    private final MemberAuthenticationRepository memberAuthenticationRepository;

    /**
     * 비밀번호 변경
     */
    @Transactional
    public ChangePasswordServiceResponse changePassword(ChangePasswordServiceRequest request) {
        // 1. 회원 인증 정보 조회
        MemberAuthentication authentication = memberAuthenticationRepository.findByMemberId(request.memberId())
                .orElseThrow(() -> new BusinessException(ErrorType.MEMBER_NOT_FOUND));

        // 2. 비밀번호 형식 검증
        validatePasswordFormat(request.newPassword());

        // 3. 현재 비밀번호 검증
        if (!authentication.verifyPassword(request.currentPassword())) {
            throw new AuthenticationException(ErrorType.INVALID_CURRENT_PASSWORD);
        }

        // 4. 새 비밀번호 암호화
        String hashedPassword = BCrypt.withDefaults().hashToString(12, request.newPassword().toCharArray());

        // 5. 비밀번호 변경
        authentication.changePassword(hashedPassword);

        return ChangePasswordServiceResponse.success();
    }

    private void validatePasswordFormat(String password) {
        String passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$";
        if (!password.matches(passwordRegex)) {
            throw new BusinessException(ErrorType.INVALID_PASSWORD_FORMAT);
        }
    }
}

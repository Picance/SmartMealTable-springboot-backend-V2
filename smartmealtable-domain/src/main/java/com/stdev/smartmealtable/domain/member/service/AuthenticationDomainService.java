package com.stdev.smartmealtable.domain.member.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.AuthenticationException;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 인증 도메인 서비스
 * 인증, 비밀번호 검증 등 인증 관련 핵심 비즈니스 로직을 담당
 */
@Service
@RequiredArgsConstructor
public class AuthenticationDomainService {

    private final MemberAuthenticationRepository memberAuthenticationRepository;

    /**
     * 이메일과 비밀번호로 인증 검증
     * 
     * @param email 이메일
     * @param password 비밀번호 (평문)
     * @return 인증된 MemberAuthentication
     * @throws AuthenticationException 인증 실패 시
     */
    public MemberAuthentication authenticate(String email, String password) {
        // 1. 이메일로 MemberAuthentication 조회
        MemberAuthentication authentication = memberAuthenticationRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException(ErrorType.INVALID_CREDENTIALS));

        // 2. 비밀번호 검증 (도메인 엔티티의 메서드 활용)
        if (!authentication.verifyPassword(password)) {
            throw new AuthenticationException(ErrorType.INVALID_CREDENTIALS);
        }

        return authentication;
    }

    /**
     * 회원 ID로 인증 정보 조회
     * 
     * @param memberId 회원 ID
     * @return MemberAuthentication 엔티티
     * @throws AuthenticationException 인증 정보가 존재하지 않을 경우
     */
    public MemberAuthentication getAuthenticationByMemberId(Long memberId) {
        return memberAuthenticationRepository.findByMemberId(memberId)
                .orElseThrow(() -> new AuthenticationException(ErrorType.MEMBER_NOT_FOUND));
    }

    /**
     * 비밀번호 형식 검증
     * 
     * @param password 검증할 비밀번호
     * @return 유효성 여부
     */
    public boolean isValidPasswordFormat(String password) {
        String passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$";
        return password.matches(passwordRegex);
    }

    /**
     * 비밀번호 변경
     * 
     * @param memberId 회원 ID
     * @param currentPassword 현재 비밀번호
     * @param newPassword 새 비밀번호
     * @throws AuthenticationException 현재 비밀번호 불일치 시
     * @throws BusinessException 새 비밀번호 형식 오류 시
     */
    public void changePassword(Long memberId, String currentPassword, String newPassword) {
        // 1. 회원 인증 정보 조회
        MemberAuthentication authentication = getAuthenticationByMemberId(memberId);

        // 2. 비밀번호 형식 검증
        if (!isValidPasswordFormat(newPassword)) {
            throw new BusinessException(ErrorType.INVALID_PASSWORD_FORMAT);
        }

        // 3. 현재 비밀번호 검증
        if (!authentication.verifyPassword(currentPassword)) {
            throw new AuthenticationException(ErrorType.INVALID_CURRENT_PASSWORD);
        }

        // 4. 새 비밀번호 암호화
        String hashedPassword = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());

        // 5. 비밀번호 변경
        authentication.changePassword(hashedPassword);
    }
}

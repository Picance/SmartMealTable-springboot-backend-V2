package com.stdev.smartmealtable.domain.member.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 회원 인증 정보 도메인 엔티티
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberAuthentication {

    private Long memberAuthenticationId;
    private Long memberId;  // 논리 FK
    private String email;
    private String hashedPassword;
    private Integer failureCount;
    private LocalDateTime passwordChangedAt;  // 비즈니스 필드
    private LocalDateTime passwordExpiresAt;  // 비즈니스 필드
    private String name;
    private LocalDateTime deletedAt;  // 비즈니스 필드
    private LocalDateTime registeredAt;  // 가입 일시 (비즈니스 필드)

    // 이메일 회원가입용 팩토리 메서드
    public static MemberAuthentication createEmailAuth(
            Long memberId,
            String email,
            String hashedPassword,
            String name
    ) {
        MemberAuthentication auth = new MemberAuthentication();
        auth.memberId = memberId;
        auth.email = email;
        auth.hashedPassword = hashedPassword;
        auth.name = name;
        auth.failureCount = 0;
        auth.passwordChangedAt = LocalDateTime.now();
        auth.passwordExpiresAt = LocalDateTime.now().plusDays(90);
        auth.registeredAt = LocalDateTime.now();
        return auth;
    }

    // 소셜 로그인용 팩토리 메서드 (비밀번호 없음)
    public static MemberAuthentication createSocialAuth(Long memberId, String email, String name) {
        MemberAuthentication auth = new MemberAuthentication();
        auth.memberId = memberId;
        auth.email = email;
        auth.name = name;
        auth.failureCount = 0;
        auth.registeredAt = LocalDateTime.now();
        return auth;
    }

    // 재구성 팩토리 메서드 (Storage에서 사용)
    public static MemberAuthentication reconstitute(
            Long memberAuthenticationId,
            Long memberId,
            String email,
            String hashedPassword,
            Integer failureCount,
            LocalDateTime passwordChangedAt,
            LocalDateTime passwordExpiresAt,
            String name,
            LocalDateTime deletedAt,
            LocalDateTime registeredAt
    ) {
        MemberAuthentication auth = new MemberAuthentication();
        auth.memberAuthenticationId = memberAuthenticationId;
        auth.memberId = memberId;
        auth.email = email;
        auth.hashedPassword = hashedPassword;
        auth.failureCount = failureCount;
        auth.passwordChangedAt = passwordChangedAt;
        auth.passwordExpiresAt = passwordExpiresAt;
        auth.name = name;
        auth.deletedAt = deletedAt;
        auth.registeredAt = registeredAt;
        return auth;
    }

    // 도메인 로직: 비밀번호 변경
    public void changePassword(String newHashedPassword) {
        this.hashedPassword = newHashedPassword;
        this.passwordChangedAt = LocalDateTime.now();
        this.passwordExpiresAt = LocalDateTime.now().plusDays(90);
        this.failureCount = 0;  // 비밀번호 변경 시 실패 횟수 초기화
    }

    // 도메인 로직: 비밀번호 만료일 연장
    public void extendPasswordExpiry() {
        if (this.passwordExpiresAt == null) {
            throw new IllegalStateException("소셜 로그인 계정은 비밀번호 만료일이 없습니다.");
        }
        this.passwordExpiresAt = this.passwordExpiresAt.plusDays(90);
    }

    // 도메인 로직: 로그인 실패 처리
    public void incrementFailureCount() {
        this.failureCount++;
    }

    // 도메인 로직: 로그인 성공 처리
    public void resetFailureCount() {
        this.failureCount = 0;
    }

    // 도메인 로직: 계정 잠금 여부
    public boolean isLocked() {
        return this.failureCount >= 5;
    }

    // 도메인 로직: 비밀번호 만료 여부
    public boolean isPasswordExpired() {
        if (passwordExpiresAt == null) {
            return false;  // 소셜 로그인 계정
        }
        return LocalDateTime.now().isAfter(passwordExpiresAt);
    }

    // 도메인 로직: 비밀번호 검증
    public boolean verifyPassword(String rawPassword) {
        if (this.hashedPassword == null) {
            return false;  // 소셜 로그인 계정
        }
        return at.favre.lib.crypto.bcrypt.BCrypt.verifyer()
                .verify(rawPassword.toCharArray(), this.hashedPassword)
                .verified;
    }

    // 도메인 로직: 비밀번호 만료 임박 여부 (7일 전)
    public boolean isPasswordExpiringSoon() {
        if (passwordExpiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(passwordExpiresAt.minusDays(7));
    }

    // 도메인 로직: 회원 탈퇴 (Soft Delete)
    public void withdraw() {
        this.deletedAt = LocalDateTime.now();
    }

    // 도메인 로직: 탈퇴 여부
    public boolean isWithdrawn() {
        return this.deletedAt != null;
    }

    // 도메인 로직: 비밀번호 존재 여부
    public boolean hasPassword() {
        return this.hashedPassword != null;
    }
    
    // 비밀번호 getter (인증 시에만 사용)
    public String getPassword() {
        return this.hashedPassword;
    }
}

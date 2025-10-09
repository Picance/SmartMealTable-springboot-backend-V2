package com.stdev.smartmealtable.domain.policy.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 약관 동의 도메인 엔티티
 * 회원의 약관 동의 내역을 관리
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PolicyAgreement {

    private Long policyAgreementId;
    private Long policyId;
    private Long memberAuthenticationId;
    private Boolean isAgreed;
    private LocalDateTime agreedAt;

    /**
     * 재구성 팩토리 메서드 (Storage에서 사용)
     */
    public static PolicyAgreement reconstitute(
            Long policyAgreementId,
            Long policyId,
            Long memberAuthenticationId,
            Boolean isAgreed,
            LocalDateTime agreedAt
    ) {
        PolicyAgreement agreement = new PolicyAgreement();
        agreement.policyAgreementId = policyAgreementId;
        agreement.policyId = policyId;
        agreement.memberAuthenticationId = memberAuthenticationId;
        agreement.isAgreed = isAgreed;
        agreement.agreedAt = agreedAt;
        return agreement;
    }

    /**
     * 약관 동의 생성 팩토리 메서드
     */
    public static PolicyAgreement agree(
            Long policyId,
            Long memberAuthenticationId
    ) {
        PolicyAgreement agreement = new PolicyAgreement();
        agreement.policyId = policyId;
        agreement.memberAuthenticationId = memberAuthenticationId;
        agreement.isAgreed = true;
        agreement.agreedAt = LocalDateTime.now();
        return agreement;
    }
}

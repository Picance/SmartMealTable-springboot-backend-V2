package com.stdev.smartmealtable.domain.policy.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 약관 도메인 엔티티
 * 서비스 이용약관, 개인정보처리방침 등
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Policy {

    private Long policyId;
    private String title;
    private String content;
    private PolicyType type;
    private String version;
    private Boolean isMandatory;
    private Boolean isActive;

    /**
     * 재구성 팩토리 메서드 (Storage에서 사용)
     */
    public static Policy reconstitute(
            Long policyId,
            String title,
            String content,
            PolicyType type,
            String version,
            Boolean isMandatory,
            Boolean isActive
    ) {
        Policy policy = new Policy();
        policy.policyId = policyId;
        policy.title = title;
        policy.content = content;
        policy.type = type;
        policy.version = version;
        policy.isMandatory = isMandatory;
        policy.isActive = isActive;
        return policy;
    }

    /**
     * 새 약관 생성 (관리자용)
     */
    public static Policy create(
            String title,
            String content,
            PolicyType type,
            String version,
            Boolean isMandatory
    ) {
        Policy policy = new Policy();
        policy.title = title;
        policy.content = content;
        policy.type = type;
        policy.version = version;
        policy.isMandatory = isMandatory;
        policy.isActive = true;
        return policy;
    }

    /**
     * 약관 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }
}

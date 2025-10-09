package com.stdev.smartmealtable.api.onboarding.service;

import com.stdev.smartmealtable.api.onboarding.service.request.PolicyAgreementServiceRequest;
import com.stdev.smartmealtable.api.onboarding.service.response.PolicyAgreementServiceResponse;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.policy.entity.Policy;
import com.stdev.smartmealtable.domain.policy.entity.PolicyAgreement;
import com.stdev.smartmealtable.domain.policy.repository.PolicyAgreementRepository;
import com.stdev.smartmealtable.domain.policy.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 약관 동의 Application Service
 */
@Service
@RequiredArgsConstructor
public class PolicyAgreementService {

    private final PolicyRepository policyRepository;
    private final PolicyAgreementRepository policyAgreementRepository;
    private final MemberAuthenticationRepository memberAuthenticationRepository;

    @Transactional
    public PolicyAgreementServiceResponse agreeToPolicies(
            Long memberId,
            List<PolicyAgreementServiceRequest> requests
    ) {
        // 1. memberId로 memberAuthenticationId 조회
        MemberAuthentication memberAuthentication = memberAuthenticationRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorType.MEMBER_NOT_FOUND));
        Long memberAuthenticationId = memberAuthentication.getMemberAuthenticationId();
        
        // 2. 모든 활성화된 약관 조회
        List<Policy> allPolicies = policyRepository.findAllActive();
        
        // 2. 필수 약관 ID 목록 추출
        List<Long> mandatoryPolicyIds = allPolicies.stream()
                .filter(policy -> Boolean.TRUE.equals(policy.getIsMandatory()))
                .map(Policy::getPolicyId)
                .collect(Collectors.toList());

        // 3. 요청된 동의 항목을 Map으로 변환 (policyId -> isAgreed)
        Map<Long, Boolean> agreementMap = requests.stream()
                .collect(Collectors.toMap(
                        PolicyAgreementServiceRequest::getPolicyId,
                        PolicyAgreementServiceRequest::getIsAgreed
                ));

        // 4. 필수 약관 미동의 검증
        for (Long mandatoryPolicyId : mandatoryPolicyIds) {
            Boolean isAgreed = agreementMap.get(mandatoryPolicyId);
            if (isAgreed == null || !isAgreed) {
                throw new BusinessException(
                        ErrorType.REQUIRED_POLICY_NOT_AGREED,
                        String.format("필수 약관 (ID: %d)에 동의하지 않았습니다.", mandatoryPolicyId)
                );
            }
        }

        // 5. 약관 동의 내역 저장
        int agreedCount = 0;
        for (PolicyAgreementServiceRequest request : requests) {
            // 이미 동의한 약관인지 확인
            boolean alreadyAgreed = policyAgreementRepository
                    .existsByMemberAuthenticationIdAndPolicyId(
                            memberAuthenticationId,
                            request.getPolicyId()
                    );

            if (!alreadyAgreed && request.getIsAgreed()) {
                PolicyAgreement agreement = PolicyAgreement.agree(
                        request.getPolicyId(),
                        memberAuthenticationId
                );
                policyAgreementRepository.save(agreement);
                agreedCount++;
            }
        }

        return new PolicyAgreementServiceResponse(memberAuthenticationId, agreedCount);
    }
}

package com.stdev.smartmealtable.admin.policy.service;

import com.stdev.smartmealtable.admin.policy.service.dto.*;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.policy.entity.Policy;
import com.stdev.smartmealtable.domain.policy.entity.PolicyPageResult;
import com.stdev.smartmealtable.domain.policy.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.stdev.smartmealtable.core.error.ErrorType.*;

/**
 * 약관 관리 Application Service
 * 
 * <p>트랜잭션 관리와 유즈케이스에 집중합니다.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PolicyApplicationService {

    private final PolicyRepository policyRepository;

    /**
     * 약관 목록 조회 (페이징)
     */
    public PolicyListServiceResponse getPolicies(PolicyListServiceRequest request) {
        log.info("[ADMIN] 약관 목록 조회 - title: {}, isActive: {}, page: {}, size: {}", 
                request.title(), request.isActive(), request.page(), request.size());
        
        PolicyPageResult pageResult = policyRepository.searchByTitle(
                request.title(),
                request.isActive(),
                request.page(),
                request.size()
        );
        
        List<PolicyServiceResponse> policies = pageResult.content().stream()
                .map(PolicyServiceResponse::from)
                .collect(Collectors.toList());
        
        log.info("[ADMIN] 약관 목록 조회 완료 - 총 {}개, {}페이지", 
                pageResult.totalElements(), pageResult.totalPages());
        
        return PolicyListServiceResponse.of(
                policies,
                pageResult.page(),
                pageResult.size(),
                pageResult.totalElements(),
                pageResult.totalPages()
        );
    }

    /**
     * 약관 상세 조회
     */
    public PolicyServiceResponse getPolicy(Long policyId) {
        log.info("[ADMIN] 약관 상세 조회 - policyId: {}", policyId);
        
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new BusinessException(POLICY_NOT_FOUND));
        
        log.info("[ADMIN] 약관 상세 조회 완료 - title: {}", policy.getTitle());
        
        return PolicyServiceResponse.from(policy);
    }

    /**
     * 약관 생성
     */
    @Transactional
    public PolicyServiceResponse createPolicy(CreatePolicyServiceRequest request) {
        log.info("[ADMIN] 약관 생성 요청 - title: {}, type: {}", request.title(), request.type());
        
        // 제목 중복 검증
        if (policyRepository.existsByTitle(request.title())) {
            throw new BusinessException(DUPLICATE_POLICY_TITLE);
        }
        
        Policy policy = Policy.create(
                request.title(),
                request.content(),
                request.type(),
                request.version(),
                request.isMandatory()
        );
        
        Policy savedPolicy = policyRepository.save(policy);
        
        log.info("[ADMIN] 약관 생성 완료 - policyId: {}, title: {}", 
                savedPolicy.getPolicyId(), savedPolicy.getTitle());
        
        return PolicyServiceResponse.from(savedPolicy);
    }

    /**
     * 약관 수정
     */
    @Transactional
    public PolicyServiceResponse updatePolicy(Long policyId, UpdatePolicyServiceRequest request) {
        log.info("[ADMIN] 약관 수정 요청 - policyId: {}, title: {}", policyId, request.title());
        
        // 약관 존재 여부 확인
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new BusinessException(POLICY_NOT_FOUND));
        
        // 제목 중복 검증 (자신 제외)
        if (policyRepository.existsByTitleAndIdNot(request.title(), policyId)) {
            throw new BusinessException(DUPLICATE_POLICY_TITLE);
        }
        
        // 수정 (재구성)
        Policy updatedPolicy = Policy.reconstitute(
                policyId,
                request.title(),
                request.content(),
                request.type(),
                request.version(),
                request.isMandatory(),
                policy.getIsActive()  // 기존 활성 상태 유지
        );
        
        Policy savedPolicy = policyRepository.save(updatedPolicy);
        
        log.info("[ADMIN] 약관 수정 완료 - policyId: {}, title: {}", 
                savedPolicy.getPolicyId(), savedPolicy.getTitle());
        
        return PolicyServiceResponse.from(savedPolicy);
    }

    /**
     * 약관 삭제 (물리적 삭제)
     */
    @Transactional
    public void deletePolicy(Long policyId) {
        log.info("[ADMIN] 약관 삭제 요청 - policyId: {}", policyId);
        
        // 약관 존재 여부 확인
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new BusinessException(POLICY_NOT_FOUND));
        
        // 동의 내역 확인
        if (policyRepository.hasAgreements(policyId)) {
            throw new BusinessException(POLICY_HAS_AGREEMENTS);
        }
        
        policyRepository.deleteById(policyId);
        
        log.info("[ADMIN] 약관 삭제 완료 - policyId: {}, title: {}", 
                policyId, policy.getTitle());
    }

    /**
     * 약관 활성/비활성 토글
     */
    @Transactional
    public PolicyServiceResponse togglePolicyActive(Long policyId) {
        log.info("[ADMIN] 약관 활성/비활성 토글 요청 - policyId: {}", policyId);
        
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new BusinessException(POLICY_NOT_FOUND));
        
        // 토글 처리
        Boolean newActiveStatus = !policy.getIsActive();
        
        Policy updatedPolicy = Policy.reconstitute(
                policy.getPolicyId(),
                policy.getTitle(),
                policy.getContent(),
                policy.getType(),
                policy.getVersion(),
                policy.getIsMandatory(),
                newActiveStatus
        );
        
        Policy savedPolicy = policyRepository.save(updatedPolicy);
        
        log.info("[ADMIN] 약관 활성/비활성 토글 완료 - policyId: {}, isActive: {}", 
                policyId, newActiveStatus);
        
        return PolicyServiceResponse.from(savedPolicy);
    }
}

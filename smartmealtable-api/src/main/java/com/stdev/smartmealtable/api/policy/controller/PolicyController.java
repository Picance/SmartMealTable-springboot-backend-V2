package com.stdev.smartmealtable.api.policy.controller;

import com.stdev.smartmealtable.api.policy.controller.dto.PolicyResponse;
import com.stdev.smartmealtable.api.policy.service.GetPoliciesService;
import com.stdev.smartmealtable.api.policy.service.GetPolicyService;
import com.stdev.smartmealtable.api.policy.service.dto.GetPoliciesServiceResponse;
import com.stdev.smartmealtable.api.policy.service.dto.GetPolicyServiceResponse;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 약관 관리 Controller
 * 온보딩 시 약관 조회 및 동의를 위한 약관 API 제공
 */
@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
@Slf4j
public class PolicyController {

    private final GetPoliciesService getPoliciesService;
    private final GetPolicyService getPolicyService;

    /**
     * 약관 목록 조회
     * GET /api/v1/policies
     *
     * @return 활성화된 모든 약관 목록 (content는 제외)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PolicyListResponse>> getPolicies() {
        log.info("약관 목록 조회 API 호출");

        GetPoliciesServiceResponse serviceResponse = getPoliciesService.getPolicies();

        // Response DTO 변환 (목록 조회 시에는 content 제외)
        List<PolicyListResponse.PolicySummary> policies = serviceResponse.policies().stream()
                .map(p -> new PolicyListResponse.PolicySummary(
                        p.policyId(),
                        p.title(),
                        p.type(),
                        p.version()
                ))
                .collect(Collectors.toList());

        PolicyListResponse response = new PolicyListResponse(policies);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 약관 상세 조회
     * GET /api/v1/policies/{policyId}
     *
     * @param policyId 약관 ID
     * @return 약관 상세 정보 (content 포함)
     */
    @GetMapping("/{policyId}")
    public ResponseEntity<ApiResponse<PolicyResponse>> getPolicy(@PathVariable Long policyId) {
        log.info("약관 상세 조회 API 호출 - policyId: {}", policyId);

        GetPolicyServiceResponse serviceResponse = getPolicyService.getPolicy(policyId);

        PolicyResponse response = new PolicyResponse(
                serviceResponse.policyId(),
                serviceResponse.title(),
                serviceResponse.content(),
                serviceResponse.type(),
                serviceResponse.version()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 약관 목록 응답 DTO (content 제외)
     */
    public record PolicyListResponse(
            List<PolicySummary> policies
    ) {
        public record PolicySummary(
                Long policyId,
                String title,
                com.stdev.smartmealtable.domain.policy.entity.PolicyType type,
                String version
        ) {
        }
    }
}

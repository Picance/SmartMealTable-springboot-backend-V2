package com.stdev.smartmealtable.admin.policy.controller;

import com.stdev.smartmealtable.admin.policy.controller.dto.*;
import com.stdev.smartmealtable.admin.policy.service.PolicyApplicationService;
import com.stdev.smartmealtable.admin.policy.service.dto.*;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 약관 관리 Controller
 * 
 * <p>약관(Policy) 생성, 조회, 수정, 삭제, 활성/비활성 관리 API를 제공합니다.</p>
 */
@RestController
@RequestMapping("/api/v1/admin/policies")
@RequiredArgsConstructor
@Slf4j
public class PolicyController {

    private final PolicyApplicationService policyApplicationService;

    /**
     * 약관 목록 조회
     */
    @GetMapping
    public ApiResponse<PolicyListResponse> getPolicies(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("[ADMIN] GET /api/v1/admin/policies - title: {}, isActive: {}, page: {}, size: {}",
                title, isActive, page, size);
        
        PolicyListServiceRequest request = new PolicyListServiceRequest(title, isActive, page, size);
        PolicyListServiceResponse serviceResponse = policyApplicationService.getPolicies(request);
        
        return ApiResponse.success(PolicyListResponse.from(serviceResponse));
    }

    /**
     * 약관 상세 조회
     */
    @GetMapping("/{policyId}")
    public ApiResponse<PolicyResponse> getPolicy(@PathVariable Long policyId) {
        log.info("[ADMIN] GET /api/v1/admin/policies/{}", policyId);
        
        PolicyServiceResponse serviceResponse = policyApplicationService.getPolicy(policyId);
        
        return ApiResponse.success(PolicyResponse.from(serviceResponse));
    }

    /**
     * 약관 생성
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PolicyResponse> createPolicy(
            @Valid @RequestBody CreatePolicyRequest request
    ) {
        log.info("[ADMIN] POST /api/v1/admin/policies - title: {}, type: {}", 
                request.title(), request.type());
        
        CreatePolicyServiceRequest serviceRequest = request.toServiceRequest();
        PolicyServiceResponse serviceResponse = policyApplicationService.createPolicy(serviceRequest);
        
        return ApiResponse.success(PolicyResponse.from(serviceResponse));
    }

    /**
     * 약관 수정
     */
    @PutMapping("/{policyId}")
    public ApiResponse<PolicyResponse> updatePolicy(
            @PathVariable Long policyId,
            @Valid @RequestBody UpdatePolicyRequest request
    ) {
        log.info("[ADMIN] PUT /api/v1/admin/policies/{} - title: {}, type: {}", 
                policyId, request.title(), request.type());
        
        UpdatePolicyServiceRequest serviceRequest = request.toServiceRequest();
        PolicyServiceResponse serviceResponse = policyApplicationService.updatePolicy(policyId, serviceRequest);
        
        return ApiResponse.success(PolicyResponse.from(serviceResponse));
    }

    /**
     * 약관 삭제
     */
    @DeleteMapping("/{policyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deletePolicy(@PathVariable Long policyId) {
        log.info("[ADMIN] DELETE /api/v1/admin/policies/{}", policyId);
        
        policyApplicationService.deletePolicy(policyId);
        
        return ApiResponse.success();
    }

    /**
     * 약관 활성/비활성 토글
     */
    @PatchMapping("/{policyId}/toggle")
    public ApiResponse<PolicyResponse> togglePolicyActive(@PathVariable Long policyId) {
        log.info("[ADMIN] PATCH /api/v1/admin/policies/{}/toggle", policyId);
        
        PolicyServiceResponse serviceResponse = policyApplicationService.togglePolicyActive(policyId);
        
        return ApiResponse.success(PolicyResponse.from(serviceResponse));
    }
}

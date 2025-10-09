package com.stdev.smartmealtable.api.member.service;

import com.stdev.smartmealtable.api.member.service.dto.ChangePasswordServiceRequest;
import com.stdev.smartmealtable.api.member.service.dto.ChangePasswordServiceResponse;
import com.stdev.smartmealtable.domain.member.service.AuthenticationDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 비밀번호 변경 Application Service
 * 유즈케이스 orchestration에 집중
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChangePasswordService {

    private final AuthenticationDomainService authenticationDomainService;

    /**
     * 비밀번호 변경
     */
    @Transactional
    public ChangePasswordServiceResponse changePassword(ChangePasswordServiceRequest request) {
        // Domain Service를 통한 비밀번호 변경
        authenticationDomainService.changePassword(
                request.memberId(),
                request.currentPassword(),
                request.newPassword()
        );
        
        return ChangePasswordServiceResponse.success();
    }
}

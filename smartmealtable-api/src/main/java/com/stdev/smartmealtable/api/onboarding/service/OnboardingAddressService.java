package com.stdev.smartmealtable.api.onboarding.service;

import com.stdev.smartmealtable.api.onboarding.service.dto.OnboardingAddressServiceRequest;
import com.stdev.smartmealtable.api.onboarding.service.dto.OnboardingAddressServiceResponse;
import com.stdev.smartmealtable.domain.member.entity.AddressHistory;
import com.stdev.smartmealtable.domain.member.repository.AddressHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 온보딩 - 주소 등록 Service
 * 첫 번째 주소는 자동으로 기본 주소로 설정됨
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OnboardingAddressService {
    
    private final AddressHistoryRepository addressHistoryRepository;
    
    /**
     * 주소 등록
     * - 첫 번째 주소는 자동으로 기본 주소로 설정됨
     * - 기존 주소가 있을 경우 새 주소는 일반 주소로 등록됨
     */
    @Transactional
    public OnboardingAddressServiceResponse registerAddress(OnboardingAddressServiceRequest request) {
        // 회원의 기존 주소 개수 확인
        long existingAddressCount = addressHistoryRepository.countByMemberId(request.memberId());
        
        // 첫 번째 주소는 자동으로 기본 주소, 이후는 일반 주소
        boolean isPrimary = (existingAddressCount == 0);
        
        // 새 주소 등록
        AddressHistory newAddress = AddressHistory.create(
                request.memberId(),
                request.address(),
                isPrimary
        );
        
        AddressHistory savedAddress = addressHistoryRepository.save(newAddress);
        
        return OnboardingAddressServiceResponse.from(savedAddress);
    }
}


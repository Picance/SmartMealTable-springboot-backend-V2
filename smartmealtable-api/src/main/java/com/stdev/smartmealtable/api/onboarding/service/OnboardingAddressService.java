package com.stdev.smartmealtable.api.onboarding.service;

import com.stdev.smartmealtable.api.onboarding.service.dto.OnboardingAddressServiceRequest;
import com.stdev.smartmealtable.api.onboarding.service.dto.OnboardingAddressServiceResponse;
import com.stdev.smartmealtable.domain.member.entity.AddressHistory;
import com.stdev.smartmealtable.domain.member.repository.AddressHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 온보딩 - 주소 등록 Service
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OnboardingAddressService {
    
    private final AddressHistoryRepository addressHistoryRepository;
    
    /**
     * 주소 등록
     * - isPrimary = true인 경우, 기존 주 주소를 일반 주소로 전환
     */
    @Transactional
    public OnboardingAddressServiceResponse registerAddress(OnboardingAddressServiceRequest request) {
        // 주 주소로 등록하는 경우, 기존 주 주소 해제
        if (Boolean.TRUE.equals(request.isPrimary())) {
            Optional<AddressHistory> existingPrimary = addressHistoryRepository.findPrimaryByMemberId(request.memberId());
            existingPrimary.ifPresent(primary -> {
                primary.unmarkAsPrimary();
                addressHistoryRepository.save(primary);
            });
        }
        
        // 새 주소 등록
        AddressHistory newAddress = AddressHistory.create(
                request.memberId(),
                request.address(),
                request.isPrimary()
        );
        
        AddressHistory savedAddress = addressHistoryRepository.save(newAddress);
        
        return OnboardingAddressServiceResponse.from(savedAddress);
    }
}

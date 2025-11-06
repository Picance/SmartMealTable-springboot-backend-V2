package com.stdev.smartmealtable.api.member.service;

import com.stdev.smartmealtable.api.member.dto.AddressServiceRequest;
import com.stdev.smartmealtable.api.member.dto.AddressServiceResponse;
import com.stdev.smartmealtable.domain.member.entity.AddressHistory;
import com.stdev.smartmealtable.domain.member.repository.AddressHistoryRepository;
import com.stdev.smartmealtable.domain.member.service.AddressDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 주소 관리 Application Service
 * 유즈케이스 orchestration에 집중
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AddressService {
    
    private final AddressHistoryRepository addressHistoryRepository;
    private final AddressDomainService addressDomainService;
    
    /**
     * 회원의 주소 목록 조회
     *
     * @param memberId 회원 ID
     * @return 주소 목록
     */
    public List<AddressServiceResponse> getAddresses(Long memberId) {
        List<AddressHistory> addresses = addressHistoryRepository.findAllByMemberId(memberId);
        return addresses.stream()
                .map(AddressServiceResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 주소 추가
     * Domain Service를 통한 비즈니스 로직 처리
     * 첫 번째 주소는 자동으로 기본 주소로 설정됨
     *
     * @param memberId 회원 ID
     * @param request  주소 등록 요청 DTO
     * @return 등록된 주소 응답 DTO
     */
    @Transactional
    public AddressServiceResponse addAddress(Long memberId, AddressServiceRequest request) {
        // Domain Service를 통한 주소 추가 (검증 + 도메인 로직 포함)
        // isPrimary 파라미터 없음 - 첫 주소는 자동으로 기본 주소로 설정됨
        AddressHistory savedAddress = addressDomainService.addAddress(
                memberId,
                request.toAddress()
        );
        
        return AddressServiceResponse.from(savedAddress);
    }
    
    /**
     * 주소 수정
     * Domain Service를 통한 비즈니스 로직 처리
     * 주소 정보만 업데이트 (기본 주소 변경은 setPrimaryAddress 사용)
     *
     * @param memberId         회원 ID
     * @param addressHistoryId 수정할 주소 ID
     * @param request          주소 수정 요청 DTO
     * @return 수정된 주소 응답 DTO
     */
    @Transactional
    public AddressServiceResponse updateAddress(
            Long memberId,
            Long addressHistoryId,
            AddressServiceRequest request
    ) {
        // Domain Service를 통한 주소 수정 (검증 + 도메인 로직 포함)
        AddressHistory updatedAddress = addressDomainService.updateAddress(
                memberId,
                addressHistoryId,
                request.toAddress()
        );
        
        return AddressServiceResponse.from(updatedAddress);
    }
    
    /**
     * 주소 삭제 (Soft Delete)
     * Domain Service를 통한 비즈니스 로직 처리
     *
     * @param memberId         회원 ID
     * @param addressHistoryId 삭제할 주소 ID
     */
    @Transactional
    public void deleteAddress(Long memberId, Long addressHistoryId) {
        // Domain Service를 통한 주소 삭제 (검증 + 도메인 로직 포함)
        addressDomainService.deleteAddress(memberId, addressHistoryId);
    }
    
    /**
     * 기본 주소 설정
     * Domain Service를 통한 비즈니스 로직 처리
     *
     * @param memberId         회원 ID
     * @param addressHistoryId 기본 주소로 설정할 주소 ID
     * @return 설정된 주소 응답 DTO
     */
    @Transactional
    public AddressServiceResponse setPrimaryAddress(Long memberId, Long addressHistoryId) {
        // Domain Service를 통한 기본 주소 설정 (검증 + 도메인 로직 포함)
        AddressHistory updatedAddress = addressDomainService.setPrimaryAddress(memberId, addressHistoryId);
        
        return AddressServiceResponse.from(updatedAddress);
    }
}

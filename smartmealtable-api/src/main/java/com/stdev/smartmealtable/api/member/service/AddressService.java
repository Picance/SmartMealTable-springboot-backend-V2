package com.stdev.smartmealtable.api.member.service;

import com.stdev.smartmealtable.api.member.dto.AddressServiceRequest;
import com.stdev.smartmealtable.api.member.dto.AddressServiceResponse;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.member.entity.AddressHistory;
import com.stdev.smartmealtable.domain.member.repository.AddressHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 주소 관리 Application Service
 * 회원의 주소 목록 조회, 추가, 수정, 삭제, 기본 주소 설정 등의 유즈케이스 처리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AddressService {
    
    private final AddressHistoryRepository addressHistoryRepository;
    
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
     * - 첫 번째 주소는 자동으로 기본 주소가 됨
     * - 기본 주소로 등록 시 기존 기본 주소는 해제됨
     *
     * @param memberId 회원 ID
     * @param request  주소 등록 요청 DTO
     * @return 등록된 주소 응답 DTO
     */
    @Transactional
    public AddressServiceResponse addAddress(Long memberId, AddressServiceRequest request) {
        long addressCount = addressHistoryRepository.countByMemberId(memberId);
        
        // 첫 번째 주소는 자동으로 기본 주소
        boolean isPrimary = (addressCount == 0) || request.getIsPrimary();
        
        // 기본 주소로 설정하는 경우, 기존 기본 주소 해제
        if (isPrimary && addressCount > 0) {
            addressHistoryRepository.unmarkAllAsPrimaryByMemberId(memberId);
        }
        
        AddressHistory addressHistory = AddressHistory.create(
                memberId,
                request.toAddress(),
                isPrimary
        );
        
        AddressHistory savedAddress = addressHistoryRepository.save(addressHistory);
        
        log.info("주소 추가 완료 - memberId: {}, addressHistoryId: {}, isPrimary: {}",
                memberId, savedAddress.getAddressHistoryId(), isPrimary);
        
        return AddressServiceResponse.from(savedAddress);
    }
    
    /**
     * 주소 수정
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
        AddressHistory addressHistory = addressHistoryRepository.findById(addressHistoryId)
                .orElseThrow(() -> new BusinessException(ErrorType.ADDRESS_NOT_FOUND));
        
        // 본인의 주소인지 확인
        if (!addressHistory.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorType.FORBIDDEN_ACCESS);
        }
        
        // 주소 정보 업데이트
        addressHistory.updateAddress(request.toAddress());
        
        // 기본 주소로 설정하는 경우, 기존 기본 주소 해제
        if (request.getIsPrimary() && !addressHistory.getIsPrimary()) {
            addressHistoryRepository.unmarkAllAsPrimaryByMemberId(memberId);
            addressHistory.markAsPrimary();
        }
        
        AddressHistory updatedAddress = addressHistoryRepository.save(addressHistory);
        
        log.info("주소 수정 완료 - memberId: {}, addressHistoryId: {}",
                memberId, addressHistoryId);
        
        return AddressServiceResponse.from(updatedAddress);
    }
    
    /**
     * 주소 삭제 (Soft Delete)
     * - 기본 주소가 1개뿐일 경우 삭제 불가
     *
     * @param memberId         회원 ID
     * @param addressHistoryId 삭제할 주소 ID
     */
    @Transactional
    public void deleteAddress(Long memberId, Long addressHistoryId) {
        AddressHistory addressHistory = addressHistoryRepository.findById(addressHistoryId)
                .orElseThrow(() -> new BusinessException(ErrorType.ADDRESS_NOT_FOUND));
        
        // 본인의 주소인지 확인
        if (!addressHistory.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorType.FORBIDDEN_ACCESS);
        }
        
        // 기본 주소가 1개뿐일 경우 삭제 불가
        if (addressHistory.getIsPrimary()) {
            long addressCount = addressHistoryRepository.countByMemberId(memberId);
            if (addressCount <= 1) {
                throw new BusinessException(ErrorType.CANNOT_DELETE_LAST_PRIMARY_ADDRESS);
            }
        }
        
        addressHistoryRepository.delete(addressHistory);
        
        log.info("주소 삭제 완료 - memberId: {}, addressHistoryId: {}",
                memberId, addressHistoryId);
    }
    
    /**
     * 기본 주소 설정
     * - 기존 기본 주소는 자동으로 해제됨
     *
     * @param memberId         회원 ID
     * @param addressHistoryId 기본 주소로 설정할 주소 ID
     * @return 설정된 주소 응답 DTO
     */
    @Transactional
    public AddressServiceResponse setPrimaryAddress(Long memberId, Long addressHistoryId) {
        AddressHistory addressHistory = addressHistoryRepository.findById(addressHistoryId)
                .orElseThrow(() -> new BusinessException(ErrorType.ADDRESS_NOT_FOUND));
        
        // 본인의 주소인지 확인
        if (!addressHistory.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorType.FORBIDDEN_ACCESS);
        }
        
        // 기존 기본 주소 모두 해제
        addressHistoryRepository.unmarkAllAsPrimaryByMemberId(memberId);
        
        // 새로운 기본 주소 설정
        addressHistory.markAsPrimary();
        AddressHistory updatedAddress = addressHistoryRepository.save(addressHistory);
        
        log.info("기본 주소 설정 완료 - memberId: {}, addressHistoryId: {}",
                memberId, addressHistoryId);
        
        return AddressServiceResponse.from(updatedAddress);
    }
}

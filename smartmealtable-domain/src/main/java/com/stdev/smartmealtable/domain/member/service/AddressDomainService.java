package com.stdev.smartmealtable.domain.member.service;

import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.member.entity.AddressHistory;
import com.stdev.smartmealtable.domain.member.repository.AddressHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 주소 도메인 서비스
 * 주소 관리와 관련된 핵심 비즈니스 로직을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddressDomainService {

    private final AddressHistoryRepository addressHistoryRepository;

    /**
     * 회원의 주소 개수 조회
     *
     * @param memberId 회원 ID
     * @return 주소 개수
     */
    public long countAddresses(Long memberId) {
        return addressHistoryRepository.countByMemberId(memberId);
    }

    /**
     * 주소 추가
     * - 첫 번째 주소는 자동으로 기본 주소
     * - 기본 주소로 설정 시 기존 기본 주소 해제
     *
     * @param memberId 회원 ID
     * @param address  주소 정보
     * @param isPrimary 기본 주소 여부
     * @return 생성된 AddressHistory
     */
    public AddressHistory addAddress(Long memberId, Address address, Boolean isPrimary) {
        long addressCount = countAddresses(memberId);

        // 첫 번째 주소는 자동으로 기본 주소
        boolean shouldBePrimary = (addressCount == 0) || (isPrimary != null && isPrimary);

        // 기본 주소로 설정하는 경우, 기존 기본 주소 해제
        if (shouldBePrimary && addressCount > 0) {
            unmarkAllAsPrimary(memberId);
        }

        AddressHistory addressHistory = AddressHistory.create(
                memberId,
                address,
                shouldBePrimary
        );

        AddressHistory saved = addressHistoryRepository.save(addressHistory);

        log.info("주소 추가 완료 - memberId: {}, addressHistoryId: {}, isPrimary: {}",
                memberId, saved.getAddressHistoryId(), shouldBePrimary);

        return saved;
    }

    /**
     * 주소 수정
     *
     * @param memberId         회원 ID
     * @param addressHistoryId 주소 ID
     * @param address          새 주소 정보
     * @param isPrimary        기본 주소 여부
     * @return 수정된 AddressHistory
     */
    public AddressHistory updateAddress(
            Long memberId,
            Long addressHistoryId,
            Address address,
            Boolean isPrimary
    ) {
        AddressHistory addressHistory = validateAndGetAddress(addressHistoryId, memberId);

        // 주소 정보 업데이트
        addressHistory.updateAddress(address);

        // 기본 주소로 설정하는 경우, 기존 기본 주소 해제
        if (isPrimary != null && isPrimary && !addressHistory.getIsPrimary()) {
            unmarkAllAsPrimary(memberId);
            addressHistory.markAsPrimary();
        }

        AddressHistory updated = addressHistoryRepository.save(addressHistory);

        log.info("주소 수정 완료 - memberId: {}, addressHistoryId: {}",
                memberId, addressHistoryId);

        return updated;
    }

    /**
     * 주소 삭제 검증
     * - 기본 주소가 마지막 1개인 경우 삭제 불가
     *
     * @param memberId         회원 ID
     * @param addressHistoryId 주소 ID
     */
    public void deleteAddress(Long memberId, Long addressHistoryId) {
        AddressHistory addressHistory = validateAndGetAddress(addressHistoryId, memberId);

        // 기본 주소가 1개뿐일 경우 삭제 불가
        if (addressHistory.getIsPrimary()) {
            long addressCount = countAddresses(memberId);
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
     * @return 설정된 AddressHistory
     */
    public AddressHistory setPrimaryAddress(Long memberId, Long addressHistoryId) {
        AddressHistory addressHistory = validateAndGetAddress(addressHistoryId, memberId);

        // 기존 기본 주소 모두 해제
        unmarkAllAsPrimary(memberId);

        // 새로운 기본 주소 설정
        addressHistory.markAsPrimary();
        AddressHistory updated = addressHistoryRepository.save(addressHistory);

        log.info("기본 주소 설정 완료 - memberId: {}, addressHistoryId: {}",
                memberId, addressHistoryId);

        return updated;
    }

    /**
     * 주소 조회 및 소유권 검증
     *
     * @param addressHistoryId 주소 ID
     * @param memberId         회원 ID
     * @return AddressHistory
     * @throws BusinessException 주소가 존재하지 않거나 본인의 주소가 아닐 경우
     */
    private AddressHistory validateAndGetAddress(Long addressHistoryId, Long memberId) {
        AddressHistory addressHistory = addressHistoryRepository.findById(addressHistoryId)
                .orElseThrow(() -> new BusinessException(ErrorType.ADDRESS_NOT_FOUND));

        // 본인의 주소인지 확인
        if (!addressHistory.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorType.FORBIDDEN_ACCESS);
        }

        return addressHistory;
    }

    /**
     * 회원의 모든 기본 주소 해제
     *
     * @param memberId 회원 ID
     */
    private void unmarkAllAsPrimary(Long memberId) {
        addressHistoryRepository.unmarkAllAsPrimaryByMemberId(memberId);
    }
}

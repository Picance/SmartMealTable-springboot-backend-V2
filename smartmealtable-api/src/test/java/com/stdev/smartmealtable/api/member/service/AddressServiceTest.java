package com.stdev.smartmealtable.api.member.service;

import com.stdev.smartmealtable.api.member.dto.AddressServiceRequest;
import com.stdev.smartmealtable.api.member.dto.AddressServiceResponse;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.member.entity.AddressHistory;
import com.stdev.smartmealtable.domain.member.repository.AddressHistoryRepository;
import com.stdev.smartmealtable.domain.member.service.AddressDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

/**
 * 주소 관리 Application Service 테스트 (Mockist 스타일 Unit Test)
 * Domain Service를 Mock하여 orchestration 로직만 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AddressService 단위 테스트")
class AddressServiceTest {
    
    @InjectMocks
    private AddressService addressService;
    
    @Mock
    private AddressHistoryRepository addressHistoryRepository;
    
    @Mock
    private AddressDomainService addressDomainService;
    
    @Test
    @DisplayName("주소 목록 조회 - 성공")
    void getAddresses_Success() {
        // given
        Long memberId = 1L;
        Address address = Address.of(
                "우리집", null, "서울특별시 강남구 테헤란로 123",
                "101동 1234호", 37.497942, 127.027621, "HOME"
        );
        AddressHistory addressHistory = AddressHistory.reconstitute(
                1L, memberId, address, true, null
        );
        
        given(addressHistoryRepository.findAllByMemberId(memberId))
                .willReturn(List.of(addressHistory));
        
        // when
        List<AddressServiceResponse> responses = addressService.getAddresses(memberId);
        
        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getAddressAlias()).isEqualTo("우리집");
        assertThat(responses.get(0).getIsPrimary()).isTrue();
        verify(addressHistoryRepository, times(1)).findAllByMemberId(memberId);
    }
    
    @Test
    @DisplayName("주소 추가 - 성공 (첫 번째 주소는 자동으로 기본 주소)")
    void addAddress_Success_FirstAddressBecomesProper() {
        // given
        Long memberId = 1L;
        AddressServiceRequest request = new AddressServiceRequest(
                "우리집", null, "서울특별시 강남구 테헤란로 123",
                "101동 1234호", 37.497942, 127.027621, "HOME"
        );
        
        AddressHistory savedAddress = AddressHistory.reconstitute(
                1L, memberId, request.toAddress(), true, null
        );
        
        given(addressDomainService.addAddress(eq(memberId), any(Address.class)))
                .willReturn(savedAddress);
        
        // when
        AddressServiceResponse response = addressService.addAddress(memberId, request);
        
        // then
        assertThat(response.getAddressAlias()).isEqualTo("우리집");
        assertThat(response.getIsPrimary()).isTrue(); // 첫 번째 주소는 자동으로 기본 주소
        verify(addressDomainService, times(1)).addAddress(eq(memberId), any(Address.class));
    }
    
    @Test
    @DisplayName("주소 추가 - 성공 (두 번째 이후 주소는 일반 주소로 등록)")
    void addAddress_Success_SecondAddressBecomesNormal() {
        // given
        Long memberId = 1L;
        AddressServiceRequest request = new AddressServiceRequest(
                "회사", null, "서울특별시 강남구 테헤란로 456",
                "5층", 37.497942, 127.027621, "OFFICE"
        );
        
        AddressHistory savedAddress = AddressHistory.reconstitute(
                2L, memberId, request.toAddress(), false, null
        );
        
        given(addressDomainService.addAddress(eq(memberId), any(Address.class)))
                .willReturn(savedAddress);
        
        // when
        AddressServiceResponse response = addressService.addAddress(memberId, request);
        
        // then
        assertThat(response.getAddressAlias()).isEqualTo("회사");
        assertThat(response.getIsPrimary()).isFalse(); // 두 번째 주소는 일반 주소
        verify(addressDomainService, times(1)).addAddress(eq(memberId), any(Address.class));
    }
    
    @Test
    @DisplayName("주소 수정 - 성공")
    void updateAddress_Success() {
        // given
        Long memberId = 1L;
        Long addressHistoryId = 1L;
        AddressServiceRequest request = new AddressServiceRequest(
                "우리집(수정)", null, "서울특별시 강남구 테헤란로 789",
                "202동 5678호", 37.497942, 127.027621, "HOME"
        );
        
        AddressHistory updatedAddress = AddressHistory.reconstitute(
                addressHistoryId, memberId, request.toAddress(), true, null
        );
        
        given(addressDomainService.updateAddress(
                eq(memberId), eq(addressHistoryId), any(Address.class)
        )).willReturn(updatedAddress);
        
        // when
        AddressServiceResponse response = addressService.updateAddress(memberId, addressHistoryId, request);
        
        // then
        assertThat(response.getAddressAlias()).isEqualTo("우리집(수정)");
        assertThat(response.getStreetNameAddress()).isEqualTo("서울특별시 강남구 테헤란로 789");
        verify(addressDomainService, times(1)).updateAddress(
                eq(memberId), eq(addressHistoryId), any(Address.class)
        );
    }
    
    @Test
    @DisplayName("주소 수정 - 실패 (존재하지 않는 주소)")
    void updateAddress_Fail_NotFound() {
        // given
        Long memberId = 1L;
        Long addressHistoryId = 999L;
        AddressServiceRequest request = new AddressServiceRequest(
                "우리집(수정)", null, "서울특별시 강남구 테헤란로 789",
                "202동 5678호", 37.497942, 127.027621, "HOME"
        );
        
        given(addressDomainService.updateAddress(
                eq(memberId), eq(addressHistoryId), any(Address.class)
        )).willThrow(new BusinessException(ErrorType.ADDRESS_NOT_FOUND));
        
        // when & then
        assertThatThrownBy(() -> addressService.updateAddress(memberId, addressHistoryId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.ADDRESS_NOT_FOUND);
    }
    
    @Test
    @DisplayName("주소 수정 - 실패 (다른 회원의 주소)")
    void updateAddress_Fail_NotOwner() {
        // given
        Long memberId = 1L;
        Long addressHistoryId = 1L;
        AddressServiceRequest request = new AddressServiceRequest(
                "우리집(수정)", null, "서울특별시 강남구 테헤란로 789",
                "202동 5678호", 37.497942, 127.027621, "HOME"
        );
        
        given(addressDomainService.updateAddress(
                eq(memberId), eq(addressHistoryId), any(Address.class)
        )).willThrow(new BusinessException(ErrorType.FORBIDDEN_ACCESS));
        
        // when & then
        assertThatThrownBy(() -> addressService.updateAddress(memberId, addressHistoryId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.FORBIDDEN_ACCESS);
    }
    
    @Test
    @DisplayName("주소 삭제 - 성공")
    void deleteAddress_Success() {
        // given
        Long memberId = 1L;
        Long addressHistoryId = 2L; // 기본 주소가 아닌 다른 주소
        
        // Domain Service가 정상 처리됨을 Mock (void 메서드는 willDoNothing 사용)
        willDoNothing().given(addressDomainService).deleteAddress(memberId, addressHistoryId);
        
        // when
        addressService.deleteAddress(memberId, addressHistoryId);
        
        // then
        verify(addressDomainService, times(1)).deleteAddress(memberId, addressHistoryId);
    }
    
    @Test
    @DisplayName("주소 삭제 - 실패 (기본 주소는 다른 주소가 있을 때만 삭제 가능)")
    void deleteAddress_Fail_CannotDeleteLastPrimary() {
        // given
        Long memberId = 1L;
        Long addressHistoryId = 1L;
        
        willThrow(new BusinessException(ErrorType.CANNOT_DELETE_LAST_PRIMARY_ADDRESS))
                .given(addressDomainService).deleteAddress(memberId, addressHistoryId);
        
        // when & then
        assertThatThrownBy(() -> addressService.deleteAddress(memberId, addressHistoryId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.CANNOT_DELETE_LAST_PRIMARY_ADDRESS);
    }
    
    @Test
    @DisplayName("기본 주소 설정 - 성공")
    void setPrimaryAddress_Success() {
        // given
        Long memberId = 1L;
        Long addressHistoryId = 2L;
        Address address = Address.of(
                "회사", null, "서울특별시 강남구 테헤란로 456",
                "5층", 37.497942, 127.027621, "OFFICE"
        );
        AddressHistory updatedAddress = AddressHistory.reconstitute(
                addressHistoryId, memberId, address, true, null
        );
        
        given(addressDomainService.setPrimaryAddress(memberId, addressHistoryId))
                .willReturn(updatedAddress);
        
        // when
        AddressServiceResponse response = addressService.setPrimaryAddress(memberId, addressHistoryId);
        
        // then
        assertThat(response.getIsPrimary()).isTrue();
        verify(addressDomainService, times(1)).setPrimaryAddress(memberId, addressHistoryId);
    }
    
    @Test
    @DisplayName("기본 주소 설정 - 실패 (존재하지 않는 주소)")
    void setPrimaryAddress_Fail_NotFound() {
        // given
        Long memberId = 1L;
        Long addressHistoryId = 999L;
        
        given(addressDomainService.setPrimaryAddress(memberId, addressHistoryId))
                .willThrow(new BusinessException(ErrorType.ADDRESS_NOT_FOUND));
        
        // when & then
        assertThatThrownBy(() -> addressService.setPrimaryAddress(memberId, addressHistoryId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.ADDRESS_NOT_FOUND);
    }
}

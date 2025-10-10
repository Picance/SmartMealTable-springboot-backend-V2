package com.stdev.smartmealtable.api.member.service;

import com.stdev.smartmealtable.api.member.dto.AddressServiceRequest;
import com.stdev.smartmealtable.api.member.dto.AddressServiceResponse;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.member.entity.AddressHistory;
import com.stdev.smartmealtable.domain.member.repository.AddressHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * 주소 관리 Service 테스트 (Mockist 스타일 Unit Test)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AddressService 단위 테스트")
class AddressServiceTest {
    
    @InjectMocks
    private AddressService addressService;
    
    @Mock
    private AddressHistoryRepository addressHistoryRepository;
    
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
                "101동 1234호", 37.497942, 127.027621, "HOME", false
        );
        
        given(addressHistoryRepository.countByMemberId(memberId)).willReturn(0L);
        given(addressHistoryRepository.save(any(AddressHistory.class)))
                .willAnswer(invocation -> {
                    AddressHistory arg = invocation.getArgument(0);
                    return AddressHistory.reconstitute(
                            1L, arg.getMemberId(), arg.getAddress(),
                            arg.getIsPrimary(), arg.getRegisteredAt()
                    );
                });
        
        // when
        AddressServiceResponse response = addressService.addAddress(memberId, request);
        
        // then
        assertThat(response.getAddressAlias()).isEqualTo("우리집");
        assertThat(response.getIsPrimary()).isTrue(); // 첫 번째 주소는 자동으로 기본 주소
        verify(addressHistoryRepository, times(1)).save(any(AddressHistory.class));
    }
    
    @Test
    @DisplayName("주소 추가 - 성공 (기본 주소로 설정 시 기존 기본 주소 해제)")
    void addAddress_Success_UnmarkPreviousPrimary() {
        // given
        Long memberId = 1L;
        AddressServiceRequest request = new AddressServiceRequest(
                "회사", null, "서울특별시 강남구 테헤란로 456",
                "5층", 37.497942, 127.027621, "OFFICE", true
        );
        
        given(addressHistoryRepository.countByMemberId(memberId)).willReturn(1L);
        given(addressHistoryRepository.save(any(AddressHistory.class)))
                .willAnswer(invocation -> {
                    AddressHistory arg = invocation.getArgument(0);
                    return AddressHistory.reconstitute(
                            2L, arg.getMemberId(), arg.getAddress(),
                            arg.getIsPrimary(), arg.getRegisteredAt()
                    );
                });
        
        // when
        AddressServiceResponse response = addressService.addAddress(memberId, request);
        
        // then
        assertThat(response.getAddressAlias()).isEqualTo("회사");
        assertThat(response.getIsPrimary()).isTrue();
        verify(addressHistoryRepository, times(1)).unmarkAllAsPrimaryByMemberId(memberId);
        verify(addressHistoryRepository, times(1)).save(any(AddressHistory.class));
    }
    
    @Test
    @DisplayName("주소 수정 - 성공")
    void updateAddress_Success() {
        // given
        Long memberId = 1L;
        Long addressHistoryId = 1L;
        Address oldAddress = Address.of(
                "우리집", null, "서울특별시 강남구 테헤란로 123",
                "101동 1234호", 37.497942, 127.027621, "HOME"
        );
        AddressHistory addressHistory = AddressHistory.reconstitute(
                addressHistoryId, memberId, oldAddress, true, null
        );
        
        AddressServiceRequest request = new AddressServiceRequest(
                "우리집(수정)", null, "서울특별시 강남구 테헤란로 789",
                "202동 5678호", 37.497942, 127.027621, "HOME", true
        );
        
        given(addressHistoryRepository.findById(addressHistoryId))
                .willReturn(Optional.of(addressHistory));
        given(addressHistoryRepository.save(any(AddressHistory.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        
        // when
        AddressServiceResponse response = addressService.updateAddress(memberId, addressHistoryId, request);
        
        // then
        assertThat(response.getAddressAlias()).isEqualTo("우리집(수정)");
        assertThat(response.getStreetNameAddress()).isEqualTo("서울특별시 강남구 테헤란로 789");
        verify(addressHistoryRepository, times(1)).save(any(AddressHistory.class));
    }
    
    @Test
    @DisplayName("주소 수정 - 실패 (존재하지 않는 주소)")
    void updateAddress_Fail_NotFound() {
        // given
        Long memberId = 1L;
        Long addressHistoryId = 999L;
        AddressServiceRequest request = new AddressServiceRequest(
                "우리집(수정)", null, "서울특별시 강남구 테헤란로 789",
                "202동 5678호", 37.497942, 127.027621, "HOME", true
        );
        
        given(addressHistoryRepository.findById(addressHistoryId))
                .willReturn(Optional.empty());
        
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
        Long otherMemberId = 2L;
        Long addressHistoryId = 1L;
        Address address = Address.of(
                "우리집", null, "서울특별시 강남구 테헤란로 123",
                "101동 1234호", 37.497942, 127.027621, "HOME"
        );
        AddressHistory addressHistory = AddressHistory.reconstitute(
                addressHistoryId, otherMemberId, address, true, null
        );
        
        AddressServiceRequest request = new AddressServiceRequest(
                "우리집(수정)", null, "서울특별시 강남구 테헤란로 789",
                "202동 5678호", 37.497942, 127.027621, "HOME", true
        );
        
        given(addressHistoryRepository.findById(addressHistoryId))
                .willReturn(Optional.of(addressHistory));
        
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
        Address address = Address.of(
                "회사", null, "서울특별시 강남구 테헤란로 456",
                "5층", 37.497942, 127.027621, "OFFICE"
        );
        AddressHistory addressHistory = AddressHistory.reconstitute(
                addressHistoryId, memberId, address, false, null
        );
        
        given(addressHistoryRepository.findById(addressHistoryId))
                .willReturn(Optional.of(addressHistory));
        // 기본 주소가 아니므로 countByMemberId는 호출되지 않음
        
        // when
        addressService.deleteAddress(memberId, addressHistoryId);
        
        // then
        verify(addressHistoryRepository, times(1)).delete(addressHistory);
    }
    
    @Test
    @DisplayName("주소 삭제 - 실패 (기본 주소는 다른 주소가 있을 때만 삭제 가능)")
    void deleteAddress_Fail_CannotDeleteLastPrimary() {
        // given
        Long memberId = 1L;
        Long addressHistoryId = 1L;
        Address address = Address.of(
                "우리집", null, "서울특별시 강남구 테헤란로 123",
                "101동 1234호", 37.497942, 127.027621, "HOME"
        );
        AddressHistory addressHistory = AddressHistory.reconstitute(
                addressHistoryId, memberId, address, true, null
        );
        
        given(addressHistoryRepository.findById(addressHistoryId))
                .willReturn(Optional.of(addressHistory));
        given(addressHistoryRepository.countByMemberId(memberId)).willReturn(1L);
        
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
        AddressHistory addressHistory = AddressHistory.reconstitute(
                addressHistoryId, memberId, address, false, null
        );
        
        given(addressHistoryRepository.findById(addressHistoryId))
                .willReturn(Optional.of(addressHistory));
        given(addressHistoryRepository.save(any(AddressHistory.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        
        // when
        AddressServiceResponse response = addressService.setPrimaryAddress(memberId, addressHistoryId);
        
        // then
        assertThat(response.getIsPrimary()).isTrue();
        verify(addressHistoryRepository, times(1)).unmarkAllAsPrimaryByMemberId(memberId);
        verify(addressHistoryRepository, times(1)).save(any(AddressHistory.class));
    }
    
    @Test
    @DisplayName("기본 주소 설정 - 실패 (존재하지 않는 주소)")
    void setPrimaryAddress_Fail_NotFound() {
        // given
        Long memberId = 1L;
        Long addressHistoryId = 999L;
        
        given(addressHistoryRepository.findById(addressHistoryId))
                .willReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> addressService.setPrimaryAddress(memberId, addressHistoryId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.ADDRESS_NOT_FOUND);
    }
}

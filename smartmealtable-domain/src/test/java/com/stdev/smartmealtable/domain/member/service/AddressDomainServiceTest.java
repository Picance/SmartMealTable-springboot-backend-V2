package com.stdev.smartmealtable.domain.member.service;

import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.member.entity.AddressHistory;
import com.stdev.smartmealtable.domain.member.repository.AddressHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

/**
 * AddressDomainService 단위 테스트 (BDD Mockist 스타일)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AddressDomainService 테스트")
class AddressDomainServiceTest {

    @Mock
    private AddressHistoryRepository addressHistoryRepository;

    @InjectMocks
    private AddressDomainService addressDomainService;

    @Nested
    @DisplayName("countAddresses 메서드는")
    class Describe_countAddresses {

        @Test
        @DisplayName("회원의 주소 개수를 반환한다")
        void it_returns_address_count() {
            // Given
            Long memberId = 1L;
            given(addressHistoryRepository.countByMemberId(memberId)).willReturn(3L);

            // When
            long count = addressDomainService.countAddresses(memberId);

            // Then
            assertThat(count).isEqualTo(3L);
            then(addressHistoryRepository).should(times(1)).countByMemberId(memberId);
        }
    }

    @Nested
    @DisplayName("addAddress 메서드는")
    class Describe_addAddress {

        @Nested
        @DisplayName("첫 번째 주소를 추가하는 경우")
        class Context_with_first_address {

            @Test
            @DisplayName("자동으로 기본 주소로 설정한다")
            void it_sets_as_primary_automatically() {
                // Given
                Long memberId = 1L;
                Address address = Address.of("우리집", "서울시 강남구 123", "서울시 강남구 테헤란로 123", "101호", 37.123, 127.456, "HOME");

                given(addressHistoryRepository.countByMemberId(memberId)).willReturn(0L);
                given(addressHistoryRepository.save(any(AddressHistory.class)))
                        .willAnswer(invocation -> invocation.getArgument(0));

                // When
                AddressHistory result = addressDomainService.addAddress(memberId, address, false);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getIsPrimary()).isTrue();
                then(addressHistoryRepository).should(times(1)).countByMemberId(memberId);
                then(addressHistoryRepository).should(times(1)).save(any(AddressHistory.class));
            }
        }

        @Nested
        @DisplayName("기본 주소로 지정하여 추가하는 경우")
        class Context_with_primary_flag {

            @Test
            @DisplayName("기존 기본 주소를 해제하고 새 주소를 기본으로 설정한다")
            void it_unmarks_existing_and_sets_new_as_primary() {
                // Given
                Long memberId = 1L;
                Address address = Address.of("회사", "서울시 서초구 456", "서울시 서초구 강남대로 456", "5층", 37.456, 127.789, "OFFICE");

                given(addressHistoryRepository.countByMemberId(memberId)).willReturn(2L);
                given(addressHistoryRepository.save(any(AddressHistory.class)))
                        .willAnswer(invocation -> invocation.getArgument(0));

                // When
                AddressHistory result = addressDomainService.addAddress(memberId, address, true);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getIsPrimary()).isTrue();
                then(addressHistoryRepository).should(times(1)).countByMemberId(memberId);
                then(addressHistoryRepository).should(times(1)).unmarkAllAsPrimaryByMemberId(memberId);
                then(addressHistoryRepository).should(times(1)).save(any(AddressHistory.class));
            }
        }

        @Nested
        @DisplayName("일반 주소로 추가하는 경우")
        class Context_with_non_primary_flag {

            @Test
            @DisplayName("기본 주소가 아닌 주소로 추가한다")
            void it_adds_as_non_primary() {
                // Given
                Long memberId = 1L;
                Address address = Address.of("할머니댁", "경기도 성남시 789", "경기도 성남시 분당구 123", null, null, null, "OTHER");

                given(addressHistoryRepository.countByMemberId(memberId)).willReturn(1L);
                given(addressHistoryRepository.save(any(AddressHistory.class)))
                        .willAnswer(invocation -> invocation.getArgument(0));

                // When
                AddressHistory result = addressDomainService.addAddress(memberId, address, false);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getIsPrimary()).isFalse();
                then(addressHistoryRepository).should(times(1)).countByMemberId(memberId);
                then(addressHistoryRepository).should(times(0)).unmarkAllAsPrimaryByMemberId(any());
                then(addressHistoryRepository).should(times(1)).save(any(AddressHistory.class));
            }
        }
    }

    @Nested
    @DisplayName("updateAddress 메서드는")
    class Describe_updateAddress {

        @Nested
        @DisplayName("유효한 주소 ID와 정보가 주어지면")
        class Context_with_valid_info {

            @Test
            @DisplayName("주소를 수정한다")
            void it_updates_address() {
                // Given
                Long memberId = 1L;
                Long addressHistoryId = 1L;
                Address newAddress = Address.of("우리집", "서울시 종로구 999", "서울시 종로구 세종대로 999", "202호", 37.999, 127.999, "HOME");

                AddressHistory existingAddress = AddressHistory.reconstitute(
                        addressHistoryId,
                        memberId,
                        Address.of("우리집", "서울시 강남구 123", "서울시 강남구 테헤란로 123", "101호", 37.123, 127.456, "HOME"),
                        false,
                        LocalDateTime.now()
                );

                given(addressHistoryRepository.findById(addressHistoryId))
                        .willReturn(Optional.of(existingAddress));
                given(addressHistoryRepository.save(any(AddressHistory.class)))
                        .willAnswer(invocation -> invocation.getArgument(0));

                // When
                AddressHistory result = addressDomainService.updateAddress(
                        memberId, addressHistoryId, newAddress, false
                );

                // Then
                assertThat(result).isNotNull();
                then(addressHistoryRepository).should(times(1)).findById(addressHistoryId);
                then(addressHistoryRepository).should(times(1)).save(any(AddressHistory.class));
            }
        }

        @Nested
        @DisplayName("기본 주소로 변경하는 경우")
        class Context_with_primary_flag {

            @Test
            @DisplayName("기존 기본 주소를 해제하고 새로운 기본 주소로 설정한다")
            void it_updates_to_primary() {
                // Given
                Long memberId = 1L;
                Long addressHistoryId = 2L;
                Address newAddress = Address.of("우리집", "서울시 종로구 999", "서울시 종로구 세종대로 999", "202호", 37.999, 127.999, "HOME");

                AddressHistory existingAddress = AddressHistory.reconstitute(
                        addressHistoryId,
                        memberId,
                        Address.of("회사", "서울시 강남구 123", "서울시 강남구 테헤란로 123", "101호", 37.123, 127.456, "OFFICE"),
                        false,
                        LocalDateTime.now()
                );

                given(addressHistoryRepository.findById(addressHistoryId))
                        .willReturn(Optional.of(existingAddress));
                given(addressHistoryRepository.save(any(AddressHistory.class)))
                        .willAnswer(invocation -> invocation.getArgument(0));

                // When
                AddressHistory result = addressDomainService.updateAddress(
                        memberId, addressHistoryId, newAddress, true
                );

                // Then
                assertThat(result).isNotNull();
                then(addressHistoryRepository).should(times(1)).findById(addressHistoryId);
                then(addressHistoryRepository).should(times(1)).unmarkAllAsPrimaryByMemberId(memberId);
                then(addressHistoryRepository).should(times(1)).save(any(AddressHistory.class));
            }
        }

        @Nested
        @DisplayName("본인의 주소가 아닌 경우")
        class Context_with_forbidden_access {

            @Test
            @DisplayName("FORBIDDEN_ACCESS 예외를 발생시킨다")
            void it_throws_forbidden_access_exception() {
                // Given
                Long memberId = 1L;
                Long addressHistoryId = 1L;
                Address newAddress = Address.of("우리집", "서울시 종로구 999", "서울시 종로구 세종대로 999", "202호", 37.999, 127.999, "HOME");

                AddressHistory otherMemberAddress = AddressHistory.reconstitute(
                        addressHistoryId,
                        2L,  // 다른 회원의 주소
                        Address.of("회사", "서울시 강남구 123", "서울시 강남구 테헤란로 123", "101호", 37.123, 127.456, "OFFICE"),
                        false,
                        LocalDateTime.now()
                );

                given(addressHistoryRepository.findById(addressHistoryId))
                        .willReturn(Optional.of(otherMemberAddress));

                // When & Then
                assertThatThrownBy(() -> addressDomainService.updateAddress(
                        memberId, addressHistoryId, newAddress, false
                ))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorType", ErrorType.FORBIDDEN_ACCESS);

                then(addressHistoryRepository).should(times(1)).findById(addressHistoryId);
                then(addressHistoryRepository).should(times(0)).save(any(AddressHistory.class));
            }
        }
    }

    @Nested
    @DisplayName("deleteAddress 메서드는")
    class Describe_deleteAddress {

        @Nested
        @DisplayName("일반 주소를 삭제하는 경우")
        class Context_with_non_primary_address {

            @Test
            @DisplayName("주소를 삭제한다")
            void it_deletes_address() {
                // Given
                Long memberId = 1L;
                Long addressHistoryId = 2L;

                AddressHistory nonPrimaryAddress = AddressHistory.reconstitute(
                        addressHistoryId,
                        memberId,
                        Address.of("회사", "서울시 강남구 123", "서울시 강남구 테헤란로 123", "101호", 37.123, 127.456, "OFFICE"),
                        false,
                        LocalDateTime.now()
                );

                given(addressHistoryRepository.findById(addressHistoryId))
                        .willReturn(Optional.of(nonPrimaryAddress));

                // When
                addressDomainService.deleteAddress(memberId, addressHistoryId);

                // Then
                then(addressHistoryRepository).should(times(1)).findById(addressHistoryId);
                then(addressHistoryRepository).should(times(1)).delete(nonPrimaryAddress);
            }
        }

        @Nested
        @DisplayName("기본 주소를 삭제하려는데 다른 주소가 있는 경우")
        class Context_with_primary_address_and_others_exist {

            @Test
            @DisplayName("주소를 삭제한다")
            void it_deletes_primary_address() {
                // Given
                Long memberId = 1L;
                Long addressHistoryId = 1L;

                AddressHistory primaryAddress = AddressHistory.reconstitute(
                        addressHistoryId,
                        memberId,
                        Address.of("우리집", "서울시 강남구 123", "서울시 강남구 테헤란로 123", "101호", 37.123, 127.456, "HOME"),
                        true,
                        LocalDateTime.now()
                );

                given(addressHistoryRepository.findById(addressHistoryId))
                        .willReturn(Optional.of(primaryAddress));
                given(addressHistoryRepository.countByMemberId(memberId)).willReturn(3L);

                // When
                addressDomainService.deleteAddress(memberId, addressHistoryId);

                // Then
                then(addressHistoryRepository).should(times(1)).findById(addressHistoryId);
                then(addressHistoryRepository).should(times(1)).countByMemberId(memberId);
                then(addressHistoryRepository).should(times(1)).delete(primaryAddress);
            }
        }

        @Nested
        @DisplayName("마지막 기본 주소를 삭제하려는 경우")
        class Context_with_last_primary_address {

            @Test
            @DisplayName("CANNOT_DELETE_LAST_PRIMARY_ADDRESS 예외를 발생시킨다")
            void it_throws_cannot_delete_exception() {
                // Given
                Long memberId = 1L;
                Long addressHistoryId = 1L;

                AddressHistory lastPrimaryAddress = AddressHistory.reconstitute(
                        addressHistoryId,
                        memberId,
                        Address.of("우리집", "서울시 강남구 123", "서울시 강남구 테헤란로 123", "101호", 37.123, 127.456, "HOME"),
                        true,
                        LocalDateTime.now()
                );

                given(addressHistoryRepository.findById(addressHistoryId))
                        .willReturn(Optional.of(lastPrimaryAddress));
                given(addressHistoryRepository.countByMemberId(memberId)).willReturn(1L);

                // When & Then
                assertThatThrownBy(() -> addressDomainService.deleteAddress(memberId, addressHistoryId))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorType", ErrorType.CANNOT_DELETE_LAST_PRIMARY_ADDRESS);

                then(addressHistoryRepository).should(times(1)).findById(addressHistoryId);
                then(addressHistoryRepository).should(times(1)).countByMemberId(memberId);
                then(addressHistoryRepository).should(times(0)).delete(any());
            }
        }
    }

    @Nested
    @DisplayName("setPrimaryAddress 메서드는")
    class Describe_setPrimaryAddress {

        @Nested
        @DisplayName("유효한 주소 ID가 주어지면")
        class Context_with_valid_address_id {

            @Test
            @DisplayName("기존 기본 주소를 해제하고 새로운 기본 주소를 설정한다")
            void it_sets_new_primary_address() {
                // Given
                Long memberId = 1L;
                Long addressHistoryId = 2L;

                AddressHistory targetAddress = AddressHistory.reconstitute(
                        addressHistoryId,
                        memberId,
                        Address.of("회사", "서울시 강남구 123", "서울시 강남구 테헤란로 123", "101호", 37.123, 127.456, "OFFICE"),
                        false,
                        LocalDateTime.now()
                );

                given(addressHistoryRepository.findById(addressHistoryId))
                        .willReturn(Optional.of(targetAddress));
                given(addressHistoryRepository.save(any(AddressHistory.class)))
                        .willAnswer(invocation -> invocation.getArgument(0));

                // When
                AddressHistory result = addressDomainService.setPrimaryAddress(memberId, addressHistoryId);

                // Then
                assertThat(result).isNotNull();
                then(addressHistoryRepository).should(times(1)).findById(addressHistoryId);
                then(addressHistoryRepository).should(times(1)).unmarkAllAsPrimaryByMemberId(memberId);
                then(addressHistoryRepository).should(times(1)).save(any(AddressHistory.class));
            }
        }

        @Nested
        @DisplayName("존재하지 않는 주소 ID가 주어지면")
        class Context_with_invalid_address_id {

            @Test
            @DisplayName("ADDRESS_NOT_FOUND 예외를 발생시킨다")
            void it_throws_address_not_found_exception() {
                // Given
                Long memberId = 1L;
                Long invalidAddressId = 999L;

                given(addressHistoryRepository.findById(invalidAddressId))
                        .willReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> addressDomainService.setPrimaryAddress(memberId, invalidAddressId))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorType", ErrorType.ADDRESS_NOT_FOUND);

                then(addressHistoryRepository).should(times(1)).findById(invalidAddressId);
                then(addressHistoryRepository).should(times(0)).unmarkAllAsPrimaryByMemberId(any());
                then(addressHistoryRepository).should(times(0)).save(any());
            }
        }
    }
}

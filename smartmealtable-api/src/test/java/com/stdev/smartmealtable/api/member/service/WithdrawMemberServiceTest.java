package com.stdev.smartmealtable.api.member.service;

import com.stdev.smartmealtable.api.member.service.dto.WithdrawMemberServiceRequest;
import com.stdev.smartmealtable.domain.member.service.MemberDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

/**
 * WithdrawMemberService 단위 테스트 (BDD Mockist 스타일)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WithdrawMemberService 테스트")
class WithdrawMemberServiceTest {

    @Mock
    private MemberDomainService memberDomainService;

    @InjectMocks
    private WithdrawMemberService withdrawMemberService;

    @Nested
    @DisplayName("withdrawMember 메서드는")
    class Describe_withdrawMember {

        @Nested
        @DisplayName("유효한 탈퇴 요청이 주어지면")
        class Context_with_valid_withdraw_request {

            @Test
            @DisplayName("MemberDomainService의 withdrawMember를 호출한다")
            void it_calls_member_domain_service() {
                // Given
                Long memberId = 1L;
                String password = "password123!";
                String reason = "서비스가 불만족스러워요";

                WithdrawMemberServiceRequest request = new WithdrawMemberServiceRequest(
                        memberId, password, reason
                );

                // When
                withdrawMemberService.withdrawMember(request);

                // Then
                then(memberDomainService).should(times(1))
                        .withdrawMember(memberId, password, reason);
            }
        }

        @Nested
        @DisplayName("탈퇴 사유가 없어도")
        class Context_with_no_reason {

            @Test
            @DisplayName("MemberDomainService의 withdrawMember를 호출한다")
            void it_calls_member_domain_service() {
                // Given
                Long memberId = 1L;
                String password = "password123!";
                String reason = null;

                WithdrawMemberServiceRequest request = new WithdrawMemberServiceRequest(
                        memberId, password, reason
                );

                // When
                withdrawMemberService.withdrawMember(request);

                // Then
                then(memberDomainService).should(times(1))
                        .withdrawMember(memberId, password, reason);
            }
        }
    }
}

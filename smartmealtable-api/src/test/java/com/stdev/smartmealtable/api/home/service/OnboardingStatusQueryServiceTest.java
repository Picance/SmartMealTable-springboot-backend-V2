package com.stdev.smartmealtable.api.home.service;

import com.stdev.smartmealtable.api.home.service.dto.OnboardingStatusServiceResponse;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.ResourceNotFoundException;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.member.repository.MonthlyBudgetConfirmationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

/**
 * OnboardingStatusQueryService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OnboardingStatusQueryService 단위 테스트")
class OnboardingStatusQueryServiceTest {

    @InjectMocks
    private OnboardingStatusQueryService onboardingStatusQueryService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MonthlyBudgetConfirmationRepository monthlyBudgetConfirmationRepository;

    @Test
    @DisplayName("온보딩 상태 조회 - 성공 (추천 유형 선택 완료, 이번 달 예산 확인 완료)")
    void getOnboardingStatus_Success_AllCompleted() {
        // given
        Long memberId = 1L;
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        String expectedMonth = String.format("%d-%02d", currentYear, currentMonth);

        Member member = Member.reconstitute(memberId, 1L, "testUser", null, RecommendationType.SAVER);
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        given(monthlyBudgetConfirmationRepository.existsByMemberIdAndYearAndMonth(memberId, currentYear, currentMonth))
                .willReturn(true);

        // when
        OnboardingStatusServiceResponse response = onboardingStatusQueryService.getOnboardingStatus(memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.isOnboardingComplete()).isTrue();
        assertThat(response.hasSelectedRecommendationType()).isTrue();
        assertThat(response.hasConfirmedMonthlyBudget()).isTrue();
        assertThat(response.currentMonth()).isEqualTo(expectedMonth);
        assertThat(response.showRecommendationTypeModal()).isFalse();  // 이미 선택함
        assertThat(response.showMonthlyBudgetModal()).isFalse();  // 이미 확인함

        then(memberRepository).should(times(1)).findById(memberId);
        then(monthlyBudgetConfirmationRepository).should(times(1))
                .existsByMemberIdAndYearAndMonth(memberId, currentYear, currentMonth);
    }

    @Test
    @DisplayName("온보딩 상태 조회 - 성공 (추천 유형 미선택, 이번 달 예산 확인 완료)")
    void getOnboardingStatus_Success_RecommendationTypeNotSelected() {
        // given
        Long memberId = 1L;
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        String expectedMonth = String.format("%d-%02d", currentYear, currentMonth);

        Member member = Member.reconstitute(memberId, 1L, "testUser", null, null);  // 추천 유형 미선택
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        given(monthlyBudgetConfirmationRepository.existsByMemberIdAndYearAndMonth(memberId, currentYear, currentMonth))
                .willReturn(true);

        // when
        OnboardingStatusServiceResponse response = onboardingStatusQueryService.getOnboardingStatus(memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.isOnboardingComplete()).isTrue();
        assertThat(response.hasSelectedRecommendationType()).isFalse();
        assertThat(response.hasConfirmedMonthlyBudget()).isTrue();
        assertThat(response.currentMonth()).isEqualTo(expectedMonth);
        assertThat(response.showRecommendationTypeModal()).isTrue();  // 추천 유형 모달 표시
        assertThat(response.showMonthlyBudgetModal()).isFalse();  // 예산 확인 완료
    }

    @Test
    @DisplayName("온보딩 상태 조회 - 성공 (추천 유형 선택 완료, 이번 달 예산 미확인)")
    void getOnboardingStatus_Success_MonthlyBudgetNotConfirmed() {
        // given
        Long memberId = 1L;
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        String expectedMonth = String.format("%d-%02d", currentYear, currentMonth);

        Member member = Member.reconstitute(memberId, 1L, "testUser", null, RecommendationType.ADVENTURER);
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        given(monthlyBudgetConfirmationRepository.existsByMemberIdAndYearAndMonth(memberId, currentYear, currentMonth))
                .willReturn(false);  // 이번 달 예산 미확인

        // when
        OnboardingStatusServiceResponse response = onboardingStatusQueryService.getOnboardingStatus(memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.isOnboardingComplete()).isTrue();
        assertThat(response.hasSelectedRecommendationType()).isTrue();
        assertThat(response.hasConfirmedMonthlyBudget()).isFalse();
        assertThat(response.currentMonth()).isEqualTo(expectedMonth);
        assertThat(response.showRecommendationTypeModal()).isFalse();  // 추천 유형 선택 완료
        assertThat(response.showMonthlyBudgetModal()).isTrue();  // 예산 확인 모달 표시
    }

    @Test
    @DisplayName("온보딩 상태 조회 - 성공 (추천 유형 미선택, 이번 달 예산 미확인)")
    void getOnboardingStatus_Success_BothNotCompleted() {
        // given
        Long memberId = 1L;
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        String expectedMonth = String.format("%d-%02d", currentYear, currentMonth);

        Member member = Member.reconstitute(memberId, 1L, "testUser", null, null);  // 추천 유형 미선택
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        given(monthlyBudgetConfirmationRepository.existsByMemberIdAndYearAndMonth(memberId, currentYear, currentMonth))
                .willReturn(false);  // 이번 달 예산 미확인

        // when
        OnboardingStatusServiceResponse response = onboardingStatusQueryService.getOnboardingStatus(memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.isOnboardingComplete()).isTrue();
        assertThat(response.hasSelectedRecommendationType()).isFalse();
        assertThat(response.hasConfirmedMonthlyBudget()).isFalse();
        assertThat(response.currentMonth()).isEqualTo(expectedMonth);
        assertThat(response.showRecommendationTypeModal()).isTrue();  // 두 모달 모두 표시
        assertThat(response.showMonthlyBudgetModal()).isTrue();
    }

    @Test
    @DisplayName("온보딩 상태 조회 - 실패 (회원 없음)")
    void getOnboardingStatus_Fail_MemberNotFound() {
        // given
        Long memberId = 999L;

        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> onboardingStatusQueryService.getOnboardingStatus(memberId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ErrorType.MEMBER_NOT_FOUND.getMessage());

        then(memberRepository).should(times(1)).findById(memberId);
        then(monthlyBudgetConfirmationRepository).should(never())
                .existsByMemberIdAndYearAndMonth(anyLong(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("온보딩 상태 조회 - 추천 유형별 (PREFERENCE_BASED)")
    void getOnboardingStatus_WithDifferentRecommendationType_PreferenceBased() {
        // given
        Long memberId = 1L;
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        Member member = Member.reconstitute(memberId, 1L, "testUser", null, RecommendationType.SAVER);
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        given(monthlyBudgetConfirmationRepository.existsByMemberIdAndYearAndMonth(memberId, currentYear, currentMonth))
                .willReturn(true);

        // when
        OnboardingStatusServiceResponse response = onboardingStatusQueryService.getOnboardingStatus(memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.hasSelectedRecommendationType()).isTrue();
        assertThat(response.showRecommendationTypeModal()).isFalse();
    }

    @Test
    @DisplayName("온보딩 상태 조회 - 추천 유형별 (BUDGET_BASED)")
    void getOnboardingStatus_WithDifferentRecommendationType_BudgetBased() {
        // given
        Long memberId = 1L;
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        Member member = Member.reconstitute(memberId, 1L, "testUser", null, RecommendationType.ADVENTURER);
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        given(monthlyBudgetConfirmationRepository.existsByMemberIdAndYearAndMonth(memberId, currentYear, currentMonth))
                .willReturn(true);

        // when
        OnboardingStatusServiceResponse response = onboardingStatusQueryService.getOnboardingStatus(memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.hasSelectedRecommendationType()).isTrue();
        assertThat(response.showRecommendationTypeModal()).isFalse();
    }

    @Test
    @DisplayName("온보딩 상태 조회 - 현재 연월 포맷 검증")
    void getOnboardingStatus_CurrentMonthFormatValidation() {
        // given
        Long memberId = 1L;
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        String expectedMonth = String.format("%d-%02d", currentYear, currentMonth);

        Member member = Member.reconstitute(memberId, 1L, "testUser", null, RecommendationType.SAVER);
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        given(monthlyBudgetConfirmationRepository.existsByMemberIdAndYearAndMonth(memberId, currentYear, currentMonth))
                .willReturn(true);

        // when
        OnboardingStatusServiceResponse response = onboardingStatusQueryService.getOnboardingStatus(memberId);

        // then
        assertThat(response.currentMonth()).isEqualTo(expectedMonth);
        assertThat(response.currentMonth()).matches("\\d{4}-\\d{2}");  // YYYY-MM 형식 검증
    }
}

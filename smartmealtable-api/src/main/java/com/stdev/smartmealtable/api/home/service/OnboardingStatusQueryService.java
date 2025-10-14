package com.stdev.smartmealtable.api.home.service;

import com.stdev.smartmealtable.api.home.service.dto.OnboardingStatusServiceResponse;
import com.stdev.smartmealtable.core.exception.ResourceNotFoundException;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.member.repository.MonthlyBudgetConfirmationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 온보딩 상태 조회 서비스
 * 온보딩 완료 여부 및 모달 표시 여부를 확인
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OnboardingStatusQueryService {

    private final MemberRepository memberRepository;
    private final MonthlyBudgetConfirmationRepository monthlyBudgetConfirmationRepository;

    /**
     * 온보딩 상태 조회
     *
     * @param memberId 회원 ID
     * @return 온보딩 상태 정보
     */
    public OnboardingStatusServiceResponse getOnboardingStatus(Long memberId) {
        // 1. 회원 정보 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorType.MEMBER_NOT_FOUND));

        // 2. 추천 유형 선택 여부 확인
        boolean hasSelectedRecommendationType = member.getRecommendationType() != null;

        // 3. 이번 달 예산 확인 여부 확인
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        
        boolean hasConfirmedMonthlyBudget = monthlyBudgetConfirmationRepository
                .existsByMemberIdAndYearAndMonth(memberId, currentYear, currentMonth);

        // 4. 모달 표시 여부 결정
        // - 추천 유형 모달: 온보딩 완료 + 추천 유형 미선택 시 표시
        boolean showRecommendationTypeModal = !hasSelectedRecommendationType;
        
        // - 월별 예산 모달: 이번 달 예산 미확인 시 표시
        boolean showMonthlyBudgetModal = !hasConfirmedMonthlyBudget;

        // 5. 응답 생성
        return OnboardingStatusServiceResponse.of(
                true, // 온보딩은 완료된 상태 (이 API를 호출한다는 것 자체가 로그인 완료 의미)
                hasSelectedRecommendationType,
                hasConfirmedMonthlyBudget,
                String.format("%d-%02d", currentYear, currentMonth),
                showRecommendationTypeModal,
                showMonthlyBudgetModal
        );
    }
}

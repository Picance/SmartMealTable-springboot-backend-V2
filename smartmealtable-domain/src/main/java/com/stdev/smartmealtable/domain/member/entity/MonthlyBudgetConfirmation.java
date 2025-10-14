package com.stdev.smartmealtable.domain.member.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 월별 예산 확인 이력 도메인 엔티티
 * 사용자가 매월 초 예산 확인 모달에서 선택한 액션을 기록
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonthlyBudgetConfirmation {

    private Long monthlyBudgetConfirmationId;
    private Long memberId;  // 논리 FK
    private Integer year;
    private Integer month;
    private BudgetConfirmAction action;
    private LocalDateTime confirmedAt;

    /**
     * 생성 팩토리 메서드
     */
    public static MonthlyBudgetConfirmation create(
            Long memberId,
            Integer year,
            Integer month,
            BudgetConfirmAction action
    ) {
        validateInputs(memberId, year, month, action);
        
        MonthlyBudgetConfirmation confirmation = new MonthlyBudgetConfirmation();
        confirmation.memberId = memberId;
        confirmation.year = year;
        confirmation.month = month;
        confirmation.action = action;
        confirmation.confirmedAt = LocalDateTime.now();
        return confirmation;
    }

    /**
     * 재구성 팩토리 메서드 (Storage에서 사용)
     */
    public static MonthlyBudgetConfirmation reconstitute(
            Long monthlyBudgetConfirmationId,
            Long memberId,
            Integer year,
            Integer month,
            BudgetConfirmAction action,
            LocalDateTime confirmedAt
    ) {
        MonthlyBudgetConfirmation confirmation = new MonthlyBudgetConfirmation();
        confirmation.monthlyBudgetConfirmationId = monthlyBudgetConfirmationId;
        confirmation.memberId = memberId;
        confirmation.year = year;
        confirmation.month = month;
        confirmation.action = action;
        confirmation.confirmedAt = confirmedAt;
        return confirmation;
    }

    /**
     * 입력값 검증
     */
    private static void validateInputs(Long memberId, Integer year, Integer month, BudgetConfirmAction action) {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("memberId는 필수이며 양수여야 합니다.");
        }
        if (year == null || year < 2000 || year > 9999) {
            throw new IllegalArgumentException("year는 2000~9999 범위여야 합니다.");
        }
        if (month == null || month < 1 || month > 12) {
            throw new IllegalArgumentException("month는 1~12 범위여야 합니다.");
        }
        if (action == null) {
            throw new IllegalArgumentException("action은 필수입니다.");
        }
    }

    /**
     * 월별 예산을 변경하러 가기로 선택했는지 확인
     */
    public boolean isChangeAction() {
        return this.action == BudgetConfirmAction.CHANGE;
    }

    /**
     * 월별 예산을 유지하기로 선택했는지 확인
     */
    public boolean isKeepAction() {
        return this.action == BudgetConfirmAction.KEEP;
    }
}

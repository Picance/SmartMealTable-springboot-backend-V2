package com.stdev.smartmealtable.domain.member.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 월별 예산 확인 액션 Enum
 * 사용자가 매월 초 예산 확인 모달에서 선택할 수 있는 액션
 */
@Getter
@RequiredArgsConstructor
public enum BudgetConfirmAction {
    
    /**
     * 기존 예산 유지
     */
    KEEP("기존 예산 유지"),
    
    /**
     * 예산 변경하러 가기
     */
    CHANGE("예산 변경하러 가기");
    
    private final String description;
}

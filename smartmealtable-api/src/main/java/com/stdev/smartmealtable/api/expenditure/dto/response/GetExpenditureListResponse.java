package com.stdev.smartmealtable.api.expenditure.dto.response;

import com.stdev.smartmealtable.api.expenditure.service.dto.ExpenditureListServiceResponse;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

/**
 * 지출 내역 목록 조회 Response DTO
 */
public record GetExpenditureListResponse(
        Summary summary,
        Page<ExpenditureInfo> expenditures
) {
    
    /**
     * Service Response → Presentation Response 변환
     */
    public static GetExpenditureListResponse from(ExpenditureListServiceResponse serviceResponse) {
        Summary summary = new Summary(
                serviceResponse.summary().totalAmount(),
                serviceResponse.summary().totalCount(),
                serviceResponse.summary().averageAmount()
        );
        
        Page<ExpenditureInfo> expenditures = serviceResponse.expenditures()
                .map(info -> new ExpenditureInfo(
                        info.expenditureId(),
                        info.storeName(),
                        info.amount(),
                        info.expendedDate(),
                        info.categoryName(),
                        info.mealType()
                ));
        
        return new GetExpenditureListResponse(summary, expenditures);
    }
    
    /**
     * 지출 내역 요약 정보
     */
    public record Summary(
            Integer totalAmount,
            Integer totalCount,
            Integer averageAmount
    ) {
    }
    
    /**
     * 개별 지출 내역 정보
     */
    public record ExpenditureInfo(
            Long expenditureId,
            String storeName,
            Integer amount,
            LocalDate expendedDate,
            String categoryName,
            MealType mealType
    ) {
    }
}

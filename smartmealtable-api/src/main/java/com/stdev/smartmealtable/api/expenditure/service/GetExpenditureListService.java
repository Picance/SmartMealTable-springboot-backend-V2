package com.stdev.smartmealtable.api.expenditure.service;

import com.stdev.smartmealtable.api.expenditure.service.dto.ExpenditureListServiceResponse;
import com.stdev.smartmealtable.api.expenditure.service.dto.ExpenditureSummaryServiceResponse;
import com.stdev.smartmealtable.domain.expenditure.Expenditure;
import com.stdev.smartmealtable.domain.expenditure.ExpenditureRepository;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 지출 내역 목록 조회 Application Service
 * - 유즈케이스: 사용자의 지출 내역을 필터링하여 페이징된 목록으로 조회
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetExpenditureListService {
    
    private final ExpenditureRepository expenditureRepository;
    
    /**
     * 지출 내역 목록 조회
     * 
     * @param memberId 회원 ID
     * @param startDate 조회 시작 날짜
     * @param endDate 조회 종료 날짜
     * @param mealType 식사 유형 필터 (optional)
     * @param categoryId 카테고리 ID 필터 (optional)
     * @param pageable 페이징 정보
     * @return 지출 내역 목록 및 요약 정보
     */
    public ExpenditureListServiceResponse getExpenditureList(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate,
            MealType mealType,
            Long categoryId,
            Pageable pageable
    ) {
        // 1. Repository에서 지출 내역 조회 (삭제되지 않은 것만)
        List<Expenditure> expenditures = expenditureRepository.findByMemberIdAndDateRange(
                memberId,
                startDate,
                endDate
        );
        
        // 2. 필터 적용 (식사 유형, 카테고리)
        List<Expenditure> filteredExpenditures = expenditures.stream()
                .filter(e -> !e.getDeleted())
                .filter(e -> mealType == null || e.getMealType() == mealType)
                .filter(e -> categoryId == null || e.getCategoryId().equals(categoryId))
                .collect(Collectors.toList());
        
        // 3. 요약 정보 계산
        ExpenditureSummaryServiceResponse summary = calculateSummary(filteredExpenditures);
        
        // 4. 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredExpenditures.size());
        List<Expenditure> pagedExpenditures = filteredExpenditures.subList(start, end);
        
        // 5. DTO 변환
        List<ExpenditureListServiceResponse.ExpenditureInfo> expenditureInfos = pagedExpenditures.stream()
                .map(this::toExpenditureInfo)
                .collect(Collectors.toList());
        
        Page<ExpenditureListServiceResponse.ExpenditureInfo> expenditurePage = 
                new PageImpl<>(expenditureInfos, pageable, filteredExpenditures.size());
        
        return new ExpenditureListServiceResponse(summary, expenditurePage);
    }
    
    /**
     * 요약 정보 계산
     */
    private ExpenditureSummaryServiceResponse calculateSummary(List<Expenditure> expenditures) {
        int totalAmount = expenditures.stream()
                .mapToInt(Expenditure::getAmount)
                .sum();
        
        int totalCount = expenditures.size();
        
        int averageAmount = totalCount > 0 ? totalAmount / totalCount : 0;
        
        return new ExpenditureSummaryServiceResponse(
                totalAmount,
                totalCount,
                averageAmount
        );
    }
    
    /**
     * Expenditure 도메인 → ExpenditureInfo DTO 변환
     * TODO: 카테고리명은 현재 별도 조회가 필요한데, 추후 카테고리 조회 로직 추가 필요
     */
    private ExpenditureListServiceResponse.ExpenditureInfo toExpenditureInfo(Expenditure expenditure) {
        return new ExpenditureListServiceResponse.ExpenditureInfo(
                expenditure.getExpenditureId(),
                expenditure.getStoreName(),
                expenditure.getAmount(),
                expenditure.getExpendedDate(),
                "카테고리명",  // TODO: 실제 카테고리명 조회
                expenditure.getMealType()
        );
    }
}

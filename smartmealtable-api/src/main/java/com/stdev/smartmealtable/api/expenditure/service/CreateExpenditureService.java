package com.stdev.smartmealtable.api.expenditure.service;

import com.stdev.smartmealtable.api.expenditure.service.dto.CreateExpenditureServiceRequest;
import com.stdev.smartmealtable.api.expenditure.service.dto.CreateExpenditureServiceResponse;
import com.stdev.smartmealtable.domain.expenditure.service.ExpenditureDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 지출 내역 등록 Application Service
 * 유즈케이스 orchestration에 집중
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreateExpenditureService {
    
    private final ExpenditureDomainService expenditureDomainService;
    
    /**
     * 지출 내역 등록
     * Domain Service를 통한 비즈니스 로직 처리
     */
    @Transactional
    public CreateExpenditureServiceResponse createExpenditure(CreateExpenditureServiceRequest request) {
        // 지출 항목 DTO 변환
        List<ExpenditureDomainService.ExpenditureItemRequest> itemRequests = request.items() != null
                ? request.items().stream()
                .map(itemReq -> new ExpenditureDomainService.ExpenditureItemRequest(
                        itemReq.foodName(),
                        itemReq.quantity(),
                        itemReq.price()
                ))
                .collect(Collectors.toList())
                : List.of();

        // Domain Service를 통한 지출 생성 (검증 + 도메인 로직 포함)
        ExpenditureDomainService.ExpenditureCreationResult result = expenditureDomainService.createExpenditure(
                request.memberId(),
                request.storeName(),
                request.amount(),
                request.expendedDate(),
                request.expendedTime(),
                request.categoryId(),
                request.mealType(),
                request.memo(),
                itemRequests
        );
        
        // 응답 생성
        return CreateExpenditureServiceResponse.from(result.expenditure(), result.categoryName());
    }
}

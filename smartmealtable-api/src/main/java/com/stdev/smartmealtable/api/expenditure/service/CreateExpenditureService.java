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
        // storeId 여부에 따라 다른 Domain Service 메서드 호출
        if (request.storeId() != null) {
            // 장바구니 시나리오: storeId + foodId 포함
            List<ExpenditureDomainService.CartExpenditureItemRequest> cartItems = request.items() != null
                    ? request.items().stream()
                    .map(itemReq -> new ExpenditureDomainService.CartExpenditureItemRequest(
                            itemReq.foodId(),
                            itemReq.foodName(),  // foodName도 함께 전달
                            itemReq.quantity(),
                            itemReq.price()
                    ))
                    .collect(Collectors.toList())
                    : List.of();

            ExpenditureDomainService.ExpenditureCreationResult result = expenditureDomainService.createExpenditureFromCart(
                    request.memberId(),
                    request.storeId(),
                    request.storeName(),
                    request.amount(),
                    request.expendedDate(),
                    request.expendedTime(),
                    request.categoryId(),
                    request.mealType(),
                    request.memo(),
                    request.discount(),  // 할인액 전달
                    cartItems
            );
            
            return CreateExpenditureServiceResponse.from(result.expenditure(), result.categoryName());
        } else {
            // 수기 입력 시나리오: storeId 없음
            List<ExpenditureDomainService.ManualExpenditureItemRequest> itemRequests = request.items() != null
                    ? request.items().stream()
                    .map(itemReq -> new ExpenditureDomainService.ManualExpenditureItemRequest(
                            itemReq.foodName(),
                            itemReq.quantity(),
                            itemReq.price()
                    ))
                    .collect(Collectors.toList())
                    : List.of();

            ExpenditureDomainService.ExpenditureCreationResult result = expenditureDomainService.createExpenditureFromManualInput(
                    request.memberId(),
                    request.storeName(),
                    request.amount(),
                    request.expendedDate(),
                    request.expendedTime(),
                    request.categoryId(),
                    request.mealType(),
                    request.memo(),
                    request.discount(),  // 할인액 전달
                    itemRequests
            );

            return CreateExpenditureServiceResponse.from(result.expenditure(), result.categoryName());
        }
    }
}

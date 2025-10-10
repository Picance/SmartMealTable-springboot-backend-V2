package com.stdev.smartmealtable.api.expenditure.controller;

import com.stdev.smartmealtable.core.auth.AuthUser;
import com.stdev.smartmealtable.core.auth.AuthenticatedUser;
import com.stdev.smartmealtable.api.expenditure.dto.request.CreateExpenditureRequest;
import com.stdev.smartmealtable.api.expenditure.dto.response.CreateExpenditureResponse;
import com.stdev.smartmealtable.api.expenditure.service.CreateExpenditureService;
import com.stdev.smartmealtable.api.expenditure.service.dto.CreateExpenditureServiceRequest;
import com.stdev.smartmealtable.api.expenditure.service.dto.CreateExpenditureServiceResponse;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 지출 내역 Controller
 */
@RestController
@RequestMapping("/api/v1/expenditures")
@RequiredArgsConstructor
public class ExpenditureController {
    
    private final CreateExpenditureService createExpenditureService;
    
    /**
     * 지출 내역 등록
     * POST /api/v1/expenditures
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateExpenditureResponse> createExpenditure(
            @AuthUser AuthenticatedUser user,
            @Valid @RequestBody CreateExpenditureRequest request
    ) {
        // DTO 변환
        CreateExpenditureServiceRequest serviceRequest = convertToServiceRequest(request, user.memberId());
        
        // Service 호출
        CreateExpenditureServiceResponse serviceResponse = createExpenditureService.createExpenditure(serviceRequest);
        
        // Response 변환
        CreateExpenditureResponse response = CreateExpenditureResponse.from(serviceResponse);
        
        return ApiResponse.success(response);
    }
    
    /**
     * Request DTO → Service Request DTO 변환
     */
    private CreateExpenditureServiceRequest convertToServiceRequest(
            CreateExpenditureRequest request,
            Long memberId
    ) {
        List<CreateExpenditureServiceRequest.ExpenditureItemServiceRequest> items = 
                request.items() != null
                        ? request.items().stream()
                        .map(item -> new CreateExpenditureServiceRequest.ExpenditureItemServiceRequest(
                                item.foodName(),
                                item.quantity(),
                                item.price()
                        ))
                        .collect(Collectors.toList())
                        : null;
        
        return new CreateExpenditureServiceRequest(
                memberId,
                request.storeName(),
                request.amount(),
                request.expendedDate(),
                request.expendedTime(),
                request.categoryId(),
                request.mealType(),
                request.memo(),
                items
        );
    }
}

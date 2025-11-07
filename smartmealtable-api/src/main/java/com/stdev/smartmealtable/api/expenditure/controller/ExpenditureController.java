package com.stdev.smartmealtable.api.expenditure.controller;

import com.stdev.smartmealtable.core.auth.AuthUser;
import com.stdev.smartmealtable.core.auth.AuthenticatedUser;
import com.stdev.smartmealtable.api.expenditure.dto.request.CreateExpenditureRequest;
import com.stdev.smartmealtable.api.expenditure.dto.request.CreateExpenditureFromCartRequest;
import com.stdev.smartmealtable.api.expenditure.dto.request.ParseSmsRequest;
import com.stdev.smartmealtable.api.expenditure.dto.request.UpdateExpenditureRequest;
import com.stdev.smartmealtable.api.expenditure.dto.response.CreateExpenditureResponse;
import com.stdev.smartmealtable.api.expenditure.dto.response.GetExpenditureDetailResponse;
import com.stdev.smartmealtable.api.expenditure.dto.response.GetExpenditureListResponse;
import com.stdev.smartmealtable.api.expenditure.dto.response.GetExpenditureStatisticsResponse;
import com.stdev.smartmealtable.api.expenditure.dto.response.ParseSmsResponse;
import com.stdev.smartmealtable.api.expenditure.service.CreateExpenditureService;
import com.stdev.smartmealtable.api.expenditure.service.DeleteExpenditureService;
import com.stdev.smartmealtable.api.expenditure.service.GetExpenditureDetailService;
import com.stdev.smartmealtable.api.expenditure.service.GetExpenditureListService;
import com.stdev.smartmealtable.api.expenditure.service.GetExpenditureStatisticsService;
import com.stdev.smartmealtable.api.expenditure.service.ParseSmsService;
import com.stdev.smartmealtable.api.expenditure.service.UpdateExpenditureService;
import com.stdev.smartmealtable.api.expenditure.service.dto.CreateExpenditureServiceRequest;
import com.stdev.smartmealtable.api.expenditure.service.dto.CreateExpenditureServiceResponse;
import com.stdev.smartmealtable.api.expenditure.service.dto.ExpenditureDetailServiceResponse;
import com.stdev.smartmealtable.api.expenditure.service.dto.ExpenditureListServiceResponse;
import com.stdev.smartmealtable.api.expenditure.service.dto.ExpenditureStatisticsServiceResponse;
import com.stdev.smartmealtable.api.expenditure.service.dto.ParseSmsServiceRequest;
import com.stdev.smartmealtable.api.expenditure.service.dto.ParseSmsServiceResponse;
import com.stdev.smartmealtable.api.expenditure.service.dto.UpdateExpenditureServiceRequest;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    private final GetExpenditureListService getExpenditureListService;
    private final GetExpenditureDetailService getExpenditureDetailService;
    private final UpdateExpenditureService updateExpenditureService;
    private final DeleteExpenditureService deleteExpenditureService;
    private final ParseSmsService parseSmsService;
    private final GetExpenditureStatisticsService getExpenditureStatisticsService;
    
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
     * 장바구니에서 지출 내역 등록
     * POST /api/v1/expenditures/from-cart
     * - storeId와 foodId를 명시적으로 포함하여 상세 페이지 링크 제공
     */
    @PostMapping("/from-cart")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateExpenditureResponse> createExpenditureFromCart(
            @AuthUser AuthenticatedUser user,
            @Valid @RequestBody CreateExpenditureFromCartRequest request
    ) {
        // Validation: item total과 amount 일치 확인
        int itemTotal = request.items() != null
                ? request.items().stream()
                .mapToInt(item -> item.price() * item.quantity())
                .sum()
                : 0;
        
        if (itemTotal != request.amount()) {
            throw new IllegalArgumentException("항목들의 총액과 결제 금액이 일치하지 않습니다");
        }
        
        // DTO 변환
        CreateExpenditureServiceRequest serviceRequest = convertCartToServiceRequest(request, user.memberId());
        
        // Service 호출
        CreateExpenditureServiceResponse serviceResponse = createExpenditureService.createExpenditure(serviceRequest);
        
        // Response 변환
        CreateExpenditureResponse response = CreateExpenditureResponse.from(serviceResponse);
        
        return ApiResponse.success(response);
    }
    
    /**
     * SMS 파싱
     * POST /api/v1/expenditures/parse-sms
     */
    @PostMapping("/parse-sms")
    public ApiResponse<ParseSmsResponse> parseSms(
            @Valid @RequestBody ParseSmsRequest request
    ) {
        // DTO 변환
        ParseSmsServiceRequest serviceRequest = new ParseSmsServiceRequest(request.smsMessage());
        
        // Service 호출
        ParseSmsServiceResponse serviceResponse = parseSmsService.parseSms(serviceRequest);
        
        // Response 변환
        ParseSmsResponse response = ParseSmsResponse.from(serviceResponse);
        
        return ApiResponse.success(response);
    }
    
    /**
     * 지출 통계 조회
     * GET /api/v1/expenditures/statistics?startDate=2025-10-01&endDate=2025-10-31
     */
    @GetMapping("/statistics")
    public ApiResponse<GetExpenditureStatisticsResponse> getExpenditureStatistics(
            @AuthUser AuthenticatedUser user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // Service 호출
        ExpenditureStatisticsServiceResponse serviceResponse = getExpenditureStatisticsService.getStatistics(
                user.memberId(),
                startDate,
                endDate
        );
        
        // Response 변환
        GetExpenditureStatisticsResponse response = GetExpenditureStatisticsResponse.from(serviceResponse);
        
        return ApiResponse.success(response);
    }
    
    /**
     * 지출 내역 목록 조회
     * GET /api/v1/expenditures?startDate=2025-10-01&endDate=2025-10-31&mealType=LUNCH&categoryId=5&page=0&size=20
     */
    @GetMapping
    public ApiResponse<GetExpenditureListResponse> getExpenditureList(
            @AuthUser AuthenticatedUser user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) MealType mealType,
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        // Service 호출
        ExpenditureListServiceResponse serviceResponse = getExpenditureListService.getExpenditureList(
                user.memberId(),
                startDate,
                endDate,
                mealType,
                categoryId,
                pageable
        );
        
        // Response 변환
        GetExpenditureListResponse response = GetExpenditureListResponse.from(serviceResponse);
        
        return ApiResponse.success(response);
    }
    
    /**
     * 지출 내역 상세 조회
     * GET /api/v1/expenditures/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<GetExpenditureDetailResponse> getExpenditureDetail(
            @AuthUser AuthenticatedUser user,
            @PathVariable Long id
    ) {
        // Service 호출
        ExpenditureDetailServiceResponse serviceResponse = getExpenditureDetailService.getExpenditureDetail(
                id,
                user.memberId()
        );
        
        // Response 변환
        GetExpenditureDetailResponse response = GetExpenditureDetailResponse.from(serviceResponse);
        
        return ApiResponse.success(response);
    }
    
    /**
     * 지출 내역 수정
     * PUT /api/v1/expenditures/{id}
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> updateExpenditure(
            @AuthUser AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateExpenditureRequest request
    ) {
        // DTO 변환
        UpdateExpenditureServiceRequest serviceRequest = convertToUpdateServiceRequest(request);
        
        // Service 호출
        updateExpenditureService.updateExpenditure(id, user.memberId(), serviceRequest);
        
        return ApiResponse.success(null);
    }
    
    /**
     * 지출 내역 삭제
     * DELETE /api/v1/expenditures/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpenditure(
            @AuthUser AuthenticatedUser user,
            @PathVariable Long id
    ) {
        deleteExpenditureService.deleteExpenditure(id, user.memberId());
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
                                item.foodId(),
                                null,               // ◆ 수기 입력은 foodName = NULL
                                item.quantity(),
                                item.price()
                        ))
                        .collect(Collectors.toList())
                        : null;
        
        return new CreateExpenditureServiceRequest(
                memberId,
                null,                           // ◆ 수기 입력은 storeId = NULL
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
    
    /**
     * CartRequest DTO → Service Request DTO 변환
     */
    private CreateExpenditureServiceRequest convertCartToServiceRequest(
            CreateExpenditureFromCartRequest request,
            Long memberId
    ) {
        List<CreateExpenditureServiceRequest.ExpenditureItemServiceRequest> items = 
                request.items() != null
                        ? request.items().stream()
                        .map(item -> new CreateExpenditureServiceRequest.ExpenditureItemServiceRequest(
                                item.foodId(),
                                item.foodName(),    // ◆ 장바구니는 foodName 포함
                                item.quantity(),
                                item.price()
                        ))
                        .collect(Collectors.toList())
                        : null;
        
        return new CreateExpenditureServiceRequest(
                memberId,
                request.storeId(),              // ◆ 장바구니는 storeId 포함
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
    
    /**
     * UpdateExpenditureRequest DTO → UpdateExpenditureServiceRequest DTO 변환
     */
    private UpdateExpenditureServiceRequest convertToUpdateServiceRequest(UpdateExpenditureRequest request) {
        List<UpdateExpenditureServiceRequest.ExpenditureItemRequest> items = 
                request.items() != null
                        ? request.items().stream()
                        .map(item -> new UpdateExpenditureServiceRequest.ExpenditureItemRequest(
                                item.foodId(),
                                item.quantity(),
                                item.price()
                        ))
                        .collect(Collectors.toList())
                        : null;
        
        return new UpdateExpenditureServiceRequest(
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

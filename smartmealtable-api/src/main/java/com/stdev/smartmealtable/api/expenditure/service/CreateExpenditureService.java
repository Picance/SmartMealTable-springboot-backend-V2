package com.stdev.smartmealtable.api.expenditure.service;

import com.stdev.smartmealtable.api.expenditure.service.dto.CreateExpenditureServiceRequest;
import com.stdev.smartmealtable.api.expenditure.service.dto.CreateExpenditureServiceResponse;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.expenditure.Expenditure;
import com.stdev.smartmealtable.domain.expenditure.ExpenditureItem;
import com.stdev.smartmealtable.domain.expenditure.ExpenditureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 지출 내역 등록 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreateExpenditureService {
    
    private final ExpenditureRepository expenditureRepository;
    private final CategoryRepository categoryRepository;
    
    /**
     * 지출 내역 등록
     */
    @Transactional
    public CreateExpenditureServiceResponse createExpenditure(CreateExpenditureServiceRequest request) {
        // 1. 카테고리 검증 (categoryId가 있는 경우만)
        String categoryName = null;
        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new BusinessException(
                            ErrorType.CATEGORY_NOT_FOUND,
                            "카테고리를 찾을 수 없습니다. ID: " + request.categoryId()
                    ));
            categoryName = category.getName();
        }
        
        // 2. 지출 항목 도메인 객체 생성
        List<ExpenditureItem> items = request.items() != null
                ? request.items().stream()
                .map(itemReq -> ExpenditureItem.create(
                        itemReq.foodName(),
                        itemReq.quantity(),
                        itemReq.price()
                ))
                .collect(Collectors.toList())
                : List.of();
        
        // 3. 지출 내역 도메인 객체 생성 (도메인 로직에서 검증 수행)
        Expenditure expenditure = Expenditure.create(
                request.memberId(),
                request.storeName(),
                request.amount(),
                request.expendedDate(),
                request.expendedTime(),
                request.categoryId(),
                request.mealType(),
                request.memo(),
                items
        );
        
        // 4. 저장
        Expenditure saved = expenditureRepository.save(expenditure);
        
        // 5. 응답 생성
        return CreateExpenditureServiceResponse.from(saved, categoryName);
    }
}

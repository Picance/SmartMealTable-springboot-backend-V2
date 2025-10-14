package com.stdev.smartmealtable.api.expenditure.service;

import com.stdev.smartmealtable.api.expenditure.service.dto.ExpenditureDetailServiceResponse;
import com.stdev.smartmealtable.api.expenditure.service.dto.ExpenditureItemServiceResponse;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.AuthorizationException;
import com.stdev.smartmealtable.core.exception.ResourceNotFoundException;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.expenditure.Expenditure;
import com.stdev.smartmealtable.domain.expenditure.ExpenditureItem;
import com.stdev.smartmealtable.domain.expenditure.ExpenditureRepository;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 지출 내역 상세 조회 Application Service
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetExpenditureDetailService {

    private final ExpenditureRepository expenditureRepository;
    private final CategoryRepository categoryRepository;
    private final FoodRepository foodRepository;

    /**
     * 지출 내역 상세 조회
     *
     * @param expenditureId 지출 내역 ID
     * @param memberId      회원 ID
     * @return 지출 내역 상세 정보
     */
    public ExpenditureDetailServiceResponse getExpenditureDetail(Long expenditureId, Long memberId) {
        // 1. 지출 내역 조회
        Expenditure expenditure = expenditureRepository.findByIdAndNotDeleted(expenditureId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorType.EXPENDITURE_NOT_FOUND));

        // 2. 소유권 검증
        if (!expenditure.isOwnedBy(memberId)) {
            throw new AuthorizationException(ErrorType.ACCESS_DENIED);
        }

        // 3. 카테고리 정보 조회
        String categoryName = null;
        if (expenditure.getCategoryId() != null) {
            categoryName = categoryRepository.findById(expenditure.getCategoryId())
                    .map(Category::getName)
                    .orElse(null);
        }

        // 4. 음식 정보 조회 (항목이 있는 경우)
        List<ExpenditureItemServiceResponse> items = List.of();
        if (expenditure.getItems() != null && !expenditure.getItems().isEmpty()) {
            List<Long> foodIds = expenditure.getItems().stream()
                    .map(ExpenditureItem::getFoodId)
                    .toList();

            Map<Long, String> foodNameMap = foodRepository.findByIdIn(foodIds).stream()
                    .collect(Collectors.toMap(Food::getFoodId, Food::getFoodName));

            items = expenditure.getItems().stream()
                    .map(item -> ExpenditureItemServiceResponse.builder()
                            .expenditureItemId(item.getExpenditureItemId())
                            .foodId(item.getFoodId())
                            .foodName(foodNameMap.get(item.getFoodId()))
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .build())
                    .toList();
        }

        // 5. 응답 생성
        return ExpenditureDetailServiceResponse.builder()
                .expenditureId(expenditure.getExpenditureId())
                .storeName(expenditure.getStoreName())
                .amount(expenditure.getAmount())
                .expendedDate(expenditure.getExpendedDate())
                .expendedTime(expenditure.getExpendedTime())
                .categoryId(expenditure.getCategoryId())
                .categoryName(categoryName)
                .mealType(expenditure.getMealType())
                .memo(expenditure.getMemo())
                .items(items)
                .build();
    }
}

package com.stdev.smartmealtable.domain.expenditure.service;

import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.expenditure.Expenditure;
import com.stdev.smartmealtable.domain.expenditure.ExpenditureItem;
import com.stdev.smartmealtable.domain.expenditure.ExpenditureRepository;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 지출 도메인 서비스
 * 지출 내역 생성, 검증 등 핵심 비즈니스 로직을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenditureDomainService {

    private final ExpenditureRepository expenditureRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 지출 내역 생성
     * - 카테고리 검증
     * - 지출 항목 도메인 객체 생성
     * - 도메인 로직에서 검증 수행
     *
     * @param memberId      회원 ID
     * @param storeName     상호명
     * @param amount        총 금액
     * @param expendedDate  지출 날짜
     * @param expendedTime  지출 시간
     * @param categoryId    카테고리 ID (선택)
     * @param mealType      식사 유형 (선택)
     * @param memo          메모 (선택)
     * @param items         지출 항목 목록
     * @return 생성된 지출 내역
     */
    public ExpenditureCreationResult createExpenditure(
            Long memberId,
            String storeName,
            Integer amount,
            LocalDate expendedDate,
            LocalTime expendedTime,
            Long categoryId,
            MealType mealType,
            String memo,
            List<ExpenditureItemRequest> items
    ) {
        // 1. 카테고리 검증 (categoryId가 있는 경우만)
        String categoryName = null;
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new BusinessException(
                            ErrorType.CATEGORY_NOT_FOUND,
                            "카테고리를 찾을 수 없습니다. ID: " + categoryId
                    ));
            categoryName = category.getName();
        }

        // 2. 지출 항목 도메인 객체 생성
        List<ExpenditureItem> expenditureItems = items != null
                ? items.stream()
                .map(itemReq -> ExpenditureItem.create(
                        itemReq.foodName(),
                        itemReq.quantity(),
                        itemReq.price()
                ))
                .collect(Collectors.toList())
                : List.of();

        // 3. 지출 내역 도메인 객체 생성 (도메인 로직에서 검증 수행)
        Expenditure expenditure = Expenditure.create(
                memberId,
                storeName,
                amount,
                expendedDate,
                expendedTime,
                categoryId,
                mealType,
                memo,
                expenditureItems
        );

        // 4. 저장
        Expenditure saved = expenditureRepository.save(expenditure);

        log.info("지출 내역 생성 완료 - memberId: {}, expenditureId: {}, amount: {}",
                memberId, saved.getExpenditureId(), amount);

        return new ExpenditureCreationResult(saved, categoryName);
    }

    /**
     * 지출 항목 요청 DTO
     */
    public record ExpenditureItemRequest(
            String foodName,
            Integer quantity,
            Integer price
    ) {
    }

    /**
     * 지출 생성 결과 DTO
     */
    public record ExpenditureCreationResult(
            Expenditure expenditure,
            String categoryName
    ) {
    }
}

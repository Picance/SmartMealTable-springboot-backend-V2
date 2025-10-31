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
     * 지출 내역 생성 (기존 메서드 - 호환성 유지)
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
                        itemReq.foodId(),
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
     * 지출 내역 생성 (장바구니 시나리오 - storeId + foodId 포함)
     */
    public ExpenditureCreationResult createExpenditureFromCart(
            Long memberId,
            Long storeId,                   // ◆ 장바구니에서 전달
            String storeName,
            Integer amount,
            LocalDate expendedDate,
            LocalTime expendedTime,
            Long categoryId,
            MealType mealType,
            String memo,
            List<CartExpenditureItemRequest> items
    ) {
        // 1. 카테고리 검증
        String categoryName = null;
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new BusinessException(
                            ErrorType.CATEGORY_NOT_FOUND,
                            "카테고리를 찾을 수 없습니다. ID: " + categoryId
                    ));
            categoryName = category.getName();
        }

        // 2. 지출 항목 도메인 객체 생성 (foodId + foodName)
        List<ExpenditureItem> expenditureItems = items != null
                ? items.stream()
                .map(itemReq -> ExpenditureItem.createFromFood(
                        itemReq.foodId(),
                        itemReq.foodName(),              // ◆ 음식명 포함
                        itemReq.quantity(),
                        itemReq.price()
                ))
                .collect(Collectors.toList())
                : List.of();

        // 3. 지출 내역 도메인 객체 생성 (createFromCart: storeId 포함)
        Expenditure expenditure = Expenditure.createFromCart(
                memberId,
                storeId,                                // ◆ storeId 포함
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

        log.info("지출 내역 생성 완료 (장바구니) - memberId: {}, storeId: {}, expenditureId: {}, amount: {}",
                memberId, storeId, saved.getExpenditureId(), amount);

        return new ExpenditureCreationResult(saved, categoryName);
    }
    
    /**
     * 지출 내역 생성 (수기 입력 시나리오 - foodName만 사용)
     */
    public ExpenditureCreationResult createExpenditureFromManualInput(
            Long memberId,
            String storeName,
            Integer amount,
            LocalDate expendedDate,
            LocalTime expendedTime,
            Long categoryId,
            MealType mealType,
            String memo,
            List<ManualExpenditureItemRequest> items
    ) {
        // 1. 카테고리 검증
        String categoryName = null;
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new BusinessException(
                            ErrorType.CATEGORY_NOT_FOUND,
                            "카테고리를 찾을 수 없습니다. ID: " + categoryId
                    ));
            categoryName = category.getName();
        }

        // 2. 지출 항목 도메인 객체 생성 (foodName만)
        List<ExpenditureItem> expenditureItems = items != null
                ? items.stream()
                .map(itemReq -> ExpenditureItem.createFromManualInput(
                        itemReq.foodName(),              // ◆ 음식명만
                        itemReq.quantity(),
                        itemReq.price()
                ))
                .collect(Collectors.toList())
                : List.of();

        // 3. 지출 내역 도메인 객체 생성 (createFromManualInput: storeId = NULL)
        Expenditure expenditure = Expenditure.createFromManualInput(
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

        log.info("지출 내역 생성 완료 (수기 입력) - memberId: {}, expenditureId: {}, amount: {}",
                memberId, saved.getExpenditureId(), amount);

        return new ExpenditureCreationResult(saved, categoryName);
    }

    /**
     * 지출 항목 요청 DTO
     */
    public record ExpenditureItemRequest(
            Long foodId,
            Integer quantity,
            Integer price
    ) {
    }
    
    /**
     * 장바구니 지출 항목 요청 DTO
     */
    public record CartExpenditureItemRequest(
            Long foodId,
            String foodName,        // ◆ 음식명 포함
            Integer quantity,
            Integer price
    ) {
    }
    
    /**
     * 수기 입력 지출 항목 요청 DTO
     */
    public record ManualExpenditureItemRequest(
            String foodName,        // ◆ 음식명만
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

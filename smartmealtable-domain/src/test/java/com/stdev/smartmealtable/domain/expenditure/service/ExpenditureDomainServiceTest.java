package com.stdev.smartmealtable.domain.expenditure.service;

import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.expenditure.Expenditure;
import com.stdev.smartmealtable.domain.expenditure.ExpenditureRepository;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

/**
 * ExpenditureDomainService 단위 테스트 (BDD Mockist 스타일)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExpenditureDomainService 테스트")
class ExpenditureDomainServiceTest {

    @Mock
    private ExpenditureRepository expenditureRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ExpenditureDomainService expenditureDomainService;

    @Nested
    @DisplayName("createExpenditure 메서드는")
    class Describe_createExpenditure {

        @Nested
        @DisplayName("카테고리 ID가 주어지고 유효한 경우")
        class Context_with_valid_category {

            @Test
            @DisplayName("지출 내역을 생성하고 카테고리명을 반환한다")
            void it_creates_expenditure_with_category_name() {
                // Given
                Long memberId = 1L;
                String storeName = "맥도날드";
                Integer amount = 15000;
                LocalDate expendedDate = LocalDate.of(2024, 1, 15);
                LocalTime expendedTime = LocalTime.of(12, 30);
                Long categoryId = 1L;
                MealType mealType = MealType.LUNCH;
                String memo = "점심 식사";

                List<ExpenditureDomainService.ExpenditureItemRequest> items = List.of(
                        new ExpenditureDomainService.ExpenditureItemRequest(1L, 2, 5000),
                        new ExpenditureDomainService.ExpenditureItemRequest(2L, 1, 5000)
                );

                Category category = Category.reconstitute(1L, "패스트푸드");
                Expenditure savedExpenditure = Expenditure.reconstruct(
                        1L,
                        memberId,
                        storeName,
                        amount,
                        expendedDate,
                        expendedTime,
                        categoryId,
                        mealType,
                        memo,
                        List.of(),
                        LocalDateTime.now(),
                        false
                );

                given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
                given(expenditureRepository.save(any(Expenditure.class))).willReturn(savedExpenditure);

                // When
                ExpenditureDomainService.ExpenditureCreationResult result = expenditureDomainService.createExpenditure(
                        memberId, storeName, amount, expendedDate, expendedTime,
                        categoryId, mealType, memo, items
                );

                // Then
                assertThat(result).isNotNull();
                assertThat(result.expenditure()).isNotNull();
                assertThat(result.categoryName()).isEqualTo("패스트푸드");

                then(categoryRepository).should(times(1)).findById(categoryId);
                then(expenditureRepository).should(times(1)).save(any(Expenditure.class));
            }
        }

        @Nested
        @DisplayName("카테고리 ID 없이 생성하는 경우")
        class Context_without_category {

            @Test
            @DisplayName("지출 내역을 생성하고 카테고리명은 null이다")
            void it_creates_expenditure_without_category() {
                // Given
                Long memberId = 1L;
                String storeName = "편의점";
                Integer amount = 5000;
                LocalDate expendedDate = LocalDate.of(2024, 1, 15);
                LocalTime expendedTime = LocalTime.of(18, 30);
                MealType mealType = MealType.OTHER;
                String memo = "간식";

                List<ExpenditureDomainService.ExpenditureItemRequest> items = List.of(
                        new ExpenditureDomainService.ExpenditureItemRequest(3L, 1, 5000)
                );

                Expenditure savedExpenditure = Expenditure.reconstruct(
                        1L,
                        memberId,
                        storeName,
                        amount,
                        expendedDate,
                        expendedTime,
                        null,
                        mealType,
                        memo,
                        List.of(),
                        LocalDateTime.now(),
                        false
                );

                given(expenditureRepository.save(any(Expenditure.class))).willReturn(savedExpenditure);

                // When
                ExpenditureDomainService.ExpenditureCreationResult result = expenditureDomainService.createExpenditure(
                        memberId, storeName, amount, expendedDate, expendedTime,
                        null, mealType, memo, items
                );

                // Then
                assertThat(result).isNotNull();
                assertThat(result.expenditure()).isNotNull();
                assertThat(result.categoryName()).isNull();

                then(categoryRepository).should(times(0)).findById(any());
                then(expenditureRepository).should(times(1)).save(any(Expenditure.class));
            }
        }

        @Nested
        @DisplayName("지출 항목 없이 생성하는 경우")
        class Context_without_items {

            @Test
            @DisplayName("지출 내역만 생성한다")
            void it_creates_expenditure_without_items() {
                // Given
                Long memberId = 1L;
                String storeName = "카페";
                Integer amount = 4500;
                LocalDate expendedDate = LocalDate.of(2024, 1, 15);
                LocalTime expendedTime = LocalTime.of(15, 0);
                Long categoryId = 2L;
                MealType mealType = MealType.OTHER;
                String memo = null;

                Category category = Category.reconstitute(2L, "카페/디저트");
                Expenditure savedExpenditure = Expenditure.reconstruct(
                        1L,
                        memberId,
                        storeName,
                        amount,
                        expendedDate,
                        expendedTime,
                        categoryId,
                        mealType,
                        memo,
                        List.of(),
                        LocalDateTime.now(),
                        false
                );

                given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
                given(expenditureRepository.save(any(Expenditure.class))).willReturn(savedExpenditure);

                // When
                ExpenditureDomainService.ExpenditureCreationResult result = expenditureDomainService.createExpenditure(
                        memberId, storeName, amount, expendedDate, expendedTime,
                        categoryId, mealType, memo, null
                );

                // Then
                assertThat(result).isNotNull();
                assertThat(result.expenditure()).isNotNull();
                assertThat(result.categoryName()).isEqualTo("카페/디저트");

                then(categoryRepository).should(times(1)).findById(categoryId);
                then(expenditureRepository).should(times(1)).save(any(Expenditure.class));
            }
        }

        @Nested
        @DisplayName("존재하지 않는 카테고리 ID가 주어지면")
        class Context_with_invalid_category {

            @Test
            @DisplayName("CATEGORY_NOT_FOUND 예외를 발생시킨다")
            void it_throws_category_not_found_exception() {
                // Given
                Long memberId = 1L;
                String storeName = "음식점";
                Integer amount = 10000;
                LocalDate expendedDate = LocalDate.of(2024, 1, 15);
                LocalTime expendedTime = LocalTime.of(19, 0);
                Long invalidCategoryId = 999L;
                MealType mealType = MealType.DINNER;
                String memo = "저녁 식사";

                List<ExpenditureDomainService.ExpenditureItemRequest> items = List.of(
                        new ExpenditureDomainService.ExpenditureItemRequest(1L, 1, 10000)
                );

                given(categoryRepository.findById(invalidCategoryId)).willReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> expenditureDomainService.createExpenditure(
                        memberId, storeName, amount, expendedDate, expendedTime,
                        invalidCategoryId, mealType, memo, items
                ))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorType", ErrorType.CATEGORY_NOT_FOUND);

                then(categoryRepository).should(times(1)).findById(invalidCategoryId);
                then(expenditureRepository).should(times(0)).save(any(Expenditure.class));
            }
        }

        @Nested
        @DisplayName("필수 정보와 선택 정보를 모두 포함하여 생성하는 경우")
        class Context_with_all_information {

            @Test
            @DisplayName("완전한 지출 내역을 생성한다")
            void it_creates_complete_expenditure() {
                // Given
                Long memberId = 1L;
                String storeName = "한식당";
                Integer amount = 25000;
                LocalDate expendedDate = LocalDate.of(2024, 1, 15);
                LocalTime expendedTime = LocalTime.of(12, 0);
                Long categoryId = 1L;
                MealType mealType = MealType.LUNCH;
                String memo = "회식";

                List<ExpenditureDomainService.ExpenditureItemRequest> items = List.of(
                        new ExpenditureDomainService.ExpenditureItemRequest(1L, 2, 12000),
                        new ExpenditureDomainService.ExpenditureItemRequest(2L, 1, 13000)
                );

                int totalAmount = 12000 * 2 + 13000 * 1; // 37000

                Category category = Category.reconstitute(1L, "한식");
                Expenditure savedExpenditure = Expenditure.reconstruct(
                        1L,
                        memberId,
                        storeName,
                        totalAmount,
                        expendedDate,
                        expendedTime,
                        categoryId,
                        mealType,
                        memo,
                        List.of(),
                        LocalDateTime.now(),
                        false
                );

                given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
                given(expenditureRepository.save(any(Expenditure.class))).willReturn(savedExpenditure);

                // When
                ExpenditureDomainService.ExpenditureCreationResult result = expenditureDomainService.createExpenditure(
                        memberId, storeName, totalAmount, expendedDate, expendedTime,
                        categoryId, mealType, memo, items
                );

                // Then
                assertThat(result).isNotNull();
                assertThat(result.expenditure()).isNotNull();
                assertThat(result.categoryName()).isEqualTo("한식");
                assertThat(result.expenditure().getStoreName()).isEqualTo(storeName);
                assertThat(result.expenditure().getAmount()).isEqualTo(totalAmount);
                assertThat(result.expenditure().getMealType()).isEqualTo(mealType);

                then(categoryRepository).should(times(1)).findById(categoryId);
                then(expenditureRepository).should(times(1)).save(any(Expenditure.class));
            }
        }
    }
}

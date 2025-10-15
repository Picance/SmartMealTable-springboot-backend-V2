package com.stdev.smartmealtable.domain.preference.service;

import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.preference.Preference;
import com.stdev.smartmealtable.domain.preference.PreferenceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

/**
 * PreferenceDomainService 단위 테스트 (BDD Mockist 스타일)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PreferenceDomainService 테스트")
class PreferenceDomainServiceTest {

    @Mock
    private PreferenceRepository preferenceRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private PreferenceDomainService preferenceDomainService;

    @Nested
    @DisplayName("resetPreferences 메서드는")
    class Describe_resetPreferences {

        @Nested
        @DisplayName("유효한 선호도 항목 리스트가 주어지면")
        class Context_with_valid_preference_items {

            @Test
            @DisplayName("기존 선호도를 삭제하고 새로운 선호도를 생성한다")
            void it_deletes_old_and_creates_new_preferences() {
                // Given
                Long memberId = 1L;
                List<PreferenceDomainService.PreferenceItem> items = List.of(
                        new PreferenceDomainService.PreferenceItem(1L, 100),
                        new PreferenceDomainService.PreferenceItem(2L, 0)
                );

                Category category1 = Category.reconstitute(1L, "한식");
                Category category2 = Category.reconstitute(2L, "중식");

                Preference pref1 = Preference.create(memberId, 1L, 100);
                Preference pref2 = Preference.create(memberId, 2L, 0);

                given(categoryRepository.findById(1L)).willReturn(Optional.of(category1));
                given(categoryRepository.findById(2L)).willReturn(Optional.of(category2));
                given(preferenceRepository.save(any(Preference.class)))
                        .willReturn(pref1, pref2);

                // When
                List<Preference> result = preferenceDomainService.resetPreferences(memberId, items);

                // Then
                assertThat(result).hasSize(2);
                then(preferenceRepository).should(times(1)).deleteByMemberId(memberId);
                then(categoryRepository).should(times(2)).findById(any());
                then(preferenceRepository).should(times(2)).save(any(Preference.class));
            }
        }

        @Nested
        @DisplayName("존재하지 않는 카테고리가 포함되면")
        class Context_with_invalid_category {

            @Test
            @DisplayName("CATEGORY_NOT_FOUND 예외를 발생시킨다")
            void it_throws_category_not_found_exception() {
                // Given
                Long memberId = 1L;
                List<PreferenceDomainService.PreferenceItem> items = List.of(
                        new PreferenceDomainService.PreferenceItem(999L, 5)
                );

                given(categoryRepository.findById(999L)).willReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> preferenceDomainService.resetPreferences(memberId, items))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorType", ErrorType.CATEGORY_NOT_FOUND);

                then(preferenceRepository).should(times(1)).deleteByMemberId(memberId);
                then(categoryRepository).should(times(1)).findById(999L);
                then(preferenceRepository).should(times(0)).save(any(Preference.class));
            }
        }
    }

    @Nested
    @DisplayName("createPreferences 메서드는")
    class Describe_createPreferences {

        @Nested
        @DisplayName("유효한 선호도 항목 리스트가 주어지면")
        class Context_with_valid_items {

            @Test
            @DisplayName("선호도를 생성하고 저장한다")
            void it_creates_and_saves_preferences() {
                // Given
                Long memberId = 1L;
                List<PreferenceDomainService.PreferenceItem> items = List.of(
                        new PreferenceDomainService.PreferenceItem(1L, 100),
                        new PreferenceDomainService.PreferenceItem(2L, 0),
                        new PreferenceDomainService.PreferenceItem(3L, -100)
                );

                Category category1 = Category.reconstitute(1L, "한식");
                Category category2 = Category.reconstitute(2L, "중식");
                Category category3 = Category.reconstitute(3L, "일식");

                given(categoryRepository.findById(1L)).willReturn(Optional.of(category1));
                given(categoryRepository.findById(2L)).willReturn(Optional.of(category2));
                given(categoryRepository.findById(3L)).willReturn(Optional.of(category3));
                given(preferenceRepository.save(any(Preference.class)))
                        .willAnswer(invocation -> invocation.getArgument(0));

                // When
                List<Preference> result = preferenceDomainService.createPreferences(memberId, items);

                // Then
                assertThat(result).hasSize(3);
                then(categoryRepository).should(times(3)).findById(any());
                then(preferenceRepository).should(times(3)).save(any(Preference.class));
            }
        }
    }

    @Nested
    @DisplayName("updateOrCreatePreferences 메서드는")
    class Describe_updateOrCreatePreferences {

        @Nested
        @DisplayName("기존 선호도가 있는 경우")
        class Context_with_existing_preferences {

            @Test
            @DisplayName("기존 선호도를 업데이트한다")
            void it_updates_existing_preferences() {
                // Given
                Long memberId = 1L;
                List<PreferenceDomainService.PreferenceItem> items = List.of(
                        new PreferenceDomainService.PreferenceItem(1L, 0)
                );

                Category category = Category.reconstitute(1L, "한식");
                Preference existingPref = Preference.create(memberId, 1L, -100);

                given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
                given(preferenceRepository.findByMemberId(memberId))
                        .willReturn(List.of(existingPref));
                given(preferenceRepository.save(any(Preference.class)))
                        .willAnswer(invocation -> invocation.getArgument(0));

                // When
                List<Preference> result = preferenceDomainService.updateOrCreatePreferences(memberId, items);

                // Then
                assertThat(result).hasSize(1);
                then(categoryRepository).should(times(1)).findById(1L);
                then(preferenceRepository).should(times(1)).findByMemberId(memberId);
                then(preferenceRepository).should(times(1)).save(any(Preference.class));
            }
        }

        @Nested
        @DisplayName("기존 선호도가 없는 경우")
        class Context_without_existing_preferences {

            @Test
            @DisplayName("새로운 선호도를 생성한다")
            void it_creates_new_preferences() {
                // Given
                Long memberId = 1L;
                List<PreferenceDomainService.PreferenceItem> items = List.of(
                        new PreferenceDomainService.PreferenceItem(1L, 100)
                );

                Category category = Category.reconstitute(1L, "한식");

                given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
                given(preferenceRepository.findByMemberId(memberId))
                        .willReturn(List.of());
                given(preferenceRepository.save(any(Preference.class)))
                        .willAnswer(invocation -> invocation.getArgument(0));

                // When
                List<Preference> result = preferenceDomainService.updateOrCreatePreferences(memberId, items);

                // Then
                assertThat(result).hasSize(1);
                then(categoryRepository).should(times(1)).findById(1L);
                then(preferenceRepository).should(times(1)).findByMemberId(memberId);
                then(preferenceRepository).should(times(1)).save(any(Preference.class));
            }
        }

        @Nested
        @DisplayName("일부는 업데이트, 일부는 새로 생성하는 경우")
        class Context_with_mixed_update_and_create {

            @Test
            @DisplayName("기존 선호도는 업데이트하고 없는 것은 생성한다")
            void it_updates_and_creates_preferences() {
                // Given
                Long memberId = 1L;
                List<PreferenceDomainService.PreferenceItem> items = List.of(
                        new PreferenceDomainService.PreferenceItem(1L, 0),  // 기존 것 업데이트
                        new PreferenceDomainService.PreferenceItem(2L, -100)   // 새로 생성
                );

                Category category1 = Category.reconstitute(1L, "한식");
                Category category2 = Category.reconstitute(2L, "중식");
                Preference existingPref = Preference.create(memberId, 1L, 100);

                given(categoryRepository.findById(1L)).willReturn(Optional.of(category1));
                given(categoryRepository.findById(2L)).willReturn(Optional.of(category2));
                given(preferenceRepository.findByMemberId(memberId))
                        .willReturn(List.of(existingPref));
                given(preferenceRepository.save(any(Preference.class)))
                        .willAnswer(invocation -> invocation.getArgument(0));

                // When
                List<Preference> result = preferenceDomainService.updateOrCreatePreferences(memberId, items);

                // Then
                assertThat(result).hasSize(2);
                then(categoryRepository).should(times(2)).findById(any());
                then(preferenceRepository).should(times(1)).findByMemberId(memberId);
                then(preferenceRepository).should(times(2)).save(any(Preference.class));
            }
        }
    }

    @Nested
    @DisplayName("validateCategories 메서드는")
    class Describe_validateCategories {

        @Nested
        @DisplayName("모든 카테고리가 존재하면")
        class Context_with_all_valid_categories {

            @Test
            @DisplayName("예외를 발생시키지 않는다")
            void it_does_not_throw_exception() {
                // Given
                List<Long> categoryIds = List.of(1L, 2L, 3L);

                Category category1 = Category.reconstitute(1L, "한식");
                Category category2 = Category.reconstitute(2L, "중식");
                Category category3 = Category.reconstitute(3L, "일식");

                given(categoryRepository.findById(1L)).willReturn(Optional.of(category1));
                given(categoryRepository.findById(2L)).willReturn(Optional.of(category2));
                given(categoryRepository.findById(3L)).willReturn(Optional.of(category3));

                // When & Then (예외가 발생하지 않음)
                preferenceDomainService.validateCategories(categoryIds);

                then(categoryRepository).should(times(3)).findById(any());
            }
        }

        @Nested
        @DisplayName("존재하지 않는 카테고리가 하나라도 있으면")
        class Context_with_invalid_category {

            @Test
            @DisplayName("CATEGORY_NOT_FOUND 예외를 발생시킨다")
            void it_throws_category_not_found_exception() {
                // Given
                List<Long> categoryIds = List.of(1L, 999L);

                Category category1 = Category.reconstitute(1L, "한식");

                given(categoryRepository.findById(1L)).willReturn(Optional.of(category1));
                given(categoryRepository.findById(999L)).willReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> preferenceDomainService.validateCategories(categoryIds))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorType", ErrorType.CATEGORY_NOT_FOUND);

                then(categoryRepository).should(times(2)).findById(any());
            }
        }
    }
}

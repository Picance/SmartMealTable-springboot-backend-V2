package com.stdev.smartmealtable.domain.preference.service;

import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.preference.Preference;
import com.stdev.smartmealtable.domain.preference.PreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 선호도 관련 Domain Service
 * - 비즈니스 로직: 선호도 재설정, 카테고리 검증, 선호도 생성
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PreferenceDomainService {

    private final PreferenceRepository preferenceRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 선호도 재설정 (기존 선호도 삭제 후 새로운 선호도 생성)
     *
     * @param memberId 회원 ID
     * @param items 선호도 항목 리스트
     * @return 생성된 선호도 리스트
     */
    public List<Preference> resetPreferences(Long memberId, List<PreferenceItem> items) {
        // 1. 기존 선호도 삭제
        preferenceRepository.deleteByMemberId(memberId);

        // 2. 새로운 선호도 생성
        return createPreferences(memberId, items);
    }

    /**
     * 선호도 일괄 생성
     *
     * @param memberId 회원 ID
     * @param items 선호도 항목 리스트
     * @return 생성된 선호도 리스트
     */
    public List<Preference> createPreferences(Long memberId, List<PreferenceItem> items) {
        // 카테고리 검증
        validateCategories(items.stream().map(PreferenceItem::categoryId).toList());

        // 선호도 생성 및 저장
        List<Preference> preferences = items.stream()
                .map(item -> Preference.create(memberId, item.categoryId(), item.weight()))
                .toList();

        preferences.forEach(preferenceRepository::save);

        return preferences;
    }

    /**
     * 선호도 업데이트 또는 생성
     * - 기존 선호도가 있으면 weight 업데이트
     * - 없으면 새로 생성
     *
     * @param memberId 회원 ID
     * @param items 선호도 항목 리스트
     * @return 업데이트/생성된 선호도 리스트
     */
    public List<Preference> updateOrCreatePreferences(Long memberId, List<PreferenceItem> items) {
        // 1. 카테고리 검증
        validateCategories(items.stream().map(PreferenceItem::categoryId).toList());

        // 2. 기존 선호도 조회
        List<Preference> existingPreferences = preferenceRepository.findByMemberId(memberId);
        java.util.Map<Long, Preference> preferenceMap = existingPreferences.stream()
                .collect(java.util.stream.Collectors.toMap(Preference::getCategoryId, p -> p));

        // 3. 선호도 업데이트 또는 생성
        List<Preference> result = new java.util.ArrayList<>();
        for (PreferenceItem item : items) {
            Preference preference = preferenceMap.get(item.categoryId());
            if (preference != null) {
                // 기존 선호도 업데이트
                preference.changeWeight(item.weight());
                result.add(preferenceRepository.save(preference));
            } else {
                // 새로운 선호도 생성
                Preference newPreference = Preference.create(memberId, item.categoryId(), item.weight());
                result.add(preferenceRepository.save(newPreference));
            }
        }

        return result;
    }

    /**
     * 카테고리 존재 여부 검증
     *
     * @param categoryIds 카테고리 ID 리스트
     * @throws BusinessException 존재하지 않는 카테고리가 있을 경우
     */
    public void validateCategories(List<Long> categoryIds) {
        for (Long categoryId : categoryIds) {
            categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new BusinessException(ErrorType.CATEGORY_NOT_FOUND));
        }
    }

    /**
     * 선호도 항목 DTO
     */
    public record PreferenceItem(
            Long categoryId,
            Integer weight
    ) {
    }
}

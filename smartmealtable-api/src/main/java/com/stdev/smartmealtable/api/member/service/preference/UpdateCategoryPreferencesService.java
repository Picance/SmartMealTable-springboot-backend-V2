package com.stdev.smartmealtable.api.member.service.preference;

import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.preference.Preference;
import com.stdev.smartmealtable.domain.preference.PreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 카테고리 선호도 수정 Application Service
 */
@Service
@RequiredArgsConstructor
public class UpdateCategoryPreferencesService {

    private final CategoryRepository categoryRepository;
    private final PreferenceRepository preferenceRepository;

    @Transactional
    public UpdateCategoryPreferencesServiceResponse execute(Long memberId, UpdateCategoryPreferencesServiceRequest request) {
        // 1. 모든 카테고리 ID 존재 여부 검증
        List<Long> categoryIds = request.getPreferences().stream()
                .map(UpdateCategoryPreferencesServiceRequest.CategoryPreferenceItem::getCategoryId)
                .toList();

        categoryIds.forEach(categoryId -> {
            categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new BusinessException(
                            ErrorType.CATEGORY_NOT_FOUND,
                            "카테고리를 찾을 수 없습니다. categoryId: " + categoryId
                    ));
        });

        // 2. 기존 선호도 정보 조회
        List<Preference> existingPreferences = preferenceRepository.findByMemberId(memberId);
        Map<Long, Preference> preferenceMap = existingPreferences.stream()
                .collect(Collectors.toMap(Preference::getCategoryId, p -> p));

        // 3. 선호도 업데이트 또는 신규 생성
        int updatedCount = 0;
        for (UpdateCategoryPreferencesServiceRequest.CategoryPreferenceItem item : request.getPreferences()) {
            Preference existingPreference = preferenceMap.get(item.getCategoryId());

            if (existingPreference != null) {
                // 기존 선호도 업데이트
                existingPreference.changeWeight(item.getWeight());
                preferenceRepository.save(existingPreference);  // 변경된 도메인 객체 저장
            } else {
                // 새로운 선호도 생성
                Preference newPreference = Preference.create(memberId, item.getCategoryId(), item.getWeight());
                preferenceRepository.save(newPreference);
            }
            updatedCount++;
        }

        return new UpdateCategoryPreferencesServiceResponse(updatedCount, LocalDateTime.now());
    }
}

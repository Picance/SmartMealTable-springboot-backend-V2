package com.stdev.smartmealtable.api.member.service.preference;

import com.stdev.smartmealtable.domain.preference.Preference;
import com.stdev.smartmealtable.domain.preference.service.PreferenceDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 카테고리 선호도 수정 Application Service
 * - 유즈케이스: 카테고리 선호도 업데이트 또는 생성
 * - Orchestration: Domain Service 호출 및 DTO 변환
 */
@Service
@RequiredArgsConstructor
public class UpdateCategoryPreferencesService {

    private final PreferenceDomainService preferenceDomainService;

    @Transactional
    public UpdateCategoryPreferencesServiceResponse execute(Long memberId, UpdateCategoryPreferencesServiceRequest request) {
        // 1. 선호도 업데이트 또는 생성 (PreferenceDomainService)
        List<PreferenceDomainService.PreferenceItem> preferenceItems = request.getPreferences().stream()
                .map(item -> new PreferenceDomainService.PreferenceItem(item.getCategoryId(), item.getWeight()))
                .toList();

        List<Preference> updatedPreferences = preferenceDomainService.updateOrCreatePreferences(memberId, preferenceItems);

        // 2. 응답 생성
        return new UpdateCategoryPreferencesServiceResponse(updatedPreferences.size(), LocalDateTime.now());
    }
}

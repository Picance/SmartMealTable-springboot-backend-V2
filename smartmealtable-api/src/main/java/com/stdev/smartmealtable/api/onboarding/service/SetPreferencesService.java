package com.stdev.smartmealtable.api.onboarding.service;

import com.stdev.smartmealtable.api.onboarding.service.dto.SetPreferencesServiceRequest;
import com.stdev.smartmealtable.api.onboarding.service.dto.SetPreferencesServiceResponse;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.service.ProfileDomainService;
import com.stdev.smartmealtable.domain.preference.Preference;
import com.stdev.smartmealtable.domain.preference.service.PreferenceDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 취향 설정 Application Service
 * - 유즈케이스: 추천 유형 설정 + 카테고리 선호도 설정
 * - Orchestration: Domain Service 호출 및 DTO 변환
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SetPreferencesService {

    private final ProfileDomainService profileDomainService;
    private final PreferenceDomainService preferenceDomainService;
    private final CategoryRepository categoryRepository;

    /**
     * 회원의 취향 설정 (추천 유형 + 카테고리별 선호도)
     */
    public SetPreferencesServiceResponse setPreferences(Long memberId, SetPreferencesServiceRequest request) {
        // 1. 추천 유형 업데이트 (ProfileDomainService)
        RecommendationType recommendationType = request.getRecommendationType();
        profileDomainService.updateRecommendationType(memberId, recommendationType);

        // 2. 선호도 재설정 (PreferenceDomainService)
        List<PreferenceDomainService.PreferenceItem> preferenceItems = request.getPreferences().stream()
                .map(item -> new PreferenceDomainService.PreferenceItem(item.getCategoryId(), item.getWeight()))
                .toList();

        List<Preference> preferences = preferenceDomainService.resetPreferences(memberId, preferenceItems);

        // 3. 응답 생성 (카테고리 이름 포함)
        Map<Long, String> categoryNameMap = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getName));

        List<SetPreferencesServiceResponse.PreferenceInfo> preferenceInfos = preferences.stream()
                .map(p -> new SetPreferencesServiceResponse.PreferenceInfo(
                        p.getCategoryId(),
                        categoryNameMap.get(p.getCategoryId()),
                        p.getWeight()
                ))
                .collect(Collectors.toList());

        return new SetPreferencesServiceResponse(recommendationType, preferenceInfos);
    }
}

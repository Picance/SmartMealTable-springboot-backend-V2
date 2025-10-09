package com.stdev.smartmealtable.api.onboarding.service;

import com.stdev.smartmealtable.api.onboarding.service.dto.SetPreferencesServiceRequest;
import com.stdev.smartmealtable.api.onboarding.service.dto.SetPreferencesServiceResponse;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.preference.Preference;
import com.stdev.smartmealtable.domain.preference.PreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 취향 설정 Service
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SetPreferencesService {

    private final MemberRepository memberRepository;
    private final PreferenceRepository preferenceRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 회원의 취향 설정 (추천 유형 + 카테고리별 선호도)
     */
    public SetPreferencesServiceResponse setPreferences(Long memberId, SetPreferencesServiceRequest request) {
        // 1. 회원 조회
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException(ErrorType.MEMBER_NOT_FOUND));

        // 2. 추천 유형 업데이트
        RecommendationType recommendationType = request.getRecommendationType();
        member.changeRecommendationType(recommendationType);
        memberRepository.save(member);

        // 3. 기존 선호도 삭제
        preferenceRepository.deleteByMemberId(memberId);

        // 4. 새로운 선호도 저장
        List<SetPreferencesServiceRequest.PreferenceItem> preferenceItems = request.getPreferences();
        List<Preference> preferences = preferenceItems.stream()
            .map(item -> {
                // 카테고리 존재 여부 검증
                categoryRepository.findById(item.getCategoryId())
                    .orElseThrow(() -> new BusinessException(ErrorType.CATEGORY_NOT_FOUND));
                
                return Preference.create(memberId, item.getCategoryId(), item.getWeight());
            })
            .toList();

        preferences.forEach(preferenceRepository::save);

        // 5. 응답 생성 (카테고리 이름 포함)
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

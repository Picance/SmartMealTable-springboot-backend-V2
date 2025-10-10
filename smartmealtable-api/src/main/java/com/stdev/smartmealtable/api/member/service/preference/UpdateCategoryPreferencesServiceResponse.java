package com.stdev.smartmealtable.api.member.service.preference;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * PUT /api/v1/members/me/preferences/categories - 카테고리 선호도 수정 Service Response
 */
@Getter
public class UpdateCategoryPreferencesServiceResponse {

    private final Integer updatedCount;
    private final LocalDateTime updatedAt;

    public UpdateCategoryPreferencesServiceResponse(Integer updatedCount, LocalDateTime updatedAt) {
        this.updatedCount = updatedCount;
        this.updatedAt = updatedAt;
    }
}

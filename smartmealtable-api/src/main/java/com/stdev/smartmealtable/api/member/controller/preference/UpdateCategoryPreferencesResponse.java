package com.stdev.smartmealtable.api.member.controller.preference;

import com.stdev.smartmealtable.api.member.service.preference.UpdateCategoryPreferencesServiceResponse;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * PUT /api/v1/members/me/preferences/categories - 카테고리 선호도 수정 Response
 */
@Getter
public class UpdateCategoryPreferencesResponse {

    private final Integer updatedCount;
    private final LocalDateTime updatedAt;

    public UpdateCategoryPreferencesResponse(Integer updatedCount, LocalDateTime updatedAt) {
        this.updatedCount = updatedCount;
        this.updatedAt = updatedAt;
    }

    public static UpdateCategoryPreferencesResponse from(UpdateCategoryPreferencesServiceResponse serviceResponse) {
        return new UpdateCategoryPreferencesResponse(
                serviceResponse.getUpdatedCount(),
                serviceResponse.getUpdatedAt()
        );
    }
}

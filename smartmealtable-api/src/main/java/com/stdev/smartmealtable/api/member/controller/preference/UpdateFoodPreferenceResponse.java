package com.stdev.smartmealtable.api.member.controller.preference;

import com.stdev.smartmealtable.api.member.service.preference.UpdateFoodPreferenceServiceResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 음식 선호도 변경 Controller Response DTO
 */
@Getter
@AllArgsConstructor
public class UpdateFoodPreferenceResponse {
    private Long foodPreferenceId;
    private Long foodId;
    private String foodName;
    private String categoryName;
    private Boolean isPreferred;
    private LocalDateTime updatedAt;

    public static UpdateFoodPreferenceResponse from(UpdateFoodPreferenceServiceResponse serviceResponse) {
        return new UpdateFoodPreferenceResponse(
                serviceResponse.getFoodPreferenceId(),
                serviceResponse.getFoodId(),
                serviceResponse.getFoodName(),
                serviceResponse.getCategoryName(),
                serviceResponse.getIsPreferred(),
                serviceResponse.getUpdatedAt()
        );
    }
}

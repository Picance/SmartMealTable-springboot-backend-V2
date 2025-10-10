package com.stdev.smartmealtable.api.member.controller.preference;

import com.stdev.smartmealtable.api.member.service.preference.AddFoodPreferenceServiceResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 음식 선호도 추가 Controller Response DTO
 */
@Getter
@AllArgsConstructor
public class AddFoodPreferenceResponse {
    private Long foodPreferenceId;
    private Long foodId;
    private String foodName;
    private String categoryName;
    private Boolean isPreferred;
    private LocalDateTime createdAt;

    public static AddFoodPreferenceResponse from(AddFoodPreferenceServiceResponse serviceResponse) {
        return new AddFoodPreferenceResponse(
                serviceResponse.getFoodPreferenceId(),
                serviceResponse.getFoodId(),
                serviceResponse.getFoodName(),
                serviceResponse.getCategoryName(),
                serviceResponse.getIsPreferred(),
                serviceResponse.getCreatedAt()
        );
    }
}

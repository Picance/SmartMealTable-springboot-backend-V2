package com.stdev.smartmealtable.api.member.controller.preference;

import com.stdev.smartmealtable.api.member.service.preference.AddFoodPreferenceServiceRequest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 음식 선호도 추가 Controller Request DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddFoodPreferenceRequest {

    @NotNull(message = "음식 ID는 필수입니다.")
    private Long foodId;

    @NotNull(message = "선호 여부는 필수입니다.")
    private Boolean isPreferred;

    public AddFoodPreferenceServiceRequest toServiceRequest() {
        return new AddFoodPreferenceServiceRequest(foodId, isPreferred);
    }
}

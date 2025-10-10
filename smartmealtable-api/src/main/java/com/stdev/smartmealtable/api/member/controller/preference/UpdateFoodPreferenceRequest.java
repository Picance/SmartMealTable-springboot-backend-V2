package com.stdev.smartmealtable.api.member.controller.preference;

import com.stdev.smartmealtable.api.member.service.preference.UpdateFoodPreferenceServiceRequest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 음식 선호도 변경 Controller Request DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFoodPreferenceRequest {

    @NotNull(message = "선호 여부는 필수입니다.")
    private Boolean isPreferred;

    public UpdateFoodPreferenceServiceRequest toServiceRequest() {
        return new UpdateFoodPreferenceServiceRequest(isPreferred);
    }
}

package com.stdev.smartmealtable.api.member.service.preference;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 음식 선호도 변경 Service Request DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFoodPreferenceServiceRequest {
    private Boolean isPreferred;
}

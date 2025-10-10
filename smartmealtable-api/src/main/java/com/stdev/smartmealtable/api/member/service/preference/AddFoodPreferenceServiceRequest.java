package com.stdev.smartmealtable.api.member.service.preference;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 음식 선호도 추가 Service Request DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddFoodPreferenceServiceRequest {
    private Long foodId;
    private Boolean isPreferred;
}

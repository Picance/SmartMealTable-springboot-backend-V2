package com.stdev.smartmealtable.api.member.service.preference;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 음식 선호도 추가 Service Response DTO
 */
@Getter
@AllArgsConstructor
public class AddFoodPreferenceServiceResponse {
    private Long foodPreferenceId;
    private Long foodId;
    private String foodName;
    private String categoryName;
    private Boolean isPreferred;
    private LocalDateTime createdAt;
}

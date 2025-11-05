package com.stdev.smartmealtable.admin.store.controller.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.DayOfWeek;

/**
 * 영업시간 추가/수정 요청 DTO (Controller)
 */
public record OpeningHourRequest(
        @NotNull(message = "요일은 필수입니다.")
        DayOfWeek dayOfWeek,

        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d$", message = "영업 시작 시간은 HH:mm:ss 형식이어야 합니다.")
        @Size(max = 8, message = "영업 시작 시간은 최대 8자까지 입력 가능합니다.")
        String openTime,

        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d$", message = "영업 종료 시간은 HH:mm:ss 형식이어야 합니다.")
        @Size(max = 8, message = "영업 종료 시간은 최대 8자까지 입력 가능합니다.")
        String closeTime,

        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d$", message = "브레이크 시작 시간은 HH:mm:ss 형식이어야 합니다.")
        @Size(max = 8, message = "브레이크 시작 시간은 최대 8자까지 입력 가능합니다.")
        String breakStartTime,

        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d$", message = "브레이크 종료 시간은 HH:mm:ss 형식이어야 합니다.")
        @Size(max = 8, message = "브레이크 종료 시간은 최대 8자까지 입력 가능합니다.")
        String breakEndTime,

        @NotNull(message = "휴무일 여부는 필수입니다.")
        boolean isHoliday
) {
}

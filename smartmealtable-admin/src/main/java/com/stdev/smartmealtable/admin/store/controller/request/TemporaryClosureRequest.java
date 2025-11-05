package com.stdev.smartmealtable.admin.store.controller.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 임시 휴무 등록 요청 DTO (Controller)
 */
public record TemporaryClosureRequest(
        @NotNull(message = "휴무 날짜는 필수입니다.")
        LocalDate closureDate,

        LocalTime startTime, // 종일 휴무인 경우 null

        LocalTime endTime, // 종일 휴무인 경우 null

        @Size(max = 200, message = "휴무 사유는 최대 200자까지 입력 가능합니다.")
        String reason
) {
}

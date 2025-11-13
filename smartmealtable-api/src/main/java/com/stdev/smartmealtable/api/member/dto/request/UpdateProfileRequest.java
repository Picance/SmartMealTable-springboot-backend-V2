package com.stdev.smartmealtable.api.member.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 프로필 수정 요청 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateProfileRequest {

    @NotNull(message = "그룹 ID는 필수입니다.")
    @Positive(message = "그룹 ID는 양수여야 합니다.")
    private Long groupId;
}

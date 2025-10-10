package com.stdev.smartmealtable.api.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 프로필 수정 요청 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateProfileRequest {

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 50, message = "닉네임은 2-50자 사이여야 합니다.")
    private String nickname;

    @NotNull(message = "그룹 ID는 필수입니다.")
    @Positive(message = "그룹 ID는 양수여야 합니다.")
    private Long groupId;
}

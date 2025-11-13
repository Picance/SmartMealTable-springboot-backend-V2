package com.stdev.smartmealtable.api.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 닉네임 수정 요청 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateNicknameRequest {

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 50, message = "닉네임은 2-50자 사이여야 합니다.")
    private String nickname;
}

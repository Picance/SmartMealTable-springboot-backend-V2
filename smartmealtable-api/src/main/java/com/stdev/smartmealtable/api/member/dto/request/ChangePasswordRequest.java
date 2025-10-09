package com.stdev.smartmealtable.api.member.dto.request;

import com.stdev.smartmealtable.api.member.service.dto.ChangePasswordServiceRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 비밀번호 변경 요청 DTO
 */
public record ChangePasswordRequest(
        @NotBlank(message = "현재 비밀번호를 입력해주세요.")
        String currentPassword,

        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        @Size(min = 8, max = 20, message = "비밀번호는 8-20자여야 합니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
                message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다."
        )
        String newPassword
) {
    public ChangePasswordServiceRequest toServiceRequest(Long memberId) {
        return new ChangePasswordServiceRequest(memberId, currentPassword, newPassword);
    }
}

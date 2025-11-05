package com.stdev.smartmealtable.admin.group.controller.request;

import com.stdev.smartmealtable.domain.member.entity.GroupType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 그룹 수정 Controller Request DTO
 */
public record UpdateGroupRequest(
        @NotBlank(message = "그룹 이름은 필수입니다.")
        @Size(max = 50, message = "그룹 이름은 최대 50자까지 입력 가능합니다.")
        String name,
        
        @NotNull(message = "그룹 타입은 필수입니다.")
        GroupType type,
        
        @Size(max = 255, message = "주소는 최대 255자까지 입력 가능합니다.")
        String address
) {
}

package com.stdev.smartmealtable.api.member.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 기본 주소 설정 응답 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PrimaryAddressResponse {
    
    private Long addressHistoryId;
    private Boolean isPrimary;
    private LocalDateTime updatedAt;
    
    public PrimaryAddressResponse(
            Long addressHistoryId,
            Boolean isPrimary,
            LocalDateTime updatedAt
    ) {
        this.addressHistoryId = addressHistoryId;
        this.isPrimary = isPrimary;
        this.updatedAt = updatedAt;
    }
    
    public static PrimaryAddressResponse of(Long addressHistoryId, Boolean isPrimary) {
        return new PrimaryAddressResponse(
                addressHistoryId,
                isPrimary,
                LocalDateTime.now()
        );
    }
}

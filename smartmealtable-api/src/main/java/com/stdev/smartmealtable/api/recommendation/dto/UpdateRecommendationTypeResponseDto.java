package com.stdev.smartmealtable.api.recommendation.dto;

import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 추천 유형 변경 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRecommendationTypeResponseDto {

    private Long memberId;
    private RecommendationType recommendationType;
    private String message;

    public static UpdateRecommendationTypeResponseDto from(Member member) {
        return new UpdateRecommendationTypeResponseDto(
                member.getMemberId(),
                member.getRecommendationType(),
                "추천 유형이 변경되었습니다."
        );
    }
}

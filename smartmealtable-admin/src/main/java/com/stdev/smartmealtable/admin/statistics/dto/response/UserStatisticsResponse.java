package com.stdev.smartmealtable.admin.statistics.dto.response;

import com.stdev.smartmealtable.domain.statistics.UserStatistics;

import java.util.Map;

/**
 * 사용자 통계 응답 DTO
 */
public record UserStatisticsResponse(
        long totalMembers,
        long socialLoginMembers,
        long emailLoginMembers,
        long deletedMembers,
        Map<String, Long> membersByGroupType
) {
    public static UserStatisticsResponse from(UserStatistics statistics) {
        return new UserStatisticsResponse(
                statistics.totalMembers(),
                statistics.socialLoginMembers(),
                statistics.emailLoginMembers(),
                statistics.deletedMembers(),
                statistics.membersByGroupType()
        );
    }
}

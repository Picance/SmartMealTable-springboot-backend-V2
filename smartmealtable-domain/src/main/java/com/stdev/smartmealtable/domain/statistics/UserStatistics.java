package com.stdev.smartmealtable.domain.statistics;

import java.util.Map;

/**
 * 사용자 통계 정보
 * POJO record 타입 - Spring Data 의존성 없음
 */
public record UserStatistics(
        long totalMembers,
        long socialLoginMembers,
        long emailLoginMembers,
        long deletedMembers,
        Map<String, Long> membersByGroupType  // UNIVERSITY, COMPANY, OTHER
) {
}

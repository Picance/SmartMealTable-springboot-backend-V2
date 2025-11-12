package com.stdev.smartmealtable.api.home.service;

import com.stdev.smartmealtable.domain.store.StoreOpeningHour;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 가게 영업시간 정보 생성 서비스
 *
 * StoreOpeningHour 정보를 기반으로 사용자 친화적인 영업시간 문자열을 생성합니다.
 * 예시:
 * - "11:00 ~ 21:00"
 * - "10:00 ~ 18:00 (13:00~14:00 휴게)"
 * - "휴무"
 * - "영업시간 미정"
 *
 * @author SmartMealTable Team
 * @since 2025-11-12
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class BusinessHoursService {

    private final StoreRepository storeRepository;

    /**
     * 가게의 오늘 영업시간 조회 및 포맷팅
     *
     * @param storeId 가게 ID
     * @return 포맷된 영업시간 문자열
     */
    public String getTodayBusinessHours(Long storeId) {
        try {
            DayOfWeek today = DayOfWeek.from(LocalDateTime.now());
            return getBusinessHoursByDayOfWeek(storeId, today);
        } catch (Exception e) {
            log.debug("Error getting today's business hours for store {}", storeId, e);
            return "영업시간 미정";
        }
    }

    /**
     * 가게의 특정 요일 영업시간 조회 및 포맷팅
     *
     * @param storeId 가게 ID
     * @param dayOfWeek 요일
     * @return 포맷된 영업시간 문자열
     */
    public String getBusinessHoursByDayOfWeek(Long storeId, DayOfWeek dayOfWeek) {
        try {
            List<StoreOpeningHour> openingHours = storeRepository.findOpeningHoursByStoreId(storeId);

            return openingHours.stream()
                    .filter(oh -> oh.dayOfWeek() == dayOfWeek)
                    .findFirst()
                    .map(this::formatOpeningHour)
                    .orElse("영업시간 미정");

        } catch (Exception e) {
            log.debug("Error getting business hours for store {} on {}", storeId, dayOfWeek, e);
            return "영업시간 미정";
        }
    }

    /**
     * 가게의 전체 주간 영업시간 조회
     *
     * @param storeId 가게 ID
     * @return 요일별 영업시간 맵 (키: 요일, 값: 포맷된 영업시간)
     */
    public Map<DayOfWeek, String> getWeeklyBusinessHours(Long storeId) {
        try {
            List<StoreOpeningHour> openingHours = storeRepository.findOpeningHoursByStoreId(storeId);

            Map<DayOfWeek, String> result = new LinkedHashMap<>();
            for (DayOfWeek day : DayOfWeek.values()) {
                result.put(day, openingHours.stream()
                        .filter(oh -> oh.dayOfWeek() == day)
                        .findFirst()
                        .map(this::formatOpeningHour)
                        .orElse("정보 없음"));
            }

            return result;

        } catch (Exception e) {
            log.debug("Error getting weekly business hours for store {}", storeId, e);
            return Collections.emptyMap();
        }
    }

    /**
     * 가게의 영업시간 정보가 있는지 확인
     *
     * @param storeId 가게 ID
     * @return 영업시간 정보 존재 여부
     */
    public boolean hasBusinessHoursInfo(Long storeId) {
        try {
            List<StoreOpeningHour> openingHours = storeRepository.findOpeningHoursByStoreId(storeId);
            return !openingHours.isEmpty();
        } catch (Exception e) {
            log.debug("Error checking business hours info for store {}", storeId, e);
            return false;
        }
    }

    /**
     * 개별 영업시간 정보 포맷팅
     *
     * 예시 결과:
     * - "11:00 ~ 21:00"
     * - "10:00 ~ 18:00 (13:00~14:00 휴게)"
     * - "휴무"
     *
     * @param openingHour 영업시간 정보
     * @return 포맷된 문자열
     */
    private String formatOpeningHour(StoreOpeningHour openingHour) {
        // 휴무일
        if (openingHour.isHoliday()) {
            return "휴무";
        }

        // 영업시간 정보 없음
        if (openingHour.openTime() == null || openingHour.closeTime() == null) {
            return "영업시간 미정";
        }

        // 기본 영업시간
        StringBuilder sb = new StringBuilder();
        sb.append(openingHour.openTime()).append(" ~ ").append(openingHour.closeTime());

        // 브레이크 타임이 있으면 추가
        if (openingHour.breakStartTime() != null && openingHour.breakEndTime() != null) {
            sb.append(" (").append(openingHour.breakStartTime())
                    .append("~").append(openingHour.breakEndTime())
                    .append(" 휴게)");
        }

        return sb.toString();
    }

    /**
     * 여러 가게의 오늘 영업시간 일괄 조회
     *
     * @param storeIds 가게 ID 목록
     * @return 가게 ID → 포맷된 영업시간 맵
     */
    public Map<Long, String> getTodayBusinessHoursBatch(List<Long> storeIds) {
        Map<Long, String> result = new HashMap<>();

        for (Long storeId : storeIds) {
            result.put(storeId, getTodayBusinessHours(storeId));
        }

        return result;
    }

    /**
     * 가게 ID로 현재 시간 기준 영업 상태 조회
     *
     * @param storeId 가게 ID
     * @return 영업 상태 정보
     */
    public StoreOperationStatus getOperationStatus(Long storeId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            DayOfWeek today = DayOfWeek.from(now);
            int currentTimeMinutes = now.getHour() * 60 + now.getMinute();

            List<StoreOpeningHour> openingHours = storeRepository.findOpeningHoursByStoreId(storeId);

            return openingHours.stream()
                    .filter(oh -> oh.dayOfWeek() == today)
                    .findFirst()
                    .map(oh -> evaluateOperationStatus(oh, currentTimeMinutes))
                    .orElse(new StoreOperationStatus("정보없음", false));

        } catch (Exception e) {
            log.debug("Error getting operation status for store {}", storeId, e);
            return new StoreOperationStatus("정보없음", false);
        }
    }

    /**
     * 영업 상태 평가
     *
     * @param openingHour 영업시간 정보
     * @param currentTimeMinutes 현재 시간 (분 단위)
     * @return 영업 상태
     */
    private StoreOperationStatus evaluateOperationStatus(
            StoreOpeningHour openingHour,
            int currentTimeMinutes
    ) {
        if (openingHour.isHoliday()) {
            return new StoreOperationStatus("휴무", false);
        }

        if (openingHour.openTime() == null || openingHour.closeTime() == null) {
            return new StoreOperationStatus("영업시간 미정", false);
        }

        int openMinutes = timeStringToMinutes(openingHour.openTime());
        int closeMinutes = timeStringToMinutes(openingHour.closeTime());
        int breakStartMinutes = openingHour.breakStartTime() != null
                ? timeStringToMinutes(openingHour.breakStartTime())
                : -1;
        int breakEndMinutes = openingHour.breakEndTime() != null
                ? timeStringToMinutes(openingHour.breakEndTime())
                : -1;

        // 휴게 시간 중
        if (breakStartMinutes >= 0 && breakEndMinutes >= 0
                && currentTimeMinutes >= breakStartMinutes
                && currentTimeMinutes < breakEndMinutes) {
            return new StoreOperationStatus("휴게중", false);
        }

        // 영업 중
        if (currentTimeMinutes >= openMinutes && currentTimeMinutes < closeMinutes) {
            return new StoreOperationStatus("영업중", true);
        }

        // 폐점
        return new StoreOperationStatus("폐점", false);
    }

    /**
     * 시간 문자열을 분 단위로 변환
     *
     * @param timeString 시간 문자열 (HH:MM:SS 형식)
     * @return 분 단위 시간
     */
    private int timeStringToMinutes(String timeString) {
        try {
            String[] parts = timeString.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            return hours * 60 + minutes;
        } catch (Exception e) {
            log.debug("Error parsing time string: {}", timeString, e);
            return 0;
        }
    }

    /**
     * 가게 운영 상태 정보
     *
     * @param status 상태 문자열 (예: "영업중", "휴무", "휴게중", "폐점")
     * @param isOpen 영업 여부
     */
    public record StoreOperationStatus(String status, boolean isOpen) {
    }
}

package com.stdev.smartmealtable.api.home.service;

import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.support.location.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 위치 맥락 정보 생성 서비스
 *
 * 사용자의 위치 정보와 가게 정보를 조합하여
 * 가게에 대한 위치 맥락 정보(contextInfo)를 생성합니다.
 *
 * @author SmartMealTable Team
 * @since 2025-11-12
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationContextService {

    private final DistanceCalculator distanceCalculator;

    /**
     * 가게에 대한 위치 맥락 정보 생성
     *
     * @param store 가게 정보
     * @param userLatitude 사용자 위도
     * @param userLongitude 사용자 경도
     * @return 위치 맥락 정보 (예: "1.5km 떨어진 강남역 인근")
     */
    public String generateLocationContext(
            Store store,
            Double userLatitude,
            Double userLongitude
    ) {
        if (store == null) {
            return null;
        }

        try {
            List<String> contextParts = new ArrayList<>();

            // 1. 거리 정보
            BigDecimal distance = calculateDistance(store, userLatitude, userLongitude);
            if (distance != null) {
                String distanceText = distanceCalculator.formatDistance(distance);
                contextParts.add(distanceText + " 떨어진");
            }

            // 2. 주소 정보 (지역명 추출)
            String areaName = extractAreaFromAddress(store.getAddress());
            if (areaName != null && !areaName.isEmpty()) {
                contextParts.add(areaName + " 인근");
            }

            return String.join(" ", contextParts);

        } catch (Exception e) {
            log.error("Error generating location context for store {}", store.getStoreId(), e);
            return null;
        }
    }

    /**
     * 거리 계산
     *
     * @param store 가게 정보
     * @param userLatitude 사용자 위도
     * @param userLongitude 사용자 경도
     * @return 거리 (킬로미터)
     */
    private BigDecimal calculateDistance(Store store, Double userLatitude, Double userLongitude) {
        if (userLatitude == null || userLongitude == null) {
            return null;
        }

        Double storeLatitude = store.getLatitude() != null ? store.getLatitude().doubleValue() : null;
        Double storeLongitude = store.getLongitude() != null ? store.getLongitude().doubleValue() : null;

        if (storeLatitude == null || storeLongitude == null) {
            return null;
        }

        try {
            return distanceCalculator.calculateDistanceKm(
                    userLatitude,
                    userLongitude,
                    storeLatitude,
                    storeLongitude
            );
        } catch (Exception e) {
            log.debug("Error calculating distance", e);
            return null;
        }
    }

    /**
     * 주소에서 지역명 추출
     *
     * 예: "서울시 강남구 강남역로 1-1" → "강남역"
     *
     * @param address 주소 정보
     * @return 지역명
     */
    private String extractAreaFromAddress(String address) {
        if (address == null || address.isEmpty()) {
            return null;
        }

        // 도로명주소(예: "서울시 강남구 강남역로 1-1")에서 주요 지역명 추출
        if (address.contains(" ")) {
            String[] parts = address.split(" ");
            // 보통 세 번째 부분이 도로명 또는 지역명
            if (parts.length >= 3) {
                String areaPart = parts[2];
                // "로", "길", "거리" 등의 도로 기호 제거
                return areaPart.replaceAll("(로|길|거리|街|街道)$", "");
            }
        }

        return null;
    }

    /**
     * 여러 가게에 대한 위치 맥락 정보 일괄 생성
     *
     * @param stores 가게 정보 목록
     * @param userLatitude 사용자 위도
     * @param userLongitude 사용자 경도
     * @return 가게 ID → 위치 맥락 정보 맵
     */
    public java.util.Map<Long, String> generateLocationContextBatch(
            List<Store> stores,
            Double userLatitude,
            Double userLongitude
    ) {
        java.util.Map<Long, String> result = new java.util.HashMap<>();

        for (Store store : stores) {
            String context = generateLocationContext(store, userLatitude, userLongitude);
            result.put(store.getStoreId(), context);
        }

        return result;
    }
}

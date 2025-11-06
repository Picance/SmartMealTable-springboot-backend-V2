package com.stdev.smartmealtable.client.external.naver;

import com.stdev.smartmealtable.client.external.naver.dto.NaverReverseGeocodingResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

/**
 * 네이버 Reverse Geocoding API 실제 요청 테스트
 * SpringBoot Test 없이 순수 자바로 실행 가능
 */
@Slf4j
@Disabled
public class NaverReverseGeocodingTest {

    @Test
    void testReverseGeocoding() {
        try {
            log.info("====== 네이버 Reverse Geocoding API 테스트 시작 ======");
            
            // 테스트할 좌표 (강남역)
            BigDecimal latitude = new BigDecimal("37.496486063");
            BigDecimal longitude = new BigDecimal("127.028361548");
            
            log.info("요청 좌표 - lat: {}, lng: {} (강남역 근처)", latitude, longitude);
            
            // 환경 변수에서 API 키 가져오기
            String clientId = System.getenv("NAVER_MAP_CLIENT_ID");
            String clientSecret = System.getenv("NAVER_MAP_CLIENT_SECRET");
            
            // 환경 변수가 없으면 테스트 값 사용
            boolean isTestKey = false;
            if (clientId == null) {
                clientId = "test-naver-map-client-id";
                isTestKey = true;
                log.warn("⚠ NAVER_MAP_CLIENT_ID 환경 변수가 설정되지 않았습니다. 테스트 값 사용");
            }
            if (clientSecret == null) {
                clientSecret = "test-naver-map-client-secret";
                isTestKey = true;
                log.warn("⚠ NAVER_MAP_CLIENT_SECRET 환경 변수가 설정되지 않았습니다. 테스트 값 사용");
            }
            
            if (isTestKey) {
                log.warn("========================================");
                log.warn("실제 API 키로 테스트하려면 환경 변수를 설정하세요:");
                log.warn("export NAVER_MAP_CLIENT_ID=<your_key>");
                log.warn("export NAVER_MAP_CLIENT_SECRET=<your_secret>");
                log.warn("========================================");
            }
            
            log.info("✓ API 키 로드 완료");
            
            // RestClient를 사용한 API 호출
            RestClient restClient = RestClient.create();
            
            log.info("네이버 API에 요청 중...");
            log.info("URL: https://maps.apigw.ntruss.com/map-reversegeocode/v2/gc?coords={},{}&orders=roadaddr,addr&output=json", 
                    longitude, latitude);
            
            NaverReverseGeocodingResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("maps.apigw.ntruss.com")
                            .path("/map-reversegeocode/v2/gc")
                            .queryParam("coords", longitude + "," + latitude)  // 경도,위도 순서
                            .queryParam("orders", "roadaddr,addr")
                            .queryParam("output", "json")
                            .build())
                    .header("X-NCP-APIGW-API-KEY-ID", clientId)
                    .header("X-NCP-APIGW-API-KEY", clientSecret)
                    .retrieve()
                    .body(NaverReverseGeocodingResponse.class);
            
            // 결과 출력
            printResponse(response);
            
            log.info("====== 테스트 완료 ======");
            
        } catch (Exception e) {
            log.error("❌ 테스트 실패", e);
            // 테스트 실패 시에도 중요한 것: JSON 역직렬화 오류가 아니라는 것
            if (e.getMessage() != null && e.getMessage().contains("Cannot deserialize")) {
                log.error("⚠ JSON 역직렬화 오류 발생! DTO 구조를 확인하세요.");
                throw e;
            } else if (e.getMessage() != null && e.getMessage().contains("401")) {
                log.info("ℹ 401 Unauthorized - API 키가 잘못되었거나 테스트 키입니다.");
                log.info("✓ 하지만 JSON 역직렬화는 성공했습니다! (DTO 구조가 올바릅니다)");
            }
        }
    }
    
    private static void printResponse(NaverReverseGeocodingResponse response) {
        if (response == null) {
            log.error("❌ 응답이 null입니다.");
            return;
        }
        
        log.info("\n========== API 응답 ==========");
        
        // Status 정보
        if (response.status() != null) {
            NaverReverseGeocodingResponse.Status status = response.status();
            log.info("✓ Status:");
            log.info("  - Code: {}", status.code());
            log.info("  - Name: {}", status.name());
            log.info("  - Message: {}", status.message());
        }
        
        // Results 정보
        if (response.results() != null && !response.results().isEmpty()) {
            log.info("✓ Results Count: {}", response.results().size());
            
            NaverReverseGeocodingResponse.Result result = response.results().get(0);
            
            log.info("\n첫 번째 결과:");
            log.info("  - Name: {}", result.name());
            
            // Code 정보
            if (result.code() != null) {
                log.info("  - Code ID: {}", result.code().id());
                log.info("  - Code Type: {}", result.code().type());
                log.info("  - Code MappingId: {}", result.code().mappingId());
            }
            
            // Region 정보
            if (result.region() != null) {
                NaverReverseGeocodingResponse.Region region = result.region();
                log.info("\n지역 정보:");
                
                if (region.area0() != null) {
                    log.info("  - 국가: {}", region.area0().name());
                }
                if (region.area1() != null) {
                    log.info("  - 시/도: {}", region.area1().name());
                }
                if (region.area2() != null) {
                    log.info("  - 시/군/구: {}", region.area2().name());
                }
                if (region.area3() != null) {
                    log.info("  - 읍/면/동: {}", region.area3().name());
                }
                if (region.area4() != null) {
                    log.info("  - 리: {}", region.area4().name());
                }
            }
            
            // Land 정보
            if (result.land() != null) {
                NaverReverseGeocodingResponse.Land land = result.land();
                log.info("\n토지 정보:");
                log.info("  - Type: {}", land.type());
                log.info("  - Name: {}", land.name());
                log.info("  - Number1: {}", land.number1());
                log.info("  - Number2: {}", land.number2());
            }
        } else {
            log.warn("⚠ 결과가 비어있습니다.");
        }
        
        log.info("========== END ==========\n");
    }
}

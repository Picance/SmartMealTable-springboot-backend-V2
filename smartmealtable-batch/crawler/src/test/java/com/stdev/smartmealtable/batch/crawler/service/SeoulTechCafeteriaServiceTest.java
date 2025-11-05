package com.stdev.smartmealtable.batch.crawler.service;

import com.stdev.smartmealtable.batch.crawler.domain.CampusCafeteriaData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SeoulTechCafeteriaService 통합 테스트
 * 
 * <p>실제 노션 페이지를 크롤링하는 테스트이므로 @Disabled로 비활성화되어 있습니다.
 * 필요 시 어노테이션을 제거하여 실행하세요.</p>
 */
@DisplayName("서울과학기술대학교 학식 크롤링 서비스 테스트")
class SeoulTechCafeteriaServiceTest {
    
    private final SeoulTechCafeteriaService crawlerService = new SeoulTechCafeteriaService();
    
    @Test
    @Disabled("실제 노션 페이지 크롤링 테스트 - 수동 실행용")
    @DisplayName("노션 페이지에서 학식 데이터를 크롤링한다")
    void crawlCafeteriaData_Success() {
        // When
        List<CampusCafeteriaData> result = crawlerService.crawlCafeteriaData();
        
        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2); // ST: Table, ST: Dining
        
        // ST: Table 검증
        CampusCafeteriaData stTable = result.stream()
                .filter(data -> data.getBuildingName().equals("ST: Table"))
                .findFirst()
                .orElseThrow();
        
        assertThat(stTable.getAddress()).contains("1학생회관");
        assertThat(stTable.getLatitude()).isNotNull();
        assertThat(stTable.getLongitude()).isNotNull();
        assertThat(stTable.getRestaurants()).isNotEmpty();
        
        // 가게 검증
        assertThat(stTable.getRestaurants()).anySatisfy(restaurant -> {
            assertThat(restaurant.getName()).isNotBlank();
            assertThat(restaurant.getCategoryName()).isNotBlank();
            assertThat(restaurant.getMenus()).isNotEmpty();
            
            // 메뉴 검증
            assertThat(restaurant.getMenus()).allSatisfy(menu -> {
                assertThat(menu.getName()).isNotBlank();
                assertThat(menu.getPrice()).isGreaterThan(0);
            });
        });
        
        // ST: Dining 검증
        CampusCafeteriaData stDining = result.stream()
                .filter(data -> data.getBuildingName().equals("ST: Dining"))
                .findFirst()
                .orElseThrow();
        
        assertThat(stDining.getAddress()).contains("2학생회관");
        assertThat(stDining.getRestaurants()).isNotEmpty();
        
        // 크롤링 결과 출력
        System.out.println("========================================");
        System.out.println("크롤링 결과:");
        System.out.println("========================================");
        
        for (CampusCafeteriaData data : result) {
            System.out.println("\n건물: " + data.getBuildingName());
            System.out.println("주소: " + data.getAddress());
            System.out.println("가게 수: " + data.getRestaurants().size());
            
            for (CampusCafeteriaData.RestaurantData restaurant : data.getRestaurants()) {
                System.out.println("\n  가게: " + restaurant.getName() + " (" + restaurant.getCategoryName() + ")");
                System.out.println("  메뉴:");
                
                for (CampusCafeteriaData.MenuData menu : restaurant.getMenus()) {
                    System.out.println("    - " + menu.getName() + ": " + menu.getPrice() + "원");
                }
            }
        }
    }
    
    @Test
    @DisplayName("크롤링 서비스 인스턴스를 생성할 수 있다")
    void createServiceInstance() {
        // Given & When
        SeoulTechCafeteriaService service = new SeoulTechCafeteriaService();
        
        // Then
        assertThat(service).isNotNull();
    }
}


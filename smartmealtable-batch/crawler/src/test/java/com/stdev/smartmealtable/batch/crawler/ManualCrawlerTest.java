package com.stdev.smartmealtable.batch.crawler;

import com.stdev.smartmealtable.batch.crawler.domain.CampusCafeteriaData;
import com.stdev.smartmealtable.batch.crawler.domain.CampusCafeteriaData.MenuData;
import com.stdev.smartmealtable.batch.crawler.domain.CampusCafeteriaData.RestaurantData;
import com.stdev.smartmealtable.batch.crawler.service.SeoulTechCafeteriaService;

import java.util.List;

/**
 * 수동 크롤링 테스트
 * 
 * <p>실제 노션 페이지를 크롤링하여 결과를 확인합니다.</p>
 * <p>IDE에서 main 메서드를 직접 실행하세요.</p>
 */
public class ManualCrawlerTest {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("서울과학기술대학교 학식 크롤링 테스트");
        System.out.println("========================================");
        System.out.println();
        
        try {
            // 크롤링 서비스 생성
            SeoulTechCafeteriaService crawlerService = new SeoulTechCafeteriaService();
            
            // 크롤링 실행
            System.out.println("크롤링 시작...");
            List<CampusCafeteriaData> result = crawlerService.crawlCafeteriaData();
            System.out.println("크롤링 완료!");
            System.out.println();
            
            // 결과 검증 및 출력
            if (result.isEmpty()) {
                System.out.println("❌ 크롤링 결과가 비어있습니다!");
                return;
            }
            
            System.out.println("✅ 크롤링 성공!");
            System.out.println("총 " + result.size() + "개 건물 발견");
            System.out.println();
            
            // 각 건물별 데이터 출력
            for (CampusCafeteriaData data : result) {
                printCafeteriaData(data);
            }
            
            // 통계 출력
            printStatistics(result);
            
        } catch (Exception e) {
            System.out.println();
            System.out.println("❌ 크롤링 중 오류 발생!");
            System.out.println("오류 메시지: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 학식당 데이터 출력
     */
    private static void printCafeteriaData(CampusCafeteriaData data) {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🏢 건물: " + data.getBuildingName());
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📍 주소: " + data.getAddress());
        System.out.println("🌍 위치: " + data.getLatitude() + ", " + data.getLongitude());
        System.out.println("🏪 가게 수: " + data.getRestaurants().size());
        System.out.println();
        
        if (data.getRestaurants().isEmpty()) {
            System.out.println("  ⚠️ 가게 정보가 없습니다.");
            System.out.println();
            return;
        }
        
        for (RestaurantData restaurant : data.getRestaurants()) {
            printRestaurantData(restaurant);
        }
    }
    
    /**
     * 가게 데이터 출력
     */
    private static void printRestaurantData(RestaurantData restaurant) {
        System.out.println("  ┌─────────────────────────────────────");
        System.out.println("  │ 🍽️  가게: " + restaurant.getName());
        System.out.println("  │ 🏷️  카테고리: " + restaurant.getCategoryName());
        System.out.println("  │ 📋 메뉴 수: " + restaurant.getMenus().size());
        System.out.println("  └─────────────────────────────────────");
        
        if (restaurant.getMenus().isEmpty()) {
            System.out.println("      ⚠️ 메뉴 정보가 없습니다.");
        } else {
            // 평균 가격 계산
            int totalPrice = restaurant.getMenus().stream()
                    .mapToInt(MenuData::getPrice)
                    .sum();
            int avgPrice = totalPrice / restaurant.getMenus().size();
            
            System.out.println("      💰 평균 가격: " + String.format("%,d", avgPrice) + "원");
            System.out.println();
            System.out.println("      메뉴 목록:");
            
            for (MenuData menu : restaurant.getMenus()) {
                System.out.printf("        • %-20s %,6d원%n", 
                        menu.getName(), menu.getPrice());
            }
        }
        System.out.println();
    }
    
    /**
     * 통계 출력
     */
    private static void printStatistics(List<CampusCafeteriaData> result) {
        System.out.println("========================================");
        System.out.println("📊 크롤링 통계");
        System.out.println("========================================");
        
        int totalRestaurants = result.stream()
                .mapToInt(data -> data.getRestaurants().size())
                .sum();
        
        int totalMenus = result.stream()
                .flatMap(data -> data.getRestaurants().stream())
                .mapToInt(restaurant -> restaurant.getMenus().size())
                .sum();
        
        int minPrice = result.stream()
                .flatMap(data -> data.getRestaurants().stream())
                .flatMap(restaurant -> restaurant.getMenus().stream())
                .mapToInt(MenuData::getPrice)
                .min()
                .orElse(0);
        
        int maxPrice = result.stream()
                .flatMap(data -> data.getRestaurants().stream())
                .flatMap(restaurant -> restaurant.getMenus().stream())
                .mapToInt(MenuData::getPrice)
                .max()
                .orElse(0);
        
        double avgPrice = result.stream()
                .flatMap(data -> data.getRestaurants().stream())
                .flatMap(restaurant -> restaurant.getMenus().stream())
                .mapToInt(MenuData::getPrice)
                .average()
                .orElse(0.0);
        
        System.out.println("총 건물 수: " + result.size());
        System.out.println("총 가게 수: " + totalRestaurants);
        System.out.println("총 메뉴 수: " + totalMenus);
        System.out.println();
        System.out.println("최저 가격: " + String.format("%,d", minPrice) + "원");
        System.out.println("최고 가격: " + String.format("%,d", maxPrice) + "원");
        System.out.println("평균 가격: " + String.format("%,.0f", avgPrice) + "원");
        System.out.println("========================================");
    }
}


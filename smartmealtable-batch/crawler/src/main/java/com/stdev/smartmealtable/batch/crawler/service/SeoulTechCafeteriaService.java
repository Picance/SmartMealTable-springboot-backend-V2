package com.stdev.smartmealtable.batch.crawler.service;

import com.stdev.smartmealtable.batch.crawler.domain.CampusCafeteriaData;
import com.stdev.smartmealtable.batch.crawler.domain.CampusCafeteriaData.MenuData;
import com.stdev.smartmealtable.batch.crawler.domain.CampusCafeteriaData.RestaurantData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 서울과학기술대학교 학식 정보 크롤링 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeoulTechCafeteriaService {
    
    private static final String NOTION_URL = "https://fern-magic-bde.notion.site/21e45244ac0a80fdb02ad064ce75d674";
    
    // ST: Table (1학생회관)
    private static final String ST_TABLE_ADDRESS = "서울 노원구 공릉로 232 1학생회관 1층";
    private static final BigDecimal ST_TABLE_LATITUDE = new BigDecimal("37.6335837919849");
    private static final BigDecimal ST_TABLE_LONGITUDE = new BigDecimal("127.07689204595525");
    
    // ST: Dining (2학생회관)
    private static final String ST_DINING_ADDRESS = "서울 노원구 공릉로 232 2학생회관 1층";
    private static final BigDecimal ST_DINING_LATITUDE = new BigDecimal("37.62985806656512");
    private static final BigDecimal ST_DINING_LONGITUDE = new BigDecimal("127.07932350755101");
    
    /**
     * 노션 페이지에서 학식 데이터를 크롤링합니다.
     */
    public List<CampusCafeteriaData> crawlCafeteriaData() {
        log.info("서울과학기술대학교 학식 데이터 크롤링 시작: {}", NOTION_URL);
        
        try {
            Document document = Jsoup.connect(NOTION_URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();
            
            List<CampusCafeteriaData> result = new ArrayList<>();
            
            // ST: Table 데이터 파싱
            CampusCafeteriaData stTable = parseStTable(document);
            if (stTable != null) {
                result.add(stTable);
            }
            
            // ST: Dining 데이터 파싱
            CampusCafeteriaData stDining = parseStDining(document);
            if (stDining != null) {
                result.add(stDining);
            }
            
            log.info("크롤링 완료: {} 개 건물, 총 {} 개 가게", 
                    result.size(), 
                    result.stream().mapToInt(c -> c.getRestaurants().size()).sum());
            
            return result;
            
        } catch (Exception e) {
            log.error("학식 데이터 크롤링 중 오류 발생", e);
            throw new RuntimeException("학식 데이터 크롤링 실패", e);
        }
    }
    
    /**
     * ST: Table (1학생회관) 데이터 파싱
     */
    private CampusCafeteriaData parseStTable(Document document) {
        log.info("ST: Table 데이터 파싱 시작");
        
        List<RestaurantData> restaurants = new ArrayList<>();
        
        // 가게별 데이터 파싱
        restaurants.add(parseRestaurant(document, "값찌개", "한식"));
        restaurants.add(parseRestaurant(document, "경성카츠", "일식"));
        restaurants.add(parseRestaurant(document, "바비든든", "한식"));
        restaurants.add(parseRestaurant(document, "포포420", "아시안"));
        restaurants.add(parseRestaurant(document, "뽀까뽀까", "한식"));
        
        return CampusCafeteriaData.builder()
                .buildingName("ST: Table")
                .address(ST_TABLE_ADDRESS)
                .latitude(ST_TABLE_LATITUDE)
                .longitude(ST_TABLE_LONGITUDE)
                .restaurants(restaurants.stream().filter(r -> !r.getMenus().isEmpty()).toList())
                .build();
    }
    
    /**
     * ST: Dining (2학생회관) 데이터 파싱
     */
    private CampusCafeteriaData parseStDining(Document document) {
        log.info("ST: Dining 데이터 파싱 시작");
        
        List<RestaurantData> restaurants = new ArrayList<>();
        
        // 가게별 데이터 파싱 (다른 지점)
        restaurants.add(parseRestaurant(document, "값찌개", "한식"));
        restaurants.add(parseRestaurant(document, "경성카츠", "일식"));
        restaurants.add(parseRestaurant(document, "중식대장", "중식"));
        restaurants.add(parseRestaurant(document, "키친101", "양식"));
        restaurants.add(parseRestaurant(document, "플라잉팬", "한식"));
        
        return CampusCafeteriaData.builder()
                .buildingName("ST: Dining")
                .address(ST_DINING_ADDRESS)
                .latitude(ST_DINING_LATITUDE)
                .longitude(ST_DINING_LONGITUDE)
                .restaurants(restaurants.stream().filter(r -> !r.getMenus().isEmpty()).toList())
                .build();
    }
    
    /**
     * 가게 데이터 파싱
     */
    private RestaurantData parseRestaurant(Document document, String restaurantName, String categoryName) {
        List<MenuData> menus = new ArrayList<>();
        
        try {
            // 노션 페이지에서 가게명을 포함한 섹션 찾기
            Elements headers = document.select("h3, h2, div[data-block-id]");
            
            for (Element header : headers) {
                String headerText = header.text();
                
                if (headerText.contains(restaurantName)) {
                    // 해당 가게의 메뉴 테이블 찾기
                    Element parent = header.parent();
                    if (parent != null) {
                        Elements tables = parent.select("table");
                        if (!tables.isEmpty()) {
                            menus.addAll(parseMenuTable(tables.first()));
                        } else {
                            // 테이블이 아닌 경우 다음 형제 요소들 탐색
                            Element sibling = header.nextElementSibling();
                            while (sibling != null && menus.isEmpty()) {
                                if (sibling.tagName().equals("table")) {
                                    menus.addAll(parseMenuTable(sibling));
                                    break;
                                }
                                Elements siblingTables = sibling.select("table");
                                if (!siblingTables.isEmpty()) {
                                    menus.addAll(parseMenuTable(siblingTables.first()));
                                    break;
                                }
                                sibling = sibling.nextElementSibling();
                            }
                        }
                    }
                    
                    if (!menus.isEmpty()) {
                        break;
                    }
                }
            }
            
            // 노션 페이지의 구조에 따라 대안 파싱 방법
            if (menus.isEmpty()) {
                // 전체 텍스트에서 가게명 찾기
                String bodyText = document.body().text();
                if (bodyText.contains(restaurantName)) {
                    Elements allTables = document.select("table");
                    for (Element table : allTables) {
                        String tableText = table.text();
                        if (tableText.contains(restaurantName) || isPotentialMenuTable(table)) {
                            List<MenuData> parsedMenus = parseMenuTable(table);
                            if (!parsedMenus.isEmpty()) {
                                menus.addAll(parsedMenus);
                                break;
                            }
                        }
                    }
                }
            }
            
            log.info("가게 '{}' 파싱 완료: {} 개 메뉴", restaurantName, menus.size());
            
        } catch (Exception e) {
            log.error("가게 '{}' 파싱 중 오류 발생", restaurantName, e);
        }
        
        return RestaurantData.builder()
                .name(restaurantName)
                .categoryName(categoryName)
                .menus(menus)
                .build();
    }
    
    /**
     * 메뉴 테이블이 실제 메뉴 정보를 담고 있는지 확인
     */
    private boolean isPotentialMenuTable(Element table) {
        String text = table.text().toLowerCase();
        return text.contains("원") || text.contains("price") || text.contains("menu");
    }
    
    /**
     * 테이블에서 메뉴 데이터 파싱
     */
    private List<MenuData> parseMenuTable(Element table) {
        List<MenuData> menus = new ArrayList<>();
        
        try {
            Elements rows = table.select("tr");
            
            for (Element row : rows) {
                Elements cells = row.select("td, th");
                
                if (cells.size() >= 2) {
                    String menuName = cells.get(0).text().trim();
                    String priceText = cells.get(1).text().trim();
                    
                    // 헤더 행 스킵
                    if (menuName.equalsIgnoreCase("메뉴") || menuName.equalsIgnoreCase("menu") ||
                        priceText.equalsIgnoreCase("가격") || priceText.equalsIgnoreCase("price")) {
                        continue;
                    }
                    
                    // 가격 파싱 (숫자만 추출)
                    Integer price = parsePrice(priceText);
                    
                    if (!menuName.isEmpty() && price != null && price > 0) {
                        menus.add(MenuData.builder()
                                .name(menuName)
                                .price(price)
                                .build());
                        
                        log.debug("메뉴 파싱: {} - {}원", menuName, price);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("테이블 파싱 중 오류 발생", e);
        }
        
        return menus;
    }
    
    /**
     * 가격 문자열에서 숫자 추출
     */
    private Integer parsePrice(String priceText) {
        try {
            // 숫자만 추출
            String numbers = priceText.replaceAll("[^0-9]", "");
            if (!numbers.isEmpty()) {
                return Integer.parseInt(numbers);
            }
        } catch (NumberFormatException e) {
            log.warn("가격 파싱 실패: {}", priceText);
        }
        return null;
    }
}


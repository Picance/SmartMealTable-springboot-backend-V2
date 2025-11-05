package com.stdev.smartmealtable.batch.crawler.service;

import com.stdev.smartmealtable.batch.crawler.domain.CampusCafeteriaData;
import com.stdev.smartmealtable.batch.crawler.domain.CampusCafeteriaData.MenuData;
import com.stdev.smartmealtable.batch.crawler.domain.CampusCafeteriaData.RestaurantData;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Selenium을 사용한 노션 페이지 크롤링 서비스
 * 
 * <p>JavaScript로 렌더링되는 노션 페이지를 실제 브라우저로 열어서 크롤링합니다.</p>
 */
@Slf4j
@Service
public class SeleniumCrawlerService {
    
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
        log.info("Selenium 크롤링 시작: {}", NOTION_URL);
        
        WebDriver driver = null;
        try {
            // WebDriver 설정
            driver = setupWebDriver();
            
            // 노션 페이지 열기
            driver.get(NOTION_URL);
            log.info("페이지 로드 완료");
            
            // 페이지 렌더링 대기 (JavaScript 실행 대기)
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            // 추가 대기 (노션 콘텐츠 로딩)
            Thread.sleep(3000);
            
            // 페이지 스크롤하여 모든 컨텐츠 로드 (지연 로딩 대응)
            log.info("페이지 스크롤 중...");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // 여러 번 스크롤하여 지연 로딩 컨텐츠 모두 로드
            for (int i = 0; i < 3; i++) {
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                Thread.sleep(1000);
            }
            
            // 맨 위로 스크롤
            js.executeScript("window.scrollTo(0, 0);");
            Thread.sleep(1000);
            
            log.info("스크롤 완료");
            
            // 페이지 텍스트 확인
            String pageText = driver.findElement(By.tagName("body")).getText();
            log.info("페이지 텍스트 길이: {} 문자", pageText.length());
            log.debug("페이지 텍스트 샘플: {}", 
                    pageText.substring(0, Math.min(500, pageText.length())));
            
            // 데이터 파싱
            List<CampusCafeteriaData> result = new ArrayList<>();
            
            // ST: Table 데이터 파싱
            CampusCafeteriaData stTable = parseStTable(driver);
            if (stTable != null && !stTable.getRestaurants().isEmpty()) {
                result.add(stTable);
            }
            
            // ST: Dining 데이터 파싱
            CampusCafeteriaData stDining = parseStDining(driver);
            if (stDining != null && !stDining.getRestaurants().isEmpty()) {
                result.add(stDining);
            }
            
            log.info("크롤링 완료: {} 개 건물, 총 {} 개 가게", 
                    result.size(), 
                    result.stream().mapToInt(c -> c.getRestaurants().size()).sum());
            
            return result;
            
        } catch (Exception e) {
            log.error("Selenium 크롤링 중 오류 발생", e);
            throw new RuntimeException("Selenium 크롤링 실패", e);
        } finally {
            if (driver != null) {
                driver.quit();
                log.info("WebDriver 종료");
            }
        }
    }
    
    /**
     * WebDriver 설정 (Headless Chrome)
     */
    private WebDriver setupWebDriver() {
        log.info("WebDriver 초기화 중...");
        
        // ChromeDriver 자동 다운로드 및 설정
        WebDriverManager.chromedriver().setup();
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // 창 없이 실행
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        
        return new ChromeDriver(options);
    }
    
    /**
     * ST: Table (1학생회관) 데이터 파싱
     */
    private CampusCafeteriaData parseStTable(WebDriver driver) {
        log.info("ST: Table 데이터 파싱 시작");
        
        String bodyText = driver.findElement(By.tagName("body")).getText();
        
        List<RestaurantConfig> configs = List.of(
                new RestaurantConfig("값찌개", "한식"),
                new RestaurantConfig("경성카츠", "일식"),
                new RestaurantConfig("바비든든", "한식"),
                new RestaurantConfig("포포420", "아시안"),
                new RestaurantConfig("뽀까뽀까", "한식")
        );
        
        List<RestaurantData> restaurants = parseBoardViewSection(bodyText, "ST: Table", configs);
        
        return CampusCafeteriaData.builder()
                .buildingName("ST: Table")
                .address(ST_TABLE_ADDRESS)
                .latitude(ST_TABLE_LATITUDE)
                .longitude(ST_TABLE_LONGITUDE)
                .restaurants(restaurants)
                .build();
    }
    
    /**
     * ST: Dining (2학생회관) 데이터 파싱
     */
    private CampusCafeteriaData parseStDining(WebDriver driver) {
        log.info("ST: Dining 데이터 파싱 시작");
        
        String bodyText = driver.findElement(By.tagName("body")).getText();
        
        List<RestaurantConfig> configs = List.of(
                new RestaurantConfig("값찌개", "한식"),
                new RestaurantConfig("경성카츠", "일식"),
                new RestaurantConfig("중식대장", "중식"),
                new RestaurantConfig("키친101", "양식"),
                new RestaurantConfig("플라잉팬", "한식")
        );
        
        List<RestaurantData> restaurants = parseBoardViewSection(bodyText, "ST: Dining", configs);
        
        return CampusCafeteriaData.builder()
                .buildingName("ST: Dining")
                .address(ST_DINING_ADDRESS)
                .latitude(ST_DINING_LATITUDE)
                .longitude(ST_DINING_LONGITUDE)
                .restaurants(restaurants)
                .build();
    }
    
    /**
     * 특수 키워드인지 확인 (메뉴명이 아닌 것들)
     */
    private boolean isSpecialKeyword(String line) {
        return line.matches("\\d+") ||  // 숫자만
               line.equals("Board") ||
               line.contains("메뉴판") ||
               line.contains("최종 편집") ||
               line.contains("Get Notion");
    }
    
    /**
     * 가격 문자열에서 숫자 추출
     */
    private Integer parsePrice(String priceText) {
        try {
            String numbers = priceText.replaceAll("[^0-9]", "");
            if (!numbers.isEmpty()) {
                return Integer.parseInt(numbers);
            }
        } catch (NumberFormatException e) {
            log.warn("가격 파싱 실패: {}", priceText);
        }
        return null;
    }
    
    /**
     * Board 뷰 방식 파싱 (노션 특성상 가게 목록이 먼저 나오고, 메뉴가 나중에 일괄 나열됨)
     * 
     * 구조:
     * - 가게1 이름
     * - 가게1 메뉴 개수
     * - 가게2 이름
     * - 가게2 메뉴 개수
     * - ...
     * - [모든 가게의 메뉴들이 순서대로 나열]
     */
    private List<RestaurantData> parseBoardViewSection(String bodyText, String sectionName, List<RestaurantConfig> configs) {
        List<RestaurantData> results = new ArrayList<>();
        
        try {
            String[] lines = bodyText.split("\n");
            
            // 1단계: 섹션 시작 위치 찾기
            int sectionStart = -1;
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].trim().equals(sectionName + " 메뉴판")) {
                    sectionStart = i;
                    break;
                }
            }
            
            if (sectionStart == -1) {
                log.warn("섹션 '{}'을 찾을 수 없습니다", sectionName);
                return results;
            }
            
            // 2단계: 각 가게의 메뉴 개수 추출
            List<RestaurantMenuCount> menuCounts = new ArrayList<>();
            int idx = sectionStart + 2; // "섹션 메뉴판" -> "Board" -> 시작
            
            for (RestaurantConfig config : configs) {
                // 가게명 찾기
                while (idx < lines.length && !lines[idx].trim().equals(config.name)) {
                    idx++;
                }
                
                if (idx >= lines.length) {
                    log.warn("가게 '{}'를 찾을 수 없습니다", config.name);
                    continue;
                }
                
                idx++; // 다음 줄 (메뉴 개수)
                
                if (idx < lines.length && lines[idx].trim().matches("\\d+")) {
                    int count = Integer.parseInt(lines[idx].trim());
                    menuCounts.add(new RestaurantMenuCount(config.name, config.category, count));
                    log.debug("가게 '{}': {} 개 메뉴 예상", config.name, count);
                }
                idx++;
            }
            
            // 3단계: 메뉴 데이터 수집 시작 위치 찾기
            // 마지막 가게명 + 개수 다음부터 메뉴 데이터 시작
            int menuDataStart = idx;
            
            // 4단계: 메뉴 데이터 추출 (메뉴명, 가격 교대로 나옴)
            List<MenuData> allMenus = new ArrayList<>();
            
            for (int i = menuDataStart; i < lines.length; i++) {
                String line = lines[i].trim();
                
                // 다음 섹션 시작하면 종료
                if (line.endsWith(" 메뉴판") || line.equals("Board")) {
                    break;
                }
                
                // 가격 라인 아님 (메뉴명)
                if (!line.startsWith("₩") && !line.isEmpty() && !isSpecialKeyword(line)) {
                    String menuName = line;
                    
                    // 다음 줄이 가격인지 확인
                    if (i + 1 < lines.length) {
                        String nextLine = lines[i + 1].trim();
                        if (nextLine.startsWith("₩")) {
                            Integer price = parsePrice(nextLine);
                            if (price != null && price > 0) {
                                allMenus.add(MenuData.builder()
                                        .name(menuName)
                                        .price(price)
                                        .build());
                                i++; // 가격 줄 스킵
                            }
                        }
                    }
                }
            }
            
            log.info("섹션 '{}': 총 {} 개 메뉴 추출", sectionName, allMenus.size());
            
            // 5단계: 메뉴를 가게별로 분배
            int menuIndex = 0;
            for (RestaurantMenuCount rmc : menuCounts) {
                List<MenuData> restaurantMenus = new ArrayList<>();
                
                for (int i = 0; i < rmc.count && menuIndex < allMenus.size(); i++) {
                    restaurantMenus.add(allMenus.get(menuIndex));
                    menuIndex++;
                }
                
                results.add(RestaurantData.builder()
                        .name(rmc.name)
                        .categoryName(rmc.category)
                        .menus(restaurantMenus)
                        .build());
                
                log.info("가게 '{}': {} 개 메뉴 할당", rmc.name, restaurantMenus.size());
            }
            
        } catch (Exception e) {
            log.error("Board 뷰 파싱 중 오류", e);
        }
        
        return results;
    }
    
    /**
     * 가게 설정 (이름, 카테고리)
     */
    private static class RestaurantConfig {
        final String name;
        final String category;
        
        RestaurantConfig(String name, String category) {
            this.name = name;
            this.category = category;
        }
    }
    
    /**
     * 가게별 메뉴 개수
     */
    private static class RestaurantMenuCount {
        final String name;
        final String category;
        final int count;
        
        RestaurantMenuCount(String name, String category, int count) {
            this.name = name;
            this.category = category;
            this.count = count;
        }
    }
}


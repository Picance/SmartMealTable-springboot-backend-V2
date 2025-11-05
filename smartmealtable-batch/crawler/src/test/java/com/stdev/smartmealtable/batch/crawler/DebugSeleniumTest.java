package com.stdev.smartmealtable.batch.crawler;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileWriter;
import java.time.Duration;
import java.util.List;

/**
 * Selenium으로 실제로 어떤 HTML이 로드되는지 확인하는 디버그 테스트
 */
public class DebugSeleniumTest {
    
    public static void main(String[] args) {
        String url = "https://fern-magic-bde.notion.site/21e45244ac0a80fdb02ad064ce75d674";
        
        WebDriver driver = null;
        try {
            System.out.println("========================================");
            System.out.println("Selenium 디버그 테스트");
            System.out.println("========================================");
            System.out.println("URL: " + url);
            System.out.println();
            
            // WebDriver 설정
            WebDriverManager.chromedriver().setup();
            
            ChromeOptions options = new ChromeOptions();
            // Headless 모드 비활성화 (실제 창을 보기 위해)
            // options.addArguments("--headless");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");
            
            driver = new ChromeDriver(options);
            
            System.out.println("브라우저 시작...");
            driver.get(url);
            
            // 페이지 로드 대기
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            System.out.println("페이지 로드 완료!");
            System.out.println();
            
            // 추가 대기 (노션 렌더링)
            System.out.println("노션 렌더링 대기 중... (5초)");
            Thread.sleep(5000);
            
            // 페이지 정보 수집
            System.out.println("========================================");
            System.out.println("페이지 정보");
            System.out.println("========================================");
            System.out.println("Title: " + driver.getTitle());
            System.out.println("Current URL: " + driver.getCurrentUrl());
            System.out.println();
            
            // Body 텍스트
            WebElement body = driver.findElement(By.tagName("body"));
            String bodyText = body.getText();
            System.out.println("Body 텍스트 길이: " + bodyText.length() + " 문자");
            System.out.println();
            
            // 텍스트 샘플 출력
            System.out.println("Body 텍스트 샘플 (처음 1000자):");
            System.out.println("----------------------------------------");
            System.out.println(bodyText.substring(0, Math.min(1000, bodyText.length())));
            System.out.println("...");
            System.out.println("----------------------------------------");
            System.out.println();
            
            // 키워드 검색
            System.out.println("키워드 검색:");
            System.out.println("----------------------------------------");
            checkKeyword(bodyText, "ST: Table");
            checkKeyword(bodyText, "ST: Dining");
            checkKeyword(bodyText, "1학생회관");
            checkKeyword(bodyText, "2학생회관");
            checkKeyword(bodyText, "값찌개");
            checkKeyword(bodyText, "경성카츠");
            checkKeyword(bodyText, "김치찌개");
            System.out.println("----------------------------------------");
            System.out.println();
            
            // HTML 구조 분석
            System.out.println("HTML 구조 분석:");
            System.out.println("----------------------------------------");
            List<WebElement> tables = driver.findElements(By.tagName("table"));
            System.out.println("테이블 개수: " + tables.size());
            
            List<WebElement> divs = driver.findElements(By.tagName("div"));
            System.out.println("div 개수: " + divs.size());
            
            List<WebElement> h1s = driver.findElements(By.tagName("h1"));
            System.out.println("h1 개수: " + h1s.size());
            
            List<WebElement> h2s = driver.findElements(By.tagName("h2"));
            System.out.println("h2 개수: " + h2s.size());
            
            List<WebElement> h3s = driver.findElements(By.tagName("h3"));
            System.out.println("h3 개수: " + h3s.size());
            System.out.println("----------------------------------------");
            System.out.println();
            
            // 노션 특정 클래스 확인
            System.out.println("노션 특정 요소 확인:");
            System.out.println("----------------------------------------");
            checkNotionElements(driver);
            System.out.println("----------------------------------------");
            System.out.println();
            
            // HTML 파일로 저장
            String html = driver.getPageSource();
            String filename = "notion-page-debug.html";
            try (FileWriter writer = new FileWriter(filename)) {
                writer.write(html);
                System.out.println("✅ HTML이 " + filename + " 파일로 저장되었습니다!");
                System.out.println("   브라우저에서 열어서 확인하세요.");
            } catch (Exception e) {
                System.out.println("❌ HTML 저장 실패: " + e.getMessage());
            }
            System.out.println();
            
            // 스크린샷 저장 (선택)
            // File screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
            // Files.copy(screenshot.toPath(), new File("notion-page-screenshot.png").toPath());
            
            System.out.println("========================================");
            System.out.println("💡 다음 단계:");
            System.out.println("========================================");
            System.out.println("1. notion-page-debug.html 파일을 브라우저로 열어보세요");
            System.out.println("2. 페이지에 메뉴 데이터가 있는지 확인");
            System.out.println("3. 로그인이 필요한지 확인");
            System.out.println("4. 페이지 구조를 파악하여 파싱 로직 수정");
            System.out.println();
            
            // 10초 대기 (창을 보기 위해)
            System.out.println("브라우저 창을 확인하세요... (10초 후 종료)");
            Thread.sleep(10000);
            
        } catch (Exception e) {
            System.out.println("❌ 오류 발생!");
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
                System.out.println("브라우저 종료");
            }
        }
    }
    
    private static void checkKeyword(String text, String keyword) {
        if (text.contains(keyword)) {
            System.out.println("  ✅ '" + keyword + "' 발견");
        } else {
            System.out.println("  ❌ '" + keyword + "' 없음");
        }
    }
    
    private static void checkNotionElements(WebDriver driver) {
        // 노션 페이지의 일반적인 클래스명들
        String[] notionClasses = {
            "notion-page-content",
            "notion-table",
            "notion-table-view",
            "notion-collection",
            "notion-selectable",
            "notion-text-block"
        };
        
        for (String className : notionClasses) {
            try {
                List<WebElement> elements = driver.findElements(By.className(className));
                if (!elements.isEmpty()) {
                    System.out.println("  ✅ ." + className + ": " + elements.size() + "개");
                }
            } catch (Exception e) {
                // 무시
            }
        }
        
        // data-block-id 속성 확인
        try {
            List<WebElement> dataBlocks = driver.findElements(By.cssSelector("[data-block-id]"));
            if (!dataBlocks.isEmpty()) {
                System.out.println("  ✅ [data-block-id]: " + dataBlocks.size() + "개");
            }
        } catch (Exception e) {
            // 무시
        }
    }
}


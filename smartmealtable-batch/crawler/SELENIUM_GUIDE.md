# Selenium 크롤러 가이드

노션 페이지는 JavaScript로 동적 렌더링되기 때문에 Selenium을 사용해야 합니다.

## 🤔 왜 Selenium이 필요한가?

### JSoup의 한계
```
JSoup (❌)
  └─ 정적 HTML만 파싱
  └─ JavaScript 실행 불가
  └─ 노션 페이지는 빈 HTML만 받아짐
```

### Selenium의 장점
```
Selenium (✅)
  └─ 실제 브라우저 실행
  └─ JavaScript 렌더링 대기
  └─ 완전히 렌더링된 페이지 크롤링
```

## 📦 필요한 의존성

```gradle
// Selenium WebDriver
implementation 'org.seleniumhq.selenium:selenium-java:4.15.0'
implementation 'io.github.bonigarcia:webdrivermanager:5.6.2'
```

## 🔧 설치 (Mac/Linux)

### Chrome 브라우저 설치
```bash
# Mac
brew install --cask google-chrome

# Ubuntu/Debian
wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
sudo dpkg -i google-chrome-stable_current_amd64.deb
```

**또는** Chrome이 이미 설치되어 있으면 추가 설치 불필요!

## 🚀 사용 방법

### 스케줄러에서 사용

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class CafeteriaCrawlerScheduler {
    
    // JSoup 서비스 대신 Selenium 서비스 사용
    private final SeleniumCrawlerService seleniumCrawler;
    private final CafeteriaDataImportService importService;
    
    @Scheduled(cron = "0 0 2 * * MON")
    public void crawlAndUpdateCafeteriaData() {
        // Selenium으로 크롤링
        List<CampusCafeteriaData> data = seleniumCrawler.crawlCafeteriaData();
        
        // DB 저장
        importService.importCafeteriaData(data);
    }
}
```

### 수동 테스트

```java
public class TestSeleniumCrawler {
    public static void main(String[] args) {
        SeleniumCrawlerService crawler = new SeleniumCrawlerService();
        List<CampusCafeteriaData> result = crawler.crawlCafeteriaData();
        
        // 결과 확인
        result.forEach(building -> {
            System.out.println(building.getBuildingName());
            building.getRestaurants().forEach(restaurant -> {
                System.out.println("  " + restaurant.getName());
                restaurant.getMenus().forEach(menu -> {
                    System.out.println("    " + menu.getName() + ": " + menu.getPrice());
                });
            });
        });
    }
}
```

## ⚙️ Headless 모드

서버 환경(GUI 없음)에서는 자동으로 Headless 모드로 실행됩니다:

```java
ChromeOptions options = new ChromeOptions();
options.addArguments("--headless");        // 창 없이 실행
options.addArguments("--no-sandbox");      // 샌드박스 비활성화 (서버용)
options.addArguments("--disable-dev-shm-usage"); // 메모리 문제 해결
```

## 🐳 Docker 환경

Docker에서 실행 시 추가 설정이 필요합니다:

### Dockerfile
```dockerfile
FROM openjdk:17-slim

# Chrome 설치
RUN apt-get update && apt-get install -y \
    wget \
    gnupg \
    && wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - \
    && echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list \
    && apt-get update \
    && apt-get install -y google-chrome-stable \
    && rm -rf /var/lib/apt/lists/*

# 애플리케이션 실행
COPY build/libs/crawler.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🔍 트러블슈팅

### 1. ChromeDriver 오류
```
오류: Could not start a new session
```
**해결**: WebDriverManager가 자동으로 처리하므로 대부분 해결됨
```java
WebDriverManager.chromedriver().setup(); // 자동 다운로드
```

### 2. Headless 모드 오류
```
오류: unknown error: Chrome failed to start
```
**해결**: 
```bash
# Chrome 설치 확인
google-chrome --version

# 또는 chromium 사용
sudo apt-get install chromium-browser
```

### 3. 메모리 부족
```
오류: session deleted because of page crash
```
**해결**: 메모리 설정 추가
```java
options.addArguments("--disable-dev-shm-usage");
options.addArguments("--memory-pressure-off");
```

### 4. 권한 오류 (Linux)
```
오류: Permission denied
```
**해결**:
```bash
# Chrome 실행 권한
chmod +x /usr/bin/google-chrome

# 또는 샌드박스 비활성화
options.addArguments("--no-sandbox");
```

## 📊 성능

| 방식 | 속도 | 메모리 | 정확도 |
|------|------|--------|--------|
| JSoup | 매우 빠름 (1초) | 낮음 (10MB) | ❌ 노션 불가 |
| Selenium | 느림 (5-10초) | 높음 (200MB) | ✅ 완벽 |
| Notion API | 빠름 (2초) | 낮음 (20MB) | ✅ 완벽 |

## 💡 대안: Notion API 사용

가장 권장하는 방법입니다:

1. **Notion Integration 생성**: https://www.notion.so/my-integrations
2. **페이지에 Integration 연결**
3. **API로 데이터 조회**

```java
// Notion SDK 사용
Client notion = new Client(System.getenv("NOTION_TOKEN"));
Page page = notion.retrievePage("페이지ID");
```

## 📝 실제 운영 권장사항

1. **학교 측에 데이터 제공 요청** (가장 좋음)
   - CSV/JSON 파일 제공
   - 또는 API 엔드포인트 제공

2. **Notion API 사용** (두 번째 좋음)
   - 공식 API로 안정적
   - 권한만 받으면 쉽게 구현

3. **Selenium 사용** (세 번째)
   - 다른 방법이 안될 때만
   - 서버 리소스 많이 사용

4. **수동 업데이트** (최후의 수단)
   - 주 1회 정도는 수동으로도 가능

---

문의사항이 있으면 Selenium 로그를 첨부해주세요!


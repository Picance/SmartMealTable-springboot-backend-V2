# 노션 크롤링 해결 방안

메뉴를 찾지 못하는 이유와 해결 방법을 정리했습니다.

## 🔍 문제 원인

페이지는 로드되었지만 메뉴 데이터를 찾지 못했습니다:
- 페이지 텍스트: 1773자 (너무 적음)
- 모든 메뉴: 0개

### 가능한 원인

1. **로그인 필요**: 노션 페이지가 비공개일 수 있음
2. **렌더링 미완료**: JavaScript 렌더링이 완료되지 않음
3. **페이지 구조 변경**: 예상한 HTML 구조와 다름
4. **접근 제한**: 봇 차단 또는 IP 제한

## 🎯 해결 방법

### 방법 1: HTML 구조 확인 (필수!)

```bash
# 디버그 테스트 실행
cd smartmealtable-batch/crawler
./gradlew test --tests DebugSeleniumTest

# 결과 확인
# 1. notion-page-debug.html 파일 생성됨
# 2. 브라우저로 열어서 내용 확인
open notion-page-debug.html  # Mac
```

**확인할 것:**
- 페이지에 실제로 메뉴 데이터가 있는가?
- 로그인 화면이 보이는가?
- "액세스 제한" 같은 메시지가 있는가?

### 방법 2: 노션 페이지 공개 설정 확인

노션 페이지가 완전히 공개되었는지 확인:

1. 노션 페이지 우측 상단 "공유" 클릭
2. "웹에 게시" 활성화
3. "검색 엔진 색인 허용" 활성화

### 방법 3: 대기 시간 늘리기

렌더링에 더 시간이 필요할 수 있습니다:

```java
// SeleniumCrawlerService.java 수정
Thread.sleep(3000);  // 현재
↓
Thread.sleep(10000); // 10초로 늘림
```

### 방법 4: 노션 API 사용 (강력 권장!)

**가장 안정적이고 빠른 방법입니다.**

#### 4-1. Notion Integration 생성
1. https://www.notion.so/my-integrations 접속
2. "New integration" 클릭
3. 이름 입력 (예: "SeoulTech Cafeteria Crawler")
4. "Submit" → **API 키 복사**

#### 4-2. 페이지에 Integration 연결
1. 학식 노션 페이지 열기
2. 우측 상단 "..." 클릭
3. "연결" → 방금 만든 Integration 선택

#### 4-3. 코드 구현

```gradle
// build.gradle
dependencies {
    implementation 'com.notion:notion-sdk-jvm-okhttp:1.2.0'
}
```

```java
// NotionApiCrawlerService.java
@Service
public class NotionApiCrawlerService {
    
    @Value("${notion.api-key}")
    private String apiKey;
    
    @Value("${notion.page-id}")
    private String pageId;
    
    public List<CampusCafeteriaData> crawlFromApi() {
        Client client = new Client(apiKey);
        
        // 페이지 내용 가져오기
        Page page = client.retrievePage(pageId);
        
        // 블록 가져오기 (테이블 등)
        BlockListResponse blocks = client.retrieveBlockChildren(pageId);
        
        // 데이터 파싱
        return parseBlocks(blocks);
    }
}
```

```yaml
# application.yml
notion:
  api-key: ${NOTION_API_KEY}
  page-id: 21e45244ac0a80fdb02ad064ce75d674
```

### 방법 5: CSV/JSON 파일로 제공받기

**가장 간단하고 현실적인 방법:**

1. **학교 학생식당 담당자에게 연락**
   - 주 1회 CSV/JSON 파일로 메뉴 제공 요청
   - 또는 Google Sheets로 공유 요청

2. **데이터 형식 예시**
```csv
building,restaurant,menu,price
ST: Table,값찌개,김치찌개,6000
ST: Table,값찌개,된장찌개,6000
ST: Dining,경성카츠,돈까스,7000
```

3. **크롤러 대신 파일 업로드**
```java
@Service
public class CsvImportService {
    public void importFromCsv(MultipartFile file) {
        // CSV 파싱 → DB 저장
    }
}
```

### 방법 6: 수동 업데이트

**임시 방안 (주 1회면 충분):**

```java
// 관리자 API 추가
@PostMapping("/api/admin/cafeteria/bulk-upload")
public void bulkUpload(@RequestBody List<CafeteriaData> data) {
    importService.importCafeteriaData(data);
}
```

Postman이나 웹 UI로 데이터 입력

## 📊 방법 비교

| 방법 | 구현 난이도 | 유지보수 | 안정성 | 추천도 |
|------|------------|----------|--------|--------|
| **CSV 제공** | ⭐ | ⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Notion API** | ⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **수동 입력** | ⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ |
| **Selenium** | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐ |

## 🔧 디버깅 체크리스트

### 1단계: HTML 확인
```bash
./gradlew test --tests DebugSeleniumTest
open notion-page-debug.html
```

**확인사항:**
- [ ] 메뉴 데이터가 HTML에 있는가?
- [ ] 로그인 화면이 보이는가?
- [ ] 페이지 구조는 어떤가?

### 2단계: 키워드 확인
로그에서 확인:
```
키워드 검색:
----------------------------------------
  ✅ 'ST: Table' 발견
  ✅ '값찌개' 발견
  ✅ '김치찌개' 발견
```

만약 모두 ❌라면 → **페이지 접근 불가**

### 3단계: 노션 설정 확인
- [ ] 페이지가 "웹에 게시" 되었는가?
- [ ] 로그인 없이 접근 가능한가?
- [ ] 브라우저에서 직접 열어보기

### 4단계: 해결 방법 선택
1. **메뉴 데이터 있음** → Selenium 파싱 로직 수정
2. **로그인 필요** → Notion API 또는 CSV 제공
3. **페이지 비공개** → 담당자에게 공개 요청 또는 CSV 제공

## 💡 추천 순서

1. **먼저**: `DebugSeleniumTest` 실행하여 HTML 확인
2. **만약 페이지가 비공개**:
   - 학교 담당자에게 CSV/JSON 제공 요청 (추천!)
   - 또는 Notion API 권한 요청
3. **만약 페이지가 공개**:
   - HTML 구조에 맞게 파싱 로직 수정
   - 대기 시간 조정

## 📞 담당자 문의 템플릿

```
안녕하세요,

서울과학기술대학교 학생식당 메뉴를 활용한 
식비 관리 앱을 개발 중인 학생입니다.

노션 페이지의 학식 메뉴 정보를 주 1회 정도 
업데이트하고 싶은데, 다음 중 가능한 방법이 있을까요?

1. CSV/Excel 파일로 제공
2. 노션 API 접근 권한
3. 노션 페이지 완전 공개 설정

감사합니다.
```

---

**가장 현실적인 해결책: 학교에 CSV 파일 요청!** 
주 1회 업데이트면 수동으로도 충분합니다. 🎯


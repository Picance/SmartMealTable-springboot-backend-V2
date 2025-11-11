package com.stdev.smartmealtable.api.config;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.stdev.smartmealtable.domain.budget.DailyBudget;
import com.stdev.smartmealtable.domain.budget.DailyBudgetRepository;
import com.stdev.smartmealtable.domain.budget.MonthlyBudget;
import com.stdev.smartmealtable.domain.budget.MonthlyBudgetRepository;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.expenditure.Expenditure;
import com.stdev.smartmealtable.domain.expenditure.ExpenditureRepository;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.member.entity.AddressHistory;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.AddressHistoryRepository;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.preference.Preference;
import com.stdev.smartmealtable.domain.preference.PreferenceRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.domain.store.StoreType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 로컬 개발 환경에서 테스트 데이터를 자동으로 초기화하는 클래스
 * 
 * 초기화 순서:
 * 1. 카테고리 (Category)
 * 2. 그룹 (Group)
 * 3. 가게 (Store)
 * 4. 음식 (Food)
 * 5. 선호도 (Preference) - 테스트 회원용
 * 
 * 실행 조건:
 * - Spring Profile이 "local"일 때만 활성화됨
 * - 애플리케이션 시작 시 자동으로 데이터 삽입
 */
@Configuration
@Profile("local")
@Slf4j
@RequiredArgsConstructor
public class TestDataInitializer {

    private final CategoryRepository categoryRepository;
    private final GroupRepository groupRepository;
    private final StoreRepository storeRepository;
    private final FoodRepository foodRepository;
    private final PreferenceRepository preferenceRepository;
    private final MemberRepository memberRepository;
    private final MemberAuthenticationRepository memberAuthenticationRepository;
    private final AddressHistoryRepository addressHistoryRepository;
    private final DailyBudgetRepository dailyBudgetRepository;
    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final ExpenditureRepository expenditureRepository;
    
    // 생성된 데이터 추적용
    private final List<Store> createdStores = new ArrayList<>();
    private final List<Category> createdCategories = new ArrayList<>();

    @Bean
    public CommandLineRunner initializeTestData() {
        return args -> {
            log.info("🚀 [LOCAL] 테스트 데이터 초기화 시작...");
            
            try {
                // 1. 카테고리 초기화
                initializeCategories();
                
                // 2. 그룹 초기화
                initializeGroups();
                
                // 3. 가게 초기화
                initializeStores();
                
                // 4. 음식 초기화
                initializeFoods();
                
                // 5. 테스트 회원 초기화
                long testMemberId = initializeTestMember();
                
                // 6. 회원 인증 정보 초기화
                initializeTestMemberAuthentication(testMemberId);
                
                // 7. 회원 주소 초기화
                initializeTestAddress(testMemberId);
                
                // 8. 월별 예산 초기화 (2025년 10-12월)
                initializeMonthlyBudgets(testMemberId);
                
                // 9. 일일 예산 초기화 (2025년 10-12월)
                initializeDailyBudgets(testMemberId);
                
                // 10. 선호도 초기화 (테스트용)
                initializePreferences(testMemberId);
                
                // 11. 지출 내역 초기화 (2025년 10-12월)
                initializeExpenditures(testMemberId);
                
                log.info("✅ [LOCAL] 테스트 데이터 초기화 완료!");
            } catch (Exception e) {
                log.error("❌ [LOCAL] 테스트 데이터 초기화 중 오류 발생", e);
            }
        };
    }

    /**
     * 기본 카테고리 데이터 초기화
     */
    private void initializeCategories() {
        log.info("📌 카테고리 데이터 초기화 중...");
        
        List<String> categoryNames = Arrays.asList(
            "한식", "중식", "일식", "양식", "베트남식", 
            "태국식", "인도식", "멕시코식", "피자", "버거",
            "카페", "분식", "회전초밥", "닭요리", "고기구이"
        );
        
        for (String categoryName : categoryNames) {
            try {
                Category category = Category.create(categoryName);
                Category savedCategory = categoryRepository.save(category);
                createdCategories.add(savedCategory);
                log.debug("✓ 카테고리 생성: {} (ID: {})", categoryName, savedCategory.getCategoryId());
            } catch (Exception e) {
                log.warn("⚠ 카테고리 생성 실패 ({}): {}", categoryName, e.getMessage());
            }
        }
    }

    /**
     * 그룹(소속) 데이터 초기화
     */
    private void initializeGroups() {
        log.info("📌 그룹(소속) 데이터 초기화 중...");
        
        GroupData[] groupsData = {
            new GroupData("서울대학교", GroupType.UNIVERSITY, "서울시 관악구 대학로 1"),
            new GroupData("고려대학교", GroupType.UNIVERSITY, "서울시 성북구 안암로 145"),
            new GroupData("연세대학교", GroupType.UNIVERSITY, "서울시 서대문구 연세로 50"),
            new GroupData("이화여자대학교", GroupType.UNIVERSITY, "서울시 서대문구 대현동 11-1"),
            new GroupData("한국과학기술원", GroupType.UNIVERSITY, "대전시 유성구 과학로 291"),
            new GroupData("카카오", GroupType.COMPANY, "서울시 강남구 테헤란로 321"),
            new GroupData("네이버", GroupType.COMPANY, "서울시 강남구 강남대로 152"),
            new GroupData("삼성전자", GroupType.COMPANY, "서울시 강남구 테헤란로 1"),
            new GroupData("LG전자", GroupType.COMPANY, "서울시 강남구 남부순환로 사거리"),
            new GroupData("쿠팡", GroupType.COMPANY, "서울시 강남구 영동대로 517")
        };
        
        for (GroupData groupData : groupsData) {
            try {
                Address address = Address.of(
                    groupData.name,      // alias
                    null,                // lotNumberAddress
                    groupData.address,   // streetNameAddress
                    null,                // detailedAddress
                    null,                // latitude
                    null,                // longitude
                    null                 // addressType
                );
                Group group = Group.create(groupData.name, groupData.type, address);
                Group savedGroup = groupRepository.save(group);
                log.debug("✓ 그룹 생성: {} - {} (ID: {})", groupData.type, groupData.name, savedGroup.getGroupId());
            } catch (Exception e) {
                log.warn("⚠ 그룹 생성 실패 ({}): {}", groupData.name, e.getMessage());
            }
        }
    }

    /**
     * 기본 가게 데이터 초기화
     */
    private void initializeStores() {
        log.info("📌 가게 데이터 초기화 중...");
        
        if (createdCategories.isEmpty()) {
            log.warn("⚠ 카테고리가 없어 가게 초기화를 건너뜁니다.");
            return;
        }
        
        StoreData[] sampleStoresData = {
            new StoreData("한식", "강남 우육탕집", "서울시 강남구 테헤란로 123", "서울시 강남구 역삼동 456",
                new BigDecimal("37.4979"), new BigDecimal("127.0276"), "02-123-4567",
                "정갈한 우육탕과 국물이 일품인 식당", 12000, 45, StoreType.RESTAURANT),
            new StoreData("한식", "종로 칼국수 전문점", "서울시 종로구 종로 789", "서울시 종로구 명동 101",
                new BigDecimal("37.5665"), new BigDecimal("126.9780"), "02-234-5678",
                "48년 전통의 칼국수와 우동", 8000, 120, StoreType.RESTAURANT),
            new StoreData("중식", "홍콩 딤섬 하우스", "서울시 중구 명동 456", "서울시 중구 명동 789",
                new BigDecimal("37.5615"), new BigDecimal("126.9836"), "02-345-6789",
                "정통 홍콩식 딤섬과 중식 요리", 15000, 78, StoreType.RESTAURANT),
            new StoreData("일식", "초밥 마스터", "서울시 서초구 강남대로 234", "서울시 서초구 서초동 567",
                new BigDecimal("37.4940"), new BigDecimal("127.0067"), "02-456-7890",
                "프리미엄 일본산 신선 생선을 사용한 초밥", 25000, 92, StoreType.RESTAURANT),
            new StoreData("한식", "학교 식당", "서울시 종로구 대학로 567", "서울시 종로구 연건동 234",
                new BigDecimal("37.5805"), new BigDecimal("126.9960"), "02-567-8901",
                "푸짐하고 저렴한 학식", 5000, 340, StoreType.CAMPUS_RESTAURANT),
            new StoreData("한식", "회사 근처 편의점 주먹밥", "서울시 강남구 테헤란로 876", "서울시 강남구 역삼동 234",
                new BigDecimal("37.4990"), new BigDecimal("127.0290"), "02-678-9012",
                "직장인을 위한 간편한 주먹밥과 김밥", 4000, 156, StoreType.RESTAURANT)
        };
        
        for (StoreData storeData : sampleStoresData) {
            try {
                Category category = createdCategories.stream()
                    .filter(c -> c.getName().equals(storeData.categoryName))
                    .findFirst()
                    .orElse(null);
                
                if (category == null) {
                    log.warn("⚠ 카테고리를 찾을 수 없음: {}", storeData.categoryName);
                    continue;
                }
                
                Store store = Store.create(
                    storeData.storeName, List.of(category.getCategoryId()), storeData.address,
                    storeData.lotNumberAddress, storeData.latitude, storeData.longitude,
                    storeData.phoneNumber, storeData.description, storeData.averagePrice,
                    storeData.reviewCount, 0, 0, storeData.storeType
                );
                
                Store savedStore = storeRepository.save(store);
                createdStores.add(savedStore);
                log.debug("✓ 가게 생성: {} (ID: {})", storeData.storeName, savedStore.getStoreId());
            } catch (Exception e) {
                log.warn("⚠ 가게 생성 실패 ({}): {}", storeData.storeName, e.getMessage());
            }
        }
    }

    /**
     * 음식(메뉴) 데이터 초기화
     */
    private void initializeFoods() {
        log.info("📌 음식(메뉴) 데이터 초기화 중...");
        
        if (createdStores.isEmpty()) {
            log.warn("⚠ 가게가 없어 음식 초기화를 건너뜁니다.");
            return;
        }
        
        List<Category> allCategories = categoryRepository.findAll();
        if (allCategories.isEmpty()) {
            log.warn("⚠ 카테고리가 없어 음식 초기화를 건너뜁니다.");
            return;
        }
        
        FoodData[] foodsData = {
            // 한식
            new FoodData("한식", "우육탕", "진하고 깊은 맛의 우육탕", null, 12000),
            new FoodData("한식", "칼국수", "쫄깃한 면발의 칼국수", null, 8000),
            new FoodData("한식", "김밥", "신선한 야채와 계란이 들어간 김밥", null, 4000),
            new FoodData("한식", "비빔밥", "여러 야채와 고추장이 어우러진 비빔밥", null, 9000),
            new FoodData("한식", "불고기", "부드럽고 맛있는 쇠고기 불고기", null, 15000),
            new FoodData("한식", "김치찌개", "시원한 국물의 김치찌개", null, 8000),
            // 중식
            new FoodData("중식", "딤섬", "정통 홍콩식 딤섬", null, 15000),
            new FoodData("중식", "마라탕", "얼얼한 맛의 마라탕", null, 12000),
            new FoodData("중식", "회냥육", "중국식 돼지고기 요리", null, 14000),
            new FoodData("중식", "양장피", "차가운 소스에 곁들인 양장피", null, 8000),
            // 일식
            new FoodData("일식", "생니기리초밥", "신선한 생선의 니기리초밥", null, 20000),
            new FoodData("일식", "롤초밥", "다양한 재료가 들어간 롤초밥", null, 15000),
            new FoodData("일식", "우동", "국물이 진한 일본식 우동", null, 9000),
            new FoodData("일식", "라면", "얼얼한 매운 일본식 라면", null, 8000),
            // 양식
            new FoodData("양식", "스테이크", "프리미엄 미국산 소고기 스테이크", null, 35000),
            new FoodData("양식", "파스타", "진한 소스의 파스타", null, 12000),
            new FoodData("양식", "리조또", "북이탈리아식 리조또", null, 14000),
            // 피자
            new FoodData("피자", "마르게리타", "신선한 토마토와 모차렐라 치즈", null, 13000),
            new FoodData("피자", "페페로니", "고소한 페페로니 피자", null, 14000),
            new FoodData("피자", "하와이안", "파인애플과 햄이 들어간 하와이안", null, 15000),
            // 버거
            new FoodData("버거", "클래식버거", "기본에 충실한 클래식버거", null, 8000),
            new FoodData("버거", "더블버거", "두 장의 패티가 들어간 더블버거", null, 11000),
            new FoodData("버거", "치즈버거", "녹진한 치즈가 들어간 치즈버거", null, 9000),
            // 카페
            new FoodData("카페", "아메리카노", "진한 맛의 아메리카노", null, 3500),
            new FoodData("카페", "카푸치노", "우유 거품이 풍성한 카푸치노", null, 4500),
            new FoodData("카페", "라떼", "부드러운 우유 라떼", null, 4500),
            new FoodData("카페", "헤이즐넛라떼", "헤이즐넛 풍미의 라떼", null, 5000),
            // 닭요리
            new FoodData("닭요리", "치킨", "바삭한 튀김 치킨", null, 16000),
            new FoodData("닭요리", "닭갈비", "매콤한 양념의 닭갈비", null, 14000),
            new FoodData("닭요리", "닭한마리", "국물이 깊은 닭한마리", null, 13000),
            // 고기구이
            new FoodData("고기구이", "소불고기", "소의 불고기", null, 18000),
            new FoodData("고기구이", "돼지불고기", "돼지의 불고기", null, 14000),
            new FoodData("고기구이", "갈비", "프리미엄 갈비", null, 35000)
        };
        
        for (FoodData foodData : foodsData) {
            try {
                Category category = allCategories.stream()
                    .filter(c -> c.getName().equals(foodData.categoryName))
                    .findFirst()
                    .orElse(null);
                
                if (category == null) {
                    log.warn("⚠ 카테고리를 찾을 수 없음: {}", foodData.categoryName);
                    continue;
                }
                
                // 가게를 순환하면서 음식 추가
                for (Store store : createdStores) {
                    Food food = Food.create(
                        foodData.foodName, store.getStoreId(), category.getCategoryId(), 
                        foodData.description, foodData.imageUrl, foodData.averagePrice,
                        false, // isMain
                        null   // displayOrder
                    );
                    
                    Food savedFood = foodRepository.save(food);
                    log.debug("✓ 음식 생성: {} (Store: {}, ID: {})", 
                        foodData.foodName, store.getStoreId(), savedFood.getFoodId());
                }
            } catch (Exception e) {
                log.warn("⚠ 음식 생성 실패 ({}): {}", foodData.foodName, e.getMessage());
            }
        }
    }

    /**
     * 테스트 회원 초기화
     * 회원: test@example.com (닉네임: 테스트유저, 소속: 서울대학교)
     */
    private long initializeTestMember() {
        log.info("📌 테스트 회원 초기화 중...");
        
        try {
            // 그룹: 먼저 저장된 그룹들 중에서 서울대학교를 찾거나 새로 만들기
            // 기존 생성된 그룹 목록에서 첫 번째를 선택하거나, 이름 검색 시도
            List<Group> universities = groupRepository.findByType(GroupType.UNIVERSITY);
            
            Group selectedGroup;
            if (!universities.isEmpty()) {
                selectedGroup = universities.get(0);  // 첫 번째 대학 선택
            } else {
                log.warn("⚠ 그룹이 없어 기본 그룹을 생성합니다.");
                Address address = Address.of(
                    "기본 그룹",         // alias
                    null,               // lotNumberAddress
                    "서울시",           // streetNameAddress
                    null,               // detailedAddress
                    null,               // latitude
                    null,               // longitude
                    null                // addressType
                );
                Group newGroup = Group.create("기본 그룹", GroupType.UNIVERSITY, address);
                selectedGroup = groupRepository.save(newGroup);
            }
            
            // 테스트 회원 생성
            Member testMember = Member.create(
                selectedGroup.getGroupId(),
                "테스트유저",
                null,
                RecommendationType.BALANCED
            );
            
            Member savedMember = memberRepository.save(testMember);
            log.info("✅ 테스트 회원 생성 완료: ID={}, 이메일=test@example.com", savedMember.getMemberId());
            
            return savedMember.getMemberId();
        } catch (Exception e) {
            log.error("❌ 테스트 회원 생성 실패", e);
            return -1L;
        }
    }

    /**
     * 회원 인증 정보 초기화 (이메일 인증)
     */
    private void initializeTestMemberAuthentication(long testMemberId) {
        if (testMemberId <= 0) {
            log.warn("⚠ 유효한 회원 ID가 없어 인증 정보 초기화를 건너뜁니다.");
            return;
        }
        
        log.info("📌 회원 인증 정보 초기화 중...");
        
        try {
            // 비밀번호 해싱 (BCrypt)
            String password = "testPassword123!";
            String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
            
            // 이메일 인증 생성
            MemberAuthentication emailAuth = MemberAuthentication.createEmailAuth(
                testMemberId,
                "test@example.com",
                hashedPassword,
                "테스트 사용자"
            );
            
            MemberAuthentication savedAuth = memberAuthenticationRepository.save(emailAuth);
            log.info("✅ 회원 인증 정보 생성: ID={}, 이메일={}", savedAuth.getMemberAuthenticationId(), "test@example.com");
        } catch (Exception e) {
            log.error("❌ 회원 인증 정보 생성 실패", e);
        }
    }

    /**
     * 회원 주소 초기화
     * 서울시 강남구에 기본 주소 설정
     */
    private void initializeTestAddress(long testMemberId) {
        if (testMemberId <= 0) {
            log.warn("⚠ 유효한 회원 ID가 없어 주소 초기화를 건너뜁니다.");
            return;
        }
        
        log.info("📌 회원 주소 정보 초기화 중...");
        
        try {
            // Address 값 객체 생성
            Address addressVO = Address.of(
                "집",
                "서울시 강남구 테헤란로 123",
                "서울시 강남구 강남대로 152",
                "102호",
                37.498095,      // 강남역 근처 위도
                127.027610,     // 강남역 근처 경도
                com.stdev.smartmealtable.domain.common.vo.AddressType.HOME
            );
            
            // 주소 생성 (isPrimary=true: 기본 주소)
            AddressHistory addressHistory = AddressHistory.create(
                testMemberId,
                addressVO,
                true
            );
            
            AddressHistory savedAddress = addressHistoryRepository.save(addressHistory);
            log.info("✅ 회원 주소 정보 생성: ID={}, 주소={}", savedAddress.getAddressHistoryId(), addressVO.getFullAddress());
        } catch (Exception e) {
            log.error("❌ 회원 주소 정보 생성 실패", e);
        }
    }

    /**
     * 월별 예산 초기화 (2025년 10월, 11월, 12월)
     * 각 달: 500,000원
     */
    private void initializeMonthlyBudgets(long testMemberId) {
        if (testMemberId <= 0) {
            log.warn("⚠ 유효한 회원 ID가 없어 월별 예산 초기화를 건너뜁니다.");
            return;
        }
        
        log.info("📌 월별 예산 데이터 초기화 중... (2025년 10월~12월)");
        
        String[] months = {"2025-10", "2025-11", "2025-12"};
        Integer monthlyBudget = 500000; // 월 50만원 예산
        
        for (String month : months) {
            try {
                MonthlyBudget budget = MonthlyBudget.create(
                    testMemberId,
                    monthlyBudget,
                    month
                );
                
                monthlyBudgetRepository.save(budget);
                log.debug("✓ 월별 예산 생성: {}년 {}월, 예산={}원", 
                    month.substring(0, 4), month.substring(5, 7), monthlyBudget);
            } catch (Exception e) {
                log.warn("⚠ 월별 예산 생성 실패 ({}): {}", month, e.getMessage());
            }
        }
        
        log.info("✅ 월별 예산 초기화 완료");
    }

    /**
     * 일일 예산 초기화 (2025년 10월 1일 ~ 12월 31일, 총 92일)
     * 각 일: 17,000원 (월 50만원 / 30일 기준)
     */
    private void initializeDailyBudgets(long testMemberId) {
        if (testMemberId <= 0) {
            log.warn("⚠ 유효한 회원 ID가 없어 일일 예산 초기화를 건너뜁니다.");
            return;
        }
        
        log.info("📌 일일 예산 데이터 초기화 중... (2025년 10월~12월)");
        
        Integer dailyBudget = 17000; // 일 1.7만원 예산
        LocalDate startDate = LocalDate.of(2025, 10, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);
        
        int count = 0;
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            try {
                DailyBudget budget = DailyBudget.create(
                    testMemberId,
                    dailyBudget,
                    currentDate
                );
                
                dailyBudgetRepository.save(budget);
                count++;
            } catch (Exception e) {
                log.warn("⚠ 일일 예산 생성 실패 ({}): {}", currentDate, e.getMessage());
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        log.info("✅ 일일 예산 초기화 완료: 총 {} 일치의 예산 생성", count);
    }

    /**
     * 선호도 데이터 초기화
     * 각 카테고리에 대한 선호도 가중치 설정 (-100 ~ 100)
     */
    private void initializePreferences(long testMemberId) {
        log.info("📌 선호도 데이터 초기화 중... (테스트 회원용)");
        
        if (testMemberId <= 0) {
            log.warn("⚠ 유효한 회원 ID가 없어 선호도 초기화를 건너뜁니다.");
            return;
        }
        
        List<Category> allCategories = categoryRepository.findAll();
        if (allCategories.isEmpty()) {
            log.warn("⚠ 카테고리가 없어 선호도 초기화를 건너뜁니다.");
            return;
        }
        
        PreferenceData[] preferencesData = {
            new PreferenceData("한식", 100),      // 좋아요
            new PreferenceData("중식", 50),       // 보통~좋아요
            new PreferenceData("일식", 80),       // 좋아요
            new PreferenceData("양식", 0),        // 보통
            new PreferenceData("베트남식", 30),   // 보통~좋아요
            new PreferenceData("태국식", -30),    // 보통~싫어요
            new PreferenceData("인도식", -50),    // 싫어요
            new PreferenceData("멕시코식", -30),  // 보통~싫어요
            new PreferenceData("피자", 60),       // 보통~좋아요
            new PreferenceData("버거", 40),       // 보통
            new PreferenceData("카페", 0),        // 보통
            new PreferenceData("분식", -20),      // 보통~싫어요
            new PreferenceData("회전초밥", 80),   // 좋아요
            new PreferenceData("닭요리", 70),     // 좋아요
            new PreferenceData("고기구이", 90)    // 아주 좋아요
        };
        
        for (PreferenceData prefData : preferencesData) {
            try {
                Category category = allCategories.stream()
                    .filter(c -> c.getName().equals(prefData.categoryName))
                    .findFirst()
                    .orElse(null);
                
                if (category == null) {
                    log.warn("⚠ 카테고리를 찾을 수 없음: {}", prefData.categoryName);
                    continue;
                }
                
                Preference preference = Preference.create(
                    testMemberId, category.getCategoryId(), prefData.weight
                );
                
                preferenceRepository.save(preference);
                log.debug("✓ 선호도 생성: 회원ID={}, 카테고리={}, 가중치={}", 
                    testMemberId, prefData.categoryName, prefData.weight);
            } catch (Exception e) {
                log.warn("⚠ 선호도 생성 실패 ({}): {}", prefData.categoryName, e.getMessage());
            }
        }
    }

    /**
     * 지출 내역 초기화 (2025년 10월~12월)
     * 실제적인 식사 패턴을 모방한 지출 데이터 생성
     */
    private void initializeExpenditures(long testMemberId) {
        if (testMemberId <= 0) {
            log.warn("⚠ 유효한 회원 ID가 없어 지출 내역 초기화를 건너뜁니다.");
            return;
        }
        
        log.info("📌 지출 내역 데이터 초기화 중... (2025년 10월~12월)");
        
        if (createdStores.isEmpty()) {
            log.warn("⚠ 가게 정보가 없어 지출 내역 초기화를 건너뜁니다.");
            return;
        }
        
        if (createdCategories.isEmpty()) {
            log.warn("⚠ 카테고리가 없어 지출 내역 초기화를 건너뜁니다.");
            return;
        }
        
        Random random = new Random();
        LocalDate startDate = LocalDate.of(2025, 10, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);
        
        int count = 0;
        LocalDate currentDate = startDate;
        
        // 실제적인 식사 패턴: 조식 50%, 중식 90%, 저녁 60%, 간식 30%
        while (!currentDate.isAfter(endDate) && count < 70) {
            try {
                // 식사 유형 결정 (일부 날짜는 여러 끼니 식사)
                MealType[] mealTypes = {MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER};
                
                for (MealType mealType : mealTypes) {
                    // 확률 기반 식사 생성
                    boolean shouldCreateMeal = shouldCreateMeal(mealType, random);
                    if (!shouldCreateMeal) continue;
                    
                    // 무작위 가게 선택
                    Store store = createdStores.get(random.nextInt(createdStores.size()));
                    
                    // 카테고리 선택
                    Category category = createdCategories.get(random.nextInt(createdCategories.size()));
                    
                    // 시간 설정
                    LocalTime mealTime = getMealTime(mealType, random);
                    
                    // 금액 설정 (현실적인 범위)
                    Integer amount = getRealisticAmount(mealType, random);
                    
                    // 지출 내역 생성
                    Expenditure expenditure = Expenditure.create(
                        testMemberId,
                        store.getName(),
                        amount,
                        currentDate,
                        mealTime,
                        category.getCategoryId(),
                        mealType,
                        "테스트 지출 내역",
                        new ArrayList<>()
                    );
                    
                    expenditureRepository.save(expenditure);
                    count++;
                    
                    log.debug("✓ 지출 내역 생성: {}시 {}분, 가게={}, 금액={}원", 
                        mealTime.getHour(), mealTime.getMinute(), store.getName(), amount);
                }
            } catch (Exception e) {
                log.warn("⚠ 지출 내역 생성 실패 ({}): {}", currentDate, e.getMessage());
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        log.info("✅ 지출 내역 초기화 완료: 총 {} 건의 지출 기록 생성", count);
    }

    /**
     * 식사 유형별로 생성 여부 결정
     */
    private boolean shouldCreateMeal(MealType mealType, Random random) {
        return switch (mealType) {
            case BREAKFAST -> random.nextDouble() < 0.5;    // 50%
            case LUNCH -> random.nextDouble() < 0.9;         // 90%
            case DINNER -> random.nextDouble() < 0.6;        // 60%
            case OTHER -> random.nextDouble() < 0.3;         // 30%
        };
    }

    /**
     * 식사 유형별 시간 설정
     */
    private LocalTime getMealTime(MealType mealType, Random random) {
        return switch (mealType) {
            case BREAKFAST -> LocalTime.of(7 + random.nextInt(2), random.nextInt(60));
            case LUNCH -> LocalTime.of(12 + random.nextInt(2), random.nextInt(60));
            case DINNER -> LocalTime.of(18 + random.nextInt(3), random.nextInt(60));
            case OTHER -> LocalTime.of(15 + random.nextInt(3), random.nextInt(60));
        };
    }

    /**
     * 식사 유형별 현실적인 금액 설정
     */
    private Integer getRealisticAmount(MealType mealType, Random random) {
        return switch (mealType) {
            case BREAKFAST -> 4000 + random.nextInt(4000);      // 4,000~8,000
            case LUNCH -> 10000 + random.nextInt(8000);         // 10,000~18,000
            case DINNER -> 15000 + random.nextInt(12000);       // 15,000~27,000
            case OTHER -> 3000 + random.nextInt(4000);          // 3,000~7,000
        };
    }


    // ============ 내부 클래스 ============
    
    private static class GroupData {
        String name;
        GroupType type;
        String address;

        GroupData(String name, GroupType type, String address) {
            this.name = name;
            this.type = type;
            this.address = address;
        }
    }

    private static class StoreData {
        String categoryName;
        String storeName;
        String address;
        String lotNumberAddress;
        BigDecimal latitude;
        BigDecimal longitude;
        String phoneNumber;
        String description;
        Integer averagePrice;
        Integer reviewCount;
        StoreType storeType;

        StoreData(String categoryName, String storeName, String address, String lotNumberAddress,
                  BigDecimal latitude, BigDecimal longitude, String phoneNumber, String description,
                  Integer averagePrice, Integer reviewCount, StoreType storeType) {
            this.categoryName = categoryName;
            this.storeName = storeName;
            this.address = address;
            this.lotNumberAddress = lotNumberAddress;
            this.latitude = latitude;
            this.longitude = longitude;
            this.phoneNumber = phoneNumber;
            this.description = description;
            this.averagePrice = averagePrice;
            this.reviewCount = reviewCount;
            this.storeType = storeType;
        }
    }

    private static class FoodData {
        String categoryName;
        String foodName;
        String description;
        String imageUrl;
        Integer averagePrice;

        FoodData(String categoryName, String foodName, String description, String imageUrl, Integer averagePrice) {
            this.categoryName = categoryName;
            this.foodName = foodName;
            this.description = description;
            this.imageUrl = imageUrl;
            this.averagePrice = averagePrice;
        }
    }

    private static class PreferenceData {
        String categoryName;
        Integer weight;

        PreferenceData(String categoryName, Integer weight) {
            this.categoryName = categoryName;
            this.weight = weight;
        }
    }
}

package com.stdev.smartmealtable.batch.crawler;

import com.stdev.smartmealtable.batch.crawler.domain.CampusCafeteriaData;
import com.stdev.smartmealtable.batch.crawler.domain.CampusCafeteriaData.MenuData;
import com.stdev.smartmealtable.batch.crawler.domain.CampusCafeteriaData.RestaurantData;
import com.stdev.smartmealtable.batch.crawler.service.CafeteriaDataImportService;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreOpeningHour;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.domain.store.StoreType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 크롤러 통합 테스트
 * 
 * <p>실제 데이터베이스를 사용하는 통합 테스트입니다.</p>
 * <p>@Transactional로 테스트 후 롤백됩니다.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("크롤러 통합 테스트")
class CrawlerIntegrationTest {
    
    @Autowired
    private CafeteriaDataImportService importService;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private StoreRepository storeRepository;
    
    @Autowired
    private FoodRepository foodRepository;
    
    @BeforeEach
    void setUp() {
        // 카테고리 생성
        createCategoriesIfNotExists();
    }
    
    private void createCategoriesIfNotExists() {
        List<String> categoryNames = List.of("한식", "중식", "일식", "양식", "아시안");
        
        for (String name : categoryNames) {
            if (!categoryRepository.existsByName(name)) {
                Category category = Category.create(name);
                categoryRepository.save(category);
            }
        }
    }
    
    @Test
    @DisplayName("학식 데이터를 데이터베이스에 저장한다")
    void importCafeteriaData_SaveToDatabase() {
        // Given
        CampusCafeteriaData testData = createTestCafeteriaData();
        
        // When
        importService.importCafeteriaData(List.of(testData));
        
        // Then - Store 검증
        List<Store> stores = storeRepository.searchByKeywordForAutocomplete("테스트가게", 10);
        assertThat(stores).isNotEmpty();
        
        Store store = stores.get(0);
        assertThat(store.getName()).contains("테스트가게");
        assertThat(store.getStoreType()).isEqualTo(StoreType.CAMPUS_RESTAURANT);
        assertThat(store.getAveragePrice()).isEqualTo(7000); // (6000 + 8000) / 2
        
        // Then - OpeningHour 검증
        List<StoreOpeningHour> openingHours = storeRepository.findOpeningHoursByStoreId(store.getStoreId());
        assertThat(openingHours).hasSize(7); // 월~일 7일
        
        // 평일 영업시간 확인
        StoreOpeningHour monday = openingHours.stream()
                .filter(h -> h.dayOfWeek() == DayOfWeek.MONDAY)
                .findFirst()
                .orElseThrow();
        assertThat(monday.openTime()).isEqualTo("10:00:00");
        assertThat(monday.closeTime()).isEqualTo("19:00:00");
        assertThat(monday.isHoliday()).isFalse();
        
        // 주말 휴무 확인
        StoreOpeningHour saturday = openingHours.stream()
                .filter(h -> h.dayOfWeek() == DayOfWeek.SATURDAY)
                .findFirst()
                .orElseThrow();
        assertThat(saturday.isHoliday()).isTrue();
        
        // Then - Food 검증
        List<Food> foods = foodRepository.findByStoreId(store.getStoreId());
        assertThat(foods).hasSize(2);
        assertThat(foods).extracting(Food::getFoodName)
                .containsExactlyInAnyOrder("테스트메뉴1", "테스트메뉴2");
        assertThat(foods).extracting(Food::getAveragePrice)
                .containsExactlyInAnyOrder(6000, 8000);
    }
    
    @Test
    @DisplayName("같은 이름의 가게가 있어도 건물명으로 구분된다")
    void importCafeteriaData_SameRestaurantName_DifferentBuilding() {
        // Given
        CampusCafeteriaData building1 = CampusCafeteriaData.builder()
                .buildingName("건물 A")
                .address("서울 노원구 공릉로 232")
                .latitude(new BigDecimal("37.6335"))
                .longitude(new BigDecimal("127.0768"))
                .restaurants(List.of(
                        RestaurantData.builder()
                                .name("같은가게")
                                .categoryName("한식")
                                .menus(List.of(MenuData.builder().name("메뉴1").price(5000).build()))
                                .build()
                ))
                .build();
        
        CampusCafeteriaData building2 = CampusCafeteriaData.builder()
                .buildingName("건물 B")
                .address("서울 노원구 공릉로 234")
                .latitude(new BigDecimal("37.6298"))
                .longitude(new BigDecimal("127.0793"))
                .restaurants(List.of(
                        RestaurantData.builder()
                                .name("같은가게")
                                .categoryName("한식")
                                .menus(List.of(MenuData.builder().name("메뉴2").price(6000).build()))
                                .build()
                ))
                .build();
        
        // When
        importService.importCafeteriaData(List.of(building1, building2));
        
        // Then - 두 개의 다른 가게가 저장되어야 함
        List<Store> stores = storeRepository.searchByKeywordForAutocomplete("같은가게", 10);
        assertThat(stores).hasSize(2);
        
        assertThat(stores).extracting(Store::getName)
                .containsExactlyInAnyOrder(
                        "같은가게 (건물 A)",
                        "같은가게 (건물 B)"
                );
    }
    
    @Test
    @DisplayName("여러 건물의 학식 데이터를 한 번에 저장한다")
    void importCafeteriaData_MultipleBuildings() {
        // Given
        CampusCafeteriaData building1 = createTestCafeteriaData("건물1");
        CampusCafeteriaData building2 = createTestCafeteriaData("건물2");
        
        // When
        importService.importCafeteriaData(List.of(building1, building2));
        
        // Then
        List<Store> building1Stores = storeRepository.searchByKeywordForAutocomplete("건물1", 10);
        List<Store> building2Stores = storeRepository.searchByKeywordForAutocomplete("건물2", 10);
        
        assertThat(building1Stores).hasSize(1);
        assertThat(building2Stores).hasSize(1);
    }
    
    private CampusCafeteriaData createTestCafeteriaData() {
        return createTestCafeteriaData("테스트건물");
    }
    
    private CampusCafeteriaData createTestCafeteriaData(String buildingName) {
        return CampusCafeteriaData.builder()
                .buildingName(buildingName)
                .address("서울 노원구 공릉로 232")
                .latitude(new BigDecimal("37.6335837919849"))
                .longitude(new BigDecimal("127.07689204595525"))
                .restaurants(List.of(
                        RestaurantData.builder()
                                .name("테스트가게")
                                .categoryName("한식")
                                .menus(List.of(
                                        MenuData.builder()
                                                .name("테스트메뉴1")
                                                .price(6000)
                                                .build(),
                                        MenuData.builder()
                                                .name("테스트메뉴2")
                                                .price(8000)
                                                .build()
                                ))
                                .build()
                ))
                .build();
    }
}


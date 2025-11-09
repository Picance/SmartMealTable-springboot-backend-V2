package com.stdev.smartmealtable.batch.crawler.service;

import com.stdev.smartmealtable.batch.crawler.domain.CampusCafeteriaData;
import com.stdev.smartmealtable.batch.crawler.domain.CampusCafeteriaData.MenuData;
import com.stdev.smartmealtable.batch.crawler.domain.CampusCafeteriaData.RestaurantData;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * CafeteriaDataImportService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("학식 데이터 저장 서비스 테스트")
class CafeteriaDataImportServiceTest {
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private StoreRepository storeRepository;
    
    @Mock
    private FoodRepository foodRepository;
    
    @InjectMocks
    private CafeteriaDataImportService importService;
    
    private List<Category> mockCategories;
    private CampusCafeteriaData mockCafeteriaData;
    
    @BeforeEach
    void setUp() {
        // Mock 카테고리 데이터
        mockCategories = List.of(
                Category.reconstitute(1L, "한식"),
                Category.reconstitute(2L, "중식"),
                Category.reconstitute(3L, "일식"),
                Category.reconstitute(4L, "양식"),
                Category.reconstitute(5L, "아시안")
        );
        
        // Mock 크롤링 데이터
        mockCafeteriaData = CampusCafeteriaData.builder()
                .buildingName("ST: Table")
                .address("서울 노원구 공릉로 232 1학생회관 1층")
                .latitude(new BigDecimal("37.6335837919849"))
                .longitude(new BigDecimal("127.07689204595525"))
                .restaurants(List.of(
                        RestaurantData.builder()
                                .name("값찌개")
                                .categoryName("한식")
                                .menus(List.of(
                                        MenuData.builder()
                                                .name("김치찌개")
                                                .price(6000)
                                                .build(),
                                        MenuData.builder()
                                                .name("된장찌개")
                                                .price(6000)
                                                .build()
                                ))
                                .build(),
                        RestaurantData.builder()
                                .name("경성카츠")
                                .categoryName("일식")
                                .menus(List.of(
                                        MenuData.builder()
                                                .name("돈까스")
                                                .price(7000)
                                                .build()
                                ))
                                .build()
                ))
                .build();
    }
    
    @Test
    @DisplayName("학식 데이터를 성공적으로 저장한다")
    void importCafeteriaData_Success() {
        // Given
        when(categoryRepository.findAll()).thenReturn(mockCategories);
        
        Store savedStore = Store.builder()
                .storeId(1L)
                .name("값찌개 (ST: Table)")
                .address("서울 노원구 공릉로 232 1학생회관 1층")
                .latitude(new BigDecimal("37.6335837919849"))
                .longitude(new BigDecimal("127.07689204595525"))
                .averagePrice(6000)
                .storeType(StoreType.CAMPUS_RESTAURANT)
                .reviewCount(0)
                .viewCount(0)
                .favoriteCount(0)
                .registeredAt(LocalDateTime.now())
                .build();
        
        when(storeRepository.save(any(Store.class))).thenReturn(savedStore);
        when(storeRepository.findOpeningHoursByStoreId(anyLong())).thenReturn(List.of());
        when(foodRepository.findByStoreId(anyLong())).thenReturn(List.of());
        
        // When
        importService.importCafeteriaData(List.of(mockCafeteriaData));
        
        // Then
        verify(categoryRepository, times(1)).findAll();
        verify(storeRepository, times(2)).save(any(Store.class)); // 2개 가게
        verify(storeRepository, atLeast(2)).saveOpeningHour(any(StoreOpeningHour.class)); // 영업시간 저장
        verify(foodRepository, atLeast(2)).save(any(Food.class)); // 최소 2개 음식
    }
    
    @Test
    @DisplayName("평균 가격이 올바르게 계산된다")
    void calculateAveragePrice() {
        // Given
        when(categoryRepository.findAll()).thenReturn(mockCategories);
        
        Store capturedStore = null;
        when(storeRepository.save(any(Store.class))).thenAnswer(invocation -> {
            Store store = invocation.getArgument(0);
            return Store.builder()
                    .storeId(1L)
                    .name(store.getName())
                    .address(store.getAddress())
                    .latitude(store.getLatitude())
                    .longitude(store.getLongitude())
                    .averagePrice(store.getAveragePrice())
                    .storeType(store.getStoreType())
                    .reviewCount(0)
                    .viewCount(0)
                    .favoriteCount(0)
                    .registeredAt(LocalDateTime.now())
                    .build();
        });
        
        when(storeRepository.findOpeningHoursByStoreId(anyLong())).thenReturn(List.of());
        when(foodRepository.findByStoreId(anyLong())).thenReturn(List.of());
        
        // When
        importService.importCafeteriaData(List.of(mockCafeteriaData));
        
        // Then
        verify(storeRepository, times(2)).save(argThat(store -> {
            if (store.getName().contains("값찌개")) {
                // 김치찌개 6000 + 된장찌개 6000 = 평균 6000
                assertThat(store.getAveragePrice()).isEqualTo(6000);
                return true;
            } else if (store.getName().contains("경성카츠")) {
                // 돈까스 7000 = 평균 7000
                assertThat(store.getAveragePrice()).isEqualTo(7000);
                return true;
            }
            return false;
        }));
    }
    
    @Test
    @DisplayName("영업시간이 올바르게 저장된다")
    void saveOpeningHours() {
        // Given
        when(categoryRepository.findAll()).thenReturn(mockCategories);
        
        Store savedStore = Store.builder()
                .storeId(1L)
                .name("값찌개 (ST: Table)")
                .storeType(StoreType.CAMPUS_RESTAURANT)
                .registeredAt(LocalDateTime.now())
                .build();
        
        when(storeRepository.save(any(Store.class))).thenReturn(savedStore);
        when(storeRepository.findOpeningHoursByStoreId(anyLong())).thenReturn(List.of());
        when(foodRepository.findByStoreId(anyLong())).thenReturn(List.of());
        
        // When
        importService.importCafeteriaData(List.of(mockCafeteriaData));
        
        // Then - 평일 5일 + 주말 2일 = 7일 * 2개 가게 = 14개 영업시간
        verify(storeRepository, times(14)).saveOpeningHour(argThat(hour -> {
            DayOfWeek day = hour.dayOfWeek();
            
            // 평일 검증
            if (day == DayOfWeek.MONDAY || day == DayOfWeek.TUESDAY || 
                day == DayOfWeek.WEDNESDAY || day == DayOfWeek.THURSDAY || 
                day == DayOfWeek.FRIDAY) {
                assertThat(hour.openTime()).isEqualTo("10:00:00");
                assertThat(hour.closeTime()).isEqualTo("19:00:00");
                assertThat(hour.isHoliday()).isFalse();
                return true;
            }
            
            // 주말 검증
            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                assertThat(hour.isHoliday()).isTrue();
                return true;
            }
            
            return false;
        }));
    }
    
    @Test
    @DisplayName("가게 타입이 CAMPUS_RESTAURANT로 설정된다")
    void storeType_IsCampusRestaurant() {
        // Given
        when(categoryRepository.findAll()).thenReturn(mockCategories);
        when(storeRepository.save(any(Store.class))).thenAnswer(invocation -> {
            Store store = invocation.getArgument(0);
            return Store.builder()
                    .storeId(1L)
                    .name(store.getName())
                    .storeType(store.getStoreType())
                    .registeredAt(LocalDateTime.now())
                    .build();
        });
        when(storeRepository.findOpeningHoursByStoreId(anyLong())).thenReturn(List.of());
        when(foodRepository.findByStoreId(anyLong())).thenReturn(List.of());
        
        // When
        importService.importCafeteriaData(List.of(mockCafeteriaData));
        
        // Then
        verify(storeRepository, times(2)).save(argThat(store -> 
                store.getStoreType() == StoreType.CAMPUS_RESTAURANT
        ));
    }
    
    @Test
    @DisplayName("카테고리를 찾을 수 없으면 가게를 저장하지 않는다")
    void importCafeteriaData_CategoryNotFound_SkipStore() {
        // Given
        when(categoryRepository.findAll()).thenReturn(List.of()); // 빈 카테고리 목록
        
        // When
        importService.importCafeteriaData(List.of(mockCafeteriaData));
        
        // Then
        verify(storeRepository, never()).save(any(Store.class));
        verify(foodRepository, never()).save(any(Food.class));
    }
}


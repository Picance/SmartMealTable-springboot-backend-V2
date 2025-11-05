package com.stdev.smartmealtable.batch.crawler.domain;

import com.stdev.smartmealtable.batch.crawler.domain.CampusCafeteriaData.MenuData;
import com.stdev.smartmealtable.batch.crawler.domain.CampusCafeteriaData.RestaurantData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CampusCafeteriaData 도메인 객체 테스트
 */
@DisplayName("학식 크롤링 데이터 DTO 테스트")
class CampusCafeteriaDataTest {
    
    @Test
    @DisplayName("CampusCafeteriaData 객체를 생성할 수 있다")
    void createCampusCafeteriaData() {
        // Given & When
        CampusCafeteriaData data = CampusCafeteriaData.builder()
                .buildingName("ST: Table")
                .address("서울 노원구 공릉로 232 1학생회관 1층")
                .latitude(new BigDecimal("37.6335837919849"))
                .longitude(new BigDecimal("127.07689204595525"))
                .restaurants(List.of())
                .build();
        
        // Then
        assertThat(data).isNotNull();
        assertThat(data.getBuildingName()).isEqualTo("ST: Table");
        assertThat(data.getAddress()).isEqualTo("서울 노원구 공릉로 232 1학생회관 1층");
        assertThat(data.getLatitude()).isEqualByComparingTo("37.6335837919849");
        assertThat(data.getLongitude()).isEqualByComparingTo("127.07689204595525");
        assertThat(data.getRestaurants()).isEmpty();
    }
    
    @Test
    @DisplayName("RestaurantData 객체를 생성할 수 있다")
    void createRestaurantData() {
        // Given & When
        RestaurantData restaurant = RestaurantData.builder()
                .name("값찌개")
                .categoryName("한식")
                .menus(List.of())
                .build();
        
        // Then
        assertThat(restaurant).isNotNull();
        assertThat(restaurant.getName()).isEqualTo("값찌개");
        assertThat(restaurant.getCategoryName()).isEqualTo("한식");
        assertThat(restaurant.getMenus()).isEmpty();
    }
    
    @Test
    @DisplayName("MenuData 객체를 생성할 수 있다")
    void createMenuData() {
        // Given & When
        MenuData menu = MenuData.builder()
                .name("김치찌개")
                .price(6000)
                .build();
        
        // Then
        assertThat(menu).isNotNull();
        assertThat(menu.getName()).isEqualTo("김치찌개");
        assertThat(menu.getPrice()).isEqualTo(6000);
    }
    
    @Test
    @DisplayName("완전한 학식 데이터 구조를 생성할 수 있다")
    void createCompleteCafeteriaData() {
        // Given & When
        MenuData menu1 = MenuData.builder()
                .name("김치찌개")
                .price(6000)
                .build();
        
        MenuData menu2 = MenuData.builder()
                .name("된장찌개")
                .price(6000)
                .build();
        
        RestaurantData restaurant = RestaurantData.builder()
                .name("값찌개")
                .categoryName("한식")
                .menus(List.of(menu1, menu2))
                .build();
        
        CampusCafeteriaData data = CampusCafeteriaData.builder()
                .buildingName("ST: Table")
                .address("서울 노원구 공릉로 232 1학생회관 1층")
                .latitude(new BigDecimal("37.6335837919849"))
                .longitude(new BigDecimal("127.07689204595525"))
                .restaurants(List.of(restaurant))
                .build();
        
        // Then
        assertThat(data.getRestaurants()).hasSize(1);
        
        RestaurantData savedRestaurant = data.getRestaurants().get(0);
        assertThat(savedRestaurant.getName()).isEqualTo("값찌개");
        assertThat(savedRestaurant.getMenus()).hasSize(2);
        
        assertThat(savedRestaurant.getMenus())
                .extracting(MenuData::getName)
                .containsExactly("김치찌개", "된장찌개");
        
        assertThat(savedRestaurant.getMenus())
                .extracting(MenuData::getPrice)
                .containsExactly(6000, 6000);
    }
}


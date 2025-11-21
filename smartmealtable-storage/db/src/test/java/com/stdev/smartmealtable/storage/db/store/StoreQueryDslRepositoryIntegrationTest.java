package com.stdev.smartmealtable.storage.db.store;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.domain.store.StoreType;
import com.stdev.smartmealtable.storage.db.food.FoodJpaEntity;
import com.stdev.smartmealtable.storage.db.food.FoodJpaRepository;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(StoreQueryDslRepositoryIntegrationTest.QuerydslTestConfig.class)
class StoreQueryDslRepositoryIntegrationTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("smartmealtable")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");
    }

    @Autowired
    private StoreJpaRepository storeJpaRepository;

    @Autowired
    private FoodJpaRepository foodJpaRepository;

    @Autowired
    private StoreQueryDslRepository storeQueryDslRepository;

    @Test
    @DisplayName("음식명으로 검색해도 해당 음식을 판매하는 가게가 추천 대상에 포함된다")
    void searchStoresMatchesFoodName() {
        StoreJpaEntity targetStore = storeJpaRepository.save(createStore("서울치킨타운", "store-1",
                new BigDecimal("37.6297526"), new BigDecimal("127.0763340")));
        StoreJpaEntity otherStore = storeJpaRepository.save(createStore("강북버거", "store-2",
                new BigDecimal("37.6300000"), new BigDecimal("127.0800000")));

        foodJpaRepository.save(createFood(targetStore.getStoreId(), "뿌링클 순살"));
        foodJpaRepository.save(createFood(targetStore.getStoreId(), "치즈볼"));
        foodJpaRepository.save(createFood(otherStore.getStoreId(), "갈릭버거"));

        StoreRepository.StoreSearchResult result = storeQueryDslRepository.searchStores(
                "뿌링",
                targetStore.getLatitude(),
                targetStore.getLongitude(),
                10.0,
                null,
                false,
                StoreType.RESTAURANT,
                "distance",
                0,
                10
        );

        List<Long> foundStoreIds = result.stores().stream()
                .map(storeWithDistance -> storeWithDistance.store().getStoreId())
                .toList();

        Assertions.assertThat(foundStoreIds)
                .contains(targetStore.getStoreId())
                .doesNotContain(otherStore.getStoreId());
    }

    private StoreJpaEntity createStore(String name, String externalId, BigDecimal latitude, BigDecimal longitude) {
        LocalDateTime now = LocalDateTime.now();
        return StoreJpaEntity.builder()
                .externalId(externalId)
                .name(name)
                .sellerId(1L)
                .address("서울시 노원구 어디로 123")
                .lotNumberAddress("서울시 노원구 어딘가 456-7")
                .latitude(latitude)
                .longitude(longitude)
                .phoneNumber("02-000-0000")
                .description("테스트 가게")
                .averagePrice(15000)
                .reviewCount(0)
                .viewCount(0)
                .favoriteCount(0)
                .storeType(StoreType.RESTAURANT)
                .imageUrl("http://example.com/image.jpg")
                .registeredAt(now)
                .build();
    }

    private FoodJpaEntity createFood(Long storeId, String foodName) {
        return FoodJpaEntity.builder()
                .foodId(null)
                .storeId(storeId)
                .foodName(foodName)
                .price(18000)
                .categoryId(1L)
                .description("테스트 메뉴")
                .imageUrl("http://example.com/menu.jpg")
                .isMain(true)
                .displayOrder(1)
                .build();
    }

    @TestConfiguration
    static class QuerydslTestConfig {
        @Bean
        JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
            return new JPAQueryFactory(entityManager);
        }

        @Bean
        StoreQueryDslRepository storeQueryDslRepository(JPAQueryFactory queryFactory) {
            return new StoreQueryDslRepository(queryFactory);
        }
    }
}

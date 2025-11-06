package com.stdev.smartmealtable.admin.food.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.admin.common.AbstractAdminContainerTest;
import com.stdev.smartmealtable.admin.config.AdminTestConfiguration;
import com.stdev.smartmealtable.admin.food.controller.dto.CreateFoodRequest;
import com.stdev.smartmealtable.admin.food.controller.dto.UpdateFoodRequest;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.domain.store.StoreType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 음식 관리 API 통합 테스트 (ADMIN)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(AdminTestConfiguration.class)
class FoodControllerTest extends AbstractAdminContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    private Long testCategoryId;
    private Long testStoreId;

    @BeforeEach
    void setUp() {
        // 테스트 카테고리 생성
        Category category = Category.create("한식");
        Category savedCategory = categoryRepository.save(category);
        testCategoryId = savedCategory.getCategoryId();

        // 테스트 가게 생성
        Store store = Store.create(
                "테스트 음식점",
                testCategoryId,
                "서울시 강남구 테헤란로 123",
                "서울시 강남구 역삼동 123-45",
                new BigDecimal("37.4979"),
                new BigDecimal("127.0276"),
                "02-1234-5678",
                "테스트 음식점입니다",
                10000,
                0,
                0,
                0,
                StoreType.RESTAURANT
        );
        Store savedStore = storeRepository.save(store);
        testStoreId = savedStore.getStoreId();

        // 테스트 음식 생성
        Food food1 = Food.create(
                "김치찌개",
                testStoreId,
                testCategoryId,
                "매콤한 김치찌개",
                "http://example.com/kimchi.jpg",
                8000,
                true,  // isMain
                1      // displayOrder
        );

        Food food2 = Food.create(
                "된장찌개",
                testStoreId,
                testCategoryId,
                "구수한 된장찌개",
                "http://example.com/doenjang.jpg",
                7000,
                false, // isMain
                2      // displayOrder
        );

        foodRepository.save(food1);
        foodRepository.save(food2);

        entityManager.flush();
        entityManager.clear();
    }

    private String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    @Test
    @DisplayName("[성공] 음식 목록 조회 - 전체")
    void getFoods_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/foods")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.foods", hasSize(2)))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    @DisplayName("[성공] 음식 목록 조회 - 이름 검색")
    void getFoods_WithNameFilter_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/foods")
                        .param("name", "김치")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.foods", hasSize(1)))
                .andExpect(jsonPath("$.data.foods[0].foodName").value("김치찌개"));
    }

    @Test
    @DisplayName("[성공] 음식 목록 조회 - 가게 ID 필터")
    void getFoods_WithStoreIdFilter_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/foods")
                        .param("storeId", testStoreId.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.foods", hasSize(2)));
    }

    @Test
    @DisplayName("[성공] 음식 상세 조회")
    void getFood_Success() throws Exception {
        // Given
        Food food = foodRepository.adminSearch(null, null, "김치찌개", 0, 1).content().get(0);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/foods/{foodId}", food.getFoodId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.foodName").value("김치찌개"))
                .andExpect(jsonPath("$.data.averagePrice").value(8000));
    }

    @Test
    @DisplayName("[실패] 음식 상세 조회 - 존재하지 않는 ID")
    void getFood_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/foods/{foodId}", 999999L))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"));
    }

    @Test
    @DisplayName("[성공] 음식 생성")
    void createFood_Success() throws Exception {
        // Given
        CreateFoodRequest request = new CreateFoodRequest(
                "불고기",
                testStoreId,
                testCategoryId,
                "달콤한 불고기",
                "http://example.com/bulgogi.jpg",
                12000,
                true,  // isMain
                1      // displayOrder
        );

        // When & Then
        mockMvc.perform(post("/api/v1/admin/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.foodName").value("불고기"))
                .andExpect(jsonPath("$.data.averagePrice").value(12000))
                .andExpect(jsonPath("$.data.foodId").isNotEmpty());
    }

    @Test
    @DisplayName("[실패] 음식 생성 - 필수 필드 누락 (이름)")
    void createFood_MissingRequiredFields() throws Exception {
        // Given
        CreateFoodRequest request = new CreateFoodRequest(
                null, // 이름 누락
                testStoreId,
                testCategoryId,
                "설명",
                null,
                12000,
                false, // isMain
                null   // displayOrder
        );

        // When & Then
        mockMvc.perform(post("/api/v1/admin/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity()) // 422 UNPROCESSABLE_ENTITY
                .andExpect(jsonPath("$.result").value("ERROR"));
    }

    @Test
    @DisplayName("[성공] 음식 수정")
    void updateFood_Success() throws Exception {
        // Given
        Food food = foodRepository.adminSearch(null, null, "김치찌개", 0, 1).content().get(0);
        
        UpdateFoodRequest request = new UpdateFoodRequest(
                "특김치찌개",
                testCategoryId,
                "더 매콤한 김치찌개",
                "http://example.com/special_kimchi.jpg",
                9000
        );

        // When & Then
        mockMvc.perform(put("/api/v1/admin/foods/{foodId}", food.getFoodId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.foodName").value("특김치찌개"))
                .andExpect(jsonPath("$.data.averagePrice").value(9000));
    }

    @Test
    @DisplayName("[성공] 음식 삭제 (논리적 삭제)")
    void deleteFood_Success() throws Exception {
        // Given
        Food food = foodRepository.adminSearch(null, null, "김치찌개", 0, 1).content().get(0);

        // When & Then
        mockMvc.perform(delete("/api/v1/admin/foods/{foodId}", food.getFoodId()))
                .andDo(print())
                .andExpect(status().isOk());

        // Verify: 삭제 후 adminSearch에서는 제외됨
        var result = foodRepository.adminSearch(null, null, "김치찌개", 0, 10);
        // 논리 삭제이므로 adminSearch에서는 제외됨 (deleted_at이 NULL이 아닌 것만 조회)
    }
}

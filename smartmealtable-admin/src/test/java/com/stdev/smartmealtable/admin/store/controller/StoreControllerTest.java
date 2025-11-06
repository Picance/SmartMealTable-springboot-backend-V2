package com.stdev.smartmealtable.admin.store.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.admin.common.AbstractAdminContainerTest;
import com.stdev.smartmealtable.admin.config.AdminTestConfiguration;
import com.stdev.smartmealtable.admin.store.controller.request.CreateStoreRequest;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
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
 * 음식점 관리 API 통합 테스트 (ADMIN)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(AdminTestConfiguration.class)
class StoreControllerTest extends AbstractAdminContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    private Long testCategoryId;

    @BeforeEach
    void setUp() {
        // 테스트 카테고리 생성
        Category category = Category.create("한식");
        Category savedCategory = categoryRepository.save(category);
        testCategoryId = savedCategory.getCategoryId();

        // 테스트 음식점 생성
        Store store1 = Store.create(
                "학생식당",
                testCategoryId,
                "서울시 강남구 테헤란로 123",
                "서울시 강남구 역삼동 123-45",
                new BigDecimal("37.4979"),
                new BigDecimal("127.0276"),
                "02-1234-5678",
                "학교 학생식당입니다",
                5000,
                0,
                0,
                0,
                StoreType.CAMPUS_RESTAURANT
        );

        Store store2 = Store.create(
                "일반음식점",
                testCategoryId,
                "서울시 강남구 테헤란로 456",
                "서울시 강남구 역삼동 456-78",
                new BigDecimal("37.4980"),
                new BigDecimal("127.0277"),
                "02-5678-1234",
                "일반 음식점입니다",
                10000,
                0,
                0,
                0,
                StoreType.RESTAURANT
        );

        storeRepository.save(store1);
        storeRepository.save(store2);

        entityManager.flush();
        entityManager.clear();
    }

    private String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    @Test
    @DisplayName("[성공] 음식점 목록 조회 - 전체")
    void getStores_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/stores")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.stores", hasSize(2)))
                .andExpect(jsonPath("$.data.pageInfo.page").value(0))
                .andExpect(jsonPath("$.data.pageInfo.totalElements").value(2));
    }

    @Test
    @DisplayName("[성공] 음식점 목록 조회 - 이름 검색")
    void getStores_WithNameFilter_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/stores")
                        .param("name", "학생")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.stores", hasSize(1)))
                .andExpect(jsonPath("$.data.stores[0].name").value("학생식당"));
    }

    @Test
    @DisplayName("[성공] 음식점 목록 조회 - 유형 필터")
    void getStores_WithTypeFilter_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/stores")
                        .param("storeType", "CAMPUS_RESTAURANT")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.stores", hasSize(1)))
                .andExpect(jsonPath("$.data.stores[0].storeType").value("CAMPUS_RESTAURANT"));
    }

    @Test
    @DisplayName("[성공] 음식점 상세 조회")
    void getStore_Success() throws Exception {
        // Given
        Store store = storeRepository.adminSearch(null, "학생식당", null, 0, 1).content().get(0);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/stores/{storeId}", store.getStoreId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.name").value("학생식당"))
                .andExpect(jsonPath("$.data.storeType").value("CAMPUS_RESTAURANT"));
    }

    @Test
    @DisplayName("[실패] 음식점 상세 조회 - 존재하지 않는 ID")
    void getStore_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/stores/{storeId}", 999999L))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"));
    }

    @Test
    @DisplayName("[성공] 음식점 생성")
    void createStore_Success() throws Exception {
        // Given
        CreateStoreRequest request = new CreateStoreRequest(
                "새로운 음식점",
                testCategoryId,
                null,
                "서울시 강남구 테헤란로 789",
                "서울시 강남구 역삼동 789-12",
                "02-9876-5432",
                "새로 오픈한 음식점입니다",
                8000,
                StoreType.RESTAURANT
        );

        // When & Then
        mockMvc.perform(post("/api/v1/admin/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.name").value("새로운 음식점"))
                .andExpect(jsonPath("$.data.averagePrice").value(8000))
                .andExpect(jsonPath("$.data.storeId").isNotEmpty());
    }

    @Test
    @DisplayName("[실패] 음식점 생성 - 필수 필드 누락 (이름)")
    void createStore_MissingRequiredFields() throws Exception {
        // Given
        CreateStoreRequest request = new CreateStoreRequest(
                null, // 이름 누락
                testCategoryId,
                null,
                "서울시 강남구 테헤란로 789",
                null,
                null,
                null,
                8000,
                StoreType.RESTAURANT
        );

        // When & Then
        mockMvc.perform(post("/api/v1/admin/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity()) // 422 UNPROCESSABLE_ENTITY
                .andExpect(jsonPath("$.result").value("ERROR"));
    }

    @Test
    @DisplayName("[성공] 음식점 삭제 (논리적 삭제)")
    void deleteStore_Success() throws Exception {
        // Given
        Store store = storeRepository.adminSearch(null, "학생식당", null, 0, 1).content().get(0);

        // When & Then
        mockMvc.perform(delete("/api/v1/admin/stores/{storeId}", store.getStoreId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verify: 삭제 후 adminSearch에서는 제외됨
        var result = storeRepository.adminSearch(null, "학생식당", null, 0, 10);
        // 논리 삭제이므로 adminSearch에서는 제외됨
    }
}

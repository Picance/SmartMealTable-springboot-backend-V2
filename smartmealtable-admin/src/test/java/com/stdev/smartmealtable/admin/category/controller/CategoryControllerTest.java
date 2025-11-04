package com.stdev.smartmealtable.admin.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.admin.category.controller.request.CreateCategoryRequest;
import com.stdev.smartmealtable.admin.category.controller.request.UpdateCategoryRequest;
import com.stdev.smartmealtable.admin.common.AbstractAdminContainerTest;
import com.stdev.smartmealtable.core.error.ErrorCode;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.storage.db.category.CategoryJpaEntity;
import com.stdev.smartmealtable.storage.db.store.StoreJpaEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 카테고리 관리 API 통합 테스트 (ADMIN)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoryControllerTest extends AbstractAdminContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // 테스트 카테고리 생성
        Category category1 = Category.create("한식");
        Category category2 = Category.create("중식");
        Category category3 = Category.create("일식");

        categoryRepository.save(category1);
        categoryRepository.save(category2);
        categoryRepository.save(category3);

        entityManager.flush();
        entityManager.clear();
    }

    private String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    @Test
    @DisplayName("[성공] 카테고리 목록 조회 - 전체")
    void getCategories_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/categories")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.categories", hasSize(3)))
                .andExpect(jsonPath("$.data.pageInfo.page").value(0))
                .andExpect(jsonPath("$.data.pageInfo.size").value(10))
                .andExpect(jsonPath("$.data.pageInfo.totalElements").value(3))
                .andExpect(jsonPath("$.data.pageInfo.totalPages").value(1));
    }

    @Test
    @DisplayName("[성공] 카테고리 목록 조회 - 이름 검색")
    void getCategories_WithSearch_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/categories")
                        .param("name", "한")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.categories", hasSize(1)))
                .andExpect(jsonPath("$.data.categories[0].name").value("한식"));
    }

    @Test
    @DisplayName("[성공] 카테고리 상세 조회")
    void getCategory_Success() throws Exception {
        // Given
        Category category = categoryRepository.findAll().get(0);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/categories/{categoryId}", category.getCategoryId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.categoryId").value(category.getCategoryId()))
                .andExpect(jsonPath("$.data.name").value(category.getName()));
    }

    @Test
    @DisplayName("[실패] 카테고리 상세 조회 - 존재하지 않는 카테고리 (404)")
    void getCategory_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/categories/{categoryId}", 99999L))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.E404.name()))
                .andExpect(jsonPath("$.error.message").value("존재하지 않는 카테고리입니다."));
    }

    @Test
    @DisplayName("[성공] 카테고리 생성")
    void createCategory_Success() throws Exception {
        // Given
        CreateCategoryRequest request = new CreateCategoryRequest("양식");

        // When & Then
        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.name").value("양식"))
                .andExpect(jsonPath("$.data.categoryId").exists());

        // DB 검증
        assertThat(categoryRepository.findAll()).hasSize(4);
    }

    @Test
    @DisplayName("[실패] 카테고리 생성 - 이름 중복 (409)")
    void createCategory_DuplicateName() throws Exception {
        // Given
        CreateCategoryRequest request = new CreateCategoryRequest("한식");

        // When & Then
        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.E409.name()))
                .andExpect(jsonPath("$.error.message").value("이미 존재하는 카테고리 이름입니다."));
    }

    @Test
    @DisplayName("[실패] 카테고리 생성 - 이름 누락 (400)")
    void createCategory_BlankName() throws Exception {
        // Given
        CreateCategoryRequest request = new CreateCategoryRequest("");

        // When & Then
        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[성공] 카테고리 수정")
    void updateCategory_Success() throws Exception {
        // Given
        Category category = categoryRepository.findAll().get(0);
        UpdateCategoryRequest request = new UpdateCategoryRequest("한식 업데이트");

        // When & Then
        mockMvc.perform(put("/api/v1/admin/categories/{categoryId}", category.getCategoryId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.categoryId").value(category.getCategoryId()))
                .andExpect(jsonPath("$.data.name").value("한식 업데이트"));
    }

    @Test
    @DisplayName("[실패] 카테고리 수정 - 이름 중복 (409)")
    void updateCategory_DuplicateName() throws Exception {
        // Given
        Category category = categoryRepository.findAll().get(0);
        UpdateCategoryRequest request = new UpdateCategoryRequest("중식");  // 이미 존재하는 이름

        // When & Then
        mockMvc.perform(put("/api/v1/admin/categories/{categoryId}", category.getCategoryId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.E409.name()));
    }

    @Test
    @DisplayName("[성공] 카테고리 삭제")
    void deleteCategory_Success() throws Exception {
        // Given
        Category category = categoryRepository.findAll().get(0);
        Long categoryId = category.getCategoryId();

        // When & Then
        mockMvc.perform(delete("/api/v1/admin/categories/{categoryId}", categoryId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"));

        // DB 검증
        assertThat(categoryRepository.findById(categoryId)).isEmpty();
    }

    @Test
    @DisplayName("[실패] 카테고리 삭제 - 존재하지 않음 (404)")
    void deleteCategory_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/admin/categories/{categoryId}", 99999L))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.E404.name()));
    }

    @Test
    @DisplayName("[실패] 카테고리 삭제 - 사용 중 (409)")
    void deleteCategory_InUse() throws Exception {
        // Given: 카테고리를 사용하는 음식점 생성
        Category category = categoryRepository.findAll().get(0);
        
        StoreJpaEntity store = StoreJpaEntity.builder()
                .name("테스트 음식점")
                .categoryId(category.getCategoryId())
                .address("서울시 강남구")
                .lotNumberAddress("서울시 강남구 123")
                .latitude(java.math.BigDecimal.valueOf(37.12345))
                .longitude(java.math.BigDecimal.valueOf(127.12345))
                .phoneNumber("02-1234-5678")
                .averagePrice(10000)
                .storeType(com.stdev.smartmealtable.domain.store.StoreType.RESTAURANT)
                .registeredAt(java.time.LocalDateTime.now())
                .reviewCount(0)
                .viewCount(0)
                .favoriteCount(0)
                .build();
        
        entityManager.persist(store);
        entityManager.flush();

        // When & Then
        mockMvc.perform(delete("/api/v1/admin/categories/{categoryId}", category.getCategoryId()))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.E409.name()))
                .andExpect(jsonPath("$.error.message").value("해당 카테고리를 사용하는 음식점이나 음식이 존재하여 삭제할 수 없습니다."));
    }
}

package com.stdev.smartmealtable.api.category.controller;

import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 카테고리 조회 API 통합 테스트
 * TDD: RED-GREEN-REFACTOR
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoryControllerTest extends AbstractContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 생성 - 카테고리는 미리 정의된 데이터로 가정
        // 실제 운영 환경에서는 마스터 데이터로 관리
    }

    @Test
    @DisplayName("카테고리 목록 조회 성공 - 전체 조회")
    void getCategories_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/categories"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.categories").isArray());
    }

    @Test
    @DisplayName("카테고리 목록 조회 성공 - 응답 필드 검증")
    void getCategories_Success_ResponseFields() throws Exception {
        // given - 테스트 카테고리 추가
        Category category = Category.reconstitute(null, "한식");
        categoryRepository.save(category);

        // when & then
        mockMvc.perform(get("/api/v1/categories"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.categories").isArray())
                .andExpect(jsonPath("$.data.categories[0].categoryId").exists())
                .andExpect(jsonPath("$.data.categories[0].name").exists());
    }

    @Test
    @DisplayName("카테고리 목록 조회 성공 - 빈 목록")
    void getCategories_Success_EmptyList() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/categories"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.categories").isArray());
    }
}

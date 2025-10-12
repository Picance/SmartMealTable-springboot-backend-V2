package com.stdev.smartmealtable.api.category.controller;

import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CategoryController REST Docs 테스트
 * 카테고리 조회 API 문서화
 */
@DisplayName("CategoryController REST Docs 테스트")
class CategoryControllerRestDocsTest extends AbstractRestDocsTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("카테고리 목록 조회 성공 - 200")
    void getCategories_success_docs() throws Exception {
        // given
        createCategory("한식");
        createCategory("중식");
        createCategory("일식");
        createCategory("양식");

        // when & then
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.categories").isArray())
                .andDo(document("category/get-categories-success",
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.categories").type(JsonFieldType.ARRAY).description("카테고리 목록"),
                                fieldWithPath("data.categories[].categoryId").type(JsonFieldType.NUMBER).description("카테고리 ID"),
                                fieldWithPath("data.categories[].name").type(JsonFieldType.STRING).description("카테고리명"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("카테고리 목록 조회 - 빈 목록 (200)")
    void getCategories_empty_docs() throws Exception {
        // given - 카테고리가 없는 상태

        // when & then
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.categories").isEmpty())
                .andDo(document("category/get-categories-empty",
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.categories").type(JsonFieldType.ARRAY).description("카테고리 목록 (빈 배열)"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }

    // Helper methods
    private Category createCategory(String name) {
        Category category = Category.reconstitute(null, name);
        return categoryRepository.save(category);
    }
}

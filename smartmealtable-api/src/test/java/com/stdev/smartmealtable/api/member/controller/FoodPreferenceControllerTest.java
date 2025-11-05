package com.stdev.smartmealtable.api.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.support.jwt.JwtTokenProvider;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 음식 선호도 관리 API 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FoodPreferenceControllerTest extends AbstractContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberAuthenticationRepository authenticationRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FoodRepository foodRepository;

    private Long memberId;
    private Long foodId;
    private String accessToken;

    @BeforeEach
    void setUp() {
        // Given: 테스트용 회원 생성
        Member member = Member.create(null, "테스트회원", null,
                com.stdev.smartmealtable.domain.member.entity.RecommendationType.BALANCED);
        Member savedMember = memberRepository.save(member);
        this.memberId = savedMember.getMemberId();

        MemberAuthentication authentication = MemberAuthentication.createEmailAuth(
                savedMember.getMemberId(),
                "test@example.com",
                "hashedPassword123",
                "테스트회원"
        );
        authenticationRepository.save(authentication);

        // JWT 토큰 생성
        this.accessToken = jwtTokenProvider.createToken(this.memberId);

        // 카테고리 및 음식 생성
        Category category = Category.reconstitute(null, "한식");
        Category savedCategory = categoryRepository.save(category);

        Food food = Food.reconstitute(null, "김치찌개", 1L, savedCategory.getCategoryId(), 
                "맛있는 김치찌개", null, 8000);
        Food savedFood = foodRepository.save(food);
        this.foodId = savedFood.getFoodId();
    }

    @Test
    @DisplayName("POST /api/v1/members/me/preferences/foods - 음식 선호도 추가 성공 (201)")
    void addFoodPreference_Success() throws Exception {
        // Given
        Map<String, Object> request = Map.of(
                "foodId", foodId,
                "isPreferred", true
        );

        // When & Then
        mockMvc.perform(post("/api/v1/members/me/preferences/foods")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.foodId").value(foodId))
                .andExpect(jsonPath("$.data.foodName").value("김치찌개"))
                .andExpect(jsonPath("$.data.categoryName").value("한식"))
                .andExpect(jsonPath("$.data.isPreferred").value(true))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

    @Test
    @DisplayName("POST /api/v1/members/me/preferences/foods - 중복 추가 실패 (409)")
    void addFoodPreference_Duplicate() throws Exception {
        // Given: 먼저 선호도 추가
        Map<String, Object> request = Map.of(
                "foodId", foodId,
                "isPreferred", true
        );
        mockMvc.perform(post("/api/v1/members/me/preferences/foods")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // When & Then: 동일한 음식에 대해 다시 추가 시도
        mockMvc.perform(post("/api/v1/members/me/preferences/foods")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E409"))
                .andExpect(jsonPath("$.error.message").value("이미 해당 음식에 대한 선호도가 등록되어 있습니다."));
    }

    @Test
    @DisplayName("POST /api/v1/members/me/preferences/foods - 존재하지 않는 음식 (404)")
    void addFoodPreference_FoodNotFound() throws Exception {
        // Given
        Map<String, Object> request = Map.of(
                "foodId", 99999L,  // 존재하지 않는 ID
                "isPreferred", true
        );

        // When & Then
        mockMvc.perform(post("/api/v1/members/me/preferences/foods")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"))
                .andExpect(jsonPath("$.error.message").value("존재하지 않는 음식입니다."));
    }

    @Test
    @DisplayName("POST /api/v1/members/me/preferences/foods - 유효성 검증 실패 (422)")
    void addFoodPreference_ValidationFailed() throws Exception {
        // Given: foodId 누락
        Map<String, Object> request = Map.of(
                "isPreferred", true
        );

        // When & Then
        mockMvc.perform(post("/api/v1/members/me/preferences/foods")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("PUT /api/v1/members/me/preferences/foods/{id} - 음식 선호도 변경 성공 (200)")
    void updateFoodPreference_Success() throws Exception {
        // Given: 먼저 선호도 추가
        Map<String, Object> addRequest = Map.of(
                "foodId", foodId,
                "isPreferred", true
        );
        String addResponse = mockMvc.perform(post("/api/v1/members/me/preferences/foods")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long foodPreferenceId = objectMapper.readTree(addResponse)
                .path("data")
                .path("foodPreferenceId")
                .asLong();

        // When: 선호도 변경
        Map<String, Object> updateRequest = Map.of(
                "isPreferred", false
        );

        // Then
        mockMvc.perform(put("/api/v1/members/me/preferences/foods/" + foodPreferenceId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.foodPreferenceId").value(foodPreferenceId))
                .andExpect(jsonPath("$.data.isPreferred").value(false))
                .andExpect(jsonPath("$.data.updatedAt").exists());
    }

    @Test
    @DisplayName("PUT /api/v1/members/me/preferences/foods/{id} - 존재하지 않는 선호도 (404)")
    void updateFoodPreference_NotFound() throws Exception {
        // Given
        Map<String, Object> request = Map.of(
                "isPreferred", false
        );

        // When & Then
        mockMvc.perform(put("/api/v1/members/me/preferences/foods/99999")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"))
                .andExpect(jsonPath("$.error.message").value("존재하지 않는 음식 선호도입니다."));
    }

    @Test
    @DisplayName("DELETE /api/v1/members/me/preferences/foods/{id} - 음식 선호도 삭제 성공 (204)")
    void deleteFoodPreference_Success() throws Exception {
        // Given: 먼저 선호도 추가
        Map<String, Object> addRequest = Map.of(
                "foodId", foodId,
                "isPreferred", true
        );
        String addResponse = mockMvc.perform(post("/api/v1/members/me/preferences/foods")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long foodPreferenceId = objectMapper.readTree(addResponse)
                .path("data")
                .path("foodPreferenceId")
                .asLong();

        // When & Then: 삭제
        mockMvc.perform(delete("/api/v1/members/me/preferences/foods/" + foodPreferenceId)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/members/me/preferences/foods/{id} - 존재하지 않는 선호도 (404)")
    void deleteFoodPreference_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/members/me/preferences/foods/99999")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"))
                .andExpect(jsonPath("$.error.message").value("존재하지 않는 음식 선호도입니다."));
    }
}

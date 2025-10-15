package com.stdev.smartmealtable.api.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.support.jwt.JwtTokenProvider;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.preference.Preference;
import com.stdev.smartmealtable.domain.preference.PreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PUT /api/v1/members/me/preferences/categories - 카테고리 선호도 수정 API 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UpdateCategoryPreferencesControllerTest extends AbstractContainerTest {

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
    private PreferenceRepository preferenceRepository;

    private Long memberId;
    private String accessToken;

    @BeforeEach
    void setUp() {
        // Given: 테스트용 회원 및 인증 정보 생성
        Member member = Member.create(null, "테스트회원", com.stdev.smartmealtable.domain.member.entity.RecommendationType.BALANCED);
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

        // 테스트용 카테고리 3개 생성
        categoryRepository.save(Category.reconstitute(null, "한식"));
        categoryRepository.save(Category.reconstitute(null, "중식"));
        categoryRepository.save(Category.reconstitute(null, "일식"));

        // 기존 선호도 데이터 생성을 위해 카테고리 ID 조회
        List<Category> savedCategories = categoryRepository.findAll();
        Long categoryId1 = savedCategories.get(0).getCategoryId();
        Long categoryId2 = savedCategories.get(1).getCategoryId();

        // 기존 선호도 데이터 생성 (한식: 100, 중식: 0)
        Preference pref1 = Preference.create(memberId, categoryId1, 100);
        Preference pref2 = Preference.create(memberId, categoryId2, 0);
        preferenceRepository.save(pref1);
        preferenceRepository.save(pref2);
    }

    @Test
    @DisplayName("카테고리 선호도 수정 성공 - 기존 선호도 업데이트 및 새로운 선호도 추가")
    void updateCategoryPreferences_Success() throws Exception {
        // Given: 카테고리 ID 조회
        List<Category> categories = categoryRepository.findAll();
        Long categoryId1 = categories.get(0).getCategoryId(); // 한식
        Long categoryId2 = categories.get(1).getCategoryId(); // 중식
        Long categoryId3 = categories.get(2).getCategoryId(); // 일식

        Map<String, Object> request = Map.of(
                "preferences", List.of(
                        Map.of("categoryId", categoryId1, "weight", 0),      // 한식: 100 → 0
                        Map.of("categoryId", categoryId2, "weight", -100),   // 중식: 0 → -100
                        Map.of("categoryId", categoryId3, "weight", 100)     // 일식: 신규 추가
                )
        );

        // When & Then
        mockMvc.perform(put("/api/v1/members/me/preferences/categories")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.updatedCount").value(3))
                .andExpect(jsonPath("$.data.updatedAt").exists())
                .andExpect(jsonPath("$.error").value(nullValue()));

        // 검증: DB에서 선호도 정보 확인
        List<Preference> preferences = preferenceRepository.findByMemberId(memberId);
        assertThat(preferences).hasSize(3);
        assertThat(preferences).extracting("categoryId", "weight")
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(categoryId1, 0),
                        org.assertj.core.groups.Tuple.tuple(categoryId2, -100),
                        org.assertj.core.groups.Tuple.tuple(categoryId3, 100)
                );
    }

    @Test
    @DisplayName("카테고리 선호도 수정 실패 - 유효하지 않은 weight 값")
    void updateCategoryPreferences_InvalidWeight() throws Exception {
        // Given
        Long categoryId1 = categoryRepository.findAll().get(0).getCategoryId();

        Map<String, Object> request = Map.of(
                "preferences", List.of(
                        Map.of("categoryId", categoryId1, "weight", 50)  // 유효하지 않은 값
                )
        );

        // When & Then
        mockMvc.perform(put("/api/v1/members/me/preferences/categories")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())  // IllegalArgumentException → 400
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E400"))
                .andExpect(jsonPath("$.error.message").exists());
    }

    @Test
    @DisplayName("카테고리 선호도 수정 실패 - 존재하지 않는 카테고리")
    void updateCategoryPreferences_CategoryNotFound() throws Exception {
        // Given
        Map<String, Object> request = Map.of(
                "preferences", List.of(
                        Map.of("categoryId", 99999L, "weight", 100)  // 존재하지 않는 카테고리
                )
        );

        // When & Then
        mockMvc.perform(put("/api/v1/members/me/preferences/categories")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"))
                .andExpect(jsonPath("$.error.message").exists());
    }

    @Test
    @DisplayName("카테고리 선호도 수정 실패 - 빈 preferences 배열")
    void updateCategoryPreferences_EmptyPreferences() throws Exception {
        // Given
        Map<String, Object> request = Map.of("preferences", List.of());

        // When & Then
        mockMvc.perform(put("/api/v1/members/me/preferences/categories")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"));
    }

    @Test
    @DisplayName("카테고리 선호도 수정 실패 - 인증 헤더 없음")
    void updateCategoryPreferences_Unauthorized() throws Exception {
        // Given
        Long categoryId1 = categoryRepository.findAll().get(0).getCategoryId();
        Map<String, Object> request = Map.of(
                "preferences", List.of(
                        Map.of("categoryId", categoryId1, "weight", 100)
                )
        );

        // When & Then
        mockMvc.perform(put("/api/v1/members/me/preferences/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())  // 인증 헤더 없음 → 401
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E401"));
    }
}

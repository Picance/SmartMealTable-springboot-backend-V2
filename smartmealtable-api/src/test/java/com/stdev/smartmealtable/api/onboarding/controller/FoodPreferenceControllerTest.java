package com.stdev.smartmealtable.api.onboarding.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.support.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 온보딩 - 개별 음식 선호도 API 통합 테스트
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
    private MemberRepository memberRepository;

    @Autowired
    private MemberAuthenticationRepository memberAuthenticationRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Long authenticatedMemberId;
    private String accessToken;
    private Long categoryId1;
    private Long categoryId2;
    private List<Long> foodIds;

    @BeforeEach
    void setUp() {
        // 테스트용 그룹 생성
        Group testGroup = Group.create("서울대학교", GroupType.UNIVERSITY, "서울특별시 관악구");
        Group savedGroup = groupRepository.save(testGroup);

        // 테스트용 회원 생성
        Member testMember = Member.create(savedGroup.getGroupId(), "테스트유저", RecommendationType.BALANCED);
        Member savedMember = memberRepository.save(testMember);
        authenticatedMemberId = savedMember.getMemberId();

        // 테스트용 인증 정보 생성
        MemberAuthentication auth = MemberAuthentication.createEmailAuth(
                savedMember.getMemberId(),
                "test@example.com",
                "hashedPasswordForTest",
                "테스트유저"
        );
        memberAuthenticationRepository.save(auth);

        // JWT 토큰 생성
        accessToken = jwtTokenProvider.createToken(authenticatedMemberId);

        // 테스트용 카테고리 생성
        Category category1 = Category.reconstitute(null, "한식");
        Category category2 = Category.reconstitute(null, "중식");
        Category savedCategory1 = categoryRepository.save(category1);
        Category savedCategory2 = categoryRepository.save(category2);
        categoryId1 = savedCategory1.getCategoryId();
        categoryId2 = savedCategory2.getCategoryId();

        // 테스트용 음식 생성 (15개)
        Food food1 = Food.reconstitute(null, "김치찌개", categoryId1, "얼큰한 김치찌개", "https://example.com/kimchi.jpg", 8000);
        Food food2 = Food.reconstitute(null, "된장찌개", categoryId1, "구수한 된장찌개", "https://example.com/doenjang.jpg", 7500);
        Food food3 = Food.reconstitute(null, "불고기", categoryId1, "고소한 불고기", "https://example.com/bulgogi.jpg", 12000);
        Food food4 = Food.reconstitute(null, "비빔밥", categoryId1, "영양만점 비빔밥", "https://example.com/bibimbap.jpg", 9000);
        Food food5 = Food.reconstitute(null, "짜장면", categoryId2, "달콤한 짜장면", "https://example.com/jjajang.jpg", 6000);
        Food food6 = Food.reconstitute(null, "짬뽕", categoryId2, "얼큰한 짬뽕", "https://example.com/jjamppong.jpg", 7000);
        Food food7 = Food.reconstitute(null, "탕수육", categoryId2, "바삭한 탕수육", "https://example.com/tangsuyuk.jpg", 18000);
        Food food8 = Food.reconstitute(null, "삼겹살", categoryId1, "고소한 삼겹살", "https://example.com/samgyeopsal.jpg", 15000);
        Food food9 = Food.reconstitute(null, "김밥", categoryId1, "간편한 김밥", "https://example.com/gimbap.jpg", 3000);
        Food food10 = Food.reconstitute(null, "라면", categoryId1, "매운 라면", "https://example.com/ramyeon.jpg", 4000);

        Food savedFood1 = foodRepository.save(food1);
        Food savedFood2 = foodRepository.save(food2);
        Food savedFood3 = foodRepository.save(food3);
        Food savedFood4 = foodRepository.save(food4);
        Food savedFood5 = foodRepository.save(food5);
        Food savedFood6 = foodRepository.save(food6);
        Food savedFood7 = foodRepository.save(food7);
        Food savedFood8 = foodRepository.save(food8);
        Food savedFood9 = foodRepository.save(food9);
        Food savedFood10 = foodRepository.save(food10);

        foodIds = Arrays.asList(
                savedFood1.getFoodId(),
                savedFood2.getFoodId(),
                savedFood3.getFoodId(),
                savedFood4.getFoodId(),
                savedFood5.getFoodId()
        );
    }

    @Test
    @DisplayName("음식 목록 조회 성공 - 전체 조회")
    void getFoods_success_all() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/onboarding/foods")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(10))
                .andExpect(jsonPath("$.data.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.data.pageable.pageSize").value(20))
                .andExpect(jsonPath("$.data.totalElements").value(10))
                .andExpect(jsonPath("$.data.first").value(true))
                .andExpect(jsonPath("$.data.last").value(true));
    }

    @Test
    @DisplayName("음식 목록 조회 성공 - 카테고리 필터링")
    void getFoods_success_withCategory() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/onboarding/foods")
                        .param("categoryId", categoryId1.toString())
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].categoryId").value(categoryId1))
                .andExpect(jsonPath("$.data.content[0].categoryName").value("한식"));
    }

    @Test
    @DisplayName("개별 음식 선호도 저장 성공")
    void saveFoodPreferences_success() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("preferredFoodIds", foodIds);

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/food-preferences")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.savedCount").value(5))
                .andExpect(jsonPath("$.data.preferredFoods").isArray())
                .andExpect(jsonPath("$.data.preferredFoods.length()").value(5))
                .andExpect(jsonPath("$.data.preferredFoods[0].foodId").exists())
                .andExpect(jsonPath("$.data.preferredFoods[0].foodName").exists())
                .andExpect(jsonPath("$.data.preferredFoods[0].categoryName").exists())
                .andExpect(jsonPath("$.data.preferredFoods[0].imageUrl").exists())
                .andExpect(jsonPath("$.data.message").value("선호 음식이 성공적으로 저장되었습니다."));
    }

    @Test
    @DisplayName("개별 음식 선호도 저장 실패 - 빈 배열")
    void saveFoodPreferences_fail_emptyList() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("preferredFoodIds", Arrays.asList());

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/food-preferences")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.savedCount").value(0));
    }

    @Test
    @DisplayName("개별 음식 선호도 저장 실패 - null")
    void saveFoodPreferences_fail_null() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("preferredFoodIds", null);

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/food-preferences")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"));
    }

    @Test
    @DisplayName("개별 음식 선호도 저장 실패 - JWT 토큰 없음")
    void saveFoodPreferences_fail_noToken() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("preferredFoodIds", foodIds);

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/food-preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}

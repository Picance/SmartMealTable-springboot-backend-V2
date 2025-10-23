package com.stdev.smartmealtable.api.member.controller;

import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.api.member.controller.preference.AddFoodPreferenceRequest;
import com.stdev.smartmealtable.api.member.controller.preference.UpdateCategoryPreferencesRequest;
import com.stdev.smartmealtable.api.member.controller.preference.UpdateFoodPreferenceRequest;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodPreference;
import com.stdev.smartmealtable.domain.food.FoodPreferenceRepository;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.preference.Preference;
import com.stdev.smartmealtable.domain.preference.PreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.List;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PreferenceController REST Docs 테스트
 */
@DisplayName("PreferenceController REST Docs")
class PreferenceControllerRestDocsTest extends AbstractRestDocsTest {

    @Autowired private MemberRepository memberRepository;
    @Autowired private MemberAuthenticationRepository memberAuthenticationRepository;
    @Autowired private GroupRepository groupRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private PreferenceRepository preferenceRepository;
    @Autowired private FoodPreferenceRepository foodPreferenceRepository;
    @Autowired private FoodRepository foodRepository;

    private Member member;
    private String accessToken;
    private Long categoryId1;
    private Long categoryId2;
    private Long foodId1;
    private Long foodId2;
    private Long foodId3;  // addFoodPreference 테스트용
    private Long likedFoodPreferenceId;   // setup에서 생성한 좋아요 선호도 ID
    private Long dislikedFoodPreferenceId; // setup에서 생성한 싫어요 선호도 ID

    @BeforeEach
    void setUpTestData() {
        // 그룹 생성
        Group testGroup = Group.create("테스트대학교", GroupType.UNIVERSITY, "서울특별시 관악구");
        Group savedGroup = groupRepository.save(testGroup);

        // 회원 생성
        Member testMember = Member.create(savedGroup.getGroupId(), "테스트유저", RecommendationType.BALANCED);
        member = memberRepository.save(testMember);

        // 회원 인증 정보 생성
        MemberAuthentication auth = MemberAuthentication.createEmailAuth(
                member.getMemberId(),
                "test@example.com",
                "hashedPasswordForTest",
                "테스트유저"
        );
        memberAuthenticationRepository.save(auth);

        // JWT 토큰 생성
        accessToken = createAccessToken(member.getMemberId());

        // 카테고리 생성
        Category category1 = Category.reconstitute(null, "한식");
        Category savedCategory1 = categoryRepository.save(category1);
        categoryId1 = savedCategory1.getCategoryId();

        Category category2 = Category.reconstitute(null, "중식");
        Category savedCategory2 = categoryRepository.save(category2);
        categoryId2 = savedCategory2.getCategoryId();

        // 음식 생성
        Food food1 = Food.reconstitute(null, "비빔밥", 1L, categoryId1, "맛있는 비빔밥", "bibimbap.jpg", 8000);
        Food savedFood1 = foodRepository.save(food1);
        foodId1 = savedFood1.getFoodId();

        Food food2 = Food.reconstitute(null, "짜장면", 1L, categoryId2, "맛있는 짜장면", "jjajangmyeon.jpg", 6000);
        Food savedFood2 = foodRepository.save(food2);
        foodId2 = savedFood2.getFoodId();

        Food food3 = Food.reconstitute(null, "김치찌개", 1L, categoryId1, "맛있는 김치찌개", "kimchi-jjigae.jpg", 7000);
        Food savedFood3 = foodRepository.save(food3);
        foodId3 = savedFood3.getFoodId();

        // 카테고리 선호도 생성
        Preference pref1 = Preference.create(member.getMemberId(), categoryId1, 100);
        Preference pref2 = Preference.create(member.getMemberId(), categoryId2, 0);
        preferenceRepository.save(pref1);
        preferenceRepository.save(pref2);

        // 음식 선호도 생성 (조회 테스트용)
        FoodPreference likedFood = FoodPreference.create(member.getMemberId(), foodId1);
        FoodPreference savedLikedFood = foodPreferenceRepository.save(likedFood);
        likedFoodPreferenceId = savedLikedFood.getFoodPreferenceId();

        FoodPreference dislikedFood = FoodPreference.create(member.getMemberId(), foodId2);
        dislikedFood.changePreference(false);  // 싫어요로 변경
        FoodPreference savedDislikedFood = foodPreferenceRepository.save(dislikedFood);
        dislikedFoodPreferenceId = savedDislikedFood.getFoodPreferenceId();
    }

    @Test
    @DisplayName("선호도 조회 성공")
    void getPreferences_Success() throws Exception {
        mockMvc.perform(get("/api/v1/members/me/preferences")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.recommendationType").value("BALANCED"))
                .andDo(document("preference-get-preferences-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.recommendationType").type(JsonFieldType.STRING)
                                        .description("추천 타입 (SAVER, ADVENTURER, BALANCED)"),
                                fieldWithPath("data.categoryPreferences").type(JsonFieldType.ARRAY)
                                        .description("카테고리 선호도 목록"),
                                fieldWithPath("data.categoryPreferences[].preferenceId").type(JsonFieldType.NUMBER)
                                        .description("선호도 ID"),
                                fieldWithPath("data.categoryPreferences[].categoryId").type(JsonFieldType.NUMBER)
                                        .description("카테고리 ID"),
                                fieldWithPath("data.categoryPreferences[].categoryName").type(JsonFieldType.STRING)
                                        .description("카테고리명"),
                                fieldWithPath("data.categoryPreferences[].weight").type(JsonFieldType.NUMBER)
                                        .description("가중치 (100: 좋아요, 0: 보통, -100: 싫어요)"),
                                fieldWithPath("data.foodPreferences").type(JsonFieldType.OBJECT)
                                        .description("음식 선호도"),
                                fieldWithPath("data.foodPreferences.liked").type(JsonFieldType.ARRAY)
                                        .description("좋아하는 음식 목록"),
                                fieldWithPath("data.foodPreferences.liked[].foodPreferenceId").type(JsonFieldType.NUMBER)
                                        .description("음식 선호도 ID"),
                                fieldWithPath("data.foodPreferences.liked[].foodId").type(JsonFieldType.NUMBER)
                                        .description("음식 ID"),
                                fieldWithPath("data.foodPreferences.liked[].foodName").type(JsonFieldType.STRING)
                                        .description("음식명"),
                                fieldWithPath("data.foodPreferences.liked[].categoryName").type(JsonFieldType.STRING)
                                        .description("카테고리명"),
                                fieldWithPath("data.foodPreferences.liked[].createdAt").type(JsonFieldType.STRING)
                                        .description("생성 일시"),
                                fieldWithPath("data.foodPreferences.disliked").type(JsonFieldType.ARRAY)
                                        .description("싫어하는 음식 목록"),
                                fieldWithPath("data.foodPreferences.disliked[].foodPreferenceId").type(JsonFieldType.NUMBER)
                                        .description("음식 선호도 ID"),
                                fieldWithPath("data.foodPreferences.disliked[].foodId").type(JsonFieldType.NUMBER)
                                        .description("음식 ID"),
                                fieldWithPath("data.foodPreferences.disliked[].foodName").type(JsonFieldType.STRING)
                                        .description("음식명"),
                                fieldWithPath("data.foodPreferences.disliked[].categoryName").type(JsonFieldType.STRING)
                                        .description("카테고리명"),
                                fieldWithPath("data.foodPreferences.disliked[].createdAt").type(JsonFieldType.STRING)
                                        .description("생성 일시"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("카테고리 선호도 수정 성공")
    void updateCategoryPreferences_Success() throws Exception {
        UpdateCategoryPreferencesRequest request = new UpdateCategoryPreferencesRequest(
                List.of(
                        new UpdateCategoryPreferencesRequest.CategoryPreferenceItem(categoryId1, 100),
                        new UpdateCategoryPreferencesRequest.CategoryPreferenceItem(categoryId2, -100)
                )
        );

        mockMvc.perform(put("/api/v1/members/me/preferences/categories")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andDo(document("preference-update-category-preferences-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        requestFields(
                                fieldWithPath("preferences").type(JsonFieldType.ARRAY)
                                        .description("카테고리 선호도 수정 목록"),
                                fieldWithPath("preferences[].categoryId").type(JsonFieldType.NUMBER)
                                        .description("카테고리 ID"),
                                fieldWithPath("preferences[].weight").type(JsonFieldType.NUMBER)
                                        .description("가중치 (100: 좋아요, 0: 보통, -100: 싫어요)")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.updatedCount").type(JsonFieldType.NUMBER)
                                        .description("수정된 선호도 개수"),
                                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING)
                                        .description("수정 일시"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("음식 선호도 추가 성공")
    void addFoodPreference_Success() throws Exception {
        AddFoodPreferenceRequest request = new AddFoodPreferenceRequest(foodId3, true);  // 선호도가 없는 food3 사용

        mockMvc.perform(post("/api/v1/members/me/preferences/foods")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andDo(document("preference-add-food-preference-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        requestFields(
                                fieldWithPath("foodId").type(JsonFieldType.NUMBER)
                                        .description("음식 ID"),
                                fieldWithPath("isPreferred").type(JsonFieldType.BOOLEAN)
                                        .description("선호 여부 (true: 좋아요, false: 싫어요)")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.foodPreferenceId").type(JsonFieldType.NUMBER)
                                        .description("생성된 음식 선호도 ID"),
                                fieldWithPath("data.foodId").type(JsonFieldType.NUMBER)
                                        .description("음식 ID"),
                                fieldWithPath("data.foodName").type(JsonFieldType.STRING)
                                        .description("음식명"),
                                fieldWithPath("data.categoryName").type(JsonFieldType.STRING)
                                        .description("카테고리명"),
                                fieldWithPath("data.isPreferred").type(JsonFieldType.BOOLEAN)
                                        .description("선호 여부 (true: 좋아요, false: 싫어요)"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING)
                                        .description("생성 일시"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("음식 선호도 변경 성공")
    void updateFoodPreference_Success() throws Exception {
        UpdateFoodPreferenceRequest request = new UpdateFoodPreferenceRequest(false);  // 좋아요 → 싫어요로 변경

        mockMvc.perform(put("/api/v1/members/me/preferences/foods/{foodPreferenceId}", 
                        likedFoodPreferenceId)  // setup에서 생성한 좋아요 선호도 ID 사용
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andDo(document("preference-update-food-preference-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        pathParameters(
                                parameterWithName("foodPreferenceId").description("음식 선호도 ID")
                        ),
                        requestFields(
                                fieldWithPath("isPreferred").type(JsonFieldType.BOOLEAN)
                                        .description("변경할 선호 여부 (true: 좋아요, false: 싫어요)")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.foodPreferenceId").type(JsonFieldType.NUMBER)
                                        .description("음식 선호도 ID"),
                                fieldWithPath("data.foodId").type(JsonFieldType.NUMBER)
                                        .description("음식 ID"),
                                fieldWithPath("data.foodName").type(JsonFieldType.STRING)
                                        .description("음식명"),
                                fieldWithPath("data.categoryName").type(JsonFieldType.STRING)
                                        .description("카테고리명"),
                                fieldWithPath("data.isPreferred").type(JsonFieldType.BOOLEAN)
                                        .description("변경된 선호 여부 (true: 좋아요, false: 싫어요)"),
                                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING)
                                        .description("수정 일시"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("음식 선호도 삭제 성공")
    void deleteFoodPreference_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/members/me/preferences/foods/{foodPreferenceId}",
                        dislikedFoodPreferenceId)  // setup에서 생성한 싫어요 선호도 ID 사용
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document("preference-delete-food-preference-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        pathParameters(
                                parameterWithName("foodPreferenceId").description("음식 선호도 ID")
                        )
                ));
    }

    @Test
    @DisplayName("선호도 조회 실패 - 회원 없음 (404)")
    void getPreferences_NotFound_MemberNotExists() throws Exception {
        String invalidToken = createAccessToken(99999L);  // 존재하지 않는 회원 ID

        mockMvc.perform(get("/api/v1/members/me/preferences")
                        .header("Authorization", invalidToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"))
                .andDo(document("preference-get-preferences-not-found",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (에러 시 null)").optional(),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드 (E404)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지"),
                                fieldWithPath("error.data").type(JsonFieldType.NULL).description("에러 상세 정보").optional()
                        )
                ));
    }
}

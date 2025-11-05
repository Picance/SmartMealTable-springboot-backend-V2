package com.stdev.smartmealtable.api.onboarding.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
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
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 개별 음식 선호도 API Rest Docs 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FoodPreferenceControllerRestDocsTest extends AbstractRestDocsTest {

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
    private List<Long> foodIds;

    @BeforeEach
    void setUpTestData() {
        Group testGroup = Group.create("서울대학교", GroupType.UNIVERSITY, "서울특별시 관악구");
        Group savedGroup = groupRepository.save(testGroup);

        Member testMember = Member.create(savedGroup.getGroupId(), "테스트유저", null, RecommendationType.BALANCED);
        Member savedMember = memberRepository.save(testMember);
        authenticatedMemberId = savedMember.getMemberId();

        MemberAuthentication auth = MemberAuthentication.createEmailAuth(
                savedMember.getMemberId(),
                "test@example.com",
                "hashedPasswordForTest",
                "테스트유저"
        );
        memberAuthenticationRepository.save(auth);

        accessToken = jwtTokenProvider.createToken(authenticatedMemberId);

        Category category1 = Category.reconstitute(null, "한식");
        Category savedCategory1 = categoryRepository.save(category1);
        categoryId1 = savedCategory1.getCategoryId();

        Food food1 = Food.reconstitute(null, "김치찌개", 1L, categoryId1, "얼큰한 김치찌개", "https://example.com/kimchi.jpg", 8000);
        Food food2 = Food.reconstitute(null, "된장찌개", 1L, categoryId1, "구수한 된장찌개", "https://example.com/doenjang.jpg", 7500);
        Food food3 = Food.reconstitute(null, "불고기", 1L, categoryId1, "고소한 불고기", "https://example.com/bulgogi.jpg", 12000);

        Food savedFood1 = foodRepository.save(food1);
        Food savedFood2 = foodRepository.save(food2);
        Food savedFood3 = foodRepository.save(food3);

        foodIds = Arrays.asList(savedFood1.getFoodId(), savedFood2.getFoodId(), savedFood3.getFoodId());
    }

    @Test
    @DisplayName("음식 목록 조회 API 문서화")
    void getFoods_docs() throws Exception {
        mockMvc.perform(get("/api/v1/onboarding/foods")
                        .param("categoryId", categoryId1.toString())
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("onboarding-foods-get",
                        queryParameters(
                                parameterWithName("categoryId").description("카테고리 ID (선택)").optional(),
                                parameterWithName("page").description("페이지 번호 (기본값: 0)"),
                                parameterWithName("size").description("페이지 크기 (기본값: 20)")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (SUCCESS/ERROR)"),
                                fieldWithPath("data.content[]").type(JsonFieldType.ARRAY).description("음식 목록"),
                                fieldWithPath("data.content[].foodId").type(JsonFieldType.NUMBER).description("음식 ID"),
                                fieldWithPath("data.content[].foodName").type(JsonFieldType.STRING).description("음식 이름"),
                                fieldWithPath("data.content[].categoryId").type(JsonFieldType.NUMBER).description("카테고리 ID"),
                                fieldWithPath("data.content[].categoryName").type(JsonFieldType.STRING).description("카테고리 이름"),
                                fieldWithPath("data.content[].imageUrl").type(JsonFieldType.STRING).description("이미지 URL"),
                                fieldWithPath("data.content[].description").type(JsonFieldType.STRING).description("음식 설명"),
                                fieldWithPath("data.content[].averagePrice").type(JsonFieldType.NUMBER).description("평균 가격"),
                                fieldWithPath("data.pageable.pageNumber").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                                fieldWithPath("data.pageable.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                                fieldWithPath("data.pageable.sort.sorted").type(JsonFieldType.BOOLEAN).description("정렬 여부"),
                                fieldWithPath("data.pageable.sort.unsorted").type(JsonFieldType.BOOLEAN).description("비정렬 여부"),
                                fieldWithPath("data.pageable.sort.empty").type(JsonFieldType.BOOLEAN).description("정렬 조건 없음"),
                                fieldWithPath("data.pageable.offset").type(JsonFieldType.NUMBER).description("오프셋"),
                                fieldWithPath("data.pageable.paged").type(JsonFieldType.BOOLEAN).description("페이징 여부"),
                                fieldWithPath("data.pageable.unpaged").type(JsonFieldType.BOOLEAN).description("비페이징 여부"),
                                fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER).description("전체 요소 개수"),
                                fieldWithPath("data.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                                fieldWithPath("data.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                                fieldWithPath("data.size").type(JsonFieldType.NUMBER).description("페이지 크기"),
                                fieldWithPath("data.number").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                                fieldWithPath("data.first").type(JsonFieldType.BOOLEAN).description("첫 페이지 여부"),
                                fieldWithPath("data.numberOfElements").type(JsonFieldType.NUMBER).description("현재 페이지 요소 개수"),
                                fieldWithPath("data.empty").type(JsonFieldType.BOOLEAN).description("빈 페이지 여부"),
                                fieldWithPath("error").type(JsonFieldType.NULL).optional()
                                        .description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("개별 음식 선호도 저장 API 문서화")
    void saveFoodPreferences_docs() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("preferredFoodIds", foodIds);

        mockMvc.perform(post("/api/v1/onboarding/food-preferences")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document("onboarding-food-preferences-post",
                        requestFields(
                                fieldWithPath("preferredFoodIds").type(JsonFieldType.ARRAY).description("선호 음식 ID 목록 (최대 50개)")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (SUCCESS/ERROR)"),
                                fieldWithPath("data.savedCount").type(JsonFieldType.NUMBER).description("저장된 음식 개수"),
                                fieldWithPath("data.preferredFoods[]").type(JsonFieldType.ARRAY).description("저장된 선호 음식 목록 (최대 10개 반환)"),
                                fieldWithPath("data.preferredFoods[].foodId").type(JsonFieldType.NUMBER).description("음식 ID"),
                                fieldWithPath("data.preferredFoods[].foodName").type(JsonFieldType.STRING).description("음식 이름"),
                                fieldWithPath("data.preferredFoods[].categoryName").type(JsonFieldType.STRING).description("카테고리 이름"),
                                fieldWithPath("data.preferredFoods[].imageUrl").type(JsonFieldType.STRING).description("이미지 URL"),
                                fieldWithPath("data.message").type(JsonFieldType.STRING).description("성공 메시지"),
                                fieldWithPath("error").type(JsonFieldType.NULL).optional()
                                        .description("에러 정보 (성공 시 null)")
                        )
                ));
    }
}

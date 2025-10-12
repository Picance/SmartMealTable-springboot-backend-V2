package com.stdev.smartmealtable.api.expenditure.controller;

import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.api.expenditure.dto.request.CreateExpenditureRequest;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ExpenditureController REST Docs 테스트
 */
@DisplayName("ExpenditureController REST Docs")
class ExpenditureControllerRestDocsTest extends AbstractRestDocsTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberAuthenticationRepository memberAuthenticationRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    private Member member;
    private String accessToken;
    private Long categoryId;

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
        Category category = Category.reconstitute(null, "한식");
        Category savedCategory = categoryRepository.save(category);
        categoryId = savedCategory.getCategoryId();
    }

    @Test
    @DisplayName("지출 내역 등록 성공 - 아이템 포함")
    void createExpenditure_WithItems_Success() throws Exception {
        // given
        CreateExpenditureRequest.ExpenditureItemRequest item1 =
                new CreateExpenditureRequest.ExpenditureItemRequest(1L, 1, 8000);
        CreateExpenditureRequest.ExpenditureItemRequest item2 =
                new CreateExpenditureRequest.ExpenditureItemRequest(2L, 1, 7000);

        CreateExpenditureRequest request = new CreateExpenditureRequest(
                "맛있는집",
                15000,
                LocalDate.of(2025, 10, 12),
                LocalTime.of(12, 30),
                categoryId,
                MealType.LUNCH,
                "점심 식사",
                List.of(item1, item2)
        );

        // when & then
        mockMvc.perform(post("/api/v1/expenditures")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value(true))
                .andExpect(jsonPath("$.data.expenditureId").exists())
                .andExpect(jsonPath("$.data.storeName").value("맛있는집"))
                .andExpect(jsonPath("$.data.amount").value(15000))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items").isNotEmpty())
                .andDo(document("expenditure/create-expenditure-with-items-success",
                        requestHeaders(
                                headerWithName("Authorization").description("JWT 액세스 토큰 (Bearer)")
                        ),
                        requestFields(
                                fieldWithPath("storeName").type(JsonFieldType.STRING)
                                        .description("가게 이름 (최대 200자)"),
                                fieldWithPath("amount").type(JsonFieldType.NUMBER)
                                        .description("총 금액 (0 이상)"),
                                fieldWithPath("expendedDate").type(JsonFieldType.STRING)
                                        .description("지출 날짜 (yyyy-MM-dd)"),
                                fieldWithPath("expendedTime").type(JsonFieldType.STRING)
                                        .description("지출 시간 (HH:mm:ss)").optional(),
                                fieldWithPath("categoryId").type(JsonFieldType.NUMBER)
                                        .description("카테고리 ID").optional(),
                                fieldWithPath("mealType").type(JsonFieldType.STRING)
                                        .description("식사 유형 (BREAKFAST, LUNCH, DINNER, OTHER)").optional(),
                                fieldWithPath("memo").type(JsonFieldType.STRING)
                                        .description("메모 (최대 500자)").optional(),
                                fieldWithPath("items").type(JsonFieldType.ARRAY)
                                        .description("지출 항목 목록").optional(),
                                fieldWithPath("items[].foodId").type(JsonFieldType.NUMBER)
                                        .description("음식 ID"),
                                fieldWithPath("items[].quantity").type(JsonFieldType.NUMBER)
                                        .description("수량 (1 이상)"),
                                fieldWithPath("items[].price").type(JsonFieldType.NUMBER)
                                        .description("가격 (0 이상)")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.BOOLEAN)
                                        .description("성공 여부"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.expenditureId").type(JsonFieldType.NUMBER)
                                        .description("생성된 지출 내역 ID"),
                                fieldWithPath("data.storeName").type(JsonFieldType.STRING)
                                        .description("가게 이름"),
                                fieldWithPath("data.amount").type(JsonFieldType.NUMBER)
                                        .description("총 금액"),
                                fieldWithPath("data.expendedDate").type(JsonFieldType.STRING)
                                        .description("지출 날짜"),
                                fieldWithPath("data.expendedTime").type(JsonFieldType.STRING)
                                        .description("지출 시간"),
                                fieldWithPath("data.categoryId").type(JsonFieldType.NUMBER)
                                        .description("카테고리 ID"),
                                fieldWithPath("data.categoryName").type(JsonFieldType.STRING)
                                        .description("카테고리 이름"),
                                fieldWithPath("data.mealType").type(JsonFieldType.STRING)
                                        .description("식사 유형"),
                                fieldWithPath("data.memo").type(JsonFieldType.STRING)
                                        .description("메모"),
                                fieldWithPath("data.items").type(JsonFieldType.ARRAY)
                                        .description("지출 항목 목록"),
                                fieldWithPath("data.items[].expenditureItemId").type(JsonFieldType.NUMBER)
                                        .description("지출 항목 ID"),
                                fieldWithPath("data.items[].foodId").type(JsonFieldType.NUMBER)
                                        .description("음식 ID"),
                                fieldWithPath("data.items[].foodName").type(JsonFieldType.STRING)
                                        .description("음식 이름"),
                                fieldWithPath("data.items[].quantity").type(JsonFieldType.NUMBER)
                                        .description("수량"),
                                fieldWithPath("data.items[].price").type(JsonFieldType.NUMBER)
                                        .description("가격"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING)
                                        .description("생성 시각"),
                                fieldWithPath("error").type(JsonFieldType.NULL)
                                        .description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("지출 내역 등록 성공 - 아이템 없이 간단 등록")
    void createExpenditure_WithoutItems_Success() throws Exception {
        // given
        CreateExpenditureRequest request = new CreateExpenditureRequest(
                "편의점",
                5000,
                LocalDate.of(2025, 10, 12),
                LocalTime.of(14, 0),
                categoryId,
                MealType.OTHER,
                null,
                null
        );

        // when & then
        mockMvc.perform(post("/api/v1/expenditures")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value(true))
                .andExpect(jsonPath("$.data.expenditureId").exists())
                .andExpect(jsonPath("$.data.storeName").value("편의점"))
                .andExpect(jsonPath("$.data.amount").value(5000))
                .andExpect(jsonPath("$.data.items").isEmpty())
                .andDo(document("expenditure/create-expenditure-without-items-success",
                        requestHeaders(
                                headerWithName("Authorization").description("JWT 액세스 토큰 (Bearer)")
                        ),
                        requestFields(
                                fieldWithPath("storeName").type(JsonFieldType.STRING)
                                        .description("가게 이름"),
                                fieldWithPath("amount").type(JsonFieldType.NUMBER)
                                        .description("총 금액"),
                                fieldWithPath("expendedDate").type(JsonFieldType.STRING)
                                        .description("지출 날짜"),
                                fieldWithPath("expendedTime").type(JsonFieldType.STRING)
                                        .description("지출 시간"),
                                fieldWithPath("categoryId").type(JsonFieldType.NUMBER)
                                        .description("카테고리 ID"),
                                fieldWithPath("mealType").type(JsonFieldType.STRING)
                                        .description("식사 유형"),
                                fieldWithPath("memo").type(JsonFieldType.NULL)
                                        .description("메모 (없음)"),
                                fieldWithPath("items").type(JsonFieldType.NULL)
                                        .description("지출 항목 목록 (없음)")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.BOOLEAN)
                                        .description("성공 여부"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.expenditureId").type(JsonFieldType.NUMBER)
                                        .description("생성된 지출 내역 ID"),
                                fieldWithPath("data.storeName").type(JsonFieldType.STRING)
                                        .description("가게 이름"),
                                fieldWithPath("data.amount").type(JsonFieldType.NUMBER)
                                        .description("총 금액"),
                                fieldWithPath("data.expendedDate").type(JsonFieldType.STRING)
                                        .description("지출 날짜"),
                                fieldWithPath("data.expendedTime").type(JsonFieldType.STRING)
                                        .description("지출 시간"),
                                fieldWithPath("data.categoryId").type(JsonFieldType.NUMBER)
                                        .description("카테고리 ID"),
                                fieldWithPath("data.categoryName").type(JsonFieldType.STRING)
                                        .description("카테고리 이름"),
                                fieldWithPath("data.mealType").type(JsonFieldType.STRING)
                                        .description("식사 유형"),
                                fieldWithPath("data.memo").type(JsonFieldType.NULL)
                                        .description("메모 (없음)"),
                                fieldWithPath("data.items").type(JsonFieldType.ARRAY)
                                        .description("지출 항목 목록 (빈 배열)"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING)
                                        .description("생성 시각"),
                                fieldWithPath("error").type(JsonFieldType.NULL)
                                        .description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("지출 내역 등록 실패 - 유효성 검증 실패 (필수 필드 누락)")
    void createExpenditure_ValidationFailed() throws Exception {
        // given - storeName이 null
        CreateExpenditureRequest request = new CreateExpenditureRequest(
                null,  // storeName 누락
                15000,
                LocalDate.of(2025, 10, 12),
                LocalTime.of(12, 30),
                categoryId,
                MealType.LUNCH,
                null,
                null
        );

        // when & then
        mockMvc.perform(post("/api/v1/expenditures")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value(false))
                .andExpect(jsonPath("$.error").exists())
                .andDo(document("expenditure/create-expenditure-validation-failed",
                        requestHeaders(
                                headerWithName("Authorization").description("JWT 액세스 토큰 (Bearer)")
                        ),
                        requestFields(
                                fieldWithPath("storeName").type(JsonFieldType.NULL)
                                        .description("가게 이름 (필수, 누락됨)"),
                                fieldWithPath("amount").type(JsonFieldType.NUMBER)
                                        .description("총 금액"),
                                fieldWithPath("expendedDate").type(JsonFieldType.STRING)
                                        .description("지출 날짜"),
                                fieldWithPath("expendedTime").type(JsonFieldType.STRING)
                                        .description("지출 시간"),
                                fieldWithPath("categoryId").type(JsonFieldType.NUMBER)
                                        .description("카테고리 ID"),
                                fieldWithPath("mealType").type(JsonFieldType.STRING)
                                        .description("식사 유형"),
                                fieldWithPath("memo").type(JsonFieldType.NULL)
                                        .description("메모"),
                                fieldWithPath("items").type(JsonFieldType.NULL)
                                        .description("지출 항목 목록")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.BOOLEAN)
                                        .description("성공 여부 (false)"),
                                fieldWithPath("data").type(JsonFieldType.NULL)
                                        .description("응답 데이터 (에러 시 null)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING)
                                        .description("에러 코드"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING)
                                        .description("에러 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("지출 내역 등록 실패 - 인증되지 않은 요청")
    void createExpenditure_Unauthorized() throws Exception {
        // given
        CreateExpenditureRequest request = new CreateExpenditureRequest(
                "맛있는집",
                15000,
                LocalDate.of(2025, 10, 12),
                LocalTime.of(12, 30),
                categoryId,
                MealType.LUNCH,
                null,
                null
        );

        // when & then - Authorization 헤더 없이 요청
        mockMvc.perform(post("/api/v1/expenditures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value(false))
                .andExpect(jsonPath("$.error").exists())
                .andDo(document("expenditure/create-expenditure-unauthorized",
                        requestFields(
                                fieldWithPath("storeName").type(JsonFieldType.STRING)
                                        .description("가게 이름"),
                                fieldWithPath("amount").type(JsonFieldType.NUMBER)
                                        .description("총 금액"),
                                fieldWithPath("expendedDate").type(JsonFieldType.STRING)
                                        .description("지출 날짜"),
                                fieldWithPath("expendedTime").type(JsonFieldType.STRING)
                                        .description("지출 시간"),
                                fieldWithPath("categoryId").type(JsonFieldType.NUMBER)
                                        .description("카테고리 ID"),
                                fieldWithPath("mealType").type(JsonFieldType.STRING)
                                        .description("식사 유형"),
                                fieldWithPath("memo").type(JsonFieldType.NULL)
                                        .description("메모"),
                                fieldWithPath("items").type(JsonFieldType.NULL)
                                        .description("지출 항목 목록")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.BOOLEAN)
                                        .description("성공 여부 (false)"),
                                fieldWithPath("data").type(JsonFieldType.NULL)
                                        .description("응답 데이터 (에러 시 null)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING)
                                        .description("에러 코드"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING)
                                        .description("에러 메시지")
                        )
                ));
    }
}

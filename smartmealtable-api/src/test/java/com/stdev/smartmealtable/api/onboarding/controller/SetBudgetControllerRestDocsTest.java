package com.stdev.smartmealtable.api.onboarding.controller;

import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
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
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 온보딩 - 예산 설정 API Rest Docs 문서화 테스트
 */
@Transactional
class SetBudgetControllerRestDocsTest extends AbstractRestDocsTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberAuthenticationRepository memberAuthenticationRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Long testMemberId;

    @BeforeEach
    void setUp() {
        // 테스트용 그룹 생성
        Group testGroup = Group.create("서울대학교", GroupType.UNIVERSITY, "서울특별시 관악구");
        Group savedGroup = groupRepository.save(testGroup);

        // 테스트용 회원 생성 (프로필 설정 완료 상태)
        Member testMember = Member.create(savedGroup.getGroupId(), "테스트닉네임", null, RecommendationType.BALANCED);
        Member savedMember = memberRepository.save(testMember);
        testMemberId = savedMember.getMemberId();

        // 테스트용 인증 정보 생성
        MemberAuthentication auth = MemberAuthentication.createEmailAuth(
                savedMember.getMemberId(),
                "test@example.com",
                "hashedPasswordForTest",
                "테스트유저"
        );
        memberAuthenticationRepository.save(auth);
    }

    /**
     * Authorization 헤더 생성 헬퍼 메서드
     */
    private String createAuthorizationHeader(Long memberId) {
        String token = jwtTokenProvider.createToken(memberId);
        return "Bearer " + token;
    }

    @Test
    @DisplayName("[Docs] 온보딩 - 예산 설정 성공")
    void setBudget_Success_Docs() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("monthlyBudget", 300000);
        request.put("dailyBudget", 10000);

        Map<String, Integer> mealBudgets = new HashMap<>();
        mealBudgets.put("BREAKFAST", 3000);
        mealBudgets.put("LUNCH", 4000);
        mealBudgets.put("DINNER", 3000);
        request.put("mealBudgets", mealBudgets);

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/budget")
                        .header("Authorization", createAuthorizationHeader(testMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document("onboarding-budget-set-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("JWT 인증 토큰 (Bearer)")
                        ),
                        requestFields(
                                fieldWithPath("monthlyBudget").type(JsonFieldType.NUMBER)
                                        .description("월별 예산 (원 단위, 0 이상)"),
                                fieldWithPath("dailyBudget").type(JsonFieldType.NUMBER)
                                        .description("일일 예산 (원 단위, 0 이상)"),
                                fieldWithPath("mealBudgets").type(JsonFieldType.OBJECT)
                                        .description("식사별 예산 (BREAKFAST, LUNCH, DINNER)"),
                                fieldWithPath("mealBudgets.BREAKFAST").type(JsonFieldType.NUMBER)
                                        .description("아침 식사 예산 (원 단위)"),
                                fieldWithPath("mealBudgets.LUNCH").type(JsonFieldType.NUMBER)
                                        .description("점심 식사 예산 (원 단위)"),
                                fieldWithPath("mealBudgets.DINNER").type(JsonFieldType.NUMBER)
                                        .description("저녁 식사 예산 (원 단위)")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING)
                                        .description("응답 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.monthlyBudget").type(JsonFieldType.NUMBER)
                                        .description("설정된 월별 예산"),
                                fieldWithPath("data.dailyBudget").type(JsonFieldType.NUMBER)
                                        .description("설정된 일일 예산"),
                                fieldWithPath("data.mealBudgets").type(JsonFieldType.ARRAY)
                                        .description("설정된 식사별 예산 목록"),
                                fieldWithPath("data.mealBudgets[].mealType").type(JsonFieldType.STRING)
                                        .description("식사 유형 (BREAKFAST, LUNCH, DINNER)"),
                                fieldWithPath("data.mealBudgets[].budget").type(JsonFieldType.NUMBER)
                                        .description("해당 식사의 예산 금액"),
                                fieldWithPath("error").type(JsonFieldType.NULL).optional()
                                        .description("에러 정보 (성공 시 null, @JsonInclude(NON_NULL)로 제외될 수 있음)")
                        )
                ));
    }

    @Test
    @DisplayName("[Docs] 온보딩 - 예산 설정 실패 (월별 예산 누락)")
    void setBudget_MonthlyBudgetNull_Docs() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("dailyBudget", 10000);

        Map<String, Integer> mealBudgets = new HashMap<>();
        mealBudgets.put("BREAKFAST", 3000);
        request.put("mealBudgets", mealBudgets);

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/budget")
                        .header("Authorization", createAuthorizationHeader(testMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andDo(document("onboarding-budget-set-validation-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("JWT 인증 토큰 (Bearer)")
                        ),
                        requestFields(
                                fieldWithPath("dailyBudget").type(JsonFieldType.NUMBER)
                                        .description("일일 예산"),
                                fieldWithPath("mealBudgets").type(JsonFieldType.OBJECT)
                                        .description("식사별 예산"),
                                fieldWithPath("mealBudgets.BREAKFAST").type(JsonFieldType.NUMBER)
                                        .description("아침 식사 예산")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING)
                                        .description("응답 결과 (ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).optional()
                                        .description("응답 데이터 (에러 시 null, @JsonInclude(NON_NULL)로 제외될 수 있음)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING)
                                        .description("에러 코드 (E422)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING)
                                        .description("에러 메시지"),
                                fieldWithPath("error.data").type(JsonFieldType.OBJECT).optional()
                                        .description("상세 에러 정보 (필드별 검증 오류)"),
                                fieldWithPath("error.data.field").type(JsonFieldType.STRING).optional()
                                        .description("검증 실패한 필드명"),
                                fieldWithPath("error.data.reason").type(JsonFieldType.STRING).optional()
                                        .description("검증 실패 사유")
                        )
                ));
    }

    @Test
    @DisplayName("[Docs] 온보딩 - 예산 설정 실패 (JWT 토큰 없음)")
    void setBudget_NoToken_Docs() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("monthlyBudget", 300000);
        request.put("dailyBudget", 10000);

        Map<String, Integer> mealBudgets = new HashMap<>();
        mealBudgets.put("BREAKFAST", 3000);
        request.put("mealBudgets", mealBudgets);

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/budget")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andDo(document("onboarding-budget-set-auth-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("monthlyBudget").type(JsonFieldType.NUMBER)
                                        .description("월별 예산"),
                                fieldWithPath("dailyBudget").type(JsonFieldType.NUMBER)
                                        .description("일일 예산"),
                                fieldWithPath("mealBudgets").type(JsonFieldType.OBJECT)
                                        .description("식사별 예산"),
                                fieldWithPath("mealBudgets.BREAKFAST").type(JsonFieldType.NUMBER)
                                        .description("아침 식사 예산")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING)
                                        .description("응답 결과 (ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).optional()
                                        .description("응답 데이터 (에러 시 null, @JsonInclude(NON_NULL)로 제외될 수 있음)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING)
                                        .description("에러 코드 (E401)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING)
                                        .description("에러 메시지"),
                                fieldWithPath("error.data").type(JsonFieldType.NULL).optional()
                                        .description("상세 에러 정보 (null, @JsonInclude(NON_NULL)로 제외될 수 있음)")
                        )
                ));
    }
}

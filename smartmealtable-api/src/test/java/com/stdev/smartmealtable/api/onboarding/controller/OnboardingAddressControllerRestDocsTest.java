package com.stdev.smartmealtable.api.onboarding.controller;

import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.api.onboarding.dto.request.OnboardingAddressRequest;
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

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 온보딩 - 주소 등록 API Rest Docs 문서화 테스트
 */
@Transactional
class OnboardingAddressControllerRestDocsTest extends AbstractRestDocsTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberAuthenticationRepository memberAuthenticationRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Long testMemberId;
    private Long testGroupId;

    @BeforeEach
    void setUp() {
        // 테스트용 그룹 생성
        Group testGroup = Group.create("서울대학교", GroupType.UNIVERSITY, "서울특별시 관악구 관악로 1");
        Group savedGroup = groupRepository.save(testGroup);
        testGroupId = savedGroup.getGroupId();

        // 테스트용 회원 생성 (프로필 설정 완료 상태)
        Member testMember = Member.create(testGroupId, "테스트닉네임", null, RecommendationType.BALANCED);
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
    @DisplayName("[Docs] 온보딩 - 주소 등록 성공 (주 거주지)")
    void registerAddress_Success_Primary_Docs() throws Exception {
        // given
        OnboardingAddressRequest request = new OnboardingAddressRequest(
                "집",
                "서울특별시 강남구 테헤란로 123",
                "서울특별시 강남구 테헤란로 427",
                "위워크 삼성역점 10층",
                37.5081,
                127.0630,
                "HOME"
        );

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/address")
                        .header("Authorization", createAuthorizationHeader(testMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document("onboarding/address/register-success-primary",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("JWT 액세스 토큰 (Bearer 스킴). 인증된 사용자의 memberId를 포함")
                        ),
                        requestFields(
                                fieldWithPath("alias").type(JsonFieldType.STRING)
                                        .description("주소 별칭 (1-50자, 예: 집, 회사, 학교)"),
                                fieldWithPath("lotNumberAddress").type(JsonFieldType.STRING)
                                        .description("지번 주소 (최대 255자, 선택 사항)").optional(),
                                fieldWithPath("streetNameAddress").type(JsonFieldType.STRING)
                                        .description("도로명 주소 (1-255자, 필수)"),
                                fieldWithPath("detailedAddress").type(JsonFieldType.STRING)
                                        .description("상세 주소 (최대 255자, 선택 사항)").optional(),
                                fieldWithPath("latitude").type(JsonFieldType.NUMBER)
                                        .description("위도 (-90.0 ~ 90.0)"),
                                fieldWithPath("longitude").type(JsonFieldType.NUMBER)
                                        .description("경도 (-180.0 ~ 180.0)"),
                                fieldWithPath("addressType").type(JsonFieldType.STRING)
                                        .description("주소 유형 (최대 20자, 예: HOME, WORK, 선택 사항)").optional()
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING)
                                        .description("응답 결과 (SUCCESS/ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.addressHistoryId").type(JsonFieldType.NUMBER)
                                        .description("주소 이력 ID"),
                                fieldWithPath("data.alias").type(JsonFieldType.STRING)
                                        .description("등록된 주소 별칭"),
                                fieldWithPath("data.lotNumberAddress").type(JsonFieldType.STRING)
                                        .description("등록된 지번 주소"),
                                fieldWithPath("data.streetNameAddress").type(JsonFieldType.STRING)
                                        .description("등록된 도로명 주소"),
                                fieldWithPath("data.detailedAddress").type(JsonFieldType.STRING)
                                        .description("등록된 상세 주소"),
                                fieldWithPath("data.latitude").type(JsonFieldType.NUMBER)
                                        .description("등록된 위도"),
                                fieldWithPath("data.longitude").type(JsonFieldType.NUMBER)
                                        .description("등록된 경도"),
                                fieldWithPath("data.addressType").type(JsonFieldType.STRING)
                                        .description("주소 유형"),
                                fieldWithPath("data.isPrimary").type(JsonFieldType.BOOLEAN)
                                        .description("주 거주지 여부"),
                                fieldWithPath("error").type(JsonFieldType.NULL).optional()
                                        .description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("[Docs] 온보딩 - 주소 등록 성공 (부 거주지)")
    void registerAddress_Success_NonPrimary_Docs() throws Exception {
        // given
        OnboardingAddressRequest request = new OnboardingAddressRequest(
                "회사",
                "서울특별시 송파구 올림픽로 289",
                "서울특별시 송파구 올림픽로 300",
                "롯데월드타워 35층",
                37.5125,
                127.1025,
                "WORK"
        );

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/address")
                        .header("Authorization", createAuthorizationHeader(testMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document("onboarding/address/register-success-non-primary",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("JWT 액세스 토큰 (Bearer 스킴)")
                        ),
                        requestFields(
                                fieldWithPath("alias").type(JsonFieldType.STRING)
                                        .description("주소 별칭"),
                                fieldWithPath("lotNumberAddress").type(JsonFieldType.STRING)
                                        .description("지번 주소").optional(),
                                fieldWithPath("streetNameAddress").type(JsonFieldType.STRING)
                                        .description("도로명 주소"),
                                fieldWithPath("detailedAddress").type(JsonFieldType.STRING)
                                        .description("상세 주소").optional(),
                                fieldWithPath("latitude").type(JsonFieldType.NUMBER)
                                        .description("위도"),
                                fieldWithPath("longitude").type(JsonFieldType.NUMBER)
                                        .description("경도"),
                                fieldWithPath("addressType").type(JsonFieldType.STRING)
                                        .description("주소 유형").optional(),
                                fieldWithPath("isPrimary").type(JsonFieldType.BOOLEAN)
                                        .description("주 거주지 여부").optional()
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING)
                                        .description("응답 결과"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.addressHistoryId").type(JsonFieldType.NUMBER)
                                        .description("주소 이력 ID"),
                                fieldWithPath("data.alias").type(JsonFieldType.STRING)
                                        .description("주소 별칭"),
                                fieldWithPath("data.lotNumberAddress").type(JsonFieldType.STRING)
                                        .description("지번 주소"),
                                fieldWithPath("data.streetNameAddress").type(JsonFieldType.STRING)
                                        .description("도로명 주소"),
                                fieldWithPath("data.detailedAddress").type(JsonFieldType.STRING)
                                        .description("상세 주소"),
                                fieldWithPath("data.latitude").type(JsonFieldType.NUMBER)
                                        .description("위도"),
                                fieldWithPath("data.longitude").type(JsonFieldType.NUMBER)
                                        .description("경도"),
                                fieldWithPath("data.addressType").type(JsonFieldType.STRING)
                                        .description("주소 유형"),
                                fieldWithPath("data.isPrimary").type(JsonFieldType.BOOLEAN)
                                        .description("주 거주지 여부"),
                                fieldWithPath("error").type(JsonFieldType.NULL).optional()
                                        .description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("[Docs] 온보딩 - 주소 등록 실패 (필수 필드 누락)")
    void registerAddress_Fail_MissingFields_Docs() throws Exception {
        // given - 도로명 주소 필드가 누락된 요청
        String invalidRequest = """
                {
                    "alias": "집",
                    "lotNumberAddress": "서울특별시 강남구 테헤란로 123",
                    "detailedAddress": "101동 202호",
                    "latitude": 37.5081,
                    "longitude": 127.0630,
                    "addressType": "HOME",
                    "isPrimary": true
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/address")
                        .header("Authorization", createAuthorizationHeader(testMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andDo(document("onboarding/address/fail-missing-fields",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("JWT 액세스 토큰 (Bearer 스킴)")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING)
                                        .description("응답 결과 (ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).optional()
                                        .description("응답 데이터 (에러 시 null)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING)
                                        .description("에러 코드 (E422)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING)
                                        .description("에러 메시지"),
                                fieldWithPath("error.data").type(JsonFieldType.OBJECT)
                                        .description("검증 오류 상세 정보"),
                                fieldWithPath("error.data.field").type(JsonFieldType.STRING)
                                        .description("오류가 발생한 필드명"),
                                fieldWithPath("error.data.reason").type(JsonFieldType.STRING)
                                        .description("검증 오류 사유")
                        )
                ));
    }

    @Test
    @DisplayName("[Docs] 온보딩 - 주소 등록 실패 (주소 길이 초과)")
    void registerAddress_Fail_AddressTooLong_Docs() throws Exception {
        // given - 256자 도로명 주소 (최대 255자 초과)
        String tooLongAddress = "서울특별시 강남구 테헤란로 ".repeat(20) + "12345"; // 256자
        OnboardingAddressRequest request = new OnboardingAddressRequest(
                "집",
                null,
                tooLongAddress,
                "상세주소",
                37.5081,
                127.0630,
                "HOME"
        );

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/address")
                        .header("Authorization", createAuthorizationHeader(testMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andDo(document("onboarding/address/fail-address-too-long",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("JWT 액세스 토큰 (Bearer 스킴)")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING)
                                        .description("응답 결과 (ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).optional()
                                        .description("응답 데이터 (에러 시 null)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING)
                                        .description("에러 코드 (E422)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING)
                                        .description("에러 메시지"),
                                fieldWithPath("error.data").type(JsonFieldType.OBJECT)
                                        .description("검증 오류 상세 정보"),
                                fieldWithPath("error.data.field").type(JsonFieldType.STRING)
                                        .description("오류가 발생한 필드명 (streetNameAddress)"),
                                fieldWithPath("error.data.reason").type(JsonFieldType.STRING)
                                        .description("검증 오류 사유")
                        )
                ));
    }

    @Test
    @DisplayName("[Docs] 온보딩 - 주소 등록 실패 (JWT 토큰 누락)")
    void registerAddress_Fail_MissingJwtToken_Docs() throws Exception {
        // given
        OnboardingAddressRequest request = new OnboardingAddressRequest(
                "집",
                null,
                "서울특별시 강남구 테헤란로 427",
                "위워크 10층",
                37.5081,
                127.0630,
                "HOME"
        );

        // when & then - Authorization 헤더 없이 요청
        mockMvc.perform(post("/api/v1/onboarding/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andDo(document("onboarding/address/fail-missing-jwt",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING)
                                        .description("응답 결과 (ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).optional()
                                        .description("응답 데이터 (에러 시 null)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING)
                                        .description("에러 코드 (E401)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING)
                                        .description("에러 메시지 (유효하지 않은 토큰입니다.)")
                        )
                ));
    }

    @Test
    @DisplayName("[Docs] 온보딩 - 주소 등록 실패 (잘못된 JWT 토큰)")
    void registerAddress_Fail_InvalidJwtToken_Docs() throws Exception {
        // given
        OnboardingAddressRequest request = new OnboardingAddressRequest(
                "집",
                null,
                "서울특별시 강남구 테헤란로 427",
                "위워크 10층",
                37.5081,
                127.0630,
                "HOME"
        );

        // when & then - 잘못된 토큰으로 요청
        mockMvc.perform(post("/api/v1/onboarding/address")
                        .header("Authorization", "Bearer invalid-token-12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andDo(document("onboarding/address/fail-invalid-jwt",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("잘못된 JWT 액세스 토큰")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING)
                                        .description("응답 결과 (ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).optional()
                                        .description("응답 데이터 (에러 시 null)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING)
                                        .description("에러 코드 (E401)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING)
                                        .description("에러 메시지 (유효하지 않은 토큰입니다.)")
                        )
                ));
    }
}

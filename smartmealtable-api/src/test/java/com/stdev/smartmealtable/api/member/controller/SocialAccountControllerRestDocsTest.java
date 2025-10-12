package com.stdev.smartmealtable.api.member.controller;

import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.api.member.service.social.AddSocialAccountServiceRequest;
import com.stdev.smartmealtable.client.auth.oauth.dto.OAuthTokenResponse;
import com.stdev.smartmealtable.client.auth.oauth.dto.OAuthUserInfo;
import com.stdev.smartmealtable.client.auth.oauth.google.GoogleAuthClient;
import com.stdev.smartmealtable.client.auth.oauth.kakao.KakaoAuthClient;
import com.stdev.smartmealtable.domain.member.entity.*;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.member.repository.SocialAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SocialAccountController REST Docs 테스트
 */
@DisplayName("SocialAccountController REST Docs")
class SocialAccountControllerRestDocsTest extends AbstractRestDocsTest {

    @Autowired private MemberRepository memberRepository;
    @Autowired private MemberAuthenticationRepository memberAuthenticationRepository;
    @Autowired private GroupRepository groupRepository;
    @Autowired private SocialAccountRepository socialAccountRepository;

    @MockBean private KakaoAuthClient kakaoAuthClient;
    @MockBean private GoogleAuthClient googleAuthClient;

    private Member member;
    private MemberAuthentication memberAuth;
    private String accessToken;
    private SocialAccount kakaoAccount;

    @BeforeEach
    void setUpTestData() {
        // 그룹 생성
        Group testGroup = Group.create("테스트대학교", GroupType.UNIVERSITY, "서울특별시 관악구");
        Group savedGroup = groupRepository.save(testGroup);

        // 회원 생성
        Member testMember = Member.create(savedGroup.getGroupId(), "테스트유저", RecommendationType.BALANCED);
        member = memberRepository.save(testMember);

        // 회원 인증 정보 생성 (이메일 + 비밀번호)
        MemberAuthentication auth = MemberAuthentication.createEmailAuth(
                member.getMemberId(),
                "test@example.com",
                "hashedPasswordForTest",
                "테스트유저"
        );
        memberAuth = memberAuthenticationRepository.save(auth);

        // 기존 카카오 소셜 계정 연동
        SocialAccount kakao = SocialAccount.create(
                memberAuth.getMemberAuthenticationId(),
                SocialProvider.KAKAO,
                "kakao_12345678",
                "kakao_access_token",
                "kakao_refresh_token",
                "bearer",
                LocalDateTime.now().plusDays(30)
        );
        kakaoAccount = socialAccountRepository.save(kakao);

        // JWT 토큰 생성
        accessToken = createAccessToken(member.getMemberId());
    }

    @Test
    @DisplayName("연동된 소셜 계정 목록 조회 성공")
    void getSocialAccountList_success_docs() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/members/me/social-accounts")
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.connectedAccounts").isArray())
                .andExpect(jsonPath("$.data.connectedAccounts[0].socialAccountId").value(kakaoAccount.getSocialAccountId()))
                .andExpect(jsonPath("$.data.connectedAccounts[0].provider").value("KAKAO"))
                .andExpect(jsonPath("$.data.hasPassword").value(true))
                .andDo(document("social-account/get-list-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.connectedAccounts").type(JsonFieldType.ARRAY).description("연동된 소셜 계정 목록"),
                                fieldWithPath("data.connectedAccounts[].socialAccountId").type(JsonFieldType.NUMBER).description("소셜 계정 ID"),
                                fieldWithPath("data.connectedAccounts[].provider").type(JsonFieldType.STRING).description("소셜 제공자 (KAKAO, GOOGLE)"),
                                fieldWithPath("data.connectedAccounts[].providerEmail").type(JsonFieldType.STRING).description("소셜 계정 이메일").optional(),
                                fieldWithPath("data.connectedAccounts[].connectedAt").type(JsonFieldType.STRING).description("연동 일시"),
                                fieldWithPath("data.hasPassword").type(JsonFieldType.BOOLEAN).description("이메일 비밀번호 설정 여부")
                        )
                ));
    }

    @Test
    @DisplayName("소셜 계정 추가 연동 성공")
    void addSocialAccount_success_docs() throws Exception {
        // given
        String authorizationCode = "new_google_auth_code_12345";
        AddSocialAccountServiceRequest request = new AddSocialAccountServiceRequest(
                SocialProvider.GOOGLE,
                authorizationCode
        );

        // GoogleAuthClient Mock
        OAuthTokenResponse tokenResponse = new OAuthTokenResponse(
                "google_access_token",
                "google_refresh_token",
                3600,
                "bearer",
                "id_token_value"
        );
        OAuthUserInfo userInfo = OAuthUserInfo.of(
                "google_87654321",
                "newuser@gmail.com",
                "New User",
                null
        );
        
        given(googleAuthClient.getAccessToken(anyString())).willReturn(tokenResponse);
        given(googleAuthClient.extractUserInfo(anyString())).willReturn(userInfo);

        // when & then
        mockMvc.perform(post("/api/v1/members/me/social-accounts")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.socialAccountId").exists())
                .andExpect(jsonPath("$.data.provider").value("GOOGLE"))
                .andExpect(jsonPath("$.data.providerEmail").value("newuser@gmail.com"))
                .andExpect(jsonPath("$.data.connectedAt").exists())
                .andDo(document("social-account/add-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        requestFields(
                                fieldWithPath("provider").type(JsonFieldType.STRING).description("소셜 제공자 (KAKAO, GOOGLE)"),
                                fieldWithPath("authorizationCode").type(JsonFieldType.STRING).description("OAuth 인증 코드")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.socialAccountId").type(JsonFieldType.NUMBER).description("새로 연동된 소셜 계정 ID"),
                                fieldWithPath("data.provider").type(JsonFieldType.STRING).description("소셜 제공자 (KAKAO, GOOGLE)"),
                                fieldWithPath("data.providerEmail").type(JsonFieldType.STRING).description("소셜 계정 이메일"),
                                fieldWithPath("data.connectedAt").type(JsonFieldType.STRING).description("연동 일시")
                        )
                ));
    }

    @Test
    @DisplayName("소셜 계정 추가 연동 실패 - 이미 연동된 계정")
    void addSocialAccount_duplicate_docs() throws Exception {
        // given - 이미 KAKAO 계정이 연동되어 있음 (setup에서 생성)
        AddSocialAccountServiceRequest request = new AddSocialAccountServiceRequest(
                SocialProvider.KAKAO,
                "duplicate_kakao_auth_code"
        );

        // KakaoAuthClient Mock - 동일한 providerId 반환
        OAuthTokenResponse tokenResponse = new OAuthTokenResponse(
                "kakao_access_token",
                "kakao_refresh_token",
                3600,
                "bearer",
                "id_token_value"
        );
        OAuthUserInfo userInfo = OAuthUserInfo.of(
                "kakao_12345678",  // 이미 setup에서 등록한 providerId
                "existing@kakao.com",
                "Existing User",
                null
        );
        
        given(kakaoAuthClient.getAccessToken(anyString())).willReturn(tokenResponse);
        given(kakaoAuthClient.extractUserInfo(anyString())).willReturn(userInfo);

        // when & then
        mockMvc.perform(post("/api/v1/members/me/social-accounts")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("E409"))
                .andExpect(jsonPath("$.error.message").value("이미 다른 계정에 연동된 소셜 계정입니다."))
                .andDo(document("social-account/add-duplicate",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        requestFields(
                                fieldWithPath("provider").type(JsonFieldType.STRING).description("소셜 제공자 (KAKAO, GOOGLE)"),
                                fieldWithPath("authorizationCode").type(JsonFieldType.STRING).description("OAuth 인증 코드")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (ERROR)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드 (E409)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("소셜 계정 추가 연동 실패 - 유효성 검증 실패")
    void addSocialAccount_validation_docs() throws Exception {
        // given - authorizationCode가 빈 문자열
        AddSocialAccountServiceRequest request = new AddSocialAccountServiceRequest(
                SocialProvider.KAKAO,
                ""  // 빈 문자열
        );

        // when & then
        mockMvc.perform(post("/api/v1/members/me/social-accounts")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("E422"))
                .andExpect(jsonPath("$.error.message").exists())
                .andDo(document("social-account/add-validation",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        requestFields(
                                fieldWithPath("provider").type(JsonFieldType.STRING).description("소셜 제공자 (KAKAO, GOOGLE)"),
                                fieldWithPath("authorizationCode").type(JsonFieldType.STRING).description("OAuth 인증 코드 (빈 문자열)")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (ERROR)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드 (E422)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지"),
                                fieldWithPath("error.data").type(JsonFieldType.OBJECT).description("에러 상세 정보").optional(),
                                fieldWithPath("error.data.field").type(JsonFieldType.STRING).description("유효성 검증 실패 필드명").optional(),
                                fieldWithPath("error.data.reason").type(JsonFieldType.STRING).description("검증 실패 사유").optional()
                        )
                ));
    }

    @Test
    @DisplayName("소셜 계정 연동 해제 성공")
    void removeSocialAccount_success_docs() throws Exception {
        // given - 카카오 계정이 연동되어 있고, 이메일 비밀번호도 있음 (안전하게 해제 가능)
        Long socialAccountIdToRemove = kakaoAccount.getSocialAccountId();

        // when & then
        mockMvc.perform(delete("/api/v1/members/me/social-accounts/{socialAccountId}", socialAccountIdToRemove)
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document("social-account/remove-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        pathParameters(
                                parameterWithName("socialAccountId").description("연동 해제할 소셜 계정 ID")
                        )
                ));
    }

    @Test
    @DisplayName("소셜 계정 연동 해제 실패 - 존재하지 않는 소셜 계정")
    void removeSocialAccount_notFound_docs() throws Exception {
        // given - 존재하지 않는 소셜 계정 ID
        Long nonExistentSocialAccountId = 99999L;

        // when & then
        mockMvc.perform(delete("/api/v1/members/me/social-accounts/{socialAccountId}", nonExistentSocialAccountId)
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("E404"))
                .andExpect(jsonPath("$.error.message").value("존재하지 않는 소셜 계정입니다."))
                .andDo(document("social-account/remove-not-found",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        pathParameters(
                                parameterWithName("socialAccountId").description("연동 해제할 소셜 계정 ID (존재하지 않음)")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (ERROR)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드 (E404)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("소셜 계정 연동 해제 실패 - 유일한 로그인 수단")
    void removeSocialAccount_lastLoginMethod_docs() throws Exception {
        // given - 비밀번호 없는 회원 생성
        Group anotherGroup = Group.create("다른대학교", GroupType.UNIVERSITY, "서울특별시 강남구");
        Group savedAnotherGroup = groupRepository.save(anotherGroup);

        Member socialOnlyMember = Member.create(savedAnotherGroup.getGroupId(), "소셜전용유저", RecommendationType.SAVER);
        Member savedSocialOnlyMember = memberRepository.save(socialOnlyMember);

        // 비밀번호 없는 인증 정보 (소셜 로그인만 가능)
        MemberAuthentication socialOnlyAuth = MemberAuthentication.createSocialAuth(
                savedSocialOnlyMember.getMemberId(),
                "social@example.com",
                "소셜전용유저"
        );
        MemberAuthentication savedSocialOnlyAuth = memberAuthenticationRepository.save(socialOnlyAuth);

        // 카카오 소셜 계정 1개만 연동 (유일한 로그인 수단)
        SocialAccount onlyKakao = SocialAccount.create(
                savedSocialOnlyAuth.getMemberAuthenticationId(),
                SocialProvider.KAKAO,
                "kakao_only_user",
                "kakao_access_token",
                "kakao_refresh_token",
                "bearer",
                LocalDateTime.now().plusDays(30)
        );
        SocialAccount savedOnlyKakao = socialAccountRepository.save(onlyKakao);

        String socialOnlyAccessToken = createAccessToken(savedSocialOnlyMember.getMemberId());

        // when & then
        mockMvc.perform(delete("/api/v1/members/me/social-accounts/{socialAccountId}", savedOnlyKakao.getSocialAccountId())
                        .header("Authorization", socialOnlyAccessToken))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("E409"))
                .andExpect(jsonPath("$.error.message").value("유일한 로그인 수단입니다. 연동 해제하려면 먼저 비밀번호를 설정해주세요."))
                .andDo(document("social-account/remove-last-login-method",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        pathParameters(
                                parameterWithName("socialAccountId").description("연동 해제할 소셜 계정 ID (유일한 로그인 수단)")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (ERROR)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드 (E409)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지")
                        )
                ));
    }
}

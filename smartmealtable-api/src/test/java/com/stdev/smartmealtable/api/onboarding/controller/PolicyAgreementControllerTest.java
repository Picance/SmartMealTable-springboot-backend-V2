package com.stdev.smartmealtable.api.onboarding.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.api.onboarding.dto.request.PolicyAgreementRequest;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.policy.entity.Policy;
import com.stdev.smartmealtable.domain.policy.entity.PolicyType;
import com.stdev.smartmealtable.storage.db.member.entity.MemberAuthenticationJpaEntity;
import com.stdev.smartmealtable.storage.db.member.entity.MemberJpaEntity;
import com.stdev.smartmealtable.storage.db.member.repository.MemberAuthenticationJpaRepository;
import com.stdev.smartmealtable.storage.db.member.repository.MemberJpaRepository;
import com.stdev.smartmealtable.storage.db.policy.PolicyJpaEntity;
import com.stdev.smartmealtable.storage.db.policy.repository.PolicyAgreementJpaRepository;
import com.stdev.smartmealtable.storage.db.policy.repository.PolicyJpaRepository;
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

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PolicyAgreement Controller 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PolicyAgreementControllerTest extends AbstractContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Autowired
    private MemberAuthenticationJpaRepository memberAuthenticationJpaRepository;

    @Autowired
    private PolicyJpaRepository policyJpaRepository;

    @Autowired
    private PolicyAgreementJpaRepository policyAgreementJpaRepository;

    private Long testMemberId;
    private String testAccessToken;
    private Policy mandatoryPolicy;
    private Policy optionalPolicy;

    @BeforeEach
    void setUp() {
        // 1. 테스트 회원 생성
        MemberJpaEntity memberEntity = MemberJpaEntity.from(
                com.stdev.smartmealtable.domain.member.entity.Member.create(
                        null,
                        "테스터",
                        RecommendationType.BALANCED
                )
        );
        memberEntity = memberJpaRepository.save(memberEntity);
        testMemberId = memberEntity.getMemberId();

        // 2. 회원 인증 정보 생성
        MemberAuthentication memberAuthentication = MemberAuthentication.createEmailAuth(
                testMemberId,
                "test@example.com",
                "encodedPassword",
                "테스터"
        );
        MemberAuthenticationJpaEntity authEntity = MemberAuthenticationJpaEntity.from(memberAuthentication);
        memberAuthenticationJpaRepository.save(authEntity);

        // 3. JWT 토큰 생성
        testAccessToken = jwtTokenProvider.createToken(testMemberId);

        // 4. 약관 생성 (필수)
        mandatoryPolicy = Policy.create(
                "서비스 이용약관",
                "서비스 이용약관 내용입니다.",
                PolicyType.REQUIRED,
                "1.0",
                true  // 필수 약관
        );
        PolicyJpaEntity mandatoryEntity = PolicyJpaEntity.from(mandatoryPolicy);
        mandatoryEntity = policyJpaRepository.save(mandatoryEntity);
        mandatoryPolicy = mandatoryEntity.toDomain();

        // 5. 약관 생성 (선택)
        optionalPolicy = Policy.create(
                "마케팅 정보 수신 동의",
                "마케팅 정보 수신 동의 내용입니다.",
                PolicyType.OPTIONAL,
                "1.0",
                false  // 선택 약관
        );
        PolicyJpaEntity optionalEntity = PolicyJpaEntity.from(optionalPolicy);
        optionalEntity = policyJpaRepository.save(optionalEntity);
        optionalPolicy = optionalEntity.toDomain();
    }

    @Test
    @DisplayName("약관 동의 성공 - 필수 및 선택 약관 모두 동의")
    void agreeToPolicies_success() throws Exception {
        // given
        PolicyAgreementRequest request = new PolicyAgreementRequest(
                List.of(
                        new PolicyAgreementRequest.AgreementItem(mandatoryPolicy.getPolicyId(), true),
                        new PolicyAgreementRequest.AgreementItem(optionalPolicy.getPolicyId(), true)
                )
        );

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/policy-agreements")
                        .header("Authorization", "Bearer " + testAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.memberAuthenticationId").isNumber())
                .andExpect(jsonPath("$.data.agreedCount").value(2))
                .andExpect(jsonPath("$.data.message").value("약관 동의가 성공적으로 완료되었습니다."));
    }

    @Test
    @DisplayName("약관 동의 성공 - 필수 약관만 동의")
    void agreeToPolicies_success_mandatoryOnly() throws Exception {
        // given
        PolicyAgreementRequest request = new PolicyAgreementRequest(
                List.of(
                        new PolicyAgreementRequest.AgreementItem(mandatoryPolicy.getPolicyId(), true),
                        new PolicyAgreementRequest.AgreementItem(optionalPolicy.getPolicyId(), false)
                )
        );

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/policy-agreements")
                        .header("Authorization", "Bearer " + testAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.agreedCount").value(1));
    }

    @Test
    @DisplayName("약관 동의 실패 - 필수 약관 미동의 (422)")
    void agreeToPolicies_fail_mandatoryPolicyNotAgreed() throws Exception {
        // given
        PolicyAgreementRequest request = new PolicyAgreementRequest(
                List.of(
                        new PolicyAgreementRequest.AgreementItem(mandatoryPolicy.getPolicyId(), false),  // 필수 약관 미동의
                        new PolicyAgreementRequest.AgreementItem(optionalPolicy.getPolicyId(), true)
                )
        );

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/policy-agreements")
                        .header("Authorization", "Bearer " + testAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"))
                .andExpect(jsonPath("$.error.message").value(containsString("필수 약관")));
    }

    @Test
    @DisplayName("약관 동의 실패 - 빈 약관 목록 (422)")
    void agreeToPolicies_fail_emptyAgreements() throws Exception {
        // given
        PolicyAgreementRequest request = new PolicyAgreementRequest(List.of());

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/policy-agreements")
                        .header("Authorization", "Bearer " + testAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"));
    }

    @Test
    @DisplayName("약관 동의 실패 - 유효하지 않은 토큰 (401)")
    void agreeToPolicies_fail_invalidToken() throws Exception {
        // given
        PolicyAgreementRequest request = new PolicyAgreementRequest(
                List.of(
                        new PolicyAgreementRequest.AgreementItem(mandatoryPolicy.getPolicyId(), true)
                )
        );

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/policy-agreements")
                        .header("Authorization", "Bearer invalid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}

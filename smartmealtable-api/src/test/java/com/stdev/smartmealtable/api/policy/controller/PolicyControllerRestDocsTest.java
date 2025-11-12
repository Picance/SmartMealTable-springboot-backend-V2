package com.stdev.smartmealtable.api.policy.controller;

import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.common.vo.AddressType;
import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.domain.policy.entity.Policy;
import com.stdev.smartmealtable.domain.policy.entity.PolicyType;
import com.stdev.smartmealtable.domain.policy.repository.PolicyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PolicyController REST Docs 테스트
 * 약관 조회 API 문서화
 */
@DisplayName("PolicyController REST Docs 테스트")
class PolicyControllerRestDocsTest extends AbstractRestDocsTest {

    @Autowired
    private PolicyRepository policyRepository;

    @Test
    @DisplayName("약관 목록 조회 성공 - 200")
    void getPolicies_success_docs() throws Exception {
        // given
        createPolicy("서비스 이용약관", PolicyType.TERMS_OF_SERVICE, "1.0", "서비스 이용약관 내용입니다.", true);
        createPolicy("개인정보 처리방침", PolicyType.PRIVACY_POLICY, "1.0", "개인정보 처리방침 내용입니다.", true);
        createPolicy("마케팅 정보 수신 동의", PolicyType.MARKETING_CONSENT, "1.0", "마케팅 정보 수신 동의 내용입니다.", false);

        // when & then
        mockMvc.perform(get("/api/v1/policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.policies").isArray())
                .andDo(document("policy/get-policies-success",
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.policies").type(JsonFieldType.ARRAY).description("약관 목록 (content 제외)"),
                                fieldWithPath("data.policies[].policyId").type(JsonFieldType.NUMBER).description("약관 ID"),
                                fieldWithPath("data.policies[].title").type(JsonFieldType.STRING).description("약관 제목"),
                                fieldWithPath("data.policies[].type").type(JsonFieldType.STRING).description("약관 유형 (REQUIRED, OPTIONAL)"),
                                fieldWithPath("data.policies[].version").type(JsonFieldType.STRING).description("약관 버전"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("약관 상세 조회 성공 - 200")
    void getPolicy_success_docs() throws Exception {
        // given
        Policy policy = createPolicy("서비스 이용약관", PolicyType.TERMS_OF_SERVICE, "1.0", "서비스 이용약관 상세 내용입니다.", true);

        // when & then
        mockMvc.perform(get("/api/v1/policies/{policyId}", policy.getPolicyId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.policyId").value(policy.getPolicyId()))
                .andDo(document("policy/get-policy-success",
                        pathParameters(
                                parameterWithName("policyId").description("약관 ID")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.policyId").type(JsonFieldType.NUMBER).description("약관 ID"),
                                fieldWithPath("data.title").type(JsonFieldType.STRING).description("약관 제목"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING).description("약관 내용"),
                                fieldWithPath("data.type").type(JsonFieldType.STRING).description("약관 유형 (REQUIRED, OPTIONAL)"),
                                fieldWithPath("data.version").type(JsonFieldType.STRING).description("약관 버전"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("약관 상세 조회 실패 - 존재하지 않는 약관 (404)")
    void getPolicy_notFound_docs() throws Exception {
        // given
        Long nonExistentPolicyId = 999L;

        // when & then
        mockMvc.perform(get("/api/v1/policies/{policyId}", nonExistentPolicyId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andDo(document("policy/get-policy-not-found",
                        pathParameters(
                                parameterWithName("policyId").description("약관 ID (존재하지 않음)")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (에러 시 null)").optional(),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지")
                        )
                ));
    }

    // Helper methods
    private Policy createPolicy(String title, PolicyType type, String version, String content, Boolean isMandatory) {
        Policy policy = Policy.create(title, content, type, version, isMandatory);
        return policyRepository.save(policy);
    }
}

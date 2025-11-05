package com.stdev.smartmealtable.admin.policy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.admin.common.AbstractAdminContainerTest;
import com.stdev.smartmealtable.admin.policy.controller.dto.CreatePolicyRequest;
import com.stdev.smartmealtable.admin.policy.controller.dto.UpdatePolicyRequest;
import com.stdev.smartmealtable.domain.policy.entity.PolicyType;
import com.stdev.smartmealtable.storage.db.policy.PolicyAgreementJpaEntity;
import com.stdev.smartmealtable.storage.db.policy.repository.PolicyAgreementJpaRepository;
import com.stdev.smartmealtable.storage.db.policy.PolicyJpaEntity;
import com.stdev.smartmealtable.storage.db.policy.repository.PolicyJpaRepository;
import com.stdev.smartmealtable.storage.db.member.entity.MemberJpaEntity;
import com.stdev.smartmealtable.storage.db.member.repository.MemberJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 약관 관리 Controller 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("약관 관리 Controller 통합 테스트")
class PolicyControllerTest extends AbstractAdminContainerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private PolicyJpaRepository policyJpaRepository;
    
    @Autowired
    private PolicyAgreementJpaRepository policyAgreementJpaRepository;
    
    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @BeforeEach
    void setUp() {
        policyAgreementJpaRepository.deleteAll();
        policyJpaRepository.deleteAll();
        memberJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("약관 목록 조회 - 성공")
    void getPolicies_Success() throws Exception {
        // given
        PolicyJpaEntity policy1 = createPolicy("서비스 이용약관", "내용1", PolicyType.REQUIRED, "1.0", true, true);
        PolicyJpaEntity policy2 = createPolicy("개인정보 처리방침", "내용2", PolicyType.REQUIRED, "1.0", true, true);
        policyJpaRepository.save(policy1);
        policyJpaRepository.save(policy2);

        // when & then
        mockMvc.perform(get("/api/v1/admin/policies")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.policies", hasSize(2)))
                .andExpect(jsonPath("$.data.pageInfo.page").value(0))
                .andExpect(jsonPath("$.data.pageInfo.size").value(20))
                .andExpect(jsonPath("$.data.pageInfo.totalElements").value(2))
                .andExpect(jsonPath("$.data.pageInfo.totalPages").value(1));
    }

    @Test
    @DisplayName("약관 목록 조회 - 제목 검색")
    void getPolicies_WithTitleSearch() throws Exception {
        // given
        PolicyJpaEntity policy1 = createPolicy("서비스 이용약관", "내용1", PolicyType.REQUIRED, "1.0", true, true);
        PolicyJpaEntity policy2 = createPolicy("개인정보 처리방침", "내용2", PolicyType.REQUIRED, "1.0", true, true);
        policyJpaRepository.save(policy1);
        policyJpaRepository.save(policy2);

        // when & then
        mockMvc.perform(get("/api/v1/admin/policies")
                        .param("title", "개인정보")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.policies", hasSize(1)))
                .andExpect(jsonPath("$.data.policies[0].title").value("개인정보 처리방침"));
    }

    @Test
    @DisplayName("약관 목록 조회 - 활성 상태 필터")
    void getPolicies_WithActiveFilter() throws Exception {
        // given
        PolicyJpaEntity activePolicy = createPolicy("활성 약관", "내용1", PolicyType.REQUIRED, "1.0", true, true);
        PolicyJpaEntity inactivePolicy = createPolicy("비활성 약관", "내용2", PolicyType.OPTIONAL, "1.0", false, false);
        policyJpaRepository.save(activePolicy);
        policyJpaRepository.save(inactivePolicy);

        // when & then
        mockMvc.perform(get("/api/v1/admin/policies")
                        .param("isActive", "true")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.policies", hasSize(1)))
                .andExpect(jsonPath("$.data.policies[0].title").value("활성 약관"))
                .andExpect(jsonPath("$.data.policies[0].isActive").value(true));
    }

    @Test
    @DisplayName("약관 상세 조회 - 성공")
    void getPolicy_Success() throws Exception {
        // given
        PolicyJpaEntity policy = createPolicy("서비스 이용약관", "상세 내용", PolicyType.REQUIRED, "1.0", true, true);
        PolicyJpaEntity saved = policyJpaRepository.save(policy);

        // when & then
        mockMvc.perform(get("/api/v1/admin/policies/{policyId}", saved.getPolicyId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.policyId").value(saved.getPolicyId()))
                .andExpect(jsonPath("$.data.title").value("서비스 이용약관"))
                .andExpect(jsonPath("$.data.content").value("상세 내용"))
                .andExpect(jsonPath("$.data.type").value("REQUIRED"))
                .andExpect(jsonPath("$.data.version").value("1.0"))
                .andExpect(jsonPath("$.data.isMandatory").value(true))
                .andExpect(jsonPath("$.data.isActive").value(true));
    }

    @Test
    @DisplayName("약관 상세 조회 - 존재하지 않는 약관 (404)")
    void getPolicy_NotFound() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/admin/policies/{policyId}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("POLICY_NOT_FOUND"));
    }

    @Test
    @DisplayName("약관 생성 - 성공")
    void createPolicy_Success() throws Exception {
        // given
        CreatePolicyRequest request = new CreatePolicyRequest(
                "서비스 이용약관",
                "상세 내용",
                PolicyType.REQUIRED,
                "1.0",
                true
        );

        // when & then
        mockMvc.perform(post("/api/v1/admin/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.policyId").isNumber())
                .andExpect(jsonPath("$.data.title").value("서비스 이용약관"))
                .andExpect(jsonPath("$.data.content").value("상세 내용"))
                .andExpect(jsonPath("$.data.type").value("REQUIRED"))
                .andExpect(jsonPath("$.data.version").value("1.0"))
                .andExpect(jsonPath("$.data.isMandatory").value(true))
                .andExpect(jsonPath("$.data.isActive").value(true));
    }

    @Test
    @DisplayName("약관 생성 - 중복된 제목 (409)")
    void createPolicy_DuplicateTitle() throws Exception {
        // given
        PolicyJpaEntity existingPolicy = createPolicy("서비스 이용약관", "기존 내용", PolicyType.REQUIRED, "1.0", true, true);
        policyJpaRepository.save(existingPolicy);

        CreatePolicyRequest request = new CreatePolicyRequest(
                "서비스 이용약관",  // 중복된 제목
                "새로운 내용",
                PolicyType.REQUIRED,
                "1.0",
                true
        );

        // when & then
        mockMvc.perform(post("/api/v1/admin/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_POLICY_TITLE"));
    }

    @Test
    @DisplayName("약관 생성 - 필수 필드 누락 (422)")
    void createPolicy_MissingRequiredFields() throws Exception {
        // given
        CreatePolicyRequest request = new CreatePolicyRequest(
                "",  // 빈 제목
                "내용",
                PolicyType.REQUIRED,
                "1.0",
                true
        );

        // when & then
        mockMvc.perform(post("/api/v1/admin/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"));
    }

    @Test
    @DisplayName("약관 수정 - 성공")
    void updatePolicy_Success() throws Exception {
        // given
        PolicyJpaEntity policy = createPolicy("서비스 이용약관", "기존 내용", PolicyType.REQUIRED, "1.0", true, true);
        PolicyJpaEntity saved = policyJpaRepository.save(policy);

        UpdatePolicyRequest request = new UpdatePolicyRequest(
                "서비스 이용약관 (수정됨)",
                "수정된 내용",
                PolicyType.OPTIONAL,
                "2.0",
                false
        );

        // when & then
        mockMvc.perform(put("/api/v1/admin/policies/{policyId}", saved.getPolicyId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.policyId").value(saved.getPolicyId()))
                .andExpect(jsonPath("$.data.title").value("서비스 이용약관 (수정됨)"))
                .andExpect(jsonPath("$.data.content").value("수정된 내용"))
                .andExpect(jsonPath("$.data.type").value("OPTIONAL"))
                .andExpect(jsonPath("$.data.version").value("2.0"))
                .andExpect(jsonPath("$.data.isMandatory").value(false));
    }

    @Test
    @DisplayName("약관 수정 - 존재하지 않는 약관 (404)")
    void updatePolicy_NotFound() throws Exception {
        // given
        UpdatePolicyRequest request = new UpdatePolicyRequest(
                "수정된 제목",
                "수정된 내용",
                PolicyType.REQUIRED,
                "2.0",
                true
        );

        // when & then
        mockMvc.perform(put("/api/v1/admin/policies/{policyId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("POLICY_NOT_FOUND"));
    }

    @Test
    @DisplayName("약관 수정 - 중복된 제목 (409)")
    void updatePolicy_DuplicateTitle() throws Exception {
        // given
        PolicyJpaEntity policy1 = createPolicy("약관1", "내용1", PolicyType.REQUIRED, "1.0", true, true);
        PolicyJpaEntity policy2 = createPolicy("약관2", "내용2", PolicyType.REQUIRED, "1.0", true, true);
        PolicyJpaEntity saved1 = policyJpaRepository.save(policy1);
        policyJpaRepository.save(policy2);

        UpdatePolicyRequest request = new UpdatePolicyRequest(
                "약관2",  // 다른 약관의 제목
                "수정된 내용",
                PolicyType.REQUIRED,
                "2.0",
                true
        );

        // when & then
        mockMvc.perform(put("/api/v1/admin/policies/{policyId}", saved1.getPolicyId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_POLICY_TITLE"));
    }

    @Test
    @DisplayName("약관 삭제 - 성공")
    void deletePolicy_Success() throws Exception {
        // given
        PolicyJpaEntity policy = createPolicy("삭제할 약관", "내용", PolicyType.OPTIONAL, "1.0", false, true);
        PolicyJpaEntity saved = policyJpaRepository.save(policy);

        // when & then
        mockMvc.perform(delete("/api/v1/admin/policies/{policyId}", saved.getPolicyId()))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.result").value("SUCCESS"));
    }

    @Test
    @DisplayName("약관 삭제 - 존재하지 않는 약관 (404)")
    void deletePolicy_NotFound() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/admin/policies/{policyId}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"));
    }

    // TODO: PolicyAgreement 엔티티 구현 후 주석 해제
    /*
    @Test
    @DisplayName("약관 삭제 - 동의 내역이 있는 경우 (409)")
    void deletePolicy_HasAgreements() throws Exception {
        // given
        MemberJpaEntity member = createMember("test@example.com");
        MemberJpaEntity savedMember = memberJpaRepository.save(member);
        
        PolicyJpaEntity policy = createPolicy("약관", "내용", PolicyType.REQUIRED, "1.0", true, true);
        PolicyJpaEntity savedPolicy = policyJpaRepository.save(policy);
        
        PolicyAgreementJpaEntity agreement = createAgreement(savedMember, savedPolicy);
        policyAgreementJpaRepository.save(agreement);

        // when & then
        mockMvc.perform(delete("/api/v1/admin/policies/{policyId}", savedPolicy.getPolicyId()))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E409"));
    }
    */

    @Test
    @DisplayName("약관 활성/비활성 토글 - 성공 (활성 → 비활성)")
    void togglePolicyActive_ActiveToInactive() throws Exception {
        // given
        PolicyJpaEntity policy = createPolicy("약관", "내용", PolicyType.REQUIRED, "1.0", true, true);
        PolicyJpaEntity saved = policyJpaRepository.save(policy);

        // when & then
        mockMvc.perform(patch("/api/v1/admin/policies/{policyId}/toggle", saved.getPolicyId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.policyId").value(saved.getPolicyId()))
                .andExpect(jsonPath("$.data.isActive").value(false));
    }

    @Test
    @DisplayName("약관 활성/비활성 토글 - 성공 (비활성 → 활성)")
    void togglePolicyActive_InactiveToActive() throws Exception {
        // given
        PolicyJpaEntity policy = createPolicy("약관", "내용", PolicyType.REQUIRED, "1.0", true, false);
        PolicyJpaEntity saved = policyJpaRepository.save(policy);

        // when & then
        mockMvc.perform(patch("/api/v1/admin/policies/{policyId}/toggle", saved.getPolicyId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.policyId").value(saved.getPolicyId()))
                .andExpect(jsonPath("$.data.isActive").value(true));
    }

    @Test
    @DisplayName("약관 활성/비활성 토글 - 존재하지 않는 약관 (404)")
    void togglePolicyActive_NotFound() throws Exception {
        // when & then
        mockMvc.perform(patch("/api/v1/admin/policies/{policyId}/toggle", 999L))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("POLICY_NOT_FOUND"));
    }

    // Helper methods
    private PolicyJpaEntity createPolicy(String title, String content, PolicyType type, 
                                          String version, Boolean isMandatory, Boolean isActive) {
        // Domain 엔티티를 통해 생성
        com.stdev.smartmealtable.domain.policy.entity.Policy domainPolicy = 
                com.stdev.smartmealtable.domain.policy.entity.Policy.reconstitute(
                        null, title, content, type, version, isMandatory, isActive
                );
        
        return PolicyJpaEntity.from(domainPolicy);
    }
}

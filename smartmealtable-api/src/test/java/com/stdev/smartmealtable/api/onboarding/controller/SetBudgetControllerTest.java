package com.stdev.smartmealtable.api.onboarding.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.api.common.AbstractContainerTest;
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

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 온보딩 - 예산 설정 API 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SetBudgetControllerTest extends AbstractContainerTest {

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
    private JwtTokenProvider jwtTokenProvider;

    private Long authenticatedMemberId;

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
    }


    @Test
    @DisplayName("예산 설정 성공 - 정상 요청")
    void setBudget_success() throws Exception {
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
                        .header("Authorization", createAuthorizationHeader(authenticatedMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.monthlyBudget").value(300000))
                .andExpect(jsonPath("$.data.dailyBudget").value(10000))
                .andExpect(jsonPath("$.data.mealBudgets").isArray())
                .andExpect(jsonPath("$.data.mealBudgets[0].mealType").exists())
                .andExpect(jsonPath("$.data.mealBudgets[0].budget").exists());
    }

    @Test
    @DisplayName("예산 설정 실패 - 월별 예산 null")
    void setBudget_monthlyBudgetNull() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("dailyBudget", 10000);
        
        Map<String, Integer> mealBudgets = new HashMap<>();
        mealBudgets.put("BREAKFAST", 3000);
        request.put("mealBudgets", mealBudgets);

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/budget")
                        .header("Authorization", createAuthorizationHeader(authenticatedMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"));
    }

    @Test
    @DisplayName("예산 설정 실패 - 일일 예산 null")
    void setBudget_dailyBudgetNull() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("monthlyBudget", 300000);
        
        Map<String, Integer> mealBudgets = new HashMap<>();
        mealBudgets.put("BREAKFAST", 3000);
        request.put("mealBudgets", mealBudgets);

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/budget")
                        .header("Authorization", createAuthorizationHeader(authenticatedMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"));
    }

    @Test
    @DisplayName("예산 설정 실패 - 식사별 예산 null")
    void setBudget_mealBudgetsNull() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("monthlyBudget", 300000);
        request.put("dailyBudget", 10000);

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/budget")
                        .header("Authorization", createAuthorizationHeader(authenticatedMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"));
    }

    @Test
    @DisplayName("예산 설정 실패 - 음수 예산")
    void setBudget_negativeBudget() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("monthlyBudget", -1000);
        request.put("dailyBudget", 10000);
        
        Map<String, Integer> mealBudgets = new HashMap<>();
        mealBudgets.put("BREAKFAST", 3000);
        request.put("mealBudgets", mealBudgets);

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/budget")
                        .header("Authorization", createAuthorizationHeader(authenticatedMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"));
    }

    @Test
    @DisplayName("예산 설정 실패 - JWT 토큰 없음")
    void setBudget_noToken() throws Exception {
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
                .andExpect(status().isBadRequest());
    }

    // === Helper Methods ===

    /**
     * JWT 토큰을 생성하여 "Bearer {token}" 형식으로 반환
     */
    private String createAuthorizationHeader(Long memberId) {
        String token = jwtTokenProvider.createToken(memberId);
        return "Bearer " + token;
    }
}

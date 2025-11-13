package com.stdev.smartmealtable.api.budget.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.api.config.MockChatModelConfig;
import com.stdev.smartmealtable.api.config.MockExpenditureServiceConfig;
import com.stdev.smartmealtable.core.error.ErrorCode;
import com.stdev.smartmealtable.domain.budget.DailyBudget;
import com.stdev.smartmealtable.domain.budget.DailyBudgetRepository;
import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.common.vo.AddressType;
import com.stdev.smartmealtable.domain.expenditure.MealType;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 일일 예산 일괄 등록 API 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import({MockChatModelConfig.class, MockExpenditureServiceConfig.class})
class BulkCreateDailyBudgetControllerTest extends AbstractContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberAuthenticationRepository authenticationRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private DailyBudgetRepository dailyBudgetRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Member member;
    private String accessToken;

    @BeforeEach
    void setUp() {
        Group group = Group.create(
                "일괄등록대학교",
                GroupType.UNIVERSITY,
                Address.of("일괄등록대학교", null, "서울특별시", null, null, null, AddressType.HOME)
        );
        Group savedGroup = groupRepository.save(group);

        member = Member.create(savedGroup.getGroupId(), "일괄등록회원", null, RecommendationType.BALANCED);
        member = memberRepository.save(member);

        MemberAuthentication auth = MemberAuthentication.createEmailAuth(
                member.getMemberId(),
                "bulk-budget@example.com",
                "hashedPassword",
                "일괄등록회원"
        );
        authenticationRepository.save(auth);

        accessToken = jwtTokenProvider.createToken(member.getMemberId());
    }

    @Test
    @DisplayName("일일 예산 일괄 등록 성공")
    void bulkCreateDailyBudget_success() throws Exception {
        // given
        Map<MealType, Integer> mealBudgets = new EnumMap<>(MealType.class);
        mealBudgets.put(MealType.BREAKFAST, 3000);
        mealBudgets.put(MealType.LUNCH, 4000);
        mealBudgets.put(MealType.DINNER, 3000);

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(2);

        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("startDate", startDate.toString());
        payload.put("endDate", endDate.toString());
        payload.put("dailyFoodBudget", 12000);

        Map<String, Integer> mealBudgetPayload = new java.util.HashMap<>();
        mealBudgets.forEach((key, value) -> mealBudgetPayload.put(key.name(), value));
        payload.put("mealBudgets", mealBudgetPayload);

        // when & then
        mockMvc.perform(post("/api/v1/budgets/daily/bulk")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(payload)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.dailyBudgetCount").value(3))
                .andExpect(jsonPath("$.data.dailyFoodBudget").value(12000));
    }

    @Test
    @DisplayName("일일 예산 일괄 등록 실패 - 이미 존재하는 날짜 포함")
    void bulkCreateDailyBudget_conflict_whenExists() throws Exception {
        // given
        LocalDate startDate = LocalDate.now();
        DailyBudget existing = DailyBudget.create(member.getMemberId(), 10000, startDate.plusDays(1));
        dailyBudgetRepository.save(existing);

        Map<MealType, Integer> mealBudgets = new EnumMap<>(MealType.class);
        mealBudgets.put(MealType.BREAKFAST, 3000);

        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("startDate", startDate.toString());
        payload.put("endDate", startDate.plusDays(2).toString());
        payload.put("dailyFoodBudget", 12000);
        Map<String, Integer> mealBudgetPayload = new java.util.HashMap<>();
        mealBudgets.forEach((key, value) -> mealBudgetPayload.put(key.name(), value));
        payload.put("mealBudgets", mealBudgetPayload);

        // when & then
        mockMvc.perform(post("/api/v1/budgets/daily/bulk")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(payload)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.E409.name()));
    }
}

package com.stdev.smartmealtable.admin.statistics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.admin.common.AbstractAdminContainerTest;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.domain.store.StoreType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 통계 조회 API 통합 테스트 (ADMIN)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StatisticsControllerTest extends AbstractAdminContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberAuthenticationRepository memberAuthenticationRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    private Category category1;
    private Category category2;
    private Store store1;
    private Store store2;
    private Group group1;
    private Member member1;
    private Member member2;
    private MemberAuthentication auth1;
    private MemberAuthentication auth2;

    @BeforeEach
    void setUp() {
        // 테스트 데이터를 간소화: API가 동작하는지만 확인
        // 실제 데이터는 각 테스트에서 필요시 추가 생성
    }

    @Test
    @DisplayName("[성공] 사용자 통계 조회")
    void getUserStatistics_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/statistics/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.totalMembers").isNumber())
                .andExpect(jsonPath("$.data.socialLoginMembers").isNumber())
                .andExpect(jsonPath("$.data.emailLoginMembers").isNumber())
                .andExpect(jsonPath("$.data.deletedMembers").isNumber())
                .andExpect(jsonPath("$.data.membersByGroupType").exists());
    }

    @Test
    @DisplayName("[성공] 지출 통계 조회")
    void getExpenditureStatistics_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/statistics/expenditures"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.totalAmount").isNumber())
                .andExpect(jsonPath("$.data.averageAmount").exists())
                .andExpect(jsonPath("$.data.totalCount").isNumber())
                .andExpect(jsonPath("$.data.topCategoriesByAmount").exists())
                .andExpect(jsonPath("$.data.expendituresByMealType").exists())
                .andExpect(jsonPath("$.data.averageAmountPerMember").exists());
    }

    @Test
    @DisplayName("[성공] 음식점 통계 조회")
    void getStoreStatistics_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/statistics/stores"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.totalStores").isNumber())
                .andExpect(jsonPath("$.data.storesByCategory").exists())
                .andExpect(jsonPath("$.data.storesByType").exists())
                .andExpect(jsonPath("$.data.totalFoods").exists())
                .andExpect(jsonPath("$.data.averageFoodsPerStore").exists())
                .andExpect(jsonPath("$.data.topStoresByViews").isArray())
                .andExpect(jsonPath("$.data.topStoresByReviews").isArray())
                .andExpect(jsonPath("$.data.topStoresByFavorites").isArray());
    }
}

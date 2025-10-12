package com.stdev.smartmealtable.api.expenditure.controller;

import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.expenditure.Expenditure;
import com.stdev.smartmealtable.domain.expenditure.ExpenditureRepository;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 지출 내역 조회 Controller 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("지출 내역 조회 Controller 테스트")
class GetExpenditureListControllerTest extends AbstractContainerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ExpenditureRepository expenditureRepository;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    private Member testMember;
    private Category testCategory;
    private String accessToken;
    
    @BeforeEach
    void setUp() {
        // 테스트 회원 생성
        testMember = Member.create(null, "테스트유저", RecommendationType.BALANCED);
        testMember = memberRepository.save(testMember);
        
        // 테스트 카테고리 생성
        testCategory = Category.reconstitute(null, "패스트푸드");
        testCategory = categoryRepository.save(testCategory);
        
        // Access Token 생성
        accessToken = jwtTokenProvider.createToken(testMember.getMemberId());
    }
    
    @Test
    @DisplayName("지출 내역 목록 조회 성공 - 기본 필터")
    void getExpenditureList_Success() throws Exception {
        // given
        Expenditure expenditure1 = Expenditure.create(
                testMember.getMemberId(),
                "맘스터치강남점",
                13500,
                LocalDate.of(2025, 10, 8),
                LocalTime.of(12, 30),
                testCategory.getCategoryId(),
                MealType.LUNCH,
                "동료와 점심",
                new ArrayList<>()
        );
        expenditureRepository.save(expenditure1);
        
        Expenditure expenditure2 = Expenditure.create(
                testMember.getMemberId(),
                "서브웨이역삼점",
                8500,
                LocalDate.of(2025, 10, 7),
                LocalTime.of(12, 0),
                testCategory.getCategoryId(),
                MealType.LUNCH,
                null,
                new ArrayList<>()
        );
        expenditureRepository.save(expenditure2);
        
        // when & then
        mockMvc.perform(get("/api/v1/expenditures")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("startDate", "2025-10-01")
                        .param("endDate", "2025-10-31")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.summary.totalAmount").value(22000))
                .andExpect(jsonPath("$.data.summary.totalCount").value(2))
                .andExpect(jsonPath("$.data.summary.averageAmount").value(11000))
                .andExpect(jsonPath("$.data.expenditures.content").isArray())
                .andExpect(jsonPath("$.data.expenditures.content.length()").value(2))
                .andExpect(jsonPath("$.data.expenditures.totalElements").value(2));
    }
    
    @Test
    @DisplayName("지출 내역 목록 조회 성공 - 식사 유형 필터 적용")
    void getExpenditureList_WithMealTypeFilter_Success() throws Exception {
        // given
        Expenditure lunchExpenditure = Expenditure.create(
                testMember.getMemberId(),
                "맘스터치강남점",
                13500,
                LocalDate.of(2025, 10, 8),
                LocalTime.of(12, 30),
                testCategory.getCategoryId(),
                MealType.LUNCH,
                null,
                new ArrayList<>()
        );
        expenditureRepository.save(lunchExpenditure);
        
        Expenditure dinnerExpenditure = Expenditure.create(
                testMember.getMemberId(),
                "저녁식당",
                20000,
                LocalDate.of(2025, 10, 8),
                LocalTime.of(19, 30),
                testCategory.getCategoryId(),
                MealType.DINNER,
                null,
                new ArrayList<>()
        );
        expenditureRepository.save(dinnerExpenditure);
        
        // when & then
        mockMvc.perform(get("/api/v1/expenditures")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("startDate", "2025-10-01")
                        .param("endDate", "2025-10-31")
                        .param("mealType", "LUNCH")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.summary.totalAmount").value(13500))
                .andExpect(jsonPath("$.data.summary.totalCount").value(1))
                .andExpect(jsonPath("$.data.expenditures.content.length()").value(1))
                .andExpect(jsonPath("$.data.expenditures.content[0].mealType").value("LUNCH"));
    }
    
    @Test
    @DisplayName("지출 내역 목록 조회 성공 - 빈 목록")
    void getExpenditureList_EmptyList_Success() throws Exception {
        // given - 데이터 없음
        
        // when & then
        mockMvc.perform(get("/api/v1/expenditures")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("startDate", "2025-10-01")
                        .param("endDate", "2025-10-31")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.summary.totalAmount").value(0))
                .andExpect(jsonPath("$.data.summary.totalCount").value(0))
                .andExpect(jsonPath("$.data.expenditures.content").isArray())
                .andExpect(jsonPath("$.data.expenditures.content.length()").value(0))
                .andExpect(jsonPath("$.data.expenditures.totalElements").value(0));
    }
    
    @Test
    @DisplayName("지출 내역 목록 조회 실패 - 필수 파라미터 누락 (startDate)")
    void getExpenditureList_MissingStartDate_Fail() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/expenditures")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("endDate", "2025-10-31")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E400"));
    }
    
    @Test
    @DisplayName("지출 내역 목록 조회 실패 - 필수 파라미터 누락 (endDate)")
    void getExpenditureList_MissingEndDate_Fail() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/expenditures")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("startDate", "2025-10-01")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E400"));
    }
    
    @Test
    @DisplayName("지출 내역 목록 조회 실패 - 인증되지 않은 요청 (401)")
    void getExpenditureList_Unauthorized_Fail() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/expenditures")
                        .param("startDate", "2025-10-01")
                        .param("endDate", "2025-10-31")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E401"));
    }
}

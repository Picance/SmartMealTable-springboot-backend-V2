package com.stdev.smartmealtable.api.expenditure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.api.expenditure.dto.request.CreateExpenditureFromCartRequest;
import com.stdev.smartmealtable.api.expenditure.dto.request.CreateExpenditureRequest;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import com.stdev.smartmealtable.storage.db.expenditure.ExpenditureJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 지출 내역 API 재설계 통합 테스트
 * - 장바구니 시나리오 (storeId + foodId 포함)
 * - 수기 입력 시나리오 (foodName만 포함)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("지출 내역 API 재설계 통합 테스트")
class ExpenditureControllerDualScenarioTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ExpenditureJpaRepository expenditureRepository;
    
    private static final String AUTH_TOKEN = "Bearer test-token";
    
    @BeforeEach
    void setUp() {
        expenditureRepository.deleteAll();
    }
    
    @Nested
    @DisplayName("장바구니 시나리오 (POST /api/v1/expenditures/from-cart)")
    class CartScenario {
        
        @Test
        @DisplayName("storeId와 foodId가 있는 지출 내역을 성공적으로 등록한다")
        void createExpenditureFromCart_Success() throws Exception {
            // Given
            Long storeId = 100L;
            Long foodId1 = 1000L;
            Long foodId2 = 1001L;
            
            CreateExpenditureFromCartRequest.CartExpenditureItemRequest item1 =
                    new CreateExpenditureFromCartRequest.CartExpenditureItemRequest(
                            foodId1,
                            "스파게티",
                            2,
                            12000
                    );
            
            CreateExpenditureFromCartRequest.CartExpenditureItemRequest item2 =
                    new CreateExpenditureFromCartRequest.CartExpenditureItemRequest(
                            foodId2,
                            "샐러드",
                            1,
                            6000
                    );
            
            CreateExpenditureFromCartRequest request = new CreateExpenditureFromCartRequest(
                    storeId,
                    "이탈리안 레스토랑",
                    30000,
                    LocalDate.of(2025, 10, 31),
                    LocalTime.of(12, 30),
                    1L,
                    MealType.LUNCH,
                    "회식",
                    List.of(item1, item2)
            );
            
            // When & Then
            MvcResult result = mockMvc.perform(
                    post("/api/v1/expenditures/from-cart")
                            .header("Authorization", AUTH_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
            )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.hasStoreLink").value(true))      // ◆ storeId가 있으므로 true
                    .andExpect(jsonPath("$.data.storeId").value(storeId))
                    .andExpect(jsonPath("$.data.items[0].hasFoodLink").value(true))   // ◆ foodId가 있으므로 true
                    .andExpect(jsonPath("$.data.items[0].foodId").value(foodId1))
                    .andExpect(jsonPath("$.data.items[0].foodName").value("스파게티"))
                    .andExpect(jsonPath("$.data.items[1].hasFoodLink").value(true))
                    .andExpect(jsonPath("$.data.items[1].foodId").value(foodId2))
                    .andExpect(jsonPath("$.data.items[1].foodName").value("샐러드"))
                    .andReturn();
            
            // 응답 검증
            String jsonResponse = result.getResponse().getContentAsString();
            assertThat(jsonResponse).contains("storeId");
            assertThat(jsonResponse).contains("hasStoreLink");
            assertThat(jsonResponse).contains("hasFoodLink");
        }
    }
    
    @Nested
    @DisplayName("수기 입력 시나리오 (POST /api/v1/expenditures)")
    class ManualInputScenario {
        
        @Test
        @DisplayName("foodId 없이 foodName만으로 지출 내역을 성공적으로 등록한다")
        void createExpenditureManualInput_Success() throws Exception {
            // Given
            CreateExpenditureRequest.ExpenditureItemRequest item1 =
                    new CreateExpenditureRequest.ExpenditureItemRequest(
                            null,              // ◆ foodId 없음
                            2,
                            12000
                    );
            
            CreateExpenditureRequest.ExpenditureItemRequest item2 =
                    new CreateExpenditureRequest.ExpenditureItemRequest(
                            null,              // ◆ foodId 없음
                            1,
                            6000
                    );
            
            CreateExpenditureRequest request = new CreateExpenditureRequest(
                    "임의의 음식점",
                    30000,
                    LocalDate.of(2025, 10, 31),
                    LocalTime.of(12, 30),
                    1L,
                    MealType.LUNCH,
                    "지출 메모",
                    List.of(item1, item2)
            );
            
            // When & Then
            MvcResult result = mockMvc.perform(
                    post("/api/v1/expenditures")
                            .header("Authorization", AUTH_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
            )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.hasStoreLink").value(false))      // ◆ storeId가 없으므로 false
                    .andExpect(jsonPath("$.data.storeId").doesNotExist())         // ◆ storeId는 null
                    .andExpect(jsonPath("$.data.items[0].hasFoodLink").value(false))  // ◆ foodId가 없으므로 false
                    .andReturn();
            
            // 응답 검증
            String jsonResponse = result.getResponse().getContentAsString();
            assertThat(jsonResponse).contains("hasStoreLink");
            assertThat(jsonResponse).contains("hasFoodLink");
        }
    }
    
    @Nested
    @DisplayName("기존 API 호환성 테스트")
    class BackwardCompatibility {
        
        @Test
        @DisplayName("기존 POST /api/v1/expenditures 엔드포인트는 여전히 동작한다")
        void createExpenditureBackwardCompatible() throws Exception {
            // Given
            CreateExpenditureRequest.ExpenditureItemRequest item =
                    new CreateExpenditureRequest.ExpenditureItemRequest(
                            1L,
                            1,
                            10000
                    );
            
            CreateExpenditureRequest request = new CreateExpenditureRequest(
                    "가게명",
                    10000,
                    LocalDate.of(2025, 10, 31),
                    LocalTime.of(12, 0),
                    null,
                    null,
                    null,
                    List.of(item)
            );
            
            // When & Then
            mockMvc.perform(
                    post("/api/v1/expenditures")
                            .header("Authorization", AUTH_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
            )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.expenditureId").exists());
        }
    }
}

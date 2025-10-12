package com.stdev.smartmealtable.api.expenditure.controller;

import com.stdev.smartmealtable.api.expenditure.service.GetExpenditureDetailService;
import com.stdev.smartmealtable.api.expenditure.service.dto.ExpenditureDetailServiceResponse;
import com.stdev.smartmealtable.api.expenditure.service.dto.ExpenditureItemServiceResponse;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpenditureController.class)
class GetExpenditureDetailControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private GetExpenditureDetailService getExpenditureDetailService;

    @Test
    @DisplayName("지출 내역 상세 조회 성공")
    void getExpenditureDetail_Success() throws Exception {
        // given
        Long memberId = 1L;
        Long expenditureId = 100L;
        
        ExpenditureDetailServiceResponse response = ExpenditureDetailServiceResponse.builder()
                .expenditureId(expenditureId)
                .storeName("맛있는 식당")
                .amount(25000)
                .expendedDate(LocalDate.of(2025, 10, 10))
                .expendedTime(LocalTime.of(12, 30))
                .categoryId(1L)
                .categoryName("한식")
                .mealType(MealType.LUNCH)
                .memo("회식")
                .items(List.of(
                        ExpenditureItemServiceResponse.builder()
                                .expenditureItemId(1L)
                                .foodId(10L)
                                .foodName("김치찌개")
                                .quantity(2)
                                .price(8000)
                                .build(),
                        ExpenditureItemServiceResponse.builder()
                                .expenditureItemId(2L)
                                .foodId(11L)
                                .foodName("불고기")
                                .quantity(1)
                                .price(9000)
                                .build()
                ))
                .build();
        
        given(getExpenditureDetailService.getExpenditureDetail(eq(expenditureId), anyLong()))
                .willReturn(response);
        
        // when & then
        mockMvc.perform(get("/api/v1/expenditures/{id}", expenditureId)
                        .header("X-Member-Id", memberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.expenditureId").value(expenditureId))
                .andExpect(jsonPath("$.data.storeName").value("맛있는 식당"))
                .andExpect(jsonPath("$.data.amount").value(25000))
                .andExpect(jsonPath("$.data.expendedDate").value("2025-10-10"))
                .andExpect(jsonPath("$.data.expendedTime").value("12:30:00"))
                .andExpect(jsonPath("$.data.categoryId").value(1))
                .andExpect(jsonPath("$.data.categoryName").value("한식"))
                .andExpect(jsonPath("$.data.mealType").value("LUNCH"))
                .andExpect(jsonPath("$.data.memo").value("회식"))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.items[0].foodName").value("김치찌개"))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2))
                .andExpect(jsonPath("$.data.items[0].price").value(8000))
                .andExpect(jsonPath("$.data.items[1].foodName").value("불고기"))
                .andExpect(jsonPath("$.data.items[1].quantity").value(1))
                .andExpect(jsonPath("$.data.items[1].price").value(9000));
    }

    @Test
    @DisplayName("존재하지 않는 지출 내역 조회 시 404 응답")
    void getExpenditureDetail_NotFound() throws Exception {
        // given
        Long memberId = 1L;
        Long expenditureId = 999L;
        
        given(getExpenditureDetailService.getExpenditureDetail(eq(expenditureId), anyLong()))
                .willThrow(new IllegalArgumentException("지출 내역을 찾을 수 없습니다."));
        
        // when & then
        mockMvc.perform(get("/api/v1/expenditures/{id}", expenditureId)
                        .header("X-Member-Id", memberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message").value("지출 내역을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("다른 회원의 지출 내역 조회 시 403 응답")
    void getExpenditureDetail_Forbidden() throws Exception {
        // given
        Long memberId = 1L;
        Long expenditureId = 100L;
        
        given(getExpenditureDetailService.getExpenditureDetail(eq(expenditureId), anyLong()))
                .willThrow(new SecurityException("해당 지출 내역에 접근할 권한이 없습니다."));
        
        // when & then
        mockMvc.perform(get("/api/v1/expenditures/{id}", expenditureId)
                        .header("X-Member-Id", memberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message").value("해당 지출 내역에 접근할 권한이 없습니다."));
    }

    @Test
    @DisplayName("삭제된 지출 내역 조회 시 404 응답")
    void getExpenditureDetail_Deleted() throws Exception {
        // given
        Long memberId = 1L;
        Long expenditureId = 100L;
        
        given(getExpenditureDetailService.getExpenditureDetail(eq(expenditureId), anyLong()))
                .willThrow(new IllegalArgumentException("삭제된 지출 내역입니다."));
        
        // when & then
        mockMvc.perform(get("/api/v1/expenditures/{id}", expenditureId)
                        .header("X-Member-Id", memberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message").value("삭제된 지출 내역입니다."));
    }
}

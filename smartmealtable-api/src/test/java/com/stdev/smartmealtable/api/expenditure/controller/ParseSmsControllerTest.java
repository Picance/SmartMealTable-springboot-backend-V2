package com.stdev.smartmealtable.api.expenditure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.api.config.MockChatModelConfig;
import com.stdev.smartmealtable.api.expenditure.dto.request.ParseSmsRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SMS 파싱 API 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("SMS 파싱 API 통합 테스트")
@Import(MockChatModelConfig.class)
class ParseSmsControllerTest extends AbstractContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/expenditures/parse-sms";

    @Test
    @DisplayName("KB국민카드 SMS 파싱 성공")
    void parseSms_KBCard_Success() throws Exception {
        // given
        String smsContent = "[KB국민카드] 10/08 12:30 승인 13,500원 일시불 맘스터치강남점";
        ParseSmsRequest request = new ParseSmsRequest(smsContent);

        // when
        ResultActions result = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.storeName").value("맘스터치강남점"))
                .andExpect(jsonPath("$.data.amount").value(13500))
                .andExpect(jsonPath("$.data.date").exists())
                .andExpect(jsonPath("$.data.time").value("12:30:00"))
                .andExpect(jsonPath("$.data.isParsed").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()));
    }

    @Test
    @DisplayName("NH농협카드 SMS 파싱 성공")
    void parseSms_NHCard_Success() throws Exception {
        // given
        String smsContent = "NH농협카드 승인 15,000원 10/09 13:45 버거킹홍대점";
        ParseSmsRequest request = new ParseSmsRequest(smsContent);

        // when
        ResultActions result = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.storeName").value("버거킹홍대점"))
                .andExpect(jsonPath("$.data.amount").value(15000))
                .andExpect(jsonPath("$.data.date").exists())
                .andExpect(jsonPath("$.data.time").value("13:45:00"))
                .andExpect(jsonPath("$.data.isParsed").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()));
    }

    @Test
    @DisplayName("신한카드 SMS 파싱 성공")
    void parseSms_ShinhanCard_Success() throws Exception {
        // given
        String smsContent = "신한카드 승인 20,000원 10/10 18:30 롯데리아강남역점";
        ParseSmsRequest request = new ParseSmsRequest(smsContent);

        // when
        ResultActions result = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.storeName").value("롯데리아강남역점"))
                .andExpect(jsonPath("$.data.amount").value(20000))
                .andExpect(jsonPath("$.data.date").exists())
                .andExpect(jsonPath("$.data.time").value("18:30:00"))
                .andExpect(jsonPath("$.data.isParsed").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()));
    }

    @Test
    @DisplayName("SMS 파싱 실패 - 지원하지 않는 형식")
    void parseSms_UnsupportedFormat_Failure() throws Exception {
        // given
        String smsContent = "알 수 없는 형식의 메시지";
        ParseSmsRequest request = new ParseSmsRequest(smsContent);

        // when
        ResultActions result = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.error.code").value("E422"))
                .andExpect(jsonPath("$.error.message").value("SMS 메시지 파싱에 실패했습니다. 지원하지 않는 형식입니다."));
    }

    @Test
    @DisplayName("SMS 파싱 실패 - 빈 메시지")
    void parseSms_EmptyMessage_Failure() throws Exception {
        // given
        ParseSmsRequest request = new ParseSmsRequest("");

        // when
        ResultActions result = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"));
    }

    @Test
    @DisplayName("SMS 파싱 실패 - null 메시지")
    void parseSms_NullMessage_Failure() throws Exception {
        // given
        String jsonRequest = "{\"smsMessage\": null}";

        // when
        ResultActions result = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest));

        // then
        result.andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"));
    }
}

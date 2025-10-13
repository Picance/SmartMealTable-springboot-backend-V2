package com.stdev.smartmealtable.api.store.controller;

import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.api.config.MockChatModelConfig;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.domain.store.StoreType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 가게 자동완성 검색 API 통합 테스트
 * TDD: RED-GREEN-REFACTOR
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
@Import(MockChatModelConfig.class)
class GetStoreAutocompleteControllerTest extends AbstractContainerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private StoreRepository storeRepository;
    
    @BeforeEach
    void setUp() {
        // 테스트 데이터 생성
        createTestStores();
    }
    
    private void createTestStores() {
        // 강남 관련 가게들
        storeRepository.save(createStore("강남 한정식", "서울특별시 강남구 강남대로 400"));
        storeRepository.save(createStore("강남 분식", "서울특별시 강남구 강남대로 410"));
        storeRepository.save(createStore("강남역 치킨", "서울특별시 강남구 강남대로 420"));
        
        // 신촌 관련 가게들
        storeRepository.save(createStore("신촌 맛집", "서울특별시 서대문구 신촌로 100"));
        storeRepository.save(createStore("신촌역 카페", "서울특별시 서대문구 신촌로 110"));
        
        // 기타 가게들
        storeRepository.save(createStore("홍대 피자", "서울특별시 마포구 홍익로 100"));
        storeRepository.save(createStore("이대 파스타", "서울특별시 서대문구 이화여대길 100"));
    }
    
    private Store createStore(String name, String address) {
        return Store.builder()
                .name(name)
                .categoryId(1L)
                .sellerId(1L)
                .address(address)
                .lotNumberAddress(address)
                .latitude(new BigDecimal("37.5"))
                .longitude(new BigDecimal("127.0"))
                .phoneNumber("02-1234-5678")
                .description("테스트 가게")
                .averagePrice(10000)
                .reviewCount(100)
                .viewCount(1000)
                .favoriteCount(50)
                .storeType(StoreType.RESTAURANT)
                .imageUrl("https://example.com/store.jpg")
                .registeredAt(LocalDateTime.now())
                .build();
    }
    
    @Test
    @DisplayName("자동완성 검색 성공 - 강남")
    void autocomplete_Success_Gangnam() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores/autocomplete")
                        .param("keyword", "강남"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.stores").isArray())
                .andExpect(jsonPath("$.data.stores.length()").value(3))
                .andExpect(jsonPath("$.data.stores[*].name").value(everyItem(containsString("강남"))));
    }
    
    @Test
    @DisplayName("자동완성 검색 성공 - 신촌")
    void autocomplete_Success_Sinchon() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores/autocomplete")
                        .param("keyword", "신촌"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.stores").isArray())
                .andExpect(jsonPath("$.data.stores.length()").value(2))
                .andExpect(jsonPath("$.data.stores[*].name").value(everyItem(containsString("신촌"))));
    }
    
    @Test
    @DisplayName("자동완성 검색 성공 - 검색 개수 제한")
    void autocomplete_Success_WithLimit() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores/autocomplete")
                        .param("keyword", "강남")
                        .param("limit", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.stores").isArray())
                .andExpect(jsonPath("$.data.stores.length()").value(lessThanOrEqualTo(2)));
    }
    
    @Test
    @DisplayName("자동완성 검색 성공 - 결과 없음")
    void autocomplete_Success_NoResult() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores/autocomplete")
                        .param("keyword", "존재하지않는가게"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.stores").isArray())
                .andExpect(jsonPath("$.data.stores.length()").value(0));
    }
    
    @Test
    @DisplayName("자동완성 검색 실패 - 키워드 없음 (400)")
    void autocomplete_Fail_NoKeyword() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores/autocomplete"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("자동완성 검색 실패 - 빈 키워드")
    void autocomplete_Fail_EmptyKeyword() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores/autocomplete")
                        .param("keyword", ""))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.stores").isArray())
                .andExpect(jsonPath("$.data.stores.length()").value(0));
    }
    
    @Test
    @DisplayName("자동완성 검색 실패 - 잘못된 limit 값 (음수)")
    void autocomplete_Fail_InvalidLimit() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores/autocomplete")
                        .param("keyword", "강남")
                        .param("limit", "-1"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("자동완성 검색 실패 - limit 최대값 초과")
    void autocomplete_Fail_LimitTooLarge() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/stores/autocomplete")
                        .param("keyword", "강남")
                        .param("limit", "100"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}

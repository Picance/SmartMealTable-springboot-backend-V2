package com.stdev.smartmealtable.api.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.api.config.MockChatModelConfig;
import com.stdev.smartmealtable.api.config.MockExpenditureServiceConfig;
import com.stdev.smartmealtable.storage.db.store.StoreCategoryJpaEntity;
import com.stdev.smartmealtable.storage.db.store.StoreCategoryJpaRepository;
import com.stdev.smartmealtable.support.jwt.JwtTokenProvider;
import com.stdev.smartmealtable.storage.cache.SearchCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

/**
 * Spring Rest Docs 기본 설정 추상 클래스
 * 
 * 모든 Rest Docs 테스트는 이 클래스를 상속받아야 합니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@ExtendWith(RestDocumentationExtension.class)
@Import({MockChatModelConfig.class, MockExpenditureServiceConfig.class})  // Mock 빈 Import
public abstract class AbstractRestDocsTest extends AbstractContainerTest {
    
    @Autowired
    protected MockMvc mockMvc;
    
    @Autowired
    protected ObjectMapper objectMapper;
    
    @Autowired
    protected JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    protected StoreCategoryJpaRepository storeCategoryJpaRepository;
    
    @MockBean
    protected SearchCacheService searchCacheService;  // Redis Mock 처리
    
    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext,
               RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .build();
        
        // Redis Mock 설정 - 모든 Redis 연산 무시
        doNothing().when(searchCacheService).incrementSearchCount(anyString(), anyString());
        doNothing().when(searchCacheService).cacheAutocompleteData(anyString(), any());
    }
    
    /**
     * JWT Access Token 생성 헬퍼
     * @param memberId 회원 ID
     * @return Bearer 토큰 문자열 (Authorization 헤더에 사용)
     */
    protected String createAccessToken(Long memberId) {
        return "Bearer " + jwtTokenProvider.createToken(memberId);
    }
    
    /**
     * Authorization 헤더 문서화 Snippet
     */
    protected org.springframework.restdocs.snippet.Snippet authorizationHeader() {
        return requestHeaders(
                headerWithName("Authorization").description("JWT 인증 토큰 (Bearer {token})")
        );
    }
    
    /**
     * 요청 전처리기: 문서화를 위해 요청을 예쁘게 출력
     */
    protected OperationRequestPreprocessor getDocumentRequest() {
        return preprocessRequest(
                modifyUris()
                        .scheme("https")
                        .host("api.smartmealtable.com")
                        .removePort(),
                prettyPrint()
        );
    }
    
    /**
     * 응답 전처리기: 문서화를 위해 응답을 예쁘게 출력
     */
    protected OperationResponsePreprocessor getDocumentResponse() {
        return preprocessResponse(prettyPrint());
    }
    
    /**
     * 공통 성공 응답 필드 (ApiResponse<T>)
     */
    protected FieldDescriptor[] commonSuccessResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("result").description("요청 처리 결과 (SUCCESS/ERROR)"),
                fieldWithPath("data").description("응답 데이터"),
                fieldWithPath("error").description("에러 정보 (성공 시 null)")
        };
    }
    
    /**
     * 공통 에러 응답 필드 (ApiResponse<Void>)
     */
    protected FieldDescriptor[] commonErrorResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("result").description("요청 처리 결과 (ERROR)"),
                fieldWithPath("data").description("응답 데이터 (에러 시 null)"),
                fieldWithPath("error").description("에러 정보"),
                fieldWithPath("error.code").description("에러 코드 (E400, E401, E404, E409, E422, E500)"),
                fieldWithPath("error.message").description("에러 메시지"),
                fieldWithPath("error.data").description("에러 상세 정보 (optional)").optional()
        };
    }
    
    /**
     * Store-Category N:N 매핑 데이터 생성 헬퍼
     * Store 생성 후 store_category 중간 테이블에 데이터를 저장합니다.
     * 
     * @param storeId 가게 ID
     * @param categoryIds 카테고리 ID 목록
     */
    protected void createStoreCategories(Long storeId, java.util.List<Long> categoryIds) {
        for (int i = 0; i < categoryIds.size(); i++) {
            StoreCategoryJpaEntity mapping = StoreCategoryJpaEntity.builder()
                    .storeId(storeId)
                    .categoryId(categoryIds.get(i))
                    .displayOrder(i)
                    .build();
            storeCategoryJpaRepository.save(mapping);
        }
    }
}

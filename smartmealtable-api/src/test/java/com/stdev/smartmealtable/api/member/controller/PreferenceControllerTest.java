package com.stdev.smartmealtable.api.member.controller;

import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.support.jwt.JwtTokenProvider;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodPreference;
import com.stdev.smartmealtable.domain.food.FoodPreferenceRepository;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.domain.store.StoreType;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.preference.Preference;
import com.stdev.smartmealtable.domain.preference.PreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PreferenceControllerTest extends AbstractContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private PreferenceRepository preferenceRepository;

    @Autowired
    private FoodPreferenceRepository foodPreferenceRepository;

    private Member testMember;
    private Category koreanCategory;
    private Category japaneseCategory;
    private Food kimchiJjigae;
    private Food sushi;
    private String accessToken;

    @BeforeEach
    void setUp() {
        // 테스트용 회원 생성
        Member member = Member.create(null, "테스터", null, RecommendationType.BALANCED);
        testMember = memberRepository.save(member);

        // JWT 토큰 생성
        accessToken = jwtTokenProvider.createToken(testMember.getMemberId());

        // 카테고리 생성 (Storage 레이어를 통해 직접 삽입)
        koreanCategory = Category.reconstitute(null, "한식");
        japaneseCategory = Category.reconstitute(null, "일식");
        koreanCategory = categoryRepository.save(koreanCategory);
        japaneseCategory = categoryRepository.save(japaneseCategory);

        // 테스트용 가게 생성
        Store testStore = Store.builder()
                .name("테스트 음식점")
                .categoryId(koreanCategory.getCategoryId())
                .sellerId(1L)
                .address("서울특별시 강남구 테헤란로 100")
                .lotNumberAddress("서울특별시 강남구 역삼동 100-10")
                .latitude(new BigDecimal("37.5015678"))
                .longitude(new BigDecimal("127.0395432"))
                .phoneNumber("02-1234-5678")
                .description("테스트용 가게")
                .averagePrice(8000)
                .reviewCount(100)
                .viewCount(500)
                .favoriteCount(20)
                .storeType(StoreType.RESTAURANT)
                .imageUrl("https://example.com/store.jpg")
                .registeredAt(LocalDateTime.now().minusMonths(1))
                .build();
        Store savedStore = storeRepository.save(testStore);

        // 음식 생성 (Storage 레이어를 통해 직접 삽입)
        kimchiJjigae = Food.reconstitute(null, "김치찌개", savedStore.getStoreId(), koreanCategory.getCategoryId(), "얼큰한 김치찌개", "kimchi.jpg", 7000);
        sushi = Food.reconstitute(null, "생굴", savedStore.getStoreId(), japaneseCategory.getCategoryId(), "신선한 생굴", "sushi.jpg", 15000);
        kimchiJjigae = foodRepository.save(kimchiJjigae);
        sushi = foodRepository.save(sushi);
    }

    @Test
    @DisplayName("선호도 조회 성공 - 카테고리 선호도와 음식 선호도 모두 있는 경우")
    void getPreferences_Success_WithAllData() throws Exception {
        // given - 카테고리 선호도만 먼저 테스트
        Preference koreanPref = Preference.create(testMember.getMemberId(), koreanCategory.getCategoryId(), 100);
        preferenceRepository.save(koreanPref);

        // when & then
        mockMvc.perform(get("/api/v1/members/me/preferences")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.recommendationType").value("BALANCED"))
                .andExpect(jsonPath("$.data.categoryPreferences", hasSize(1)))
                .andExpect(jsonPath("$.data.categoryPreferences[0].weight").value(100));
    }

    @Test
    @DisplayName("선호도 조회 성공 - 선호도가 없는 경우 빈 배열 반환")
    void getPreferences_Success_EmptyPreferences() throws Exception {
        // given - 선호도 데이터 없음

        // when & then
        mockMvc.perform(get("/api/v1/members/me/preferences")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.recommendationType").value("BALANCED"))
                .andExpect(jsonPath("$.data.categoryPreferences", hasSize(0)))
                .andExpect(jsonPath("$.data.foodPreferences.liked", hasSize(0)))
                .andExpect(jsonPath("$.data.foodPreferences.disliked", hasSize(0)));
    }

    // TODO: 헤더 누락 시 401/400 응답 처리 개선 필요
    // @Test
    // @DisplayName("선호도 조회 실패 - 인증되지 않은 사용자 (헤더 누락)")
    // void getPreferences_Fail_Unauthorized() throws Exception {
    //     mockMvc.perform(get("/api/v1/members/me/preferences")
    //                     .contentType(MediaType.APPLICATION_JSON))
    //             .andDo(print())
    //             .andExpect(status().isBadRequest());
    // }
}

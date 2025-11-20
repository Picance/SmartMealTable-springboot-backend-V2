package com.stdev.smartmealtable.api.recommendation.service;

import com.stcom.smartmealtable.recommendation.domain.model.RecommendationResult;
import com.stcom.smartmealtable.recommendation.domain.model.UserProfile;
import com.stcom.smartmealtable.recommendation.domain.repository.RecommendationDataRepository;
import com.stcom.smartmealtable.recommendation.domain.service.RecommendationDomainService;
import com.stdev.smartmealtable.api.home.service.BusinessHoursService;
import com.stdev.smartmealtable.api.recommendation.dto.RecommendationRequestDto;
import com.stdev.smartmealtable.api.recommendation.dto.RecommendationResponseDto;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.favorite.FavoriteRepository;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationApplicationService 테스트")
class RecommendationApplicationServiceTest {

    private static final Long MEMBER_ID = 1L;

    @Mock private RecommendationDomainService recommendationDomainService;
    @Mock private RecommendationDataRepository recommendationDataRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private FavoriteRepository favoriteRepository;
    @Mock private BusinessHoursService businessHoursService;
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks
    private RecommendationApplicationService recommendationApplicationService;

    private RecommendationRequestDto baseRequest;
    private UserProfile baseProfile;

    @BeforeEach
    void setUp() {
        baseRequest = RecommendationRequestDto.builder()
                .latitude(new BigDecimal("37.5000"))
                .longitude(new BigDecimal("127.0000"))
                .radius(1.0)
                .page(0)
                .size(10)
                .build();

        baseProfile = UserProfile.builder()
                .memberId(MEMBER_ID)
                .recommendationType(RecommendationType.BALANCED)
                .currentLatitude(new BigDecimal("37.5000"))
                .currentLongitude(new BigDecimal("127.0000"))
                .categoryPreferences(Collections.emptyMap())
                .recentExpenditures(Collections.emptyMap())
                .storeLastVisitDates(Collections.emptyMap())
                .build();
    }

    @Test
    @DisplayName("추천 목록에 isFavorite, isOpen 상태가 포함된다")
    void getRecommendations_setsFavoriteAndOpenFlags() {
        // given
        RecommendationResult favoriteOpenResult = createResult(101L, 11L, 95.0);
        RecommendationResult normalClosedResult = createResult(202L, 22L, 82.0);

        given(recommendationDataRepository.loadUserProfile(MEMBER_ID)).willReturn(baseProfile);
        given(recommendationDataRepository.findStoresInRadius(
                any(BigDecimal.class),
                any(BigDecimal.class),
                anyDouble(),
                anyList(),
                anyBoolean(),
                any()
        )).willReturn(List.of());
        given(recommendationDomainService.calculateRecommendations(anyList(), any(UserProfile.class)))
                .willReturn(List.of(favoriteOpenResult, normalClosedResult));
        given(categoryRepository.findByIdIn(anyList()))
                .willReturn(List.of(
                        Category.reconstitute(11L, "한식"),
                        Category.reconstitute(22L, "중식")
                ));
        given(favoriteRepository.findStoreIdsByMemberIdAndStoreIdIn(eq(MEMBER_ID), anyList()))
                .willReturn(List.of(101L));
        given(businessHoursService.getOperationStatus(101L))
                .willReturn(new BusinessHoursService.StoreOperationStatus("영업중", true));
        given(businessHoursService.getOperationStatus(202L))
                .willReturn(new BusinessHoursService.StoreOperationStatus("휴무", false));

        // when
        List<RecommendationResponseDto> response =
                recommendationApplicationService.getRecommendations(MEMBER_ID, baseRequest);

        // then
        assertThat(response).hasSize(2);

        RecommendationResponseDto first = response.get(0);
        assertThat(first.getStoreId()).isEqualTo(101L);
        assertThat(first.getIsFavorite()).isTrue();
        assertThat(first.getIsOpen()).isTrue();

        RecommendationResponseDto second = response.get(1);
        assertThat(second.getStoreId()).isEqualTo(202L);
        assertThat(second.getIsFavorite()).isFalse();
        assertThat(second.getIsOpen()).isFalse();
    }

    private RecommendationResult createResult(long storeId, long categoryId, double score) {
        return RecommendationResult.builder()
                .storeId(storeId)
                .storeName("Store " + storeId)
                .categoryId(categoryId)
                .address("서울시 테스트로 " + storeId)
                .finalScore(score)
                .distance(0.5)
                .averagePrice(10000)
                .reviewCount(200)
                .imageUrl("https://example.com/" + storeId)
                .latitude(new BigDecimal("37.5000"))
                .longitude(new BigDecimal("127.0000"))
                .scoreDetail(null)
                .build();
    }
}

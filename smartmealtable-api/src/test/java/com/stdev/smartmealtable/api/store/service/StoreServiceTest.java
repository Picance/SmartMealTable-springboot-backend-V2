package com.stdev.smartmealtable.api.store.service;

import com.stdev.smartmealtable.api.store.dto.StoreAutocompleteResponse;
import com.stdev.smartmealtable.api.store.dto.StoreDetailResponse;
import com.stdev.smartmealtable.api.store.dto.StoreListRequest;
import com.stdev.smartmealtable.api.store.dto.StoreListResponse;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.member.entity.AddressHistory;
import com.stdev.smartmealtable.domain.member.repository.AddressHistoryRepository;
import com.stdev.smartmealtable.domain.store.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * StoreService 단위 테스트 (Mockist 스타일)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StoreService 테스트")
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StoreOpeningHourRepository storeOpeningHourRepository;

    @Mock
    private StoreTemporaryClosureRepository storeTemporaryClosureRepository;

    @Mock
    private StoreViewHistoryRepository storeViewHistoryRepository;

    @Mock
    private AddressHistoryRepository addressHistoryRepository;

    @InjectMocks
    private StoreService storeService;

    private Long testMemberId;
    private Long testStoreId;
    private Store testStore;
    private AddressHistory testAddress;
    private StoreWithDistance testStoreWithDistance;

    @BeforeEach
    void setUp() {
        testMemberId = 1L;
        testStoreId = 100L;

        // 테스트용 주소 생성
        Address address = Address.of(
                "우리집",
                "서울특별시 강남구 역삼동 123-45",
                "서울특별시 강남구 테헤란로 123",
                "",
                37.5012345,
                127.0398765,
                "HOME"
        );
        testAddress = AddressHistory.create(testMemberId, address, true);

        // 테스트용 가게 생성
        testStore = Store.builder()
                .storeId(testStoreId)
                .name("맛있는 한식당")
                .categoryId(1L)
                .sellerId(1L)
                .address("서울특별시 강남구 테헤란로 100")
                .lotNumberAddress("서울특별시 강남구 역삼동 100-10")
                .latitude(new BigDecimal("37.5015678"))
                .longitude(new BigDecimal("127.0395432"))
                .phoneNumber("02-1234-5678")
                .description("신선한 재료로 만드는 한식 전문점")
                .averagePrice(8000)
                .reviewCount(150)
                .viewCount(500)
                .favoriteCount(30)
                .storeType(StoreType.RESTAURANT)
                .imageUrl("https://example.com/store1.jpg")
                .registeredAt(LocalDateTime.now().minusMonths(3))
                .deletedAt(null)
                .build();

        testStoreWithDistance = new StoreWithDistance(testStore, new BigDecimal("0.5"));
    }

    @Test
    @DisplayName("가게 목록 조회 성공 - 기본 주소 존재")
    void getStores_success() {
        // given
        StoreListRequest request = new StoreListRequest(
                null, null, null, null, null, null, 0, 20
        );

        given(addressHistoryRepository.findPrimaryByMemberId(testMemberId))
                .willReturn(Optional.of(testAddress));

        StoreRepository.StoreSearchResult searchResult = new StoreRepository.StoreSearchResult(
                List.of(testStoreWithDistance),
                1L
        );

        given(storeRepository.searchStores(
                isNull(),
                any(BigDecimal.class),
                any(BigDecimal.class),
                eq(3.0), // 기본값
                isNull(),
                isNull(),
                isNull(),
                eq("distance"), // 기본값
                eq(0),
                eq(20)
        )).willReturn(searchResult);

        // when
        StoreListResponse response = storeService.getStores(testMemberId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.stores()).hasSize(1);
        assertThat(response.totalCount()).isEqualTo(1);
        assertThat(response.currentPage()).isEqualTo(0);
        assertThat(response.pageSize()).isEqualTo(20);
        assertThat(response.totalPages()).isEqualTo(1);

        StoreListResponse.StoreItem storeItem = response.stores().get(0);
        assertThat(storeItem.storeId()).isEqualTo(testStoreId);
        assertThat(storeItem.name()).isEqualTo("맛있는 한식당");
        assertThat(storeItem.distance()).isEqualByComparingTo(new BigDecimal("0.5"));

        verify(addressHistoryRepository).findPrimaryByMemberId(testMemberId);
        verify(storeRepository).searchStores(
                isNull(),
                any(BigDecimal.class),
                any(BigDecimal.class),
                eq(3.0), // 기본값
                isNull(),
                isNull(),
                isNull(),
                eq("distance"), // 기본값
                eq(0),
                eq(20)
        );
    }

    @Test
    @DisplayName("가게 목록 조회 실패 - 기본 주소 없음")
    void getStores_fail_noPrimaryAddress() {
        // given
        StoreListRequest request = new StoreListRequest(
                null, null, null, null, null, null, 0, 20
        );

        given(addressHistoryRepository.findPrimaryByMemberId(testMemberId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> storeService.getStores(testMemberId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.ADDRESS_NOT_FOUND);

        verify(addressHistoryRepository).findPrimaryByMemberId(testMemberId);
    }

    @Test
    @DisplayName("가게 목록 조회 성공 - 필터 적용")
    void getStores_success_withFilters() {
        // given
        StoreListRequest request = new StoreListRequest(
                "한식",
                5.0,
                1L,
                true,
                StoreType.RESTAURANT,
                "distance",
                0,
                10
        );

        given(addressHistoryRepository.findPrimaryByMemberId(testMemberId))
                .willReturn(Optional.of(testAddress));

        StoreRepository.StoreSearchResult searchResult = new StoreRepository.StoreSearchResult(
                List.of(testStoreWithDistance),
                1L
        );

        given(storeRepository.searchStores(
                eq("한식"),
                any(BigDecimal.class),
                any(BigDecimal.class),
                eq(5.0),
                eq(1L),
                eq(true),
                eq(StoreType.RESTAURANT),
                eq("distance"),
                eq(0),
                eq(10)
        )).willReturn(searchResult);

        // when
        StoreListResponse response = storeService.getStores(testMemberId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.stores()).hasSize(1);
        assertThat(response.totalCount()).isEqualTo(1);
        assertThat(response.pageSize()).isEqualTo(10);

        verify(storeRepository).searchStores(
                eq("한식"),
                any(BigDecimal.class),
                any(BigDecimal.class),
                eq(5.0),
                eq(1L),
                eq(true),
                eq(StoreType.RESTAURANT),
                eq("distance"),
                eq(0),
                eq(10)
        );
    }

    @Test
    @DisplayName("가게 상세 조회 성공")
    void getStoreDetail_success() {
        // given
        List<StoreOpeningHour> openingHours = List.of(
                new StoreOpeningHour(
                        1L,
                        testStoreId,
                        DayOfWeek.MONDAY,
                        "09:00",
                        "21:00",
                        null,
                        null,
                        false
                )
        );

        List<StoreTemporaryClosure> temporaryClosures = List.of();

        StoreViewHistory viewHistory = new StoreViewHistory(
                null,
                testStoreId,
                testMemberId,
                LocalDateTime.now()
        );

        given(storeRepository.findByIdAndDeletedAtIsNull(testStoreId))
                .willReturn(Optional.of(testStore));
        given(storeViewHistoryRepository.createViewHistory(testStoreId, testMemberId))
                .willReturn(viewHistory);
        given(storeOpeningHourRepository.findByStoreId(testStoreId))
                .willReturn(openingHours);
        given(storeTemporaryClosureRepository.findByStoreId(testStoreId))
                .willReturn(temporaryClosures);

        // when
        StoreDetailResponse response = storeService.getStoreDetail(testMemberId, testStoreId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.storeId()).isEqualTo(testStoreId);
        assertThat(response.name()).isEqualTo("맛있는 한식당");
        assertThat(response.description()).isEqualTo("신선한 재료로 만드는 한식 전문점");
        assertThat(response.phoneNumber()).isEqualTo("02-1234-5678");
        assertThat(response.averagePrice()).isEqualTo(8000);
        assertThat(response.reviewCount()).isEqualTo(150);
        assertThat(response.viewCount()).isEqualTo(501); // 조회수 증가 확인
        assertThat(response.openingHours()).hasSize(1);
        assertThat(response.temporaryClosures()).isEmpty();

        verify(storeRepository).findByIdAndDeletedAtIsNull(testStoreId);
        verify(storeViewHistoryRepository).createViewHistory(testStoreId, testMemberId);
        verify(storeViewHistoryRepository).save(viewHistory);
        verify(storeRepository).save(testStore);
        verify(storeOpeningHourRepository).findByStoreId(testStoreId);
        verify(storeTemporaryClosureRepository).findByStoreId(testStoreId);
    }

    @Test
    @DisplayName("가게 상세 조회 실패 - 가게 없음")
    void getStoreDetail_fail_storeNotFound() {
        // given
        given(storeRepository.findByIdAndDeletedAtIsNull(testStoreId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> storeService.getStoreDetail(testMemberId, testStoreId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.STORE_NOT_FOUND);

        verify(storeRepository).findByIdAndDeletedAtIsNull(testStoreId);
    }

    @Test
    @DisplayName("가게 상세 조회 성공 - 임시 휴무 포함")
    void getStoreDetail_success_withTemporaryClosure() {
        // given
        List<StoreOpeningHour> openingHours = List.of(
                new StoreOpeningHour(
                        1L,
                        testStoreId,
                        DayOfWeek.MONDAY,
                        "09:00",
                        "21:00",
                        null,
                        null,
                        false
                )
        );

        LocalDate today = LocalDate.now();
        List<StoreTemporaryClosure> temporaryClosures = List.of(
                new StoreTemporaryClosure(
                        1L,
                        testStoreId,
                        today,
                        LocalTime.of(0, 0),
                        LocalTime.of(23, 59),
                        "재료 수급 문제로 임시 휴무"
                )
        );

        StoreViewHistory viewHistory = new StoreViewHistory(
                null,
                testStoreId,
                testMemberId,
                LocalDateTime.now()
        );

        given(storeRepository.findByIdAndDeletedAtIsNull(testStoreId))
                .willReturn(Optional.of(testStore));
        given(storeViewHistoryRepository.createViewHistory(testStoreId, testMemberId))
                .willReturn(viewHistory);
        given(storeOpeningHourRepository.findByStoreId(testStoreId))
                .willReturn(openingHours);
        given(storeTemporaryClosureRepository.findByStoreId(testStoreId))
                .willReturn(temporaryClosures);

        // when
        StoreDetailResponse response = storeService.getStoreDetail(testMemberId, testStoreId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.storeId()).isEqualTo(testStoreId);
        assertThat(response.temporaryClosures()).hasSize(1);
        assertThat(response.temporaryClosures().get(0).reason()).isEqualTo("재료 수급 문제로 임시 휴무");
        assertThat(response.temporaryClosures().get(0).closureDate()).isEqualTo(today);
        assertThat(response.temporaryClosures().get(0).startTime()).isEqualTo(LocalTime.of(0, 0));
        assertThat(response.temporaryClosures().get(0).endTime()).isEqualTo(LocalTime.of(23, 59));

        verify(storeTemporaryClosureRepository).findByStoreId(testStoreId);
    }

    @Test
    @DisplayName("자동완성 검색 성공")
    void autocomplete_success() {
        // given
        String keyword = "한식";
        Integer limit = 10;

        List<Store> stores = List.of(testStore);

        given(storeRepository.searchByKeywordForAutocomplete(keyword, limit))
                .willReturn(stores);

        // when
        List<StoreAutocompleteResponse> response = storeService.autocomplete(keyword, limit);

        // then
        assertThat(response).hasSize(1);
        assertThat(response.get(0).storeId()).isEqualTo(testStoreId);
        assertThat(response.get(0).name()).isEqualTo("맛있는 한식당");
        assertThat(response.get(0).address()).isEqualTo("서울특별시 강남구 테헤란로 100");

        verify(storeRepository).searchByKeywordForAutocomplete(keyword, limit);
    }

    @Test
    @DisplayName("자동완성 검색 - 빈 키워드")
    void autocomplete_emptyKeyword() {
        // given
        String keyword = "";
        Integer limit = 10;

        // when
        List<StoreAutocompleteResponse> response = storeService.autocomplete(keyword, limit);

        // then
        assertThat(response).isEmpty();
    }

    @Test
    @DisplayName("자동완성 검색 - null 키워드")
    void autocomplete_nullKeyword() {
        // given
        String keyword = null;
        Integer limit = 10;

        // when
        List<StoreAutocompleteResponse> response = storeService.autocomplete(keyword, limit);

        // then
        assertThat(response).isEmpty();
    }

    @Test
    @DisplayName("자동완성 검색 - limit 기본값 적용")
    void autocomplete_defaultLimit() {
        // given
        String keyword = "한식";
        Integer limit = null;

        List<Store> stores = List.of(testStore);

        given(storeRepository.searchByKeywordForAutocomplete(keyword, 10))
                .willReturn(stores);

        // when
        List<StoreAutocompleteResponse> response = storeService.autocomplete(keyword, limit);

        // then
        assertThat(response).hasSize(1);

        verify(storeRepository).searchByKeywordForAutocomplete(keyword, 10);
    }

    @Test
    @DisplayName("자동완성 검색 - 결과 없음")
    void autocomplete_noResults() {
        // given
        String keyword = "존재하지않는가게";
        Integer limit = 10;

        given(storeRepository.searchByKeywordForAutocomplete(keyword, limit))
                .willReturn(List.of());

        // when
        List<StoreAutocompleteResponse> response = storeService.autocomplete(keyword, limit);

        // then
        assertThat(response).isEmpty();

        verify(storeRepository).searchByKeywordForAutocomplete(keyword, limit);
    }
}

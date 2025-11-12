package com.stdev.smartmealtable.api.home.service;

import com.stdev.smartmealtable.domain.store.StoreOpeningHour;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * BusinessHoursService 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BusinessHoursService 테스트")
class BusinessHoursServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private BusinessHoursService businessHoursService;

    private StoreOpeningHour regularDay;
    private StoreOpeningHour holidayDay;
    private StoreOpeningHour dayWithBreakTime;

    @BeforeEach
    void setUp() {
        // 일반 영업일 (11:00 ~ 21:00)
        regularDay = new StoreOpeningHour(
                1L,
                1L,
                DayOfWeek.MONDAY,
                "11:00:00",
                "21:00:00",
                null,
                null,
                false
        );

        // 휴무일
        holidayDay = new StoreOpeningHour(
                2L,
                1L,
                DayOfWeek.TUESDAY,
                null,
                null,
                null,
                null,
                true
        );

        // 휴게시간이 있는 영업일 (10:00 ~ 18:00, 13:00~14:00 휴게)
        dayWithBreakTime = new StoreOpeningHour(
                3L,
                1L,
                DayOfWeek.WEDNESDAY,
                "10:00:00",
                "18:00:00",
                "13:00:00",
                "14:00:00",
                false
        );
    }

    @Test
    @DisplayName("일반 영업일의 영업시간 포맷팅")
    void testFormatRegularDay() {
        when(storeRepository.findOpeningHoursByStoreId(anyLong()))
                .thenReturn(List.of(regularDay));

        String result = businessHoursService.getBusinessHoursByDayOfWeek(1L, DayOfWeek.MONDAY);

        assertEquals("11:00:00 ~ 21:00:00", result);
    }

    @Test
    @DisplayName("휴무일의 영업시간 포맷팅")
    void testFormatHolidayDay() {
        when(storeRepository.findOpeningHoursByStoreId(anyLong()))
                .thenReturn(List.of(holidayDay));

        String result = businessHoursService.getBusinessHoursByDayOfWeek(1L, DayOfWeek.TUESDAY);

        assertEquals("휴무", result);
    }

    @Test
    @DisplayName("휴게시간이 있는 영업일의 포맷팅")
    void testFormatDayWithBreakTime() {
        when(storeRepository.findOpeningHoursByStoreId(anyLong()))
                .thenReturn(List.of(dayWithBreakTime));

        String result = businessHoursService.getBusinessHoursByDayOfWeek(1L, DayOfWeek.WEDNESDAY);

        assertEquals("10:00:00 ~ 18:00:00 (13:00:00~14:00:00 휴게)", result);
    }

    @Test
    @DisplayName("정보 없는 요일의 기본값 반환")
    void testDefaultForMissingDay() {
        when(storeRepository.findOpeningHoursByStoreId(anyLong()))
                .thenReturn(List.of(regularDay)); // MONDAY만 있음

        String result = businessHoursService.getBusinessHoursByDayOfWeek(1L, DayOfWeek.FRIDAY);

        assertEquals("영업시간 미정", result);
    }

    @Test
    @DisplayName("주간 전체 영업시간 조회")
    void testGetWeeklyBusinessHours() {
        when(storeRepository.findOpeningHoursByStoreId(anyLong()))
                .thenReturn(List.of(
                        regularDay,           // MONDAY
                        holidayDay,           // TUESDAY
                        dayWithBreakTime      // WEDNESDAY
                ));

        Map<DayOfWeek, String> result = businessHoursService.getWeeklyBusinessHours(1L);

        assertAll(
                () -> assertEquals("11:00:00 ~ 21:00:00", result.get(DayOfWeek.MONDAY)),
                () -> assertEquals("휴무", result.get(DayOfWeek.TUESDAY)),
                () -> assertEquals("10:00:00 ~ 18:00:00 (13:00:00~14:00:00 휴게)", result.get(DayOfWeek.WEDNESDAY)),
                () -> assertEquals("정보 없음", result.get(DayOfWeek.THURSDAY))
        );
    }

    @Test
    @DisplayName("영업시간 정보 존재 여부 확인 - 정보 있음")
    void testHasBusinessHoursInfo_True() {
        when(storeRepository.findOpeningHoursByStoreId(anyLong()))
                .thenReturn(List.of(regularDay));

        boolean result = businessHoursService.hasBusinessHoursInfo(1L);

        assertTrue(result);
    }

    @Test
    @DisplayName("영업시간 정보 존재 여부 확인 - 정보 없음")
    void testHasBusinessHoursInfo_False() {
        when(storeRepository.findOpeningHoursByStoreId(anyLong()))
                .thenReturn(List.of());

        boolean result = businessHoursService.hasBusinessHoursInfo(1L);

        assertFalse(result);
    }

    @Test
    @DisplayName("일괄 조회 - 여러 가게의 오늘 영업시간")
    void testGetTodayBusinessHoursBatch() {
        when(storeRepository.findOpeningHoursByStoreId(1L))
                .thenReturn(List.of(regularDay));
        when(storeRepository.findOpeningHoursByStoreId(2L))
                .thenReturn(List.of(holidayDay));

        Map<Long, String> result = businessHoursService.getTodayBusinessHoursBatch(List.of(1L, 2L));

        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertNotNull(result.get(1L)),
                () -> assertNotNull(result.get(2L))
        );
    }

    @Test
    @DisplayName("영업 상태 조회 - 영업 시간 정보 없음")
    void testGetOperationStatus_NoInfo() {
        when(storeRepository.findOpeningHoursByStoreId(anyLong()))
                .thenReturn(List.of());

        BusinessHoursService.StoreOperationStatus result = businessHoursService.getOperationStatus(1L);

        assertEquals("정보없음", result.status());
        assertFalse(result.isOpen());
    }

    @Test
    @DisplayName("영업 상태 조회 - 휴무일")
    void testGetOperationStatus_Holiday() {
        // 모든 요일이 휴무일 수 있도록 설정
        when(storeRepository.findOpeningHoursByStoreId(anyLong()))
                .thenReturn(List.of(
                        new StoreOpeningHour(1L, 1L, DayOfWeek.MONDAY, null, null, null, null, true),
                        new StoreOpeningHour(2L, 1L, DayOfWeek.TUESDAY, null, null, null, null, true),
                        new StoreOpeningHour(3L, 1L, DayOfWeek.WEDNESDAY, null, null, null, null, true),
                        new StoreOpeningHour(4L, 1L, DayOfWeek.THURSDAY, null, null, null, null, true),
                        new StoreOpeningHour(5L, 1L, DayOfWeek.FRIDAY, null, null, null, null, true),
                        new StoreOpeningHour(6L, 1L, DayOfWeek.SATURDAY, null, null, null, null, true),
                        new StoreOpeningHour(7L, 1L, DayOfWeek.SUNDAY, null, null, null, null, true)
                ));

        BusinessHoursService.StoreOperationStatus result = businessHoursService.getOperationStatus(1L);

        assertEquals("휴무", result.status());
        assertFalse(result.isOpen());
    }

    @Test
    @DisplayName("예외 처리 - Repository 조회 실패")
    void testExceptionHandling() {
        when(storeRepository.findOpeningHoursByStoreId(anyLong()))
                .thenThrow(new RuntimeException("Database error"));

        String result = businessHoursService.getBusinessHoursByDayOfWeek(1L, DayOfWeek.MONDAY);

        assertEquals("영업시간 미정", result);
    }

    @Test
    @DisplayName("영업시간 문자열 파싱 - 유효한 형식")
    void testTimeStringParsing_Valid() {
        when(storeRepository.findOpeningHoursByStoreId(anyLong()))
                .thenReturn(List.of(
                        new StoreOpeningHour(
                                1L, 1L, DayOfWeek.MONDAY,
                                "09:30:00", "18:45:00",
                                null, null, false
                        )
                ));

        String result = businessHoursService.getBusinessHoursByDayOfWeek(1L, DayOfWeek.MONDAY);

        assertEquals("09:30:00 ~ 18:45:00", result);
    }

    @Test
    @DisplayName("특수 시간대 형식 - 자정을 넘어가는 경우")
    void testLateNightHours() {
        StoreOpeningHour lateNightDay = new StoreOpeningHour(
                1L, 1L, DayOfWeek.FRIDAY,
                "21:00:00", "03:00:00",  // 밤 9시부터 새벽 3시까지
                null, null, false
        );

        when(storeRepository.findOpeningHoursByStoreId(anyLong()))
                .thenReturn(List.of(lateNightDay));

        String result = businessHoursService.getBusinessHoursByDayOfWeek(1L, DayOfWeek.FRIDAY);

        assertEquals("21:00:00 ~ 03:00:00", result);
    }

    @Test
    @DisplayName("영업시간 정보만 있고 휴게시간 없음")
    void testNoBreakTimeInfo() {
        StoreOpeningHour dayNoBreak = new StoreOpeningHour(
                1L, 1L, DayOfWeek.SATURDAY,
                "08:00:00", "22:00:00",
                null, null,  // 휴게시간 없음
                false
        );

        when(storeRepository.findOpeningHoursByStoreId(anyLong()))
                .thenReturn(List.of(dayNoBreak));

        String result = businessHoursService.getBusinessHoursByDayOfWeek(1L, DayOfWeek.SATURDAY);

        assertEquals("08:00:00 ~ 22:00:00", result);
    }
}

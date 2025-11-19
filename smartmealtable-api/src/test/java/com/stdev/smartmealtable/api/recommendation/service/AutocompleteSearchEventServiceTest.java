package com.stdev.smartmealtable.api.recommendation.service;

import com.stdev.smartmealtable.domain.search.SearchKeywordEvent;
import com.stdev.smartmealtable.domain.search.SearchKeywordEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AutocompleteSearchEventServiceTest {

    @Mock
    private SearchKeywordEventRepository searchKeywordEventRepository;

    private AutocompleteSearchEventService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new AutocompleteSearchEventService(searchKeywordEventRepository);
    }

    @Test
    void logSearchEvent_normalizesKeywordAndPersists() {
        when(searchKeywordEventRepository.save(any(SearchKeywordEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AutocompleteSearchEventService.AutocompleteSearchEventCommand command =
                AutocompleteSearchEventService.AutocompleteSearchEventCommand.builder()
                        .rawKeyword("  김치+찌개  ")
                        .memberId(42L)
                        .latitude(new BigDecimal("37.1234567"))
                        .longitude(new BigDecimal("127.1234567"))
                        .build();

        service.logSearchEvent(command);

        ArgumentCaptor<SearchKeywordEvent> captor = ArgumentCaptor.forClass(SearchKeywordEvent.class);
        verify(searchKeywordEventRepository).save(captor.capture());

        SearchKeywordEvent saved = captor.getValue();
        assertThat(saved.getMemberId()).isEqualTo(42L);
        assertThat(saved.getRawKeyword()).isEqualTo("  김치+찌개  ");
        assertThat(saved.getNormalizedKeyword()).isEqualTo("김치찌개");
        assertThat(saved.getLatitude()).isEqualTo(new BigDecimal("37.1234567"));
        assertThat(saved.getLongitude()).isEqualTo(new BigDecimal("127.1234567"));
    }
}

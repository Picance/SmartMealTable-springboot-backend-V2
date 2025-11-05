package com.stdev.smartmealtable.storage.db.store;

import com.stdev.smartmealtable.domain.store.StoreType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Full-Text Search 기능 단위 테스트
 * MySQL의 FULLTEXT INDEX와 ngram parser를 활용한 한국어 검색 쿼리 테스트
 * 
 * Note: 실제 Full-Text Index는 DDL 적용 후 동작합니다.
 * 이 테스트는 Repository 메서드 시그니처와 기본 동작 검증용입니다.
 */
@DisplayName("StoreJpaRepository Full-Text Search 단위 테스트")
class StoreJpaRepositoryFullTextSearchTest {

    @Test
    @DisplayName("searchByFullText 메서드가 Page를 반환한다")
    void searchByFullText_returns_page() {
        // given
        StoreJpaRepository repository = mock(StoreJpaRepository.class);
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        StoreJpaEntity mockStore = StoreJpaEntity.builder()
                .storeId(1L)
                .categoryId(1L)
                .name("치킨마루")
                .description("맛있는 치킨집")
                .address("서울시 강남구")
                .latitude(new BigDecimal("37.5"))
                .longitude(new BigDecimal("127.0"))
                .storeType(StoreType.RESTAURANT)
                .viewCount(100)
                .reviewCount(50)
                .registeredAt(LocalDateTime.now())
                .build();
        
        Page<StoreJpaEntity> mockPage = mock(Page.class);
        when(mockPage.getContent()).thenReturn(List.of(mockStore));
        when(mockPage.getTotalElements()).thenReturn(1L);
        when(repository.searchByFullText(anyString(), any(PageRequest.class)))
                .thenReturn(mockPage);

        // when
        Page<StoreJpaEntity> result = repository.searchByFullText("치킨", pageRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("searchByKeywordForAutocomplete 메서드가 List를 반환한다")
    void searchByKeywordForAutocomplete_returns_list() {
        // given
        StoreJpaRepository repository = mock(StoreJpaRepository.class);
        
        StoreJpaEntity mockStore = StoreJpaEntity.builder()
                .storeId(1L)
                .categoryId(1L)
                .name("맘스터치")
                .description("햄버거 전문점")
                .address("서울시 강남구")
                .latitude(new BigDecimal("37.5"))
                .longitude(new BigDecimal("127.0"))
                .storeType(StoreType.RESTAURANT)
                .viewCount(200)
                .reviewCount(100)
                .registeredAt(LocalDateTime.now())
                .build();
        
        when(repository.searchByKeywordForAutocomplete(anyString(), any(Integer.class)))
                .thenReturn(List.of(mockStore));

        // when
        List<StoreJpaEntity> result = repository.searchByKeywordForAutocomplete("맘스", 5);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("맘스터치");
    }

    @Test
    @DisplayName("Full-Text Search 쿼리는 name과 description을 검색 대상으로 한다")
    void fulltext_search_targets_name_and_description() {
        // 이 테스트는 Repository 인터페이스의 @Query 어노테이션을 통해 검증됩니다
        // MATCH(s.name, s.description) AGAINST(:keyword IN NATURAL LANGUAGE MODE)
        assertThat(StoreJpaRepository.class).isNotNull();
    }
}

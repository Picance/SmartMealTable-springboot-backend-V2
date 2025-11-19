package com.stdev.smartmealtable.storage.db.searchkeyword;

import com.stdev.smartmealtable.domain.search.SearchKeywordAggregate;
import com.stdev.smartmealtable.domain.search.SearchKeywordEvent;
import com.stdev.smartmealtable.domain.search.SearchKeywordEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 검색 키워드 이벤트 저장소 구현
 */
@Repository
@RequiredArgsConstructor
public class SearchKeywordEventRepositoryImpl implements SearchKeywordEventRepository {

    private final SearchKeywordEventJpaRepository jpaRepository;

    @Override
    public SearchKeywordEvent save(SearchKeywordEvent event) {
        SearchKeywordEventJpaEntity entity = SearchKeywordEventJpaEntity.from(event);
        SearchKeywordEventJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public List<SearchKeywordAggregate> aggregateBetween(LocalDateTime from, LocalDateTime to, int prefixLength) {
        return jpaRepository.aggregateKeywordCounts(from, to, prefixLength)
                .stream()
                .map(projection -> new SearchKeywordAggregate(
                        projection.getPrefix(),
                        projection.getKeyword(),
                        projection.getSearch_cnt(),
                        projection.getClick_cnt()
                ))
                .collect(Collectors.toList());
    }
}

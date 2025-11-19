package com.stdev.smartmealtable.storage.db.searchkeyword;

import com.stdev.smartmealtable.domain.search.SearchKeywordEvent;
import com.stdev.smartmealtable.domain.search.SearchKeywordEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
}

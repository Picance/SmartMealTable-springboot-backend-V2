package com.stdev.smartmealtable.domain.search;

/**
 * 검색 키워드 이벤트 저장소
 */
public interface SearchKeywordEventRepository {

    /**
     * 검색 이벤트 저장
     *
     * @param event 저장할 이벤트
     * @return 저장된 이벤트
     */
    SearchKeywordEvent save(SearchKeywordEvent event);
}

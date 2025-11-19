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

    /**
     * 시간 범위 내 검색 이벤트를 prefix 단위로 집계
     *
     * @param from 시작 시각(포함)
     * @param to 종료 시각(미포함)
     * @param prefixLength 사용할 prefix 길이
     * @return 키워드 집계 결과 목록
     */
    java.util.List<SearchKeywordAggregate> aggregateBetween(
            java.time.LocalDateTime from,
            java.time.LocalDateTime to,
            int prefixLength
    );
}

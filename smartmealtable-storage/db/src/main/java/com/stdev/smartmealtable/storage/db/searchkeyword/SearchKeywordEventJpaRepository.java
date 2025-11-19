package com.stdev.smartmealtable.storage.db.searchkeyword;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SearchKeywordEventJpaRepository extends JpaRepository<SearchKeywordEventJpaEntity, Long> {

    @Query(value = """
            SELECT 
                SUBSTRING(normalized_keyword, 1, :prefixLength) AS prefix,
                normalized_keyword AS keyword,
                COUNT(*) AS search_cnt,
                SUM(CASE WHEN clicked_food_id IS NOT NULL THEN 1 ELSE 0 END) AS click_cnt
            FROM search_keyword_event
            WHERE created_at >= :from AND created_at < :to
            GROUP BY prefix, normalized_keyword
            """, nativeQuery = true)
    List<KeywordAggregationProjection> aggregateKeywordCounts(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("prefixLength") int prefixLength
    );

    interface KeywordAggregationProjection {
        String getPrefix();
        String getKeyword();
        long getSearch_cnt();
        long getClick_cnt();
    }
}

package com.stdev.smartmealtable.storage.db.searchkeyword;

import com.stdev.smartmealtable.domain.search.SearchKeywordEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 검색 키워드 이벤트 JPA 엔티티
 */
@Entity
@Table(name = "search_keyword_event")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchKeywordEventJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "search_keyword_event_id")
    private Long searchKeywordEventId;

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "raw_keyword", nullable = false, length = 100)
    private String rawKeyword;

    @Column(name = "normalized_keyword", nullable = false, length = 60)
    private String normalizedKeyword;

    @Column(name = "clicked_food_id")
    private Long clickedFoodId;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public SearchKeywordEvent toDomain() {
        return SearchKeywordEvent.builder()
                .searchKeywordEventId(searchKeywordEventId)
                .memberId(memberId)
                .rawKeyword(rawKeyword)
                .normalizedKeyword(normalizedKeyword)
                .clickedFoodId(clickedFoodId)
                .latitude(latitude)
                .longitude(longitude)
                .createdAt(createdAt)
                .build();
    }

    public static SearchKeywordEventJpaEntity from(SearchKeywordEvent event) {
        return SearchKeywordEventJpaEntity.builder()
                .searchKeywordEventId(event.getSearchKeywordEventId())
                .memberId(event.getMemberId())
                .rawKeyword(event.getRawKeyword())
                .normalizedKeyword(event.getNormalizedKeyword())
                .clickedFoodId(event.getClickedFoodId())
                .latitude(event.getLatitude())
                .longitude(event.getLongitude())
                .build();
    }
}

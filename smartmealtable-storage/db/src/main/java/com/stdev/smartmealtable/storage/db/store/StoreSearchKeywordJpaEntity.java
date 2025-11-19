package com.stdev.smartmealtable.storage.db.store;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "store_search_keyword",
        indexes = {
                @Index(name = "idx_store_keyword_prefix", columnList = "keyword_prefix, keyword_type"),
                @Index(name = "idx_store_keyword_store", columnList = "store_id, keyword_type")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StoreSearchKeywordJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_search_keyword_id")
    private Long storeSearchKeywordId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "keyword", nullable = false, length = 100)
    private String keyword;

    @Column(name = "keyword_prefix", nullable = false, length = 30)
    private String keywordPrefix;

    @Enumerated(EnumType.STRING)
    @Column(name = "keyword_type", nullable = false, length = 20)
    private StoreSearchKeywordType keywordType;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public static StoreSearchKeywordJpaEntity of(
            Long storeId,
            String keyword,
            String keywordPrefix,
            StoreSearchKeywordType keywordType
    ) {
        return StoreSearchKeywordJpaEntity.builder()
                .storeId(storeId)
                .keyword(keyword)
                .keywordPrefix(keywordPrefix)
                .keywordType(keywordType)
                .build();
    }
}

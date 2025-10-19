package com.stdev.smartmealtable.storage.db.favorite;

import com.stdev.smartmealtable.domain.favorite.Favorite;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class FavoriteEntityMappingTest {

    @Test
    @DisplayName("FavoriteEntity toDomain and fromDomainWithId roundtrip")
    void roundtrip_entity_to_domain_and_back() {
        // given
        Favorite domain = Favorite.builder()
                .favoriteId(31L)
                .memberId(7L)
                .storeId(8L)
                .priority(2L)
                .favoritedAt(LocalDateTime.now())
                .build();

        // when
        FavoriteEntity entity = FavoriteEntity.fromDomainWithId(domain);
        Favorite toDomain = entity.toDomain();

        // then
        assertThat(toDomain.getFavoriteId()).isEqualTo(domain.getFavoriteId());
        assertThat(toDomain.getMemberId()).isEqualTo(domain.getMemberId());
        assertThat(toDomain.getStoreId()).isEqualTo(domain.getStoreId());
        assertThat(toDomain.getPriority()).isEqualTo(domain.getPriority());
    }
}

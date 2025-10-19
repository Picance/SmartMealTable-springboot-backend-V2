package com.stdev.smartmealtable.storage.db.favorite;

import com.stdev.smartmealtable.domain.favorite.Favorite;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class FavoriteRepositoryImplTest {

    @Mock
    private FavoriteJpaRepository favoriteJpaRepository;

    @InjectMocks
    private FavoriteRepositoryImpl favoriteRepository;

    @Test
    @DisplayName("save should convert domain to entity and return domain after save")
    void save_converts_and_returns_domain() {
        // given
        Favorite favorite = Favorite.builder()
                .memberId(1L)
                .storeId(2L)
                .priority(5L)
                .favoritedAt(LocalDateTime.now())
                .build();

        FavoriteEntity savedEntity = FavoriteEntity.fromDomainWithId(Favorite.builder()
                .favoriteId(10L)
                .memberId(1L)
                .storeId(2L)
                .priority(5L)
                .favoritedAt(favorite.getFavoritedAt())
                .build());

        given(favoriteJpaRepository.save(org.mockito.ArgumentMatchers.any(FavoriteEntity.class)))
                .willReturn(savedEntity);

        // when
        Favorite result = favoriteRepository.save(favorite);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFavoriteId()).isEqualTo(10L);
        assertThat(result.getMemberId()).isEqualTo(1L);
        then(favoriteJpaRepository).should(times(1)).save(org.mockito.ArgumentMatchers.any(FavoriteEntity.class));
    }

    @Test
    @DisplayName("findById should map entity to domain when present")
    void findById_maps_entity_to_domain() {
        // given
        FavoriteEntity entity = new FavoriteEntity();
        // set fields via reflection/constructor helpers
        Favorite fav = Favorite.builder().favoriteId(11L).memberId(3L).storeId(4L).priority(1L).favoritedAt(LocalDateTime.now()).build();
        FavoriteEntity saved = FavoriteEntity.fromDomainWithId(fav);

        given(favoriteJpaRepository.findById(11L)).willReturn(Optional.of(saved));

        // when
        Optional<Favorite> opt = favoriteRepository.findById(11L);

        // then
        assertThat(opt).isPresent();
        assertThat(opt.get().getFavoriteId()).isEqualTo(11L);
        then(favoriteJpaRepository).should(times(1)).findById(11L);
    }

    @Test
    @DisplayName("findByMemberIdOrderByPriorityAsc should map list of entities to domain list")
    void findByMemberIdOrderByPriorityAsc_maps_list() {
        // given
        Favorite f1 = Favorite.builder().favoriteId(21L).memberId(5L).storeId(6L).priority(1L).favoritedAt(LocalDateTime.now()).build();
        Favorite f2 = Favorite.builder().favoriteId(22L).memberId(5L).storeId(7L).priority(2L).favoritedAt(LocalDateTime.now()).build();

        FavoriteEntity e1 = FavoriteEntity.fromDomainWithId(f1);
        FavoriteEntity e2 = FavoriteEntity.fromDomainWithId(f2);

        given(favoriteJpaRepository.findByMemberIdOrderByPriorityAsc(5L)).willReturn(List.of(e1, e2));

        // when
        List<Favorite> results = favoriteRepository.findByMemberIdOrderByPriorityAsc(5L);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getFavoriteId()).isEqualTo(21L);
        then(favoriteJpaRepository).should(times(1)).findByMemberIdOrderByPriorityAsc(5L);
    }
}

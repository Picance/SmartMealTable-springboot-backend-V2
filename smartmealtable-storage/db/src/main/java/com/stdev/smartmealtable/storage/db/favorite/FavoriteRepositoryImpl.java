package com.stdev.smartmealtable.storage.db.favorite;

import com.stdev.smartmealtable.domain.favorite.Favorite;
import com.stdev.smartmealtable.domain.favorite.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * FavoriteRepository 구현체
 */
@Repository
@RequiredArgsConstructor
public class FavoriteRepositoryImpl implements FavoriteRepository {
    
    private final FavoriteJpaRepository favoriteJpaRepository;
    
    @Override
    public Favorite save(Favorite favorite) {
        FavoriteEntity entity = FavoriteMapper.toEntity(favorite);
        FavoriteEntity savedEntity = favoriteJpaRepository.save(entity);
        return FavoriteMapper.toDomain(savedEntity);
    }
    
    @Override
    public Optional<Favorite> findById(Long favoriteId) {
        return favoriteJpaRepository.findById(favoriteId)
                .map(FavoriteMapper::toDomain);
    }
    
    @Override
    public List<Favorite> findByMemberIdOrderByPriorityAsc(Long memberId) {
        return favoriteJpaRepository.findByMemberIdOrderByPriorityAsc(memberId)
                .stream()
                .map(FavoriteMapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<Favorite> findByMemberIdAndStoreId(Long memberId, Long storeId) {
        return favoriteJpaRepository.findByMemberIdAndStoreId(memberId, storeId)
                .map(FavoriteMapper::toDomain);
    }
    
    @Override
    public long countByMemberId(Long memberId) {
        return favoriteJpaRepository.countByMemberId(memberId);
    }
    
    @Override
    public Long findMaxPriorityByMemberId(Long memberId) {
        Long maxPriority = favoriteJpaRepository.findMaxPriorityByMemberId(memberId);
        return maxPriority != null ? maxPriority : 0L;
    }
    
    @Override
    public void delete(Favorite favorite) {
        FavoriteEntity entity = FavoriteMapper.toEntity(favorite);
        favoriteJpaRepository.delete(entity);
    }
    
    @Override
    public List<Favorite> saveAll(List<Favorite> favorites) {
        List<FavoriteEntity> entities = favorites.stream()
                .map(FavoriteMapper::toEntity)
                .collect(Collectors.toList());
        
        List<FavoriteEntity> savedEntities = favoriteJpaRepository.saveAll(entities);
        
        return savedEntities.stream()
                .map(FavoriteMapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsByMemberIdAndStoreId(Long memberId, Long storeId) {
        return favoriteJpaRepository.existsByMemberIdAndStoreId(memberId, storeId);
    }
}

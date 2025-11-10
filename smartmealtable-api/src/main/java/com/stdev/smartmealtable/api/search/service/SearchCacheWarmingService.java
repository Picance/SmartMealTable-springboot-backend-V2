package com.stdev.smartmealtable.api.search.service;

import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.storage.cache.ChosungIndexBuilder;
import com.stdev.smartmealtable.storage.cache.ChosungIndexBuilder.SearchableEntity;
import com.stdev.smartmealtable.storage.cache.SearchCacheService;
import com.stdev.smartmealtable.storage.cache.SearchCacheService.AutocompleteEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 검색 캐시 워밍 서비스 (단순화 버전)
 * <p>
 * 서버 시작 시 또는 스케줄러를 통해 Store, Food, Group 데이터를 Redis에 사전 로드합니다.
 * Repository에서 페이징으로 데이터를 조회한 후 SearchCacheService에 직접 저장합니다.
 * </p>
 * 
 * <p>
 * 설계 원칙:
 * - Repository 페이징 조회로 메모리 효율성 확보
 * - SearchCacheService와 ChosungIndexBuilder 직접 호출
 * - 단순한 Entity → DTO 변환 로직
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchCacheWarmingService {

    private final StoreRepository storeRepository;
    private final FoodRepository foodRepository;
    private final GroupRepository groupRepository;
    private final SearchCacheService searchCacheService;
    private final ChosungIndexBuilder chosungIndexBuilder;

    /**
     * 전체 도메인의 캐시 워밍을 수행합니다.
     */
    @Transactional(readOnly = true)
    public void warmAllCaches() {
        log.info("===== 전체 캐시 워밍 시작 =====");
        
        long startTime = System.currentTimeMillis();
        
        try {
            warmStoreCache(100);
            warmFoodCache(500);
            warmGroupCache(50);
            
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("===== 전체 캐시 워밍 완료 (소요 시간: {}ms) =====", elapsed);
        } catch (Exception e) {
            log.error("===== 전체 캐시 워밍 실패 =====", e);
            throw new RuntimeException("캐시 워밍 실패", e);
        }
    }

    /**
     * Store 도메인의 캐시를 워밍합니다.
     * 
     * @param batchSize 배치 처리 크기
     */
    @Transactional(readOnly = true)
    public void warmStoreCache(int batchSize) {
        log.info("Store 캐시 워밍 시작 (배치 크기: {})", batchSize);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 전체 개수 조회
            long totalCount = storeRepository.count();
            if (totalCount == 0) {
                log.warn("Store 데이터가 없습니다. 캐시 워밍을 스킵합니다.");
                return;
            }
            
            // 페이징 처리
            int totalPages = (int) Math.ceil((double) totalCount / batchSize);
            List<AutocompleteEntity> allAutocompleteEntities = new ArrayList<>();
            List<SearchableEntity> allSearchableEntities = new ArrayList<>();
            
            for (int page = 0; page < totalPages; page++) {
                List<Store> stores = storeRepository.findAll(page, batchSize);
                
                for (Store store : stores) {
                    // AutocompleteEntity 생성
                    allAutocompleteEntities.add(new AutocompleteEntity(
                            store.getStoreId(),
                            store.getName(),
                            1.0,  // 기본 popularity
                            new HashMap<>()  // 빈 attributes
                    ));
                    
                    // SearchableEntity 생성 (초성 인덱스용)
                    allSearchableEntities.add(new SearchableEntity(
                            store.getStoreId(),
                            store.getName()
                    ));
                }
            }
            
            // Redis에 일괄 저장
            searchCacheService.cacheAutocompleteData("store", allAutocompleteEntities);
            chosungIndexBuilder.buildChosungIndex("store", allSearchableEntities);
            
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("Store 캐시 워밍 완료 (개수: {}, 소요 시간: {}ms)", totalCount, elapsed);
            
        } catch (Exception e) {
            log.error("Store 캐시 워밍 실패", e);
            throw new RuntimeException("Store 캐시 워밍 실패", e);
        }
    }

    /**
     * Food 도메인의 캐시를 워밍합니다.
     * 
     * @param batchSize 배치 처리 크기
     */
    @Transactional(readOnly = true)
    public void warmFoodCache(int batchSize) {
        log.info("Food 캐시 워밍 시작 (배치 크기: {})", batchSize);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 전체 개수 조회
            long totalCount = foodRepository.count();
            if (totalCount == 0) {
                log.warn("Food 데이터가 없습니다. 캐시 워밍을 스킵합니다.");
                return;
            }
            
            // 페이징 처리
            int totalPages = (int) Math.ceil((double) totalCount / batchSize);
            List<AutocompleteEntity> allAutocompleteEntities = new ArrayList<>();
            List<SearchableEntity> allSearchableEntities = new ArrayList<>();
            
            for (int page = 0; page < totalPages; page++) {
                List<Food> foods = foodRepository.findAll(page, batchSize);
                
                for (Food food : foods) {
                    // AutocompleteEntity 생성
                    allAutocompleteEntities.add(new AutocompleteEntity(
                            food.getFoodId(),
                            food.getFoodName(),
                            1.0,  // 기본 popularity
                            new HashMap<>()  // 빈 attributes
                    ));
                    
                    // SearchableEntity 생성 (초성 인덱스용)
                    allSearchableEntities.add(new SearchableEntity(
                            food.getFoodId(),
                            food.getFoodName()
                    ));
                }
            }
            
            // Redis에 일괄 저장
            searchCacheService.cacheAutocompleteData("food", allAutocompleteEntities);
            chosungIndexBuilder.buildChosungIndex("food", allSearchableEntities);
            
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("Food 캐시 워밍 완료 (개수: {}, 소요 시간: {}ms)", totalCount, elapsed);
            
        } catch (Exception e) {
            log.error("Food 캐시 워밍 실패", e);
            throw new RuntimeException("Food 캐시 워밍 실패", e);
        }
    }

    /**
     * Group 도메인의 캐시를 워밍합니다.
     * 
     * @param batchSize 배치 처리 크기
     */
    @Transactional(readOnly = true)
    public void warmGroupCache(int batchSize) {
        log.info("Group 캐시 워밍 시작 (배치 크기: {})", batchSize);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 전체 개수 조회
            long totalCount = groupRepository.count();
            if (totalCount == 0) {
                log.warn("Group 데이터가 없습니다. 캐시 워밍을 스킵합니다.");
                return;
            }
            
            // 페이징 처리
            int totalPages = (int) Math.ceil((double) totalCount / batchSize);
            List<AutocompleteEntity> allAutocompleteEntities = new ArrayList<>();
            List<SearchableEntity> allSearchableEntities = new ArrayList<>();
            
            for (int page = 0; page < totalPages; page++) {
                List<Group> groups = groupRepository.findAll(page, batchSize);
                
                for (Group group : groups) {
                    // AutocompleteEntity 생성
                    allAutocompleteEntities.add(new AutocompleteEntity(
                            group.getGroupId(),
                            group.getName(),
                            1.0,  // 기본 popularity
                            new HashMap<>()  // 빈 attributes
                    ));
                    
                    // SearchableEntity 생성 (초성 인덱스용)
                    allSearchableEntities.add(new SearchableEntity(
                            group.getGroupId(),
                            group.getName()
                    ));
                }
            }
            
            // Redis에 일괄 저장
            searchCacheService.cacheAutocompleteData("group", allAutocompleteEntities);
            chosungIndexBuilder.buildChosungIndex("group", allSearchableEntities);
            
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("Group 캐시 워밍 완료 (개수: {}, 소요 시간: {}ms)", totalCount, elapsed);
            
        } catch (Exception e) {
            log.error("Group 캐시 워밍 실패", e);
            throw new RuntimeException("Group 캐시 워밍 실패", e);
        }
    }
}

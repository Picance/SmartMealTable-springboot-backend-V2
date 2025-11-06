package com.stdev.smartmealtable.batch.crawler.job.reader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.batch.crawler.dto.CrawledStoreDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON 파일에서 크롤링 데이터를 읽어오는 ItemReader
 * 네이버 플레이스 등의 크롤링 데이터를 배치로 처리
 */
@Slf4j
public class JsonStoreItemReader implements ItemReader<CrawledStoreDto> {
    
    private final ObjectMapper objectMapper;
    private final List<CrawledStoreDto> storeList;
    private int currentIndex = 0;
    
    public JsonStoreItemReader(Resource resource, ObjectMapper objectMapper) throws IOException {
        this.objectMapper = objectMapper;
        this.storeList = loadStoresFromJson(resource);
        log.info("Loaded {} stores from JSON file: {}", storeList.size(), resource.getFilename());
    }
    
    /**
     * JSON 파일에서 가게 데이터를 읽어옴
     */
    private List<CrawledStoreDto> loadStoresFromJson(Resource resource) throws IOException {
        try {
            List<CrawledStoreDto> stores = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<List<CrawledStoreDto>>() {}
            );
            return stores != null ? stores : new ArrayList<>();
        } catch (IOException e) {
            log.error("Failed to load JSON file: {}", resource.getFilename(), e);
            throw e;
        }
    }
    
    /**
     * 다음 아이템 읽기
     */
    @Override
    public CrawledStoreDto read() {
        if (currentIndex < storeList.size()) {
            CrawledStoreDto store = storeList.get(currentIndex);
            currentIndex++;
            log.debug("Reading store: {} (index: {}/{})", 
                    store.getName(), currentIndex, storeList.size());
            return store;
        }
        return null; // 더 이상 읽을 데이터가 없음
    }
    
    /**
     * 전체 데이터 개수 반환
     */
    public int getTotalCount() {
        return storeList.size();
    }
}

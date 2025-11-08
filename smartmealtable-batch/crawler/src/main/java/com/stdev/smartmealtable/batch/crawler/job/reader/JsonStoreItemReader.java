package com.stdev.smartmealtable.batch.crawler.job.reader;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
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
        this.objectMapper = createCustomObjectMapper(objectMapper);
        this.storeList = loadStoresFromJson(resource);
        log.info("Loaded {} stores from JSON file: {}", storeList.size(), resource.getFilename());
    }

    /**
     * 커스텀 ObjectMapper 생성 - 큰 숫자 처리를 위한 설정
     */
    private ObjectMapper createCustomObjectMapper(ObjectMapper originalMapper) {
        ObjectMapper mapper = originalMapper.copy();

        // Integer 범위를 초과하는 숫자를 처리하기 위한 커스텀 디시리얼라이저
        mapper.registerModule(new com.fasterxml.jackson.databind.module.SimpleModule() {
            @Override
            public void setupModule(SetupContext context) {
                super.setupModule(context);
                context.addBeanDeserializerModifier(new BeanDeserializerModifier() {
                    @Override
                    public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                                                            BeanDescription beanDesc,
                                                            JsonDeserializer<?> deserializer) {
                        if (CrawledStoreDto.MenuInfo.class.isAssignableFrom(beanDesc.getBeanClass())) {
                            return new MenuInfoDeserializer(deserializer);
                        }
                        return deserializer;
                    }
                });
            }
        });

        return mapper;
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

            if (stores == null) {
                return new ArrayList<>();
            }

            // 각 가게의 메뉴 목록에서 null을 제거하고 스킵된 메뉴 수 기록
            int totalSkippedMenus = 0;
            for (CrawledStoreDto store : stores) {
                if (store.getMenus() != null) {
                    int originalSize = store.getMenus().size();
                    store.getMenus().removeIf(menu -> menu == null);
                    int skippedCount = originalSize - store.getMenus().size();
                    if (skippedCount > 0) {
                        totalSkippedMenus += skippedCount;
                        log.info("Skipped {} menus with invalid prices for store: {}", skippedCount, store.getName());
                    }
                }
            }

            if (totalSkippedMenus > 0) {
                log.info("Total skipped menus due to price overflow: {}", totalSkippedMenus);
            }

            return stores;
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

    /**
     * MenuInfo용 커스텀 디시리얼라이저 - 가격이 Integer 범위를 초과할 경우 해당 메뉴를 스킵
     */
    private static class MenuInfoDeserializer extends StdDeserializer<CrawledStoreDto.MenuInfo> {
        private final JsonDeserializer<?> defaultDeserializer;

        public MenuInfoDeserializer(JsonDeserializer<?> defaultDeserializer) {
            super(CrawledStoreDto.MenuInfo.class);
            this.defaultDeserializer = defaultDeserializer;
        }

        @Override
        public CrawledStoreDto.MenuInfo deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            try {
                // 기본 디시리얼라이저로 시도
                return (CrawledStoreDto.MenuInfo) defaultDeserializer.deserialize(p, ctxt);
            } catch (JsonMappingException e) {
                if (e.getCause() instanceof com.fasterxml.jackson.core.exc.InputCoercionException
                    && e.getCause().getMessage().contains("out of range of int")) {
                    // Integer 범위 초과 시 해당 메뉴는 스킵하고 null 반환
                    log.warn("Skipping menu due to integer overflow: {}", e.getOriginalMessage());
                    return null;
                }
                throw e;
            }
        }
    }
}

package com.stdev.smartmealtable.batch.crawler.job.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.batch.crawler.dto.CrawledStoreDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 실제 JSON 파일 파싱 테스트
 */
@DisplayName("실제 JSON 파일 파싱 테스트")
class JsonStoreItemReaderIntegrationTest {

    @Test
    @DisplayName("실제 노원구 JSON 파일 파싱 성공")
    void testParsingRealJsonFile() throws Exception {
        // Arrange
        String filePath = "districts_before/노원구_공릉동.json";
        Resource resource = new FileSystemResource(filePath);

        // Act
        ObjectMapper objectMapper = new ObjectMapper();
        JsonStoreItemReader reader = new JsonStoreItemReader(resource, objectMapper);

        // Assert
        assertThat(reader.getTotalCount()).isGreaterThan(0);
        
        CrawledStoreDto store = reader.read();
        assertThat(store).isNotNull();
        assertThat(store.getName()).isNotNull();
        assertThat(store.getMenus()).isNotEmpty();

        CrawledStoreDto.MenuInfo menu = store.getMenus().get(0);
        assertThat(menu).isNotNull();
        assertThat(menu.getIsMain()).isNotNull();
        assertThat(menu.getName()).isNotNull();
        assertThat(menu.getPrice()).isNotNull();
    }
}

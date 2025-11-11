package com.stdev.smartmealtable.batch.crawler.job.reader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.batch.crawler.dto.GroupDataDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Iterator;

/**
 * JSON 파일에서 그룹 데이터를 읽는 ItemReader
 * korea-univ-data.json 형식 지원 (fields + records 구조)
 */
@Slf4j
public class JsonGroupDataItemReader implements ItemReader<GroupDataDto> {

    private final Resource resource;
    private final ObjectMapper objectMapper;
    private Iterator<JsonNode> recordIterator;
    private boolean initialized = false;

    public JsonGroupDataItemReader(Resource resource) {
        this.resource = resource;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * JSON 파일을 읽어서 초기화
     */
    private void initialize() throws IOException {
        if (initialized) {
            return;
        }

        log.info("Initializing JsonGroupDataItemReader with resource: {}", resource.getURI());

        // JSON 파일 읽기
        JsonNode rootNode = objectMapper.readTree(resource.getInputStream());

        // records 배열 추출
        JsonNode recordsNode = rootNode.get("records");
        if (recordsNode == null || !recordsNode.isArray()) {
            throw new IllegalArgumentException("JSON file must have 'records' array");
        }

        // Iterator 생성
        recordIterator = recordsNode.elements();

        int totalRecords = recordsNode.size();
        log.info("Found {} records in JSON file", totalRecords);

        initialized = true;
    }

    @Override
    public GroupDataDto read() throws Exception {
        // 지연 초기화
        if (!initialized) {
            initialize();
        }

        // 다음 레코드가 없으면 null 반환 (배치 종료)
        if (recordIterator == null || !recordIterator.hasNext()) {
            log.info("All records have been read");
            return null;
        }

        // 다음 레코드 읽기
        JsonNode recordNode = recordIterator.next();

        // DTO로 변환
        GroupDataDto dto = objectMapper.treeToValue(recordNode, GroupDataDto.class);

        if (log.isDebugEnabled()) {
            log.debug("Read group data: {}", dto.getSchoolName());
        }

        return dto;
    }
}

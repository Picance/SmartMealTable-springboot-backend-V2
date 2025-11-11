package com.stdev.smartmealtable.batch.crawler.job.writer;

import com.stdev.smartmealtable.batch.crawler.dto.GroupDataDto;
import com.stdev.smartmealtable.batch.crawler.service.GroupDataImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * 그룹 데이터를 저장하는 Writer
 * GroupDataImportService를 통해 DB에 저장
 */
@Slf4j
@RequiredArgsConstructor
public class GroupDataWriter implements ItemWriter<GroupDataDto> {

    private final GroupDataImportService groupDataImportService;

    @Override
    public void write(Chunk<? extends GroupDataDto> chunk) throws Exception {
        log.info("Writing {} group data items to database", chunk.size());

        // Chunk를 List로 변환
        List<GroupDataDto> dataList = new ArrayList<>(chunk.getItems());

        try {
            // 일괄 저장
            int savedCount = groupDataImportService.importGroupData(dataList);

            log.info("Successfully wrote {} out of {} group data items to database",
                    savedCount, chunk.size());

            // 통계 로깅
            if (savedCount < chunk.size()) {
                int skippedCount = chunk.size() - savedCount;
                log.warn("{} items were skipped (likely duplicates or errors)", skippedCount);
            }

        } catch (Exception e) {
            log.error("Failed to write group data chunk", e);
            throw e;
        }
    }
}

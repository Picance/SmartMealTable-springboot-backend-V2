package com.stdev.smartmealtable.batch.crawler.job.processor;

import com.stdev.smartmealtable.batch.crawler.dto.GroupDataDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

/**
 * 그룹 데이터를 처리하는 Processor
 * 데이터 검증, 변환, 정제 등의 작업 수행
 */
@Slf4j
public class GroupDataProcessor implements ItemProcessor<GroupDataDto, GroupDataDto> {

    @Override
    public GroupDataDto process(GroupDataDto item) throws Exception {
        if (item == null) {
            return null;
        }

        // 필수 필드 검증
        if (isInvalidData(item)) {
            log.warn("Skipping invalid group data: {}", item);
            return null; // null 반환 시 해당 아이템은 스킵됨
        }

        // 데이터 정제
        cleanupData(item);

        if (log.isDebugEnabled()) {
            log.debug("Processed group data: {} ({})", item.getSchoolName(), item.getRegionName());
        }

        return item;
    }

    /**
     * 데이터 유효성 검증
     */
    private boolean isInvalidData(GroupDataDto item) {
        // 학교명은 필수
        if (item.getSchoolName() == null || item.getSchoolName().isBlank()) {
            log.warn("School name is required");
            return true;
        }

        // 주소는 최소 하나 이상 있어야 함
        boolean hasAddress = (item.getRoadAddress() != null && !item.getRoadAddress().isBlank())
                || (item.getJibunAddress() != null && !item.getJibunAddress().isBlank());

        if (!hasAddress) {
            log.warn("At least one address is required for school: {}", item.getSchoolName());
            return true;
        }

        return false;
    }

    /**
     * 데이터 정제
     */
    private void cleanupData(GroupDataDto item) {
        // 공백 문자열을 null로 변환
        if (item.getSchoolNameEn() != null && item.getSchoolNameEn().isBlank()) {
            item.setSchoolNameEn(null);
        }
        if (item.getWebsite() != null && item.getWebsite().isBlank()) {
            item.setWebsite(null);
        }
        if (item.getPhoneNumber() != null && item.getPhoneNumber().isBlank()) {
            item.setPhoneNumber(null);
        }
        if (item.getFaxNumber() != null && item.getFaxNumber().isBlank()) {
            item.setFaxNumber(null);
        }

        // 웹사이트 URL 정규화 (http:// 또는 https:// 추가)
        if (item.getWebsite() != null && !item.getWebsite().startsWith("http")) {
            item.setWebsite("https://" + item.getWebsite());
        }

        // 전화번호 포맷 정리 (공백 제거)
        if (item.getPhoneNumber() != null) {
            item.setPhoneNumber(item.getPhoneNumber().replaceAll("\\s+", ""));
        }
    }
}

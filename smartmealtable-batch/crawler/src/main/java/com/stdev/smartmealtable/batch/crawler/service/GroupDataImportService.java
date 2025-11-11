package com.stdev.smartmealtable.batch.crawler.service;

import com.stdev.smartmealtable.batch.crawler.dto.GroupDataDto;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 그룹 데이터 Import 서비스
 * GroupDataDto를 Domain으로 변환하여 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupDataImportService {

    private final GroupRepository groupRepository;

    /**
     * 그룹 데이터 일괄 저장
     * 중복 체크 후 신규 데이터만 저장
     *
     * @param dataList 그룹 데이터 DTO 리스트
     * @return 저장된 개수
     */
    @Transactional
    public int importGroupData(List<GroupDataDto> dataList) {
        int savedCount = 0;

        for (GroupDataDto dto : dataList) {
            try {
                // 중복 체크 (이름 기준)
                boolean exists = groupRepository.existsByName(dto.getSchoolName());

                if (exists) {
                    log.debug("Group already exists, skipping: {}", dto.getSchoolName());
                    continue;
                }

                // DTO -> Domain 변환
                Group group = convertToGroup(dto);

                // 저장
                groupRepository.save(group);
                savedCount++;

                if (log.isDebugEnabled()) {
                    log.debug("Saved group: {} ({})", group.getName(), group.getRegionName());
                }

            } catch (Exception e) {
                log.error("Failed to save group: {}", dto.getSchoolName(), e);
                // 개별 실패 시 다음 레코드 처리 계속
            }
        }

        log.info("Successfully imported {} groups out of {} total", savedCount, dataList.size());
        return savedCount;
    }

    /**
     * 단건 저장
     *
     * @param dto 그룹 데이터 DTO
     * @return 저장된 Group
     */
    @Transactional
    public Group importSingleGroup(GroupDataDto dto) {
        // 중복 체크
        boolean exists = groupRepository.existsByName(dto.getSchoolName());

        if (exists) {
            log.warn("Group already exists: {}", dto.getSchoolName());
            throw new IllegalStateException("Group with name '" + dto.getSchoolName() + "' already exists");
        }

        // DTO -> Domain 변환
        Group group = convertToGroup(dto);

        // 저장
        Group saved = groupRepository.save(group);

        log.info("Saved group: {} (ID: {})", saved.getName(), saved.getGroupId());
        return saved;
    }

    /**
     * DTO를 Domain Group으로 변환
     */
    private Group convertToGroup(GroupDataDto dto) {
        return Group.createFromImport(
                dto.getSchoolName(),
                dto.getSchoolNameEn(),
                dto.getCampusType(),
                dto.getUniversityType(),
                dto.getSchoolType(),
                dto.getEstablishmentType(),
                dto.getRegionCode(),
                dto.getRegionName(),
                dto.getRoadAddress(),
                dto.getJibunAddress(),
                dto.getRoadPostalCode() != null && !dto.getRoadPostalCode().isBlank()
                        ? dto.getRoadPostalCode()
                        : dto.getPostalCode(),
                dto.getWebsite(),
                dto.getPhoneNumber(),
                dto.getFaxNumber(),
                dto.getEstablishmentDate()
        );
    }
}

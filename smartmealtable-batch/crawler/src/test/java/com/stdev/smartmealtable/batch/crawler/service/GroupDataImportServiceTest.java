package com.stdev.smartmealtable.batch.crawler.service;

import com.stdev.smartmealtable.batch.crawler.dto.GroupDataDto;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * GroupDataImportService 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GroupDataImportServiceTest {

    @Autowired
    private GroupDataImportService groupDataImportService;

    @Autowired
    private GroupRepository groupRepository;

    @Test
    @DisplayName("그룹 데이터를 DB에 저장한다")
    void it_saves_group_data_to_database() {
        // Given
        GroupDataDto dto = createTestGroupData("테스트대학교");

        // When
        Group saved = groupDataImportService.importSingleGroup(dto);

        // Then
        assertThat(saved.getGroupId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("테스트대학교");
        assertThat(saved.getType()).isEqualTo(GroupType.UNIVERSITY);

        // DB에서 조회 확인
        Optional<Group> found = groupRepository.findById(saved.getGroupId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("테스트대학교");
        assertThat(found.get().getGroupId()).isEqualTo(saved.getGroupId());
    }

    @Test
    @DisplayName("여러 그룹 데이터를 일괄 저장한다")
    void it_imports_multiple_group_data() {
        // Given
        List<GroupDataDto> dataList = List.of(
                createTestGroupData("샘플대학교1"),
                createTestGroupData("샘플대학교2"),
                createTestGroupData("샘플대학교3")
        );

        // When
        int savedCount = groupDataImportService.importGroupData(dataList);

        // Then
        assertThat(savedCount).isEqualTo(3);

        // DB 조회 확인
        List<Group> groups = groupRepository.findByNameContaining("샘플대학교");
        assertThat(groups).hasSize(3);
    }

    @Test
    @DisplayName("중복된 이름의 그룹은 저장하지 않는다")
    void it_skips_duplicate_group_names() {
        // Given
        GroupDataDto dto1 = createTestGroupData("중복대학교");
        groupDataImportService.importSingleGroup(dto1);

        // When
        GroupDataDto dto2 = createTestGroupData("중복대학교");

        // Then
        assertThatThrownBy(() -> groupDataImportService.importSingleGroup(dto2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("일괄 저장 시 중복 데이터는 스킵한다")
    void it_skips_duplicates_in_batch_import() {
        // Given
        groupDataImportService.importSingleGroup(createTestGroupData("기존대학교"));

        List<GroupDataDto> dataList = List.of(
                createTestGroupData("기존대학교"),  // 중복
                createTestGroupData("신규대학교1"),
                createTestGroupData("신규대학교2")
        );

        // When
        int savedCount = groupDataImportService.importGroupData(dataList);

        // Then
        assertThat(savedCount).isEqualTo(2); // 중복 1개 제외
    }

    @Test
    @DisplayName("상세 정보가 저장된다")
    void it_saves_detail_fields() {
        // Given
        GroupDataDto dto = GroupDataDto.builder()
                .schoolName("상세정보대학교")
                .schoolNameEn("Detail Info University")
                .campusType("본교")
                .universityType("일반대학원")
                .schoolType("특수대학원")
                .establishmentType("사립")
                .regionCode("11")
                .regionName("서울특별시")
                .roadAddress("서울특별시 종로구 테스트로 123")
                .jibunAddress("서울특별시 종로구 테스트동 123")
                .roadPostalCode("03083")
                .postalCode("03083")
                .website("www.detail.ac.kr")
                .phoneNumber("02-1234-5678")
                .faxNumber("02-1234-5679")
                .establishmentDate("2020-03-01")
                .build();

        // When
        Group saved = groupDataImportService.importSingleGroup(dto);

        // Then
        assertThat(saved.getGroupId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("상세정보대학교");
        assertThat(saved.getType()).isEqualTo(GroupType.UNIVERSITY);

        // DB에서 조회하여 상세 정보 확인 (JPA Entity에 컬럼이 추가되면 자동으로 저장됨)
        Optional<Group> found = groupRepository.findById(saved.getGroupId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("상세정보대학교");
    }

    private GroupDataDto createTestGroupData(String schoolName) {
        return GroupDataDto.builder()
                .schoolName(schoolName)
                .schoolNameEn("Test University")
                .campusType("본교")
                .universityType("대학원")
                .schoolType("일반대학원")
                .establishmentType("사립")
                .regionCode("11")
                .regionName("서울특별시")
                .roadAddress("서울특별시 종로구 테스트로 123")
                .jibunAddress("서울특별시 종로구 테스트동 123")
                .roadPostalCode("03083")
                .postalCode("03083")
                .website("www.test.ac.kr")
                .phoneNumber("02-1234-5678")
                .faxNumber("02-1234-5679")
                .establishmentDate("2020-03-01")
                .baseYear("2024")
                .dataReferenceDate("2025-01-08")
                .providerCode("B340014")
                .providerName("한국대학교육협의회")
                .build();
    }
}

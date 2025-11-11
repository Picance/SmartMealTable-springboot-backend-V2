package com.stdev.smartmealtable.api.group.controller;

import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.common.vo.AddressType;
import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 그룹 검색 API 통합 테스트
 * TDD: RED-GREEN-REFACTOR
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GroupControllerTest extends AbstractContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GroupRepository groupRepository;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 생성
        groupRepository.save(Group.create("서울대학교", GroupType.UNIVERSITY, Address.of("서울대학교", null, "서울특별시 관악구", null, null, null, null)));
        groupRepository.save(Group.create("연세대학교", GroupType.UNIVERSITY, Address.of("연세대학교", null, "서울특별시 서대문구", null, null, null, null)));
        groupRepository.save(Group.create("고려대학교", GroupType.UNIVERSITY, Address.of("고려대학교", null, "서울특별시 성북구", null, null, null, null)));
        groupRepository.save(Group.create("카카오", GroupType.COMPANY, Address.of("카카오", null, "경기도 성남시", null, null, null, null)));
        groupRepository.save(Group.create("네이버", GroupType.COMPANY, Address.of("네이버", null, "경기도 성남시", null, null, null, null)));
        groupRepository.save(Group.create("삼성전자", GroupType.COMPANY, Address.of("삼성전자", null, "경기도 수원시", null, null, null, null)));
    }

    @Test
    @DisplayName("그룹 목록 조회 성공 - 전체 조회")
    void searchGroups_Success_All() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/groups")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(6))
                .andExpect(jsonPath("$.data.pageInfo.page").value(0))
                .andExpect(jsonPath("$.data.pageInfo.size").value(20))
                .andExpect(jsonPath("$.data.pageInfo.totalElements").value(6))
                .andExpect(jsonPath("$.data.pageInfo.totalPages").value(1))
                .andExpect(jsonPath("$.data.pageInfo.last").value(true));
    }

    @Test
    @DisplayName("그룹 목록 조회 성공 - 타입 필터링 (UNIVERSITY)")
    void searchGroups_Success_FilterByType() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/groups")
                        .param("type", "UNIVERSITY")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(3))
                .andExpect(jsonPath("$.data.content[0].type").value("UNIVERSITY"))
                .andExpect(jsonPath("$.data.pageInfo.totalElements").value(3));
    }

    @Test
    @DisplayName("그룹 목록 조회 성공 - 이름 검색 (서울)")
    void searchGroups_Success_FilterByName() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/groups")
                        .param("name", "서울")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("서울대학교"))
                .andExpect(jsonPath("$.data.pageInfo.totalElements").value(1));
    }

    @Test
    @DisplayName("그룹 목록 조회 성공 - 타입과 이름 동시 필터링")
    void searchGroups_Success_FilterByTypeAndName() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/groups")
                        .param("type", "COMPANY")
                        .param("name", "카카오")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("카카오"))
                .andExpect(jsonPath("$.data.content[0].type").value("COMPANY"));
    }

    @Test
    @DisplayName("그룹 목록 조회 성공 - 페이징 (첫 번째 페이지)")
    void searchGroups_Success_Paging_FirstPage() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/groups")
                        .param("page", "0")
                        .param("size", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.pageInfo.page").value(0))
                .andExpect(jsonPath("$.data.pageInfo.size").value(2))
                .andExpect(jsonPath("$.data.pageInfo.totalElements").value(6))
                .andExpect(jsonPath("$.data.pageInfo.totalPages").value(3))
                .andExpect(jsonPath("$.data.pageInfo.last").value(false));
    }

    @Test
    @DisplayName("그룹 목록 조회 성공 - 페이징 (두 번째 페이지)")
    void searchGroups_Success_Paging_SecondPage() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/groups")
                        .param("page", "1")
                        .param("size", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.pageInfo.page").value(1))
                .andExpect(jsonPath("$.data.pageInfo.last").value(false));
    }

    @Test
    @DisplayName("그룹 목록 조회 성공 - 페이징 (마지막 페이지)")
    void searchGroups_Success_Paging_LastPage() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/groups")
                        .param("page", "2")
                        .param("size", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.pageInfo.page").value(2))
                .andExpect(jsonPath("$.data.pageInfo.last").value(true));
    }

    @Test
    @DisplayName("그룹 목록 조회 성공 - 검색 결과 없음")
    void searchGroups_Success_EmptyResult() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/groups")
                        .param("name", "존재하지않는그룹")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(0))
                .andExpect(jsonPath("$.data.pageInfo.totalElements").value(0))
                .andExpect(jsonPath("$.data.pageInfo.totalPages").value(0));
    }

    @Test
    @DisplayName("그룹 목록 조회 성공 - 페이지 범위 초과")
    void searchGroups_Success_PageOutOfRange() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/groups")
                        .param("page", "100")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(0));
    }

    @Test
    @DisplayName("그룹 목록 조회 성공 - 응답 필드 검증")
    void searchGroups_Success_ResponseFields() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/groups")
                        .param("type", "UNIVERSITY")
                        .param("page", "0")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content[0].groupId").exists())
                .andExpect(jsonPath("$.data.content[0].name").exists())
                .andExpect(jsonPath("$.data.content[0].type").exists())
                .andExpect(jsonPath("$.data.content[0].address").exists());
    }
}

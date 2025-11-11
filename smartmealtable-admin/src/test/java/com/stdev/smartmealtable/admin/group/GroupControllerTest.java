package com.stdev.smartmealtable.admin.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.admin.common.AbstractAdminContainerTest;
import com.stdev.smartmealtable.admin.config.AdminTestConfiguration;
import com.stdev.smartmealtable.admin.group.controller.request.CreateGroupRequest;
import com.stdev.smartmealtable.admin.group.controller.request.UpdateGroupRequest;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.storage.db.member.entity.GroupJpaEntity;
import com.stdev.smartmealtable.storage.db.member.entity.MemberJpaEntity;
import com.stdev.smartmealtable.storage.db.member.repository.GroupJpaRepository;
import com.stdev.smartmealtable.storage.db.member.repository.MemberJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 그룹 관리 API 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(AdminTestConfiguration.class)
class GroupControllerTest extends AbstractAdminContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GroupJpaRepository groupJpaRepository;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @AfterEach
    void tearDown() {
        memberJpaRepository.deleteAll();
        groupJpaRepository.deleteAll();
    }

    // ==================== 목록 조회 테스트 ====================

    @Test
    @DisplayName("그룹 목록 조회 - 성공")
    void getGroups_Success() throws Exception {
        // given
        createTestGroup("서울대학교", GroupType.UNIVERSITY, "서울시 관악구");
        createTestGroup("카카오", GroupType.COMPANY, "경기도 성남시");
        createTestGroup("기타 그룹", GroupType.OTHER, "서울시 강남구");

        // when & then
        mockMvc.perform(get("/api/v1/admin/groups")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.groups", hasSize(3)))
                .andExpect(jsonPath("$.data.pageInfo.totalElements").value(3))
                .andExpect(jsonPath("$.data.pageInfo.totalPages").value(1));
    }

    @Test
    @DisplayName("그룹 목록 조회 - 타입 필터링")
    void getGroups_FilterByType() throws Exception {
        // given
        createTestGroup("서울대학교", GroupType.UNIVERSITY, "서울시 관악구");
        createTestGroup("카카오", GroupType.COMPANY, "경기도 성남시");

        // when & then - UNIVERSITY만 조회
        mockMvc.perform(get("/api/v1/admin/groups")
                        .param("type", "UNIVERSITY")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.groups", hasSize(1)))
                .andExpect(jsonPath("$.data.groups[0].name").value("서울대학교"))
                .andExpect(jsonPath("$.data.groups[0].type").value("UNIVERSITY"));
    }

    @Test
    @DisplayName("그룹 목록 조회 - 이름 검색")
    void getGroups_SearchByName() throws Exception {
        // given
        createTestGroup("서울대학교", GroupType.UNIVERSITY, "서울시 관악구");
        createTestGroup("서울시립대학교", GroupType.UNIVERSITY, "서울시 동대문구");
        createTestGroup("카카오", GroupType.COMPANY, "경기도 성남시");

        // when & then - "서울" 검색
        mockMvc.perform(get("/api/v1/admin/groups")
                        .param("name", "서울")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.groups", hasSize(2)))
                .andExpect(jsonPath("$.data.groups[*].name", containsInAnyOrder("서울대학교", "서울시립대학교")));
    }

    // ==================== 상세 조회 테스트 ====================

    @Test
    @DisplayName("그룹 상세 조회 - 성공")
    void getGroup_Success() throws Exception {
        // given
        GroupJpaEntity group = createTestGroup("서울대학교", GroupType.UNIVERSITY, "서울시 관악구");

        // when & then
        mockMvc.perform(get("/api/v1/admin/groups/{groupId}", group.getGroupId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.groupId").value(group.getGroupId()))
                .andExpect(jsonPath("$.data.name").value("서울대학교"))
                .andExpect(jsonPath("$.data.type").value("UNIVERSITY"))
                .andExpect(jsonPath("$.data.address").value("서울시 관악구"));
    }

    @Test
    @DisplayName("그룹 상세 조회 - 존재하지 않는 그룹 (404)")
    void getGroup_NotFound() throws Exception {
        // given
        Long nonExistentGroupId = 99999L;

        // when & then
        mockMvc.perform(get("/api/v1/admin/groups/{groupId}", nonExistentGroupId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"))
                .andExpect(jsonPath("$.error.message").value("존재하지 않는 그룹입니다."));
    }

    // ==================== 생성 테스트 ====================

    @Test
    @DisplayName("그룹 생성 - 성공")
    void createGroup_Success() throws Exception {
        // given
        CreateGroupRequest request = new CreateGroupRequest(
                "연세대학교",
                GroupType.UNIVERSITY,
                "서울시 서대문구"
        );

        // when & then
        mockMvc.perform(post("/api/v1/admin/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.groupId").exists())
                .andExpect(jsonPath("$.data.name").value("연세대학교"))
                .andExpect(jsonPath("$.data.type").value("UNIVERSITY"))
                .andExpect(jsonPath("$.data.address").value("서울시 서대문구"));
    }

    @Test
    @DisplayName("그룹 생성 - 중복된 이름 (409)")
    void createGroup_DuplicateName() throws Exception {
        // given
        createTestGroup("서울대학교", GroupType.UNIVERSITY, "서울시 관악구");

        CreateGroupRequest request = new CreateGroupRequest(
                "서울대학교",  // 중복
                GroupType.UNIVERSITY,
                "서울시 관악구"
        );

        // when & then
        mockMvc.perform(post("/api/v1/admin/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E409"))
                .andExpect(jsonPath("$.error.message").value("이미 존재하는 그룹 이름입니다."));
    }

    @Test
    @DisplayName("그룹 생성 - 필수 필드 누락 (422)")
    void createGroup_MissingRequiredFields() throws Exception {
        // given
        CreateGroupRequest request = new CreateGroupRequest(
                "",  // 빈 이름
                null,  // null 타입
                "서울시"
        );

        // when & then
        mockMvc.perform(post("/api/v1/admin/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());  // 422
    }

    // ==================== 수정 테스트 ====================

    @Test
    @DisplayName("그룹 수정 - 성공")
    void updateGroup_Success() throws Exception {
        // given
        GroupJpaEntity group = createTestGroup("서울대학교", GroupType.UNIVERSITY, "서울시 관악구");

        UpdateGroupRequest request = new UpdateGroupRequest(
                "서울대학교 (수정)",
                GroupType.UNIVERSITY,
                "서울시 관악구 관악로 1"
        );

        // when & then
        mockMvc.perform(put("/api/v1/admin/groups/{groupId}", group.getGroupId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.groupId").value(group.getGroupId()))
                .andExpect(jsonPath("$.data.name").value("서울대학교 (수정)"))
                .andExpect(jsonPath("$.data.address").value("서울시 관악구 관악로 1"));
    }

    @Test
    @DisplayName("그룹 수정 - 존재하지 않는 그룹 (404)")
    void updateGroup_NotFound() throws Exception {
        // given
        Long nonExistentGroupId = 99999L;
        UpdateGroupRequest request = new UpdateGroupRequest(
                "연세대학교",
                GroupType.UNIVERSITY,
                "서울시 서대문구"
        );

        // when & then
        mockMvc.perform(put("/api/v1/admin/groups/{groupId}", nonExistentGroupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"));
    }

    @Test
    @DisplayName("그룹 수정 - 중복된 이름 (409)")
    void updateGroup_DuplicateName() throws Exception {
        // given
        GroupJpaEntity group1 = createTestGroup("서울대학교", GroupType.UNIVERSITY, "서울시 관악구");
        GroupJpaEntity group2 = createTestGroup("연세대학교", GroupType.UNIVERSITY, "서울시 서대문구");

        UpdateGroupRequest request = new UpdateGroupRequest(
                "서울대학교",  // group1의 이름으로 수정 시도
                GroupType.UNIVERSITY,
                "서울시 서대문구"
        );

        // when & then
        mockMvc.perform(put("/api/v1/admin/groups/{groupId}", group2.getGroupId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E409"))
                .andExpect(jsonPath("$.error.message").value("이미 존재하는 그룹 이름입니다."));
    }

    // ==================== 삭제 테스트 ====================

    @Test
    @DisplayName("그룹 삭제 - 성공")
    void deleteGroup_Success() throws Exception {
        // given
        GroupJpaEntity group = createTestGroup("서울대학교", GroupType.UNIVERSITY, "서울시 관악구");

        // when & then
        mockMvc.perform(delete("/api/v1/admin/groups/{groupId}", group.getGroupId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        // 실제 삭제 확인
        assert groupJpaRepository.findById(group.getGroupId()).isEmpty();
    }

    @Test
    @DisplayName("그룹 삭제 - 존재하지 않는 그룹 (404)")
    void deleteGroup_NotFound() throws Exception {
        // given
        Long nonExistentGroupId = 99999L;

        // when & then
        mockMvc.perform(delete("/api/v1/admin/groups/{groupId}", nonExistentGroupId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"));
    }

    @Test
    @DisplayName("그룹 삭제 - 회원이 속한 그룹 (409)")
    void deleteGroup_HasMembers() throws Exception {
        // given
        GroupJpaEntity group = createTestGroup("서울대학교", GroupType.UNIVERSITY, "서울시 관악구");
        createTestMember(group.getGroupId(), "닉네임1");

        // when & then
        mockMvc.perform(delete("/api/v1/admin/groups/{groupId}", group.getGroupId()))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E409"))
                .andExpect(jsonPath("$.error.message").value("해당 그룹에 속한 회원이 존재하여 삭제할 수 없습니다."));
    }

    // ==================== Helper Methods ====================

    private GroupJpaEntity createTestGroup(String name, GroupType type, String address) {
        // String address를 Address VO로 변환
        com.stdev.smartmealtable.domain.common.vo.Address addressVO =
                com.stdev.smartmealtable.domain.common.vo.Address.of(
                        name,           // alias
                        null,           // lotNumberAddress
                        address,        // streetNameAddress
                        null,           // detailedAddress
                        null,           // latitude
                        null,           // longitude
                        null            // addressType
                );

        // Domain 엔티티를 통해 생성
        com.stdev.smartmealtable.domain.member.entity.Group domainGroup =
                com.stdev.smartmealtable.domain.member.entity.Group.create(name, type, addressVO);

        GroupJpaEntity jpaEntity = GroupJpaEntity.from(domainGroup);
        return groupJpaRepository.save(jpaEntity);
    }

    private MemberJpaEntity createTestMember(Long groupId, String nickname) {
        // Domain 엔티티를 통해 생성
        com.stdev.smartmealtable.domain.member.entity.Member domainMember = 
                com.stdev.smartmealtable.domain.member.entity.Member.create(
                        groupId,
                        nickname,
                        null,
                        com.stdev.smartmealtable.domain.member.entity.RecommendationType.BALANCED
                );
        
        MemberJpaEntity jpaEntity = MemberJpaEntity.from(domainMember);
        return memberJpaRepository.save(jpaEntity);
    }
}

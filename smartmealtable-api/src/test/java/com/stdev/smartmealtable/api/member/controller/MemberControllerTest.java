package com.stdev.smartmealtable.api.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.api.auth.service.SignupService;
import com.stdev.smartmealtable.api.auth.service.dto.SignupServiceRequest;
import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 회원 프로필 관리 API 통합 테스트
 * - 10.1 내 프로필 조회
 * - 10.2 프로필 수정
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("회원 프로필 관리 API 테스트")
class MemberControllerTest extends AbstractContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SignupService signupService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private GroupRepository groupRepository;

    private Long testMemberId;
    private String testEmail = "test@example.com";
    private Long testGroupId;

    @BeforeEach
    void setUp() {
        // 테스트용 그룹 생성
        Group group = Group.create("서울대학교", GroupType.UNIVERSITY, "서울특별시 관악구");
        Group savedGroup = groupRepository.save(group);
        testGroupId = savedGroup.getGroupId();

        // 테스트용 회원 생성
        var response = signupService.signup(new SignupServiceRequest(
                "테스트사용자",
                testEmail,
                "Test@1234"
        ));
        testMemberId = response.getMemberId();

        // 회원 프로필 초기화 (닉네임, 그룹 설정)
        Member member = memberRepository.findById(testMemberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        member.changeNickname("초기닉네임");
        member.changeGroup(testGroupId);
        memberRepository.save(member);
    }

    // ===== 10.1 내 프로필 조회 =====

    @Test
    @DisplayName("내 프로필 조회 성공 - 200 OK")
    void getMyProfile_success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/members/me")
                        .header("X-Member-Id", testMemberId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.memberId").value(testMemberId))
                .andExpect(jsonPath("$.data.nickname").value("초기닉네임"))
                .andExpect(jsonPath("$.data.group.groupId").value(testGroupId))
                .andExpect(jsonPath("$.data.group.name").value("서울대학교"))
                .andExpect(jsonPath("$.data.recommendationType").exists())
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("내 프로필 조회 실패 - 존재하지 않는 회원 (404 Not Found)")
    void getMyProfile_memberNotFound() throws Exception {
        // given
        Long invalidMemberId = 99999L;

        // when & then
        mockMvc.perform(get("/api/v1/members/me")
                        .header("X-Member-Id", invalidMemberId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"))
                .andExpect(jsonPath("$.error.message").value("존재하지 않는 회원입니다."));
    }

    // ===== 10.2 프로필 수정 =====

    @Test
    @DisplayName("프로필 수정 성공 - 닉네임만 변경 (200 OK)")
    void updateProfile_nicknameOnly_success() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("nickname", "변경된닉네임");
        request.put("groupId", testGroupId); // groupId는 필수 (기존 값 유지)

        // when & then
        mockMvc.perform(put("/api/v1/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Member-Id", testMemberId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.memberId").value(testMemberId))
                .andExpect(jsonPath("$.data.nickname").value("변경된닉네임"))
                .andExpect(jsonPath("$.data.group.groupId").value(testGroupId)) // 기존 그룹 유지
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("프로필 수정 성공 - 그룹만 변경 (200 OK)")
    void updateProfile_groupOnly_success() throws Exception {
        // given
        Group newGroup = Group.create("연세대학교", GroupType.UNIVERSITY, "서울특별시 서대문구");
        Group savedNewGroup = groupRepository.save(newGroup);
        Long newGroupId = savedNewGroup.getGroupId();

        Map<String, Object> request = new HashMap<>();
        request.put("nickname", "초기닉네임"); // nickname은 필수 (기존 값 유지)
        request.put("groupId", newGroupId);

        // when & then
        mockMvc.perform(put("/api/v1/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Member-Id", testMemberId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.memberId").value(testMemberId))
                .andExpect(jsonPath("$.data.nickname").value("초기닉네임")) // 기존 닉네임 유지
                .andExpect(jsonPath("$.data.group.groupId").value(newGroupId))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("프로필 수정 성공 - 닉네임과 그룹 모두 변경 (200 OK)")
    void updateProfile_both_success() throws Exception {
        // given
        Group newGroup = Group.create("고려대학교", GroupType.UNIVERSITY, "서울특별시 성북구");
        Group savedNewGroup = groupRepository.save(newGroup);
        Long newGroupId = savedNewGroup.getGroupId();

        Map<String, Object> request = new HashMap<>();
        request.put("nickname", "새닉네임");
        request.put("groupId", newGroupId);

        // when & then
        mockMvc.perform(put("/api/v1/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Member-Id", testMemberId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.memberId").value(testMemberId))
                .andExpect(jsonPath("$.data.nickname").value("새닉네임"))
                .andExpect(jsonPath("$.data.group.groupId").value(newGroupId))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("프로필 수정 실패 - 존재하지 않는 회원 (404 Not Found)")
    void updateProfile_memberNotFound() throws Exception {
        // given
        Long invalidMemberId = 99999L;
        Map<String, Object> request = new HashMap<>();
        request.put("nickname", "변경된닉네임");
        request.put("groupId", testGroupId);

        // when & then
        mockMvc.perform(put("/api/v1/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Member-Id", invalidMemberId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"))
                .andExpect(jsonPath("$.error.message").value("존재하지 않는 회원입니다."));
    }

    @Test
    @DisplayName("프로필 수정 실패 - 존재하지 않는 그룹 (404 Not Found)")
    void updateProfile_groupNotFound() throws Exception {
        // given
        Long invalidGroupId = 99999L;
        Map<String, Object> request = new HashMap<>();
        request.put("nickname", "변경된닉네임");
        request.put("groupId", invalidGroupId);

        // when & then
        mockMvc.perform(put("/api/v1/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Member-Id", testMemberId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"))
                .andExpect(jsonPath("$.error.message").value("존재하지 않는 그룹입니다."));
    }

    @Test
    @DisplayName("프로필 수정 실패 - 닉네임 길이 초과 (422 Unprocessable Entity)")
    void updateProfile_nicknameTooLong() throws Exception {
        // given
        String longNickname = "a".repeat(51); // 50자 초과
        Map<String, Object> request = new HashMap<>();
        request.put("nickname", longNickname);
        request.put("groupId", null);

        // when & then
        mockMvc.perform(put("/api/v1/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Member-Id", testMemberId))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"));
    }

    @Test
    @DisplayName("프로필 수정 실패 - 빈 닉네임 (422 Unprocessable Entity)")
    void updateProfile_emptyNickname() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("nickname", "");
        request.put("groupId", null);

        // when & then
        mockMvc.perform(put("/api/v1/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Member-Id", testMemberId))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"));
    }
}

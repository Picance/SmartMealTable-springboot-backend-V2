package com.stdev.smartmealtable.api.group.controller;

import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.common.vo.AddressType;
import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GroupController REST Docs 테스트
 * 그룹 검색 API 문서화
 */
@DisplayName("GroupController REST Docs 테스트")
class GroupControllerRestDocsTest extends AbstractRestDocsTest {

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
    @DisplayName("그룹 목록 조회 성공 - 전체 조회 (200)")
    void searchGroups_success_all_docs() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/groups")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andDo(document("group/search-groups-all",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("page").description("페이지 번호 (0부터 시작)").optional(),
                                parameterWithName("size").description("페이지 크기 (기본값: 20)").optional()
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("그룹 목록"),
                                fieldWithPath("data.content[].groupId").type(JsonFieldType.NUMBER).description("그룹 ID"),
                                fieldWithPath("data.content[].name").type(JsonFieldType.STRING).description("그룹명"),
                                fieldWithPath("data.content[].type").type(JsonFieldType.STRING).description("그룹 타입 (UNIVERSITY, COMPANY, OTHER)"),
                                fieldWithPath("data.content[].address").type(JsonFieldType.STRING).description("주소"),
                                fieldWithPath("data.pageInfo").type(JsonFieldType.OBJECT).description("페이징 정보"),
                                fieldWithPath("data.pageInfo.page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                                fieldWithPath("data.pageInfo.size").type(JsonFieldType.NUMBER).description("페이지 크기"),
                                fieldWithPath("data.pageInfo.totalElements").type(JsonFieldType.NUMBER).description("전체 요소 개수"),
                                fieldWithPath("data.pageInfo.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 개수"),
                                fieldWithPath("data.pageInfo.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("그룹 목록 조회 성공 - 타입 필터링 (200)")
    void searchGroups_success_filterByType_docs() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/groups")
                        .param("type", "UNIVERSITY")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].type").value("UNIVERSITY"))
                .andDo(document("group/search-groups-by-type",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("type").description("그룹 타입 필터 (UNIVERSITY, COMPANY, OTHER)").optional(),
                                parameterWithName("page").description("페이지 번호 (0부터 시작)").optional(),
                                parameterWithName("size").description("페이지 크기 (기본값: 20)").optional()
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("필터링된 그룹 목록"),
                                fieldWithPath("data.content[].groupId").type(JsonFieldType.NUMBER).description("그룹 ID"),
                                fieldWithPath("data.content[].name").type(JsonFieldType.STRING).description("그룹명"),
                                fieldWithPath("data.content[].type").type(JsonFieldType.STRING).description("그룹 타입"),
                                fieldWithPath("data.content[].address").type(JsonFieldType.STRING).description("주소"),
                                fieldWithPath("data.pageInfo").type(JsonFieldType.OBJECT).description("페이징 정보"),
                                fieldWithPath("data.pageInfo.page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                                fieldWithPath("data.pageInfo.size").type(JsonFieldType.NUMBER).description("페이지 크기"),
                                fieldWithPath("data.pageInfo.totalElements").type(JsonFieldType.NUMBER).description("전체 요소 개수"),
                                fieldWithPath("data.pageInfo.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 개수"),
                                fieldWithPath("data.pageInfo.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("그룹 목록 조회 성공 - 이름 검색 (200)")
    void searchGroups_success_filterByName_docs() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/groups")
                        .param("name", "서울")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].name").value("서울대학교"))
                .andDo(document("group/search-groups-by-name",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("name").description("그룹명 검색어 (부분 일치)").optional(),
                                parameterWithName("page").description("페이지 번호 (0부터 시작)").optional(),
                                parameterWithName("size").description("페이지 크기 (기본값: 20)").optional()
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("검색된 그룹 목록"),
                                fieldWithPath("data.content[].groupId").type(JsonFieldType.NUMBER).description("그룹 ID"),
                                fieldWithPath("data.content[].name").type(JsonFieldType.STRING).description("그룹명"),
                                fieldWithPath("data.content[].type").type(JsonFieldType.STRING).description("그룹 타입"),
                                fieldWithPath("data.content[].address").type(JsonFieldType.STRING).description("주소"),
                                fieldWithPath("data.pageInfo").type(JsonFieldType.OBJECT).description("페이징 정보"),
                                fieldWithPath("data.pageInfo.page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                                fieldWithPath("data.pageInfo.size").type(JsonFieldType.NUMBER).description("페이지 크기"),
                                fieldWithPath("data.pageInfo.totalElements").type(JsonFieldType.NUMBER).description("전체 요소 개수"),
                                fieldWithPath("data.pageInfo.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 개수"),
                                fieldWithPath("data.pageInfo.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("그룹 목록 조회 성공 - 타입과 이름 동시 필터링 (200)")
    void searchGroups_success_filterByTypeAndName_docs() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/groups")
                        .param("type", "COMPANY")
                        .param("name", "카카오")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].name").value("카카오"))
                .andExpect(jsonPath("$.data.content[0].type").value("COMPANY"))
                .andDo(document("group/search-groups-by-type-and-name",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("type").description("그룹 타입 필터 (UNIVERSITY, COMPANY, OTHER)").optional(),
                                parameterWithName("name").description("그룹명 검색어 (부분 일치)").optional(),
                                parameterWithName("page").description("페이지 번호 (0부터 시작)").optional(),
                                parameterWithName("size").description("페이지 크기 (기본값: 20)").optional()
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("필터링된 그룹 목록"),
                                fieldWithPath("data.content[].groupId").type(JsonFieldType.NUMBER).description("그룹 ID"),
                                fieldWithPath("data.content[].name").type(JsonFieldType.STRING).description("그룹명"),
                                fieldWithPath("data.content[].type").type(JsonFieldType.STRING).description("그룹 타입"),
                                fieldWithPath("data.content[].address").type(JsonFieldType.STRING).description("주소"),
                                fieldWithPath("data.pageInfo").type(JsonFieldType.OBJECT).description("페이징 정보"),
                                fieldWithPath("data.pageInfo.page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                                fieldWithPath("data.pageInfo.size").type(JsonFieldType.NUMBER).description("페이지 크기"),
                                fieldWithPath("data.pageInfo.totalElements").type(JsonFieldType.NUMBER).description("전체 요소 개수"),
                                fieldWithPath("data.pageInfo.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 개수"),
                                fieldWithPath("data.pageInfo.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("그룹 목록 조회 성공 - 검색 결과 없음 (200)")
    void searchGroups_success_emptyResult_docs() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/groups")
                        .param("name", "존재하지않는그룹")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(0))
                .andDo(document("group/search-groups-empty",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("name").description("그룹명 검색어").optional(),
                                parameterWithName("page").description("페이지 번호").optional(),
                                parameterWithName("size").description("페이지 크기").optional()
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("그룹 목록 (빈 배열)"),
                                fieldWithPath("data.pageInfo").type(JsonFieldType.OBJECT).description("페이징 정보"),
                                fieldWithPath("data.pageInfo.page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                                fieldWithPath("data.pageInfo.size").type(JsonFieldType.NUMBER).description("페이지 크기"),
                                fieldWithPath("data.pageInfo.totalElements").type(JsonFieldType.NUMBER).description("전체 요소 개수 (0)"),
                                fieldWithPath("data.pageInfo.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 개수 (0)"),
                                fieldWithPath("data.pageInfo.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("그룹 목록 조회 성공 - 페이징 (200)")
    void searchGroups_success_paging_docs() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/groups")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.pageInfo.page").value(0))
                .andExpect(jsonPath("$.data.pageInfo.size").value(2))
                .andExpect(jsonPath("$.data.pageInfo.totalElements").value(6))
                .andExpect(jsonPath("$.data.pageInfo.totalPages").value(3))
                .andExpect(jsonPath("$.data.pageInfo.last").value(false))
                .andDo(document("group/search-groups-paging",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("page").description("페이지 번호 (0부터 시작)").optional(),
                                parameterWithName("size").description("페이지 크기").optional()
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("그룹 목록"),
                                fieldWithPath("data.content[].groupId").type(JsonFieldType.NUMBER).description("그룹 ID"),
                                fieldWithPath("data.content[].name").type(JsonFieldType.STRING).description("그룹명"),
                                fieldWithPath("data.content[].type").type(JsonFieldType.STRING).description("그룹 타입"),
                                fieldWithPath("data.content[].address").type(JsonFieldType.STRING).description("주소"),
                                fieldWithPath("data.pageInfo").type(JsonFieldType.OBJECT).description("페이징 정보"),
                                fieldWithPath("data.pageInfo.page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                                fieldWithPath("data.pageInfo.size").type(JsonFieldType.NUMBER).description("페이지 크기"),
                                fieldWithPath("data.pageInfo.totalElements").type(JsonFieldType.NUMBER).description("전체 요소 개수"),
                                fieldWithPath("data.pageInfo.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 개수"),
                                fieldWithPath("data.pageInfo.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부 (false)"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }
}

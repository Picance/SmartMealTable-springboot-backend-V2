package com.stdev.smartmealtable.api.member.controller;

import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.api.member.dto.request.AddressRequest;
import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.member.entity.AddressHistory;
import com.stdev.smartmealtable.domain.member.repository.AddressHistoryRepository;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AddressController REST Docs 테스트
 */
@DisplayName("AddressController REST Docs")
class AddressControllerRestDocsTest extends AbstractRestDocsTest {

    @Autowired private MemberRepository memberRepository;
    @Autowired private MemberAuthenticationRepository memberAuthenticationRepository;
    @Autowired private GroupRepository groupRepository;
    @Autowired private AddressHistoryRepository addressHistoryRepository;

    private Member member;
    private String accessToken;
    private Long addressHistoryId;

    @BeforeEach
    void setUpTestData() {
        // 그룹 생성
        Group testGroup = Group.create("테스트대학교", GroupType.UNIVERSITY, "서울특별시 관악구");
        Group savedGroup = groupRepository.save(testGroup);

        // 회원 생성
        Member testMember = Member.create(savedGroup.getGroupId(), "테스트유저", RecommendationType.BALANCED);
        member = memberRepository.save(testMember);

        // 회원 인증 정보 생성
        MemberAuthentication auth = MemberAuthentication.createEmailAuth(
                member.getMemberId(),
                "test@example.com",
                "hashedPasswordForTest",
                "테스트유저"
        );
        memberAuthenticationRepository.save(auth);

        // JWT 토큰 생성
        accessToken = createAccessToken(member.getMemberId());

        // 기본 주소 생성
        Address primaryAddressValue = Address.of(
                "집",
                "서울특별시 관악구 봉천동 1234",
                "서울특별시 관악구 관악로 1",
                "101동 101호",
                37.4783,
                126.9516,
                "HOME"
        );
        AddressHistory primaryAddress = AddressHistory.create(
                member.getMemberId(),
                primaryAddressValue,
                true
        );
        AddressHistory savedAddress = addressHistoryRepository.save(primaryAddress);
        addressHistoryId = savedAddress.getAddressHistoryId();

        // 추가 주소 생성
        Address workAddressValue = Address.of(
                "회사",
                "서울특별시 강남구 역삼동 5678",
                "서울특별시 강남구 테헤란로 123",
                "5층",
                37.4979,
                127.0276,
                "WORK"
        );
        AddressHistory workAddress = AddressHistory.create(
                member.getMemberId(),
                workAddressValue,
                false
        );
        addressHistoryRepository.save(workAddress);
    }

    @Test
    @DisplayName("주소 목록 조회 성공")
    void getAddresses_Success() throws Exception {
        mockMvc.perform(get("/api/v1/members/me/addresses")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andDo(document("address-get-addresses-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.ARRAY).description("주소 목록"),
                                fieldWithPath("data[].addressHistoryId").type(JsonFieldType.NUMBER).description("주소 이력 ID"),
                                fieldWithPath("data[].addressAlias").type(JsonFieldType.STRING).description("주소 별칭"),
                                fieldWithPath("data[].addressType").type(JsonFieldType.STRING).description("주소 유형 (HOME, WORK, ETC)"),
                                fieldWithPath("data[].lotNumberAddress").type(JsonFieldType.STRING).description("지번 주소"),
                                fieldWithPath("data[].streetNameAddress").type(JsonFieldType.STRING).description("도로명 주소"),
                                fieldWithPath("data[].detailedAddress").type(JsonFieldType.STRING).description("상세 주소").optional(),
                                fieldWithPath("data[].latitude").type(JsonFieldType.NUMBER).description("위도"),
                                fieldWithPath("data[].longitude").type(JsonFieldType.NUMBER).description("경도"),
                                fieldWithPath("data[].isPrimary").type(JsonFieldType.BOOLEAN).description("기본 주소 여부"),
                                fieldWithPath("data[].registeredAt").type(JsonFieldType.STRING).description("등록 일시"),
                                fieldWithPath("error").type(JsonFieldType.NULL)
                                        .description("에러 정보 (성공 시 null)")
                                        .optional()
                        )
                ));
    }

    @Test
    @DisplayName("주소 추가 성공")
    void addAddress_Success() throws Exception {
        AddressRequest request = new AddressRequest(
                "학교",
                "서울특별시 서대문구 연희동 1234",
                "서울특별시 서대문구 연희로 567",
                "본관 201호",
                37.5662,
                126.9346,
                "SCHOOL",
                false
        );

        mockMvc.perform(post("/api/v1/members/me/addresses")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.addressAlias").value("학교"))
                .andDo(document("address-add-address-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        requestFields(
                                fieldWithPath("addressAlias").type(JsonFieldType.STRING).description("주소 별칭 (최대 50자)"),
                                fieldWithPath("lotNumberAddress").type(JsonFieldType.STRING).description("지번 주소").optional(),
                                fieldWithPath("streetNameAddress").type(JsonFieldType.STRING).description("도로명 주소 (필수, 최대 255자)"),
                                fieldWithPath("detailedAddress").type(JsonFieldType.STRING).description("상세 주소 (최대 255자)").optional(),
                                fieldWithPath("latitude").type(JsonFieldType.NUMBER).description("위도").optional(),
                                fieldWithPath("longitude").type(JsonFieldType.NUMBER).description("경도").optional(),
                                fieldWithPath("addressType").type(JsonFieldType.STRING).description("주소 유형 (최대 20자)").optional(),
                                fieldWithPath("isPrimary").type(JsonFieldType.BOOLEAN).description("기본 주소 여부").optional()
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("생성된 주소 정보"),
                                fieldWithPath("data.addressHistoryId").type(JsonFieldType.NUMBER).description("주소 이력 ID"),
                                fieldWithPath("data.addressAlias").type(JsonFieldType.STRING).description("주소 별칭"),
                                fieldWithPath("data.addressType").type(JsonFieldType.STRING).description("주소 유형"),
                                fieldWithPath("data.lotNumberAddress").type(JsonFieldType.STRING).description("지번 주소").optional(),
                                fieldWithPath("data.streetNameAddress").type(JsonFieldType.STRING).description("도로명 주소"),
                                fieldWithPath("data.detailedAddress").type(JsonFieldType.STRING).description("상세 주소").optional(),
                                fieldWithPath("data.latitude").type(JsonFieldType.NUMBER).description("위도").optional(),
                                fieldWithPath("data.longitude").type(JsonFieldType.NUMBER).description("경도").optional(),
                                fieldWithPath("data.isPrimary").type(JsonFieldType.BOOLEAN).description("기본 주소 여부"),
                                fieldWithPath("data.registeredAt").type(JsonFieldType.STRING).description("등록 일시"),
                                fieldWithPath("error").type(JsonFieldType.NULL)
                                        .description("에러 정보 (성공 시 null)")
                                        .optional()
                        )
                ));
    }

    @Test
    @DisplayName("주소 추가 실패 - 유효성 검증 실패 (422)")
    void addAddress_ValidationFailure() throws Exception {
        AddressRequest request = new AddressRequest(
                "",  // 빈 별칭 (유효성 검증 실패)
                null,
                "",  // 빈 도로명 주소 (유효성 검증 실패)
                null,
                null,
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/v1/members/me/addresses")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"))
                .andDo(document("address-add-address-validation-failure",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        requestFields(
                                fieldWithPath("addressAlias").type(JsonFieldType.STRING).description("주소 별칭 (빈 값)"),
                                fieldWithPath("lotNumberAddress").type(JsonFieldType.NULL).description("지번 주소").optional(),
                                fieldWithPath("streetNameAddress").type(JsonFieldType.STRING).description("도로명 주소 (빈 값)"),
                                fieldWithPath("detailedAddress").type(JsonFieldType.NULL).description("상세 주소").optional(),
                                fieldWithPath("latitude").type(JsonFieldType.NULL).description("위도").optional(),
                                fieldWithPath("longitude").type(JsonFieldType.NULL).description("경도").optional(),
                                fieldWithPath("addressType").type(JsonFieldType.NULL).description("주소 유형").optional(),
                                fieldWithPath("isPrimary").type(JsonFieldType.NULL).description("기본 주소 여부").optional()
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (에러 시 null)").optional(),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드 (E422)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지"),
                                fieldWithPath("error.data").type(JsonFieldType.OBJECT).description("유효성 검증 실패 상세"),
                                fieldWithPath("error.data.field").type(JsonFieldType.STRING).description("검증 실패 필드명"),
                                fieldWithPath("error.data.reason").type(JsonFieldType.STRING).description("검증 실패 사유")
                        )
                ));
    }

    @Test
    @DisplayName("주소 수정 성공")
    void updateAddress_Success() throws Exception {
        AddressRequest request = new AddressRequest(
                "우리집",  // 별칭 변경
                "서울특별시 관악구 봉천동 1234",
                "서울특별시 관악구 관악로 1",
                "102동 102호",  // 상세 주소 변경
                37.4783,
                126.9516,
                "HOME",
                null
        );

        mockMvc.perform(put("/api/v1/members/me/addresses/{addressHistoryId}", addressHistoryId)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.addressAlias").value("우리집"))
                .andDo(document("address-update-address-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        pathParameters(
                                parameterWithName("addressHistoryId").description("수정할 주소 이력 ID")
                        ),
                        requestFields(
                                fieldWithPath("addressAlias").type(JsonFieldType.STRING).description("주소 별칭"),
                                fieldWithPath("lotNumberAddress").type(JsonFieldType.STRING).description("지번 주소").optional(),
                                fieldWithPath("streetNameAddress").type(JsonFieldType.STRING).description("도로명 주소"),
                                fieldWithPath("detailedAddress").type(JsonFieldType.STRING).description("상세 주소").optional(),
                                fieldWithPath("latitude").type(JsonFieldType.NUMBER).description("위도").optional(),
                                fieldWithPath("longitude").type(JsonFieldType.NUMBER).description("경도").optional(),
                                fieldWithPath("addressType").type(JsonFieldType.STRING).description("주소 유형").optional(),
                                fieldWithPath("isPrimary").type(JsonFieldType.NULL).description("기본 주소 여부").optional()
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("수정된 주소 정보"),
                                fieldWithPath("data.addressHistoryId").type(JsonFieldType.NUMBER).description("주소 이력 ID"),
                                fieldWithPath("data.addressAlias").type(JsonFieldType.STRING).description("주소 별칭"),
                                fieldWithPath("data.addressType").type(JsonFieldType.STRING).description("주소 유형"),
                                fieldWithPath("data.lotNumberAddress").type(JsonFieldType.STRING).description("지번 주소").optional(),
                                fieldWithPath("data.streetNameAddress").type(JsonFieldType.STRING).description("도로명 주소"),
                                fieldWithPath("data.detailedAddress").type(JsonFieldType.STRING).description("상세 주소").optional(),
                                fieldWithPath("data.latitude").type(JsonFieldType.NUMBER).description("위도").optional(),
                                fieldWithPath("data.longitude").type(JsonFieldType.NUMBER).description("경도").optional(),
                                fieldWithPath("data.isPrimary").type(JsonFieldType.BOOLEAN).description("기본 주소 여부"),
                                fieldWithPath("data.registeredAt").type(JsonFieldType.STRING).description("등록 일시"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("주소 수정 실패 - 존재하지 않는 주소 (404)")
    void updateAddress_NotFound() throws Exception {
        AddressRequest request = new AddressRequest(
                "테스트",
                "서울특별시 관악구 봉천동 1234",
                "서울특별시 관악구 관악로 1",
                "101동 101호",
                37.4783,
                126.9516,
                "HOME",
                null
        );

        mockMvc.perform(put("/api/v1/members/me/addresses/{addressHistoryId}", 99999L)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"))
                .andDo(document("address-update-address-not-found",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        pathParameters(
                                parameterWithName("addressHistoryId").description("존재하지 않는 주소 이력 ID")
                        ),
                        requestFields(
                                fieldWithPath("addressAlias").type(JsonFieldType.STRING).description("주소 별칭"),
                                fieldWithPath("lotNumberAddress").type(JsonFieldType.STRING).description("지번 주소").optional(),
                                fieldWithPath("streetNameAddress").type(JsonFieldType.STRING).description("도로명 주소"),
                                fieldWithPath("detailedAddress").type(JsonFieldType.STRING).description("상세 주소").optional(),
                                fieldWithPath("latitude").type(JsonFieldType.NUMBER).description("위도").optional(),
                                fieldWithPath("longitude").type(JsonFieldType.NUMBER).description("경도").optional(),
                                fieldWithPath("addressType").type(JsonFieldType.STRING).description("주소 유형").optional(),
                                fieldWithPath("isPrimary").type(JsonFieldType.NULL).description("기본 주소 여부").optional()
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (에러 시 null)").optional(),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드 (E404)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지"),
                                fieldWithPath("error.data").type(JsonFieldType.NULL).description("에러 상세 정보").optional()
                        )
                ));
    }

    @Test
    @DisplayName("주소 삭제 성공 (204)")
    void deleteAddress_Success() throws Exception {
        // 삭제할 새로운 주소 생성 (기본 주소가 아닌 주소)
        Address addressValue = Address.of(
                "삭제할 주소",
                "서울특별시 종로구 세종로 1",
                "서울특별시 종로구 세종대로 1",
                "1층",
                37.5720,
                126.9763,
                "ETC"
        );
        AddressHistory addressToDelete = AddressHistory.create(
                member.getMemberId(),
                addressValue,
                false
        );
        AddressHistory saved = addressHistoryRepository.save(addressToDelete);

        mockMvc.perform(delete("/api/v1/members/me/addresses/{addressHistoryId}", saved.getAddressHistoryId())
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document("address-delete-address-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        pathParameters(
                                parameterWithName("addressHistoryId").description("삭제할 주소 이력 ID")
                        )
                ));
    }

    @Test
    @DisplayName("주소 삭제 실패 - 존재하지 않는 주소 (404)")
    void deleteAddress_NotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/members/me/addresses/{addressHistoryId}", 99999L)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"))
                .andDo(document("address-delete-address-not-found",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        pathParameters(
                                parameterWithName("addressHistoryId").description("존재하지 않는 주소 이력 ID")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (에러 시 null)").optional(),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드 (E404)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지"),
                                fieldWithPath("error.data").type(JsonFieldType.NULL).description("에러 상세 정보").optional()
                        )
                ));
    }

    @Test
    @DisplayName("기본 주소 설정 성공")
    void setPrimaryAddress_Success() throws Exception {
        // 새로운 주소를 기본 주소로 설정
        Address newAddressValue = Address.of(
                "새 기본 주소",
                "서울특별시 서초구 서초동 1234",
                "서울특별시 서초구 서초대로 1",
                "201호",
                37.4837,
                127.0324,
                "HOME"
        );
        AddressHistory newAddress = AddressHistory.create(
                member.getMemberId(),
                newAddressValue,
                false
        );
        AddressHistory saved = addressHistoryRepository.save(newAddress);

        mockMvc.perform(put("/api/v1/members/me/addresses/{addressHistoryId}/primary", saved.getAddressHistoryId())
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.isPrimary").value(true))
                .andDo(document("address-set-primary-address-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        pathParameters(
                                parameterWithName("addressHistoryId").description("기본 주소로 설정할 주소 이력 ID")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.addressHistoryId").type(JsonFieldType.NUMBER).description("기본 주소로 설정된 주소 이력 ID"),
                                fieldWithPath("data.isPrimary").type(JsonFieldType.BOOLEAN).description("기본 주소 여부 (항상 true)"),
                                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정 일시"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("기본 주소 설정 실패 - 존재하지 않는 주소 (404)")
    void setPrimaryAddress_NotFound() throws Exception {
        mockMvc.perform(put("/api/v1/members/me/addresses/{addressHistoryId}/primary", 99999L)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"))
                .andDo(document("address-set-primary-address-not-found",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        pathParameters(
                                parameterWithName("addressHistoryId").description("존재하지 않는 주소 이력 ID")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (에러 시 null)").optional(),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드 (E404)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지"),
                                fieldWithPath("error.data").type(JsonFieldType.NULL).description("에러 상세 정보").optional()
                        )
                ));
    }

    @Test
    @DisplayName("주소 목록 조회 - 주소가 없는 경우 빈 배열 반환 (200)")
    void getAddresses_EmptyList() throws Exception {
        // 주소가 없는 새로운 회원 생성
        Member newMember = Member.create(member.getMemberId(), "주소없음회원", RecommendationType.BALANCED);
        Member saved = memberRepository.save(newMember);
        String newMemberToken = createAccessToken(saved.getMemberId());

        mockMvc.perform(get("/api/v1/members/me/addresses")
                        .header("Authorization", newMemberToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andDo(document("address-get-addresses-empty",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 처리 결과 (SUCCESS)"),
                                fieldWithPath("data").type(JsonFieldType.ARRAY).description("주소 목록 (빈 배열)"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
                        )
                ));
    }
}

package com.stdev.smartmealtable.api.onboarding.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.api.onboarding.dto.request.OnboardingAddressRequest;
import com.stdev.smartmealtable.domain.member.entity.AddressHistory;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.repository.AddressHistoryRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.support.jwt.JwtTokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 온보딩 - 주소 등록 API 통합 테스트
 * TDD 방식: RED-GREEN-REFACTOR
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OnboardingAddressControllerTest extends AbstractContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberAuthenticationRepository memberAuthenticationRepository;

    @Autowired
    private AddressHistoryRepository addressHistoryRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @AfterEach
    void tearDown() {
        addressHistoryRepository.findAllByMemberId(1L).forEach(address -> 
            addressHistoryRepository.save(address)
        );
    }

    @Test
    @DisplayName("주소 등록 성공 - 첫 번째 주소 (자동으로 주 주소)")
    void registerAddress_Success_FirstAddress() throws Exception {
        // given
        Member member = createMember("test@example.com");
        Long memberId = member.getMemberId();

        OnboardingAddressRequest request = new OnboardingAddressRequest(
                "우리집",
                "서울시 강남구 테헤란로 123",
                "서울시 강남구 테헤란로 123",
                "101동 1001호",
                37.123456,
                127.123456,
                "HOME",
                true
        );

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/address")
                        .header("Authorization", createAuthorizationHeader(memberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.alias").value("우리집"))
                .andExpect(jsonPath("$.data.streetNameAddress").value("서울시 강남구 테헤란로 123"))
                .andExpect(jsonPath("$.data.detailedAddress").value("101동 1001호"))
                .andExpect(jsonPath("$.data.latitude").value(37.123456))
                .andExpect(jsonPath("$.data.longitude").value(127.123456))
                .andExpect(jsonPath("$.data.addressType").value("HOME"))
                .andExpect(jsonPath("$.data.isPrimary").value(true));
    }

    @Test
    @DisplayName("주소 등록 성공 - 두 번째 주소 등록 시 기존 주 주소 해제")
    void registerAddress_Success_UnmarkExistingPrimary() throws Exception {
        // given
        Member member = createMember("test2@example.com");
        Long memberId = member.getMemberId();

        // 기존 주 주소 등록
        createAddress(memberId, "기존집", true);

        // 새 주 주소 등록 요청
        OnboardingAddressRequest request = new OnboardingAddressRequest(
                "새집",
                "서울시 서초구 강남대로 456",
                "서울시 서초구 강남대로 456",
                "202동 2002호",
                37.234567,
                127.234567,
                "HOME",
                true  // 주 주소로 설정
        );

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/address")
                        .header("Authorization", createAuthorizationHeader(memberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.alias").value("새집"))
                .andExpect(jsonPath("$.data.isPrimary").value(true));

        // 기존 주 주소가 해제되었는지 검증
        var addresses = addressHistoryRepository.findAllByMemberId(memberId);
        long primaryCount = addresses.stream().filter(AddressHistory::getIsPrimary).count();
        org.assertj.core.api.Assertions.assertThat(primaryCount).isEqualTo(1); // 주 주소는 하나만 존재해야 함
    }

    @Test
    @DisplayName("주소 등록 실패 - 도로명 주소 누락 (422)")
    void registerAddress_Fail_MissingStreetNameAddress() throws Exception {
        // given
        Member member = createMember("test3@example.com");
        Long memberId = member.getMemberId();

        OnboardingAddressRequest request = new OnboardingAddressRequest(
                "우리집",
                "서울시 강남구 테헤란로 123",
                null,  // 도로명 주소 누락
                "101동 1001호",
                37.123456,
                127.123456,
                "HOME",
                true
        );

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/address")
                        .header("Authorization", createAuthorizationHeader(memberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"));
    }

    @Test
    @DisplayName("주소 등록 실패 - 별칭 길이 초과 (422)")
    void registerAddress_Fail_AliasTooLong() throws Exception {
        // given
        Member member = createMember("test4@example.com");
        Long memberId = member.getMemberId();

        String longAlias = "a".repeat(51);  // 50자 초과
        OnboardingAddressRequest request = new OnboardingAddressRequest(
                longAlias,
                "서울시 강남구 테헤란로 123",
                "서울시 강남구 테헤란로 123",
                "101동 1001호",
                37.123456,
                127.123456,
                "HOME",
                true
        );

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/address")
                        .header("Authorization", createAuthorizationHeader(memberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"));
    }

    @Test
    @DisplayName("주소 등록 실패 - 위도 누락 (422)")
    void registerAddress_Fail_MissingLatitude() throws Exception {
        // given
        Member member = createMember("test5@example.com");
        Long memberId = member.getMemberId();

        OnboardingAddressRequest request = new OnboardingAddressRequest(
                "우리집",
                "서울시 강남구 테헤란로 123",
                "서울시 강남구 테헤란로 123",
                "101동 1001호",
                null,  // 위도 누락
                127.123456,
                "HOME",
                true
        );

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/address")
                        .header("Authorization", createAuthorizationHeader(memberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"));
    }

    @Test
    @DisplayName("주소 등록 성공 - 일반 주소 (isPrimary = false)")
    void registerAddress_Success_NonPrimaryAddress() throws Exception {
        // given
        Member member = createMember("test6@example.com");
        Long memberId = member.getMemberId();

        OnboardingAddressRequest request = new OnboardingAddressRequest(
                "회사",
                "서울시 종로구 세종대로 110",
                "서울시 종로구 세종대로 110",
                "5층",
                37.345678,
                126.987654,
                "OFFICE",
                false  // 일반 주소
        );

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/address")
                        .header("Authorization", createAuthorizationHeader(memberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.alias").value("회사"))
                .andExpect(jsonPath("$.data.addressType").value("OFFICE"))
                .andExpect(jsonPath("$.data.isPrimary").value(false));
    }

    // === Helper Methods ===

    /**
     * JWT 토큰을 생성하여 "Bearer {token}" 형식으로 반환
     */
    private String createAuthorizationHeader(Long memberId) {
        String token = jwtTokenProvider.createToken(memberId);
        return "Bearer " + token;
    }

    private Member createMember(String email) {
        // Member 먼저 생성
        Member member = Member.create(
                null,  // authenticationId는 나중에 설정
                "테스트닉네임",  // nickname required
                com.stdev.smartmealtable.domain.member.entity.RecommendationType.BALANCED  // recommendationType required
        );
        Member savedMember = memberRepository.save(member);

        // MemberAuthentication 생성 및 연결
        MemberAuthentication auth = MemberAuthentication.createEmailAuth(
                savedMember.getMemberId(),
                email,
                "hashedPassword123!",
                "테스터"
        );
        memberAuthenticationRepository.save(auth);

        return savedMember;
    }

    private AddressHistory createAddress(Long memberId, String alias, Boolean isPrimary) {
        AddressHistory address = AddressHistory.create(
                memberId,
                com.stdev.smartmealtable.domain.common.vo.Address.of(
                        alias,
                        "서울시 강남구 테헤란로 123",
                        "서울시 강남구 테헤란로 123",
                        "101동 1001호",
                        37.123456,
                        127.123456,
                        "HOME"
                ),
                isPrimary
        );
        return addressHistoryRepository.save(address);
    }
}

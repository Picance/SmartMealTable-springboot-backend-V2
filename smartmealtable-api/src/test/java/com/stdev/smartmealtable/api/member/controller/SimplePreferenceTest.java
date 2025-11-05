package com.stdev.smartmealtable.api.member.controller;

import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.support.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SimplePreferenceTest extends AbstractContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("간단한 선호도 조회 테스트")
    void simpleTest() throws Exception {
        // given
        Member member = Member.create(null, "테스터", null, RecommendationType.BALANCED);
        Member savedMember = memberRepository.save(member);
        String accessToken = jwtTokenProvider.createToken(savedMember.getMemberId());

        // when & then
        mockMvc.perform(get("/api/v1/members/me/preferences")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }
}

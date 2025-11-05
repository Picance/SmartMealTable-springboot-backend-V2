package com.stdev.smartmealtable.support.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JwtTokenProvider 단위 테스트
 */
@DisplayName("JwtTokenProvider 테스트")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        String testSecret = "smartmealtable-test-secret-key-for-jwt-token-generation-minimum-256-bits-required";
        long testValidity = 3600000L; // 1시간
        jwtTokenProvider = new JwtTokenProvider(testSecret, testValidity);
    }

    @Nested
    @DisplayName("토큰 생성 테스트")
    class CreateTokenTest {

        @Test
        @DisplayName("유효한 memberId로 JWT 토큰을 생성한다")
        void createToken_WithValidMemberId_ReturnsValidToken() {
            // given
            Long memberId = 12345L;

            // when
            String token = jwtTokenProvider.createToken(memberId);

            // then
            assertThat(token).isNotNull();
            assertThat(token).contains(".");  // JWT 형식 확인 (header.payload.signature)
            assertThat(token.split("\\.")).hasSize(3);  // JWT는 3개 부분으로 구성
        }

        @Test
        @DisplayName("생성된 토큰에서 memberId를 정확히 추출할 수 있다")
        void createToken_ExtractMemberId_ReturnsCorrectId() {
            // given
            Long originalMemberId = 99999L;
            String token = jwtTokenProvider.createToken(originalMemberId);

            // when
            Long extractedMemberId = jwtTokenProvider.extractMemberId(token);

            // then
            assertThat(extractedMemberId).isEqualTo(originalMemberId);
        }
    }

    @Nested
    @DisplayName("토큰 검증 테스트")
    class ValidateTokenTest {

        @Test
        @DisplayName("유효한 토큰은 검증을 통과한다")
        void validateToken_WithValidToken_ReturnsTrue() {
            // given
            String validToken = jwtTokenProvider.createToken(12345L);

            // when
            boolean isValid = jwtTokenProvider.validateToken(validToken);

            // then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("잘못된 형식의 토큰은 검증에 실패한다")
        void validateToken_WithInvalidFormat_ReturnsFalse() {
            // given
            String invalidToken = "invalid.token.format";

            // when
            boolean isValid = jwtTokenProvider.validateToken(invalidToken);

            // then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("빈 문자열 토큰은 검증에 실패한다")
        void validateToken_WithEmptyString_ReturnsFalse() {
            // given
            String emptyToken = "";

            // when
            boolean isValid = jwtTokenProvider.validateToken(emptyToken);

            // then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("period가 없는 토큰은 검증에 실패한다")
        void validateToken_WithNoPeriod_ReturnsFalse() {
            // given
            String noPeriodToken = "invalidtokenwithnoperiod";

            // when
            boolean isValid = jwtTokenProvider.validateToken(noPeriodToken);

            // then
            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("Authorization 헤더에서 토큰 추출 테스트")
    class ExtractTokenFromHeaderTest {

        @Test
        @DisplayName("정상적인 Bearer 토큰을 추출한다")
        void extractTokenFromHeader_WithValidBearerToken_ReturnsToken() {
            // given
            String token = jwtTokenProvider.createToken(12345L);
            String authHeader = "Bearer " + token;

            // when
            String extracted = jwtTokenProvider.extractTokenFromHeader(authHeader);

            // then
            assertThat(extracted).isEqualTo(token);
        }

        @Test
        @DisplayName("Bearer 스킴이 없는 경우 null을 반환한다")
        void extractTokenFromHeader_WithoutBearerScheme_ReturnsNull() {
            // given
            String authHeader = "InvalidScheme token123";

            // when
            String extracted = jwtTokenProvider.extractTokenFromHeader(authHeader);

            // then
            assertThat(extracted).isNull();
        }

        @Test
        @DisplayName("null 헤더는 null을 반환한다")
        void extractTokenFromHeader_WithNullHeader_ReturnsNull() {
            // given
            String authHeader = null;

            // when
            String extracted = jwtTokenProvider.extractTokenFromHeader(authHeader);

            // then
            assertThat(extracted).isNull();
        }

        @Test
        @DisplayName("'Bearer ' 뒤에 빈 문자열만 있는 경우 null을 반환한다")
        void extractTokenFromHeader_WithBearerAndEmptyToken_ReturnsNull() {
            // given
            String authHeader = "Bearer ";

            // when
            String extracted = jwtTokenProvider.extractTokenFromHeader(authHeader);

            // then
            assertThat(extracted).isNull();
        }

        @Test
        @DisplayName("'Bearer ' 뒤에 공백만 있는 경우 null을 반환한다")
        void extractTokenFromHeader_WithBearerAndWhitespace_ReturnsNull() {
            // given
            String authHeader = "Bearer    ";

            // when
            String extracted = jwtTokenProvider.extractTokenFromHeader(authHeader);

            // then
            assertThat(extracted).isNull();
        }

        @Test
        @DisplayName("토큰 앞뒤 공백은 제거된다")
        void extractTokenFromHeader_WithWhitespaceAroundToken_TrimsToken() {
            // given
            String token = jwtTokenProvider.createToken(12345L);
            String authHeader = "Bearer   " + token + "   ";

            // when
            String extracted = jwtTokenProvider.extractTokenFromHeader(authHeader);

            // then
            assertThat(extracted).isEqualTo(token);
        }
    }

    @Nested
    @DisplayName("memberId 추출 테스트")
    class ExtractMemberIdTest {

        @Test
        @DisplayName("유효한 토큰에서 memberId를 추출한다")
        void extractMemberId_WithValidToken_ReturnsMemberId() {
            // given
            Long expectedMemberId = 54321L;
            String token = jwtTokenProvider.createToken(expectedMemberId);

            // when
            Long actualMemberId = jwtTokenProvider.extractMemberId(token);

            // then
            assertThat(actualMemberId).isEqualTo(expectedMemberId);
        }

        @Test
        @DisplayName("다양한 memberId 값을 정확히 추출한다")
        void extractMemberId_WithVariousMemberIds_ReturnsCorrectIds() {
            // given
            Long[] memberIds = {1L, 100L, 99999L, Long.MAX_VALUE};

            for (Long memberId : memberIds) {
                // when
                String token = jwtTokenProvider.createToken(memberId);
                Long extracted = jwtTokenProvider.extractMemberId(token);

                // then
                assertThat(extracted).isEqualTo(memberId);
            }
        }
    }
}

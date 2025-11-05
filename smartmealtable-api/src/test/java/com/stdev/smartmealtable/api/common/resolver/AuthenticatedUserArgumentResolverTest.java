package com.stdev.smartmealtable.api.common.resolver;

import com.stdev.smartmealtable.core.auth.AuthenticatedUser;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.AuthenticationException;
import com.stdev.smartmealtable.support.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * AuthenticatedUserArgumentResolver 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticatedUserArgumentResolver 테스트")
class AuthenticatedUserArgumentResolverTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private NativeWebRequest nativeWebRequest;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private MethodParameter methodParameter;

    @InjectMocks
    private AuthenticatedUserArgumentResolver resolver;

    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NSIsImlhdCI6MTYxNjIzOTAyMn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    private static final Long MEMBER_ID = 12345L;

    @BeforeEach
    void setUp() {
        // NativeWebRequest에서 HttpServletRequest를 반환하도록 설정 (공통)
        lenient().when(nativeWebRequest.getNativeRequest(HttpServletRequest.class))
                .thenReturn(httpServletRequest);
    }

    @Nested
    @DisplayName("supportsParameter 테스트")
    class SupportsParameterTest {

        @Test
        @DisplayName("AuthenticatedUser 타입 파라미터를 지원한다")
        void supportsParameter_WithAuthenticatedUserType_ReturnsTrue() {
            // given
            given(methodParameter.getParameterType()).willReturn((Class) AuthenticatedUser.class);

            // when
            boolean supports = resolver.supportsParameter(methodParameter);

            // then
            assertThat(supports).isTrue();
        }

        @Test
        @DisplayName("다른 타입 파라미터는 지원하지 않는다")
        void supportsParameter_WithOtherType_ReturnsFalse() {
            // given
            given(methodParameter.getParameterType()).willReturn((Class) String.class);

            // when
            boolean supports = resolver.supportsParameter(methodParameter);

            // then
            assertThat(supports).isFalse();
        }
    }

    @Nested
    @DisplayName("resolveArgument - 성공 케이스")
    class ResolveArgumentSuccessTest {

        @Test
        @DisplayName("유효한 Bearer 토큰으로 AuthenticatedUser를 생성한다")
        void resolveArgument_WithValidBearerToken_ReturnsAuthenticatedUser() {
            // given
            String authHeader = "Bearer " + VALID_TOKEN;
            given(httpServletRequest.getHeader("Authorization")).willReturn(authHeader);
            given(jwtTokenProvider.extractTokenFromHeader(authHeader)).willReturn(VALID_TOKEN);
            given(jwtTokenProvider.validateToken(VALID_TOKEN)).willReturn(true);
            given(jwtTokenProvider.extractMemberId(VALID_TOKEN)).willReturn(MEMBER_ID);

            // when
            Object result = resolver.resolveArgument(methodParameter, null, nativeWebRequest, null);

            // then
            assertThat(result).isInstanceOf(AuthenticatedUser.class);
            AuthenticatedUser user = (AuthenticatedUser) result;
            assertThat(user.memberId()).isEqualTo(MEMBER_ID);
        }
    }

    @Nested
    @DisplayName("resolveArgument - 실패 케이스")
    class ResolveArgumentFailureTest {

        @Test
        @DisplayName("Authorization 헤더가 없으면 AuthenticationException을 던진다")
        void resolveArgument_WithoutAuthorizationHeader_ThrowsException() {
            // given
            given(httpServletRequest.getHeader("Authorization")).willReturn(null);

            // when & then
            assertThatThrownBy(() -> 
                resolver.resolveArgument(methodParameter, null, nativeWebRequest, null)
            )
            .isInstanceOf(AuthenticationException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_TOKEN);
        }

        @Test
        @DisplayName("Bearer 스킴이 없으면 AuthenticationException을 던진다")
        void resolveArgument_WithoutBearerScheme_ThrowsException() {
            // given
            given(httpServletRequest.getHeader("Authorization")).willReturn("InvalidScheme token123");

            // when & then
            assertThatThrownBy(() -> 
                resolver.resolveArgument(methodParameter, null, nativeWebRequest, null)
            )
            .isInstanceOf(AuthenticationException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_TOKEN);
        }

        @Test
        @DisplayName("'Bearer ' 뒤에 빈 토큰이 있으면 AuthenticationException을 던진다")
        void resolveArgument_WithEmptyToken_ThrowsException() {
            // given
            String authHeader = "Bearer ";
            given(httpServletRequest.getHeader("Authorization")).willReturn(authHeader);
            given(jwtTokenProvider.extractTokenFromHeader(authHeader)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> 
                resolver.resolveArgument(methodParameter, null, nativeWebRequest, null)
            )
            .isInstanceOf(AuthenticationException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_TOKEN);
        }

        @Test
        @DisplayName("'Bearer ' 뒤에 공백만 있으면 AuthenticationException을 던진다")
        void resolveArgument_WithWhitespaceToken_ThrowsException() {
            // given
            String authHeader = "Bearer    ";
            given(httpServletRequest.getHeader("Authorization")).willReturn(authHeader);
            given(jwtTokenProvider.extractTokenFromHeader(authHeader)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> 
                resolver.resolveArgument(methodParameter, null, nativeWebRequest, null)
            )
            .isInstanceOf(AuthenticationException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_TOKEN);
        }

        @Test
        @DisplayName("유효하지 않은 토큰이면 AuthenticationException을 던진다")
        void resolveArgument_WithInvalidToken_ThrowsException() {
            // given
            String invalidToken = "invalid.token.format";
            String authHeader = "Bearer " + invalidToken;
            given(httpServletRequest.getHeader("Authorization")).willReturn(authHeader);
            given(jwtTokenProvider.extractTokenFromHeader(authHeader)).willReturn(invalidToken);
            given(jwtTokenProvider.validateToken(invalidToken)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> 
                resolver.resolveArgument(methodParameter, null, nativeWebRequest, null)
            )
            .isInstanceOf(AuthenticationException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_TOKEN);
        }

        @Test
        @DisplayName("토큰 파싱 중 예외가 발생하면 AuthenticationException을 던진다")
        void resolveArgument_WithParsingException_ThrowsException() {
            // given
            String authHeader = "Bearer " + VALID_TOKEN;
            given(httpServletRequest.getHeader("Authorization")).willReturn(authHeader);
            given(jwtTokenProvider.extractTokenFromHeader(authHeader)).willReturn(VALID_TOKEN);
            given(jwtTokenProvider.validateToken(VALID_TOKEN)).willReturn(true);
            given(jwtTokenProvider.extractMemberId(VALID_TOKEN)).willThrow(new RuntimeException("Parsing failed"));

            // when & then
            assertThatThrownBy(() -> 
                resolver.resolveArgument(methodParameter, null, nativeWebRequest, null)
            )
            .isInstanceOf(AuthenticationException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_TOKEN);
        }

        @Test
        @DisplayName("HttpServletRequest가 null이면 AuthenticationException을 던진다")
        void resolveArgument_WithNullHttpServletRequest_ThrowsException() {
            // given
            given(nativeWebRequest.getNativeRequest(HttpServletRequest.class)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> 
                resolver.resolveArgument(methodParameter, null, nativeWebRequest, null)
            )
            .isInstanceOf(AuthenticationException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_TOKEN);
        }
    }
}

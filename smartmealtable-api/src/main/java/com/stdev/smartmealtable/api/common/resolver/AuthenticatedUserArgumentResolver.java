package com.stdev.smartmealtable.api.common.resolver;

import com.stdev.smartmealtable.core.auth.AuthenticatedUser;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.AuthenticationException;
import com.stdev.smartmealtable.support.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * JWT 토큰을 파싱하여 AuthenticatedUser 객체를 Controller 파라미터로 주입하는 ArgumentResolver
 * 
 * 사용 예시:
 * <pre>
 * {@code
 * @GetMapping("/me")
 * public ResponseEntity<ApiResponse<UserDto>> getMyProfile(AuthenticatedUser user) {
 *     // user.memberId() 사용
 * }
 * }
 * </pre>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticatedUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * AuthenticatedUser 타입의 파라미터에 대해서만 이 Resolver 적용
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(AuthenticatedUser.class);
    }

    /**
     * Authorization 헤더에서 JWT 토큰을 추출하고 파싱하여 AuthenticatedUser 생성
     */
    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            throw new AuthenticationException(ErrorType.INVALID_TOKEN);
        }

        String authorizationHeader = request.getHeader("Authorization");
        log.debug("Authorization header: {}", authorizationHeader);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new AuthenticationException(ErrorType.INVALID_TOKEN);
        }

        String token = jwtTokenProvider.extractTokenFromHeader(authorizationHeader);
        if (token == null) {
            throw new AuthenticationException(ErrorType.INVALID_TOKEN);
        }

        if (!jwtTokenProvider.validateToken(token)) {
            throw new AuthenticationException(ErrorType.INVALID_TOKEN);
        }

        try {
            Long memberId = jwtTokenProvider.extractMemberId(token);
            log.debug("Authenticated memberId: {}", memberId);
            return AuthenticatedUser.of(memberId);
        } catch (Exception e) {
            log.error("JWT 토큰 파싱 실패", e);
            throw new AuthenticationException(ErrorType.INVALID_TOKEN);
        }
    }
}

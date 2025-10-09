package com.stdev.smartmealtable.api.config.resolver;

import com.stdev.smartmealtable.core.auth.AuthUser;
import com.stdev.smartmealtable.core.auth.AuthenticatedUser;
import com.stdev.smartmealtable.support.jwt.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
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
 * @AuthUser 애노테이션이 붙은 파라미터에 JWT 토큰에서 추출한 인증 정보를 주입하는 ArgumentResolver
 * Authorization 헤더에서 Bearer 토큰을 추출하여 파싱합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthUser.class) 
                && AuthenticatedUser.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter, 
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, 
            WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        
        if (request == null) {
            log.warn("HttpServletRequest를 가져올 수 없습니다.");
            throw new IllegalStateException("HttpServletRequest를 가져올 수 없습니다.");
        }

        String authorizationHeader = request.getHeader("Authorization");
        
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            log.warn("Authorization 헤더가 없습니다. URI: {}", request.getRequestURI());
            throw new IllegalArgumentException("인증 토큰이 필요합니다.");
        }

        String token = jwtTokenProvider.extractTokenFromHeader(authorizationHeader);
        
        if (token == null) {
            log.warn("Bearer 토큰 형식이 아닙니다. Authorization: {}", authorizationHeader);
            throw new IllegalArgumentException("올바른 인증 토큰 형식이 아닙니다. (Bearer token 필요)");
        }

        try {
            Long memberId = jwtTokenProvider.extractMemberId(token);
            log.debug("JWT 토큰에서 memberId 추출 성공: {}", memberId);
            return AuthenticatedUser.of(memberId);
        } catch (JwtException e) {
            log.warn("JWT 토큰 파싱 실패: {}", e.getMessage());
            throw new IllegalArgumentException("유효하지 않은 인증 토큰입니다.", e);
        }
    }
}

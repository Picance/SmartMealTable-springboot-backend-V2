package com.stdev.smartmealtable.api.common.config;

import com.stdev.smartmealtable.api.common.resolver.AuthenticatedUserArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Spring MVC 설정
 * ArgumentResolver, Interceptor, CORS 등 웹 계층 설정
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver;

    /**
     * Custom ArgumentResolver 등록
     * JWT 토큰을 파싱하여 AuthenticatedUser 객체 주입
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authenticatedUserArgumentResolver);
    }
}

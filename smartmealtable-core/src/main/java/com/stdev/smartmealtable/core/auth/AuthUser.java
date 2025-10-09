package com.stdev.smartmealtable.core.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JWT 인증된 사용자 정보를 주입받기 위한 애노테이션
 * ArgumentResolver를 통해 Authorization 헤더에서 JWT 토큰을 파싱하여 memberId를 추출합니다.
 * 
 * 사용 예시:
 * <pre>
 * {@code
 * @PostMapping("/profile")
 * public ResponseEntity<?> updateProfile(@AuthUser Long memberId) {
 *     // memberId 사용
 * }
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthUser {
}

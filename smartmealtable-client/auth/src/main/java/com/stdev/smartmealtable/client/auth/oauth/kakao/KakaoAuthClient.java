package com.stdev.smartmealtable.client.auth.oauth.kakao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.stdev.smartmealtable.client.auth.oauth.dto.OAuthTokenResponse;
import com.stdev.smartmealtable.client.auth.oauth.dto.OAuthUserInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 카카오 OAuth 인증 클라이언트
 * 카카오 인증 서버와 통신하여 토큰 발급 및 사용자 정보를 조회합니다.
 */
@Component
@Slf4j
public class KakaoAuthClient {

    private final RestClient restClient;
    private final String clientId;
    private final String redirectUri;
    private final ObjectMapper objectMapper;

    public KakaoAuthClient(
            @Value("${oauth.kakao.client-id}") String clientId,
            @Value("${oauth.kakao.redirect-uri}") String redirectUri
    ) {
        this.restClient = RestClient.create();
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Authorization Code로 Access Token 발급
     *
     * @param authorizationCode 인가 코드
     * @return OAuth 토큰 응답
     */
    public OAuthTokenResponse getAccessToken(String authorizationCode) {
        try {
            KakaoTokenResponse response = restClient.post()
                    .uri("https://kauth.kakao.com/oauth/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(createTokenRequestBody(authorizationCode))
                    .retrieve()
                    .body(KakaoTokenResponse.class);

            if (response == null) {
                throw new IllegalStateException("카카오 토큰 응답이 null입니다.");
            }

            return new OAuthTokenResponse(
                    response.getAccessToken(),
                    response.getRefreshToken(),
                    response.getExpiresIn(),
                    response.getTokenType(),
                    response.getIdToken()
            );

        } catch (RestClientException e) {
            log.error("카카오 토큰 발급 실패: {}", e.getMessage(), e);
            throw new IllegalStateException("카카오 인증 서버와 통신 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * ID Token에서 사용자 정보 추출
     *
     * @param idToken ID 토큰
     * @return OAuth 사용자 정보
     */
    public OAuthUserInfo extractUserInfo(String idToken) {
        try {
            if (idToken == null || idToken.isEmpty()) {
                throw new IllegalArgumentException("ID Token이 null이거나 비어있습니다.");
            }

            // JWT 구조: header.payload.signature
            String[] jwtParts = idToken.split("\\.");
            if (jwtParts.length != 3) {
                throw new IllegalArgumentException("유효하지 않은 ID Token 형식입니다.");
            }

            // Payload 디코딩
            String payload = new String(Base64.getUrlDecoder().decode(jwtParts[1]), StandardCharsets.UTF_8);
            JsonNode payloadJson = objectMapper.readTree(payload);

            // 필수 필드 추출
            String providerId = payloadJson.path("sub").asText(null);
            String email = payloadJson.path("email").asText(null);
            String nickname = payloadJson.path("nickname").asText(null);
            String picture = payloadJson.path("picture").asText(null);

            if (providerId == null) {
                throw new IllegalStateException("ID Token에서 사용자 ID(sub)를 찾을 수 없습니다.");
            }

            log.info("카카오 사용자 정보 추출 성공 - providerId: {}, email: {}", providerId, email);
            return OAuthUserInfo.of(providerId, email, nickname, picture);

        } catch (Exception e) {
            log.error("카카오 ID Token 파싱 실패: {}", e.getMessage(), e);
            throw new IllegalStateException("ID Token 파싱 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 토큰 요청 Body 생성
     */
    private MultiValueMap<String, String> createTokenRequestBody(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", clientId);
        formData.add("redirect_uri", redirectUri);
        formData.add("code", code);
        return formData;
    }

    /**
     * 카카오 토큰 응답 DTO
     */
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    static class KakaoTokenResponse {
        private String tokenType;
        private String accessToken;
        private String idToken;
        private Integer expiresIn;
        private String refreshToken;
        private Integer refreshTokenExpiresIn;
    }
}

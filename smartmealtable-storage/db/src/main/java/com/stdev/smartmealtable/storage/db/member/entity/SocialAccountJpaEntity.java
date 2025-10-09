package com.stdev.smartmealtable.storage.db.member.entity;

import com.stdev.smartmealtable.domain.member.entity.SocialAccount;
import com.stdev.smartmealtable.domain.member.entity.SocialProvider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SocialAccount JPA 엔티티
 */
@Entity
@Table(name = "social_account",
        indexes = {
                @Index(name = "idx_member_authentication_id", columnList = "member_authentication_id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccountJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_account_id")
    private Long socialAccountId;

    @Column(name = "member_authentication_id", nullable = false)
    private Long memberAuthenticationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private SocialProvider provider;

    @Column(name = "provider_id", nullable = false, length = 255)
    private String providerId;

    @Column(name = "token_type", length = 20)
    private String tokenType;

    @Column(name = "access_token", nullable = false, length = 255)
    private String accessToken;

    @Column(name = "refresh_token", length = 255)
    private String refreshToken;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // Domain -> JPA Entity
    public static SocialAccountJpaEntity from(SocialAccount account) {
        SocialAccountJpaEntity entity = new SocialAccountJpaEntity();
        entity.socialAccountId = account.getSocialAccountId();
        entity.memberAuthenticationId = account.getMemberAuthenticationId();
        entity.provider = account.getProvider();
        entity.providerId = account.getProviderId();
        entity.tokenType = account.getTokenType();
        entity.accessToken = account.getAccessToken();
        entity.refreshToken = account.getRefreshToken();
        entity.expiresAt = account.getExpiresAt();
        return entity;
    }

    // JPA Entity -> Domain
    public SocialAccount toDomain() {
        return SocialAccount.reconstitute(
                this.socialAccountId,
                this.memberAuthenticationId,
                this.provider,
                this.providerId,
                this.accessToken,
                this.refreshToken,
                this.tokenType,
                this.expiresAt
        );
    }

    // Domain 변경사항 동기화
    public void syncFromDomain(SocialAccount account) {
        this.accessToken = account.getAccessToken();
        this.refreshToken = account.getRefreshToken();
        this.expiresAt = account.getExpiresAt();
    }
}

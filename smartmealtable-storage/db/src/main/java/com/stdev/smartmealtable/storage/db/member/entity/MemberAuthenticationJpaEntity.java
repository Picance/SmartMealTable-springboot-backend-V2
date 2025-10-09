package com.stdev.smartmealtable.storage.db.member.entity;

import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * MemberAuthentication JPA 엔티티
 */
@Entity
@Table(name = "member_authentication",
        indexes = {
                @Index(name = "idx_deleted_at", columnList = "deleted_at")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberAuthenticationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_authentication_id")
    private Long memberAuthenticationId;

    @Column(name = "member_id", nullable = false, unique = true)
    private Long memberId;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "hashed_password", length = 255)
    private String hashedPassword;

    @Column(name = "failure_count", nullable = false)
    private Integer failureCount = 0;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "password_expires_at")
    private LocalDateTime passwordExpiresAt;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Domain -> JPA Entity
    public static MemberAuthenticationJpaEntity from(MemberAuthentication auth) {
        MemberAuthenticationJpaEntity entity = new MemberAuthenticationJpaEntity();
        entity.memberAuthenticationId = auth.getMemberAuthenticationId();
        entity.memberId = auth.getMemberId();
        entity.email = auth.getEmail();
        entity.hashedPassword = auth.getHashedPassword();
        entity.failureCount = auth.getFailureCount();
        entity.passwordChangedAt = auth.getPasswordChangedAt();
        entity.passwordExpiresAt = auth.getPasswordExpiresAt();
        entity.name = auth.getName();
        entity.deletedAt = auth.getDeletedAt();
        return entity;
    }

    // JPA Entity -> Domain
    public MemberAuthentication toDomain() {
        return MemberAuthentication.reconstitute(
                this.memberAuthenticationId,
                this.memberId,
                this.email,
                this.hashedPassword,
                this.failureCount,
                this.passwordChangedAt,
                this.passwordExpiresAt,
                this.name,
                this.deletedAt
        );
    }

    // Domain 변경사항 동기화
    public void syncFromDomain(MemberAuthentication auth) {
        this.hashedPassword = auth.getHashedPassword();
        this.failureCount = auth.getFailureCount();
        this.passwordChangedAt = auth.getPasswordChangedAt();
        this.passwordExpiresAt = auth.getPasswordExpiresAt();
        this.deletedAt = auth.getDeletedAt();
    }
}

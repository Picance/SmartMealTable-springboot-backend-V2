package com.stdev.smartmealtable.storage.db.member.entity;

import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Member JPA 엔티티
 * Domain Entity <-> JPA Entity 변환
 */
@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation_type", nullable = false, length = 20)
    private RecommendationType recommendationType;

    // Domain -> JPA Entity
    public static MemberJpaEntity from(Member member) {
        MemberJpaEntity entity = new MemberJpaEntity();
        entity.memberId = member.getMemberId();
        entity.groupId = member.getGroupId();
        entity.nickname = member.getNickname();
        entity.recommendationType = member.getRecommendationType();
        return entity;
    }

    // JPA Entity -> Domain
    public Member toDomain() {
        return Member.reconstitute(
                this.memberId,
                this.groupId,
                this.nickname,
                this.recommendationType
        );
    }
}

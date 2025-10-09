package com.stdev.smartmealtable.storage.db.member.entity;

import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Group JPA 엔티티
 */
@Entity
@Table(name = "groups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private GroupType type;

    @Column(name = "address", length = 255)
    private String address;

    // Domain -> JPA Entity
    public static GroupJpaEntity from(Group group) {
        GroupJpaEntity entity = new GroupJpaEntity();
        entity.groupId = group.getGroupId();
        entity.name = group.getName();
        entity.type = group.getType();
        entity.address = group.getAddress();
        return entity;
    }

    // JPA Entity -> Domain
    public Group toDomain() {
        return Group.reconstitute(
                this.groupId,
                this.name,
                this.type,
                this.address
        );
    }
}

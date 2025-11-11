package com.stdev.smartmealtable.storage.db.member.entity;

import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.storage.db.common.vo.AddressEmbeddable;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Group JPA 엔티티
 */
@Entity
@Table(name = "member_group")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private GroupType type;

    @Embedded
    private AddressEmbeddable address;

    // 상세 정보 (대학 정보 등)
    @Column(name = "name_en", length = 200)
    private String nameEn;

    @Column(name = "campus_type", length = 50)
    private String campusType;

    @Column(name = "university_type", length = 50)
    private String universityType;

    @Column(name = "school_type", length = 50)
    private String schoolType;

    @Column(name = "establishment_type", length = 50)
    private String establishmentType;

    @Column(name = "region_code", length = 10)
    private String regionCode;

    @Column(name = "region_name", length = 50)
    private String regionName;

    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "phone_number", length = 50)
    private String phoneNumber;

    @Column(name = "fax_number", length = 50)
    private String faxNumber;

    @Column(name = "establishment_date", length = 20)
    private String establishmentDate;

    /**
     * Domain -> JPA Entity 변환
     */
    public static GroupJpaEntity from(Group group) {
        GroupJpaEntity entity = new GroupJpaEntity();
        entity.groupId = group.getGroupId();
        entity.name = group.getName();
        entity.type = group.getType();
        entity.address = AddressEmbeddable.from(group.getAddress());
        entity.nameEn = group.getNameEn();
        entity.campusType = group.getCampusType();
        entity.universityType = group.getUniversityType();
        entity.schoolType = group.getSchoolType();
        entity.establishmentType = group.getEstablishmentType();
        entity.regionCode = group.getRegionCode();
        entity.regionName = group.getRegionName();
        entity.postalCode = group.getPostalCode();
        entity.website = group.getWebsite();
        entity.phoneNumber = group.getPhoneNumber();
        entity.faxNumber = group.getFaxNumber();
        entity.establishmentDate = group.getEstablishmentDate();
        return entity;
    }

    /**
     * JPA Entity -> Domain 변환 (간단 버전 - 하위 호환성 유지)
     */
    public Group toDomain() {
        return Group.reconstitute(
                this.groupId,
                this.name,
                this.type,
                this.address != null ? this.address.toDomain() : null
        );
    }

    /**
     * JPA Entity -> Domain 변환 (상세 정보 포함)
     */
    public Group toDomainWithDetails() {
        return Group.reconstituteWithDetails(
                this.groupId,
                this.name,
                this.type,
                this.address != null ? this.address.toDomain() : null,
                this.nameEn,
                this.campusType,
                this.universityType,
                this.schoolType,
                this.establishmentType,
                this.regionCode,
                this.regionName,
                this.postalCode,
                this.website,
                this.phoneNumber,
                this.faxNumber,
                this.establishmentDate
        );
    }
}

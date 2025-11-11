package com.stdev.smartmealtable.domain.member.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 그룹 도메인 엔티티
 * 사용자의 소속 집단 (대학교, 회사 등)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Group {

    private Long groupId;
    private String name;
    private GroupType type;
    private String address;

    // 상세 정보 (대학 정보 등)
    private String nameEn;              // 영문명
    private String campusType;          // 본분교 구분 (본교, 분교 등)
    private String universityType;      // 대학 구분 (일반대학원, 특수대학원 등)
    private String schoolType;          // 학교 구분
    private String establishmentType;   // 설립 형태 (사립, 국립 등)
    private String regionCode;          // 시도 코드
    private String regionName;          // 시도명
    private String roadAddress;         // 도로명 주소
    private String jibunAddress;        // 지번 주소
    private String postalCode;          // 우편번호
    private String website;             // 홈페이지
    private String phoneNumber;         // 전화번호
    private String faxNumber;           // 팩스번호
    private String establishmentDate;   // 설립일자

    public static Group create(String name, GroupType type, String address) {
        Group group = new Group();
        group.name = name;
        group.type = type;
        group.address = address;
        return group;
    }

    // 재구성 팩토리 메서드 (Storage에서 사용)
    public static Group reconstitute(Long groupId, String name, GroupType type, String address) {
        Group group = new Group();
        group.groupId = groupId;
        group.name = name;
        group.type = type;
        group.address = address;
        return group;
    }

    // 상세 정보를 포함한 재구성 팩토리 메서드
    public static Group reconstituteWithDetails(
            Long groupId,
            String name,
            GroupType type,
            String address,
            String nameEn,
            String campusType,
            String universityType,
            String schoolType,
            String establishmentType,
            String regionCode,
            String regionName,
            String roadAddress,
            String jibunAddress,
            String postalCode,
            String website,
            String phoneNumber,
            String faxNumber,
            String establishmentDate) {
        Group group = new Group();
        group.groupId = groupId;
        group.name = name;
        group.type = type;
        group.address = address;
        group.nameEn = nameEn;
        group.campusType = campusType;
        group.universityType = universityType;
        group.schoolType = schoolType;
        group.establishmentType = establishmentType;
        group.regionCode = regionCode;
        group.regionName = regionName;
        group.roadAddress = roadAddress;
        group.jibunAddress = jibunAddress;
        group.postalCode = postalCode;
        group.website = website;
        group.phoneNumber = phoneNumber;
        group.faxNumber = faxNumber;
        group.establishmentDate = establishmentDate;
        return group;
    }

    // 배치 Import용 생성 팩토리 메서드
    public static Group createFromImport(
            String name,
            String nameEn,
            String campusType,
            String universityType,
            String schoolType,
            String establishmentType,
            String regionCode,
            String regionName,
            String roadAddress,
            String jibunAddress,
            String postalCode,
            String website,
            String phoneNumber,
            String faxNumber,
            String establishmentDate) {
        Group group = new Group();
        group.name = name;
        group.type = GroupType.UNIVERSITY; // 대학 데이터는 기본적으로 UNIVERSITY
        group.address = roadAddress != null && !roadAddress.isBlank() ? roadAddress : jibunAddress;
        group.nameEn = nameEn;
        group.campusType = campusType;
        group.universityType = universityType;
        group.schoolType = schoolType;
        group.establishmentType = establishmentType;
        group.regionCode = regionCode;
        group.regionName = regionName;
        group.roadAddress = roadAddress;
        group.jibunAddress = jibunAddress;
        group.postalCode = postalCode;
        group.website = website;
        group.phoneNumber = phoneNumber;
        group.faxNumber = faxNumber;
        group.establishmentDate = establishmentDate;
        return group;
    }
}

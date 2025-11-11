package com.stdev.smartmealtable.batch.crawler.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * 그룹 데이터 DTO (학교 정보 등)
 * korea-univ-data.json의 records 배열 항목을 매핑
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class GroupDataDto {

    /**
     * 학교명
     */
    @JsonProperty("학교명")
    private String schoolName;

    /**
     * 학교 영문명
     */
    @JsonProperty("학교 영문명")
    private String schoolNameEn;

    /**
     * 본분교구분명
     */
    @JsonProperty("본분교구분명")
    private String campusType;

    /**
     * 대학구분명
     */
    @JsonProperty("대학구분명")
    private String universityType;

    /**
     * 학교구분명
     */
    @JsonProperty("학교구분명")
    private String schoolType;

    /**
     * 설립형태구분명
     */
    @JsonProperty("설립형태구분명")
    private String establishmentType;

    /**
     * 시도코드
     */
    @JsonProperty("시도코드")
    private String regionCode;

    /**
     * 시도명
     */
    @JsonProperty("시도명")
    private String regionName;

    /**
     * 소재지도로명주소
     */
    @JsonProperty("소재지도로명주소")
    private String roadAddress;

    /**
     * 소재지지번주소
     */
    @JsonProperty("소재지지번주소")
    private String jibunAddress;

    /**
     * 도로명우편번호
     */
    @JsonProperty("도로명우편번호")
    private String roadPostalCode;

    /**
     * 소재지우편번호
     */
    @JsonProperty("소재지우편번호")
    private String postalCode;

    /**
     * 홈페이지주소
     */
    @JsonProperty("홈페이지주소")
    private String website;

    /**
     * 대표전화번호
     */
    @JsonProperty("대표전화번호")
    private String phoneNumber;

    /**
     * 대표팩스번호
     */
    @JsonProperty("대표팩스번호")
    private String faxNumber;

    /**
     * 설립일자
     */
    @JsonProperty("설립일자")
    private String establishmentDate;

    /**
     * 기준연도
     */
    @JsonProperty("기준연도")
    private String baseYear;

    /**
     * 데이터기준일자
     */
    @JsonProperty("데이터기준일자")
    private String dataReferenceDate;

    /**
     * 제공기관코드
     */
    @JsonProperty("제공기관코드")
    private String providerCode;

    /**
     * 제공기관명
     */
    @JsonProperty("제공기관명")
    private String providerName;
}

package com.stdev.smartmealtable.domain.common.vo;

/**
 * 주소 유형 (Value Object Enum)
 * 주소의 용도나 특성을 나타냄
 */
public enum AddressType {
    HOME("집"),           // 집, 자택
    OFFICE("회사"),       // 회사, 직장
    SCHOOL("학교"),       // 학교, 교육기관
    ETC("기타");          // 기타

    private final String description;

    AddressType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

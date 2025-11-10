package com.stdev.smartmealtable.api.group.service.dto;

import com.stdev.smartmealtable.domain.member.entity.GroupType;

import java.util.List;

/**
 * 그룹 자동완성 응답 DTO
 * 
 * @param suggestions 자동완성 제안 목록
 */
public record GroupAutocompleteResponse(
    List<GroupSuggestion> suggestions
) {
    
    /**
     * 자동완성 제안 항목
     * 
     * @param groupId 그룹 ID
     * @param name 그룹 이름
     * @param type 그룹 타입
     * @param address 주소
     */
    public record GroupSuggestion(
        Long groupId,
        String name,
        GroupType type,
        String address
    ) {}
}

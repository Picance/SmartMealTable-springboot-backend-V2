package com.stdev.smartmealtable.api.group.service;

import com.stdev.smartmealtable.api.group.service.dto.SearchGroupsServiceResponse;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 그룹 검색 Application Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SearchGroupsService {

    private final GroupRepository groupRepository;

    /**
     * 그룹 검색
     *
     * @param type 그룹 타입 (optional)
     * @param name 그룹 이름 검색어 (optional)
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 검색된 그룹 목록과 총 개수
     */
    public SearchGroupsServiceResponse searchGroups(GroupType type, String name, int page, int size) {
        log.info("그룹 검색 서비스 호출 - type: {}, name: {}, page: {}, size: {}", type, name, page, size);

        // 도메인 리포지토리에서 전체 목록 조회
        List<Group> allGroups = groupRepository.searchGroups(type, name);

        // 페이징 처리 (Application 레이어에서 처리)
        int totalCount = allGroups.size();
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalCount);

        List<Group> pagedGroups;
        if (fromIndex >= totalCount) {
            pagedGroups = List.of();
        } else {
            pagedGroups = allGroups.subList(fromIndex, toIndex);
        }

        // DTO 변환
        List<SearchGroupsServiceResponse.GroupInfo> groupInfos = pagedGroups.stream()
                .map(SearchGroupsServiceResponse.GroupInfo::from)
                .collect(Collectors.toList());

        return new SearchGroupsServiceResponse(groupInfos, totalCount);
    }
}

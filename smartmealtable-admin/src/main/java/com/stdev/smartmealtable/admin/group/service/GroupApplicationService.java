package com.stdev.smartmealtable.admin.group.service;

import com.stdev.smartmealtable.admin.group.service.dto.*;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupPageResult;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 그룹 관리 Application Service (ADMIN)
 * 
 * <p>관리자가 그룹(학교/회사) 마스터 데이터를 관리하는 서비스입니다.</p>
 * <p>트랜잭션 관리는 이 계층에서 담당합니다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GroupApplicationService {

    private final GroupRepository groupRepository;

    /**
     * 그룹 목록 조회 (페이징, 검색)
     *
     * @param request 조회 조건 (타입, 이름, 페이징 정보)
     * @return 그룹 목록 및 페이징 정보
     */
    public GroupListServiceResponse getGroups(GroupListServiceRequest request) {
        log.debug("그룹 목록 조회 요청: type={}, name={}, page={}, size={}", 
                request.type(), request.name(), request.page(), request.size());
        
        GroupPageResult pageResult = groupRepository.searchByTypeAndName(
                request.type(),
                request.name(),
                request.page(),
                request.size()
        );
        
        return GroupListServiceResponse.from(pageResult);
    }

    /**
     * 그룹 상세 조회
     *
     * @param groupId 그룹 ID
     * @return 그룹 상세 정보
     * @throws BusinessException GROUP_NOT_FOUND - 존재하지 않는 그룹
     */
    public GroupServiceResponse getGroup(Long groupId) {
        log.debug("그룹 상세 조회 요청: groupId={}", groupId);
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorType.GROUP_NOT_FOUND));
        
        return GroupServiceResponse.from(group);
    }

    /**
     * 그룹 생성
     *
     * @param request 그룹 생성 정보
     * @return 생성된 그룹 정보
     * @throws BusinessException DUPLICATE_GROUP_NAME - 중복된 그룹 이름
     */
    @Transactional
    public GroupServiceResponse createGroup(CreateGroupServiceRequest request) {
        log.info("그룹 생성 요청: name={}, type={}", request.name(), request.type());
        
        // 중복 이름 검증
        validateDuplicateName(request.name());
        
        // 도메인 엔티티 생성
        Group group = Group.create(request.name(), request.type(), request.address());
        
        // 저장
        Group savedGroup = groupRepository.save(group);
        
        log.info("그룹 생성 완료: groupId={}, name={}", savedGroup.getGroupId(), savedGroup.getName());
        return GroupServiceResponse.from(savedGroup);
    }

    /**
     * 그룹 수정
     *
     * @param groupId 그룹 ID
     * @param request 그룹 수정 정보
     * @return 수정된 그룹 정보
     * @throws BusinessException GROUP_NOT_FOUND - 존재하지 않는 그룹
     * @throws BusinessException DUPLICATE_GROUP_NAME - 중복된 그룹 이름
     */
    @Transactional
    public GroupServiceResponse updateGroup(Long groupId, UpdateGroupServiceRequest request) {
        log.info("그룹 수정 요청: groupId={}, name={}, type={}", groupId, request.name(), request.type());
        
        // 그룹 존재 확인
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorType.GROUP_NOT_FOUND));
        
        // 중복 이름 검증 (자기 자신 제외)
        validateDuplicateNameForUpdate(request.name(), groupId);
        
        // 도메인 엔티티 재생성 (불변성 유지)
        Group updatedGroup = Group.reconstitute(
                group.getGroupId(),
                request.name(),
                request.type(),
                request.address()
        );
        
        // 저장
        Group savedGroup = groupRepository.save(updatedGroup);
        
        log.info("그룹 수정 완료: groupId={}, name={}", savedGroup.getGroupId(), savedGroup.getName());
        return GroupServiceResponse.from(savedGroup);
    }

    /**
     * 그룹 삭제
     *
     * @param groupId 그룹 ID
     * @throws BusinessException GROUP_NOT_FOUND - 존재하지 않는 그룹
     * @throws BusinessException GROUP_HAS_MEMBERS - 해당 그룹에 속한 회원이 존재
     */
    @Transactional
    public void deleteGroup(Long groupId) {
        log.info("그룹 삭제 요청: groupId={}", groupId);
        
        // 그룹 존재 확인
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorType.GROUP_NOT_FOUND));
        
        // 회원 존재 확인
        if (groupRepository.hasMembers(groupId)) {
            throw new BusinessException(ErrorType.GROUP_HAS_MEMBERS);
        }
        
        // 물리적 삭제
        groupRepository.deleteById(groupId);
        
        log.info("그룹 삭제 완료: groupId={}, name={}", groupId, group.getName());
    }

    // ==================== Private Helper Methods ====================

    /**
     * 그룹 이름 중복 검증 (생성 시)
     */
    private void validateDuplicateName(String name) {
        if (groupRepository.existsByName(name)) {
            throw new BusinessException(ErrorType.DUPLICATE_GROUP_NAME);
        }
    }

    /**
     * 그룹 이름 중복 검증 (수정 시 - 자기 자신 제외)
     */
    private void validateDuplicateNameForUpdate(String name, Long groupId) {
        if (groupRepository.existsByNameAndIdNot(name, groupId)) {
            throw new BusinessException(ErrorType.DUPLICATE_GROUP_NAME);
        }
    }
}

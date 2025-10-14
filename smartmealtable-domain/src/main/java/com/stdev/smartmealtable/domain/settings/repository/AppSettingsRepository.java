package com.stdev.smartmealtable.domain.settings.repository;

import com.stdev.smartmealtable.domain.settings.entity.AppSettings;

import java.util.Optional;

/**
 * 앱 설정 Repository 인터페이스
 */
public interface AppSettingsRepository {
    
    /**
     * 앱 설정 저장
     * 
     * @param appSettings 앱 설정
     * @return 저장된 앱 설정
     */
    AppSettings save(AppSettings appSettings);
    
    /**
     * 회원 ID로 앱 설정 조회
     * 
     * @param memberId 회원 ID
     * @return 앱 설정 (Optional)
     */
    Optional<AppSettings> findByMemberId(Long memberId);
    
    /**
     * 앱 설정 삭제
     * 
     * @param appSettings 앱 설정
     */
    void delete(AppSettings appSettings);
}

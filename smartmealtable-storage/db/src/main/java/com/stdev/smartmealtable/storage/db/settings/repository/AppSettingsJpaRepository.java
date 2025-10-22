package com.stdev.smartmealtable.storage.db.settings.repository;

import com.stdev.smartmealtable.storage.db.settings.entity.AppSettingsJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 앱 설정 JPA Repository
 */
public interface AppSettingsJpaRepository extends JpaRepository<AppSettingsJpaEntity, Long> {
    
    /**
     * 회원 ID로 앱 설정 조회
     * 
     * @param memberId 회원 ID
     * @return 앱 설정 JPA Entity (Optional)
     */
    @Query("SELECT a FROM AppSettingsJpaEntity a WHERE a.memberId = :memberId")
    Optional<AppSettingsJpaEntity> findByMemberId(@Param("memberId") Long memberId);
}

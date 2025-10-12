package com.stdev.smartmealtable.storage.db.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA 설정
 * - JPA Auditing 활성화 (BaseTimeEntity의 created_at, updated_at 자동 관리)
 * - JPA Repository 스캔 경로 설정
 * - QueryDSL JPAQueryFactory Bean 등록
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.stdev.smartmealtable.storage.db")
public class JpaConfig {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * QueryDSL JPAQueryFactory Bean 등록
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}

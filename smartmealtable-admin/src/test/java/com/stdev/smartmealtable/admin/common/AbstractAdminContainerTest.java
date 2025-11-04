package com.stdev.smartmealtable.admin.common;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

/**
 * ADMIN 모듈 통합 테스트 Base 클래스
 * 
 * <p>TestContainers를 사용하여 MySQL 컨테이너를 공유합니다.</p>
 * <p>모든 통합 테스트가 동일한 DB 인스턴스를 사용하여 리소스 효율성을 높입니다.</p>
 */
public abstract class AbstractAdminContainerTest {

    /**
     * 모든 테스트에서 공유하는 단일 MySQL 컨테이너
     */
    @ServiceConnection
    protected static final MySQLContainer<?> MYSQL_CONTAINER;

    static {
        MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("smartmealtable_admin_test")
                .withUsername("admin_test")
                .withPassword("admin_test")
                .withReuse(true);  // 컨테이너 재사용
        
        MYSQL_CONTAINER.start();
    }

    /**
     * Spring Boot DataSource 동적 구성
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
        
        // Spring AI 자동 설정 비활성화
        registry.add("spring.ai.vertex.ai.gemini.enabled", () -> "false");
    }
}

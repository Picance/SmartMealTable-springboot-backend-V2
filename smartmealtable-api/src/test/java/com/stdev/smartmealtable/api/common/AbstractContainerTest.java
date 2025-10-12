package com.stdev.smartmealtable.api.common;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

/**
 * 모든 통합 테스트가 단일 MySQL 컨테이너를 공유하도록 하는 Base 클래스
 * 
 * TestContainers 병렬 실행 시 리소스 경쟁 문제를 해결합니다.
 * - 각 테스트 클래스가 개별 컨테이너를 생성하는 대신 하나의 컨테이너를 공유
 * - static 필드로 싱글톤 패턴 적용
 * - 모든 테스트가 동일한 DB 인스턴스 사용
 */
public abstract class AbstractContainerTest {

    /**
     * 모든 테스트에서 공유하는 단일 MySQL 컨테이너
     * - static: JVM당 한 번만 생성
     * - 첫 번째 테스트 실행 시 시작되고, 모든 테스트 종료 후 정리
     */
    @ServiceConnection
    protected static final MySQLContainer<?> MYSQL_CONTAINER;

    static {
        MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("smartmealtable_test")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);  // 컨테이너 재사용 설정
        
        MYSQL_CONTAINER.start();
    }

    /**
     * Spring Boot의 DataSource 설정을 컨테이너 정보로 동적 구성
     * Spring AI ChatModel을 자동 설정에서 제외하여 테스트 환경에서 불필요한 의존성 방지
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
        
        // Spring AI 자동 설정 비활성화 (테스트에서 불필요)
        registry.add("spring.ai.vertex.ai.gemini.enabled", () -> "false");
    }
}

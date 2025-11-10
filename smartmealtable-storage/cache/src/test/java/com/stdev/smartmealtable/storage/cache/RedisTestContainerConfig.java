package com.stdev.smartmealtable.storage.cache;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Redis Testcontainer 설정
 * 
 * <p>통합 테스트에서 사용할 Redis 컨테이너를 설정합니다.</p>
 * <p>싱글톤 패턴으로 컨테이너를 재사용하여 테스트 속도를 최적화합니다.</p>
 */
@TestConfiguration
public class RedisTestContainerConfig {

    private static final String REDIS_DOCKER_IMAGE = "redis:7-alpine";
    private static final int REDIS_PORT = 6379;

    /**
     * 싱글톤 Redis 컨테이너
     * 모든 테스트에서 동일한 컨테이너 인스턴스를 재사용합니다.
     */
    private static GenericContainer<?> redisContainer;

    static {
        redisContainer = new GenericContainer<>(DockerImageName.parse(REDIS_DOCKER_IMAGE))
                .withExposedPorts(REDIS_PORT)
                .withReuse(true);  // 컨테이너 재사용으로 테스트 속도 향상
        redisContainer.start();
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(
                redisContainer.getHost(),
                redisContainer.getMappedPort(REDIS_PORT)
        );
        factory.afterPropertiesSet();
        return factory;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}

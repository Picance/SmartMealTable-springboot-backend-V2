# 빌드 스테이지 (선택사항: 멀티 스테이지 빌드 사용 가능)
FROM eclipse-temurin:21-jre-jammy as base

# 실행 스테이지
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# 헬스체크 추가
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD java -cp . HealthCheck || exit 1

# 보안: 루트 사용자가 아닌 별도 사용자로 실행
RUN useradd -m -u 1000 appuser
USER appuser

# 명시적으로 모듈별 JAR 파일을 지정해서 복사
# 예) smartmealtable-api, smartmealtable-admin, smartmealtable-scheduler 등
# 이 부분은 각 모듈별 Dockerfile에서 오버라이드됨
COPY --chown=appuser:appuser build/libs/*.jar app.jar

# JVM 메모리 설정 (t3.micro 환경에 맞춤)
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]

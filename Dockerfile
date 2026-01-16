# Stage 1: Build
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Gradle 설정 파일만 먼저 복사하여 종속성 캐싱 최적화
COPY build.gradle settings.gradle ./
# 소스 코드 복사
COPY src ./src

# Gradle 내장 명령어로 빌드 (반드시 clean을 수행하여 이전 결과물 제거)
RUN gradle clean bootJar --no-daemon

# Stage 2: Run
FROM amazoncorretto:17-al2023-headless
WORKDIR /app

# 타임존 설정
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 빌드된 JAR 파일 복사 (패턴 매칭으로 정확한 파일 선택)
COPY --from=build /app/build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Seoul", "app.jar"]

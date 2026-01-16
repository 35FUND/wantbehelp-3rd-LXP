# Stage 1: Build
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Gradle 설정 파일만 먼저 복사하여 종속성 캐싱 최적화
COPY build.gradle settings.gradle ./
# 소스 코드 복사
COPY src ./src

# Gradle 내장 명령어로 빌드
RUN gradle bootJar --no-daemon

# Stage 2: Run
# openjdk 이미지는 deprecated 되었으므로 유지보수가 잘 되는 eclipse-temurin을 사용합니다.
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 타임존 설정
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Seoul", "app.jar"]

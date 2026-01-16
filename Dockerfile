# Stage 1: Build
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# 설정 파일 및 소스 복사
COPY build.gradle settings.gradle ./
COPY src ./src

# Gradle 빌드 (clean 필수, bootJar 실행)
RUN gradle clean bootJar --no-daemon

# Stage 2: Run
FROM amazoncorretto:17-al2023-headless
WORKDIR /app

# 타임존 설정
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 빌드 단계에서 생성된 'app.jar'를 정확하게 복사
COPY --from=build /app/build/libs/app.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

# 메인 클래스를 명시적으로 지정하여 실행 (매니페스트 오류 방지)
ENTRYPOINT ["java", "-cp", "app.jar", "org.springframework.boot.loader.launch.JarLauncher", "com.example.shortudy.ShortsApplication"]

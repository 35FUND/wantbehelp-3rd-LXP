# Stage 1: Build
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . .
RUN ./gradlew clean bootJar --no-daemon

# Stage 2: Run
FROM openjdk:17-jdk-slim
WORKDIR /app

# 타임존 설정 (한국 시간)
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 환경 변수 기본값 설정
ENV SPRING_PROFILES_ACTIVE=prod
ENV DB_URL=jdbc:mysql://db:3306/shortudy?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
ENV DB_USERNAME=root
ENV DB_PASSWORD=password
ENV REDIS_HOST=redis
ENV REDIS_PORT=6379

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Seoul", "app.jar"]

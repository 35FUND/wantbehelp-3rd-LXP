# 1. Build Stage
FROM gradle:8.5-jdk17-alpine AS builder
WORKDIR /app

# Gradle 캐싱
COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사 및 빌드
COPY src ./src
RUN gradle clean bootJar --no-daemon -x test

# 2. Run Stage
FROM amazoncorretto:17
WORKDIR /app

# [수정] libs 폴더 전체를 복사 (파일 지정 에러 방지)
COPY --from=builder /app/build/libs/ /app/libs/

ENV TZ=Asia/Seoul

# [수정] libs 폴더 내에서 실행 가능한 jar(plain 제외) 하나를 찾아 실행
ENTRYPOINT ["sh", "-c", "java -jar /app/libs/$(ls /app/libs/ | grep SNAPSHOT | grep -v plain | head -n 1)"]

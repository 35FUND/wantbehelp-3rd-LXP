# 📚 Shortudy - 숏폼 학습 플랫폼

> 짧은 영상으로 빠르게 배우는 학습 플랫폼

## 🚀 프로젝트 소개

Shortudy는 숏폼(Short-form) 형식의 학습 영상을 공유하고 시청할 수 있는 플랫폼입니다.
1분 내외의 짧은 영상으로 프로그래밍, 디자인, 마케팅 등 다양한 분야를 학습할 수 있습니다.

## 🛠 기술 스택

### Backend
- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security** - JWT 인증
- **Spring Data JPA** - ORM
- **MySQL** - 데이터베이스
- **Swagger (SpringDoc)** - API 문서화

### Frontend
- **HTML/CSS/JavaScript** (Vanilla)
- 다크 모드 UI

## 📁 프로젝트 구조

```
src/main/java/com/example/shortudy/
├── domain/
│   ├── category/       # 카테고리 도메인
│   ├── shorts/         # 숏폼 도메인
│   ├── tag/            # 태그 도메인
│   ├── tagging/        # 숏폼-태그 연결
│   └── user/           # 사용자 도메인
├── global/
│   ├── common/         # 공통 클래스
│   ├── config/         # 설정 (Security, Swagger)
│   ├── controller/     # 파일 업로드 등
│   ├── entity/         # 기본 엔티티
│   ├── error/          # 예외 처리
│   ├── jwt/            # JWT 인증
│   └── service/        # 파일 저장 서비스
└── ShortsApplication.java
```

## ⚙️ 실행 방법

### 1. 요구사항
- JDK 17 이상
- MySQL 8.0 이상

### 2. 데이터베이스 설정
```sql
CREATE DATABASE shortudy;
```

### 3. application.properties 설정
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/shortudy
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 4. 실행
```bash
# Windows
gradlew.bat bootRun

# Mac/Linux
./gradlew bootRun
```

### 5. 접속
- **웹 페이지**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html

## 📋 주요 기능

### 사용자
- [x] 회원가입 / 로그인 (JWT)
- [x] 내 정보 조회
- [x] 내 숏폼 목록 조회

### 숏폼
- [x] 영상 업로드
- [x] 영상 목록 조회 (페이징)
- [x] 영상 상세 조회
- [x] 영상 수정 / 삭제

### 카테고리 & 태그
- [x] 카테고리 CRUD
- [x] 태그 CRUD
- [x] 숏폼에 태그 연결

### 파일
- [x] 비디오 업로드
- [x] 썸네일 업로드

## 📄 API 문서

자세한 API 명세는 [API-DOCS.md](./API-DOCS.md)를 참조하세요.

## 🌐 웹 페이지

| 페이지 | 경로 | 설명 |
|--------|------|------|
| 메인 | `/index.html` | 숏폼 목록 |
| 로그인 | `/login.html` | 로그인 |
| 회원가입 | `/signup.html` | 회원가입 |
| 영상 시청 | `/watch.html?id={id}` | 영상 재생 |
| 업로드 | `/upload.html` | 영상 업로드 |
| 마이페이지 | `/mypage.html` | 내 정보 |
| 내 영상 | `/my-shorts.html` | 내 영상 관리 |
| 인기 | `/trending.html` | 인기 영상 |
| 최신 | `/latest.html` | 최신 영상 |
| 검색 | `/search.html?q={검색어}` | 검색 결과 |
| 관리자 | `/admin.html` | 카테고리/태그 관리 |

## 👥 팀원

- Backend 개발

## 📝 라이선스

MIT License


# 📚 Shortudy API 명세서

> **Base URL**: `http://localhost:8080`  
> **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`  
> **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

---

## 🔐 인증 방법

### JWT 토큰 인증
1. 회원가입 (`POST /api/v1/auth/signup`)
2. 로그인 (`POST /api/v1/auth/login`) → `accessToken` 발급
3. API 요청 시 헤더에 토큰 추가:
```
Authorization: Bearer {accessToken}
```

---

## 📁 API 목록

### 🔐 Auth (인증)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/v1/auth/signup` | 회원가입 | ❌ |
| POST | `/api/v1/auth/login` | 로그인 | ❌ |
| POST | `/api/v1/auth/logout` | 로그아웃 | ✅ |
| GET | `/api/v1/auth/stat` | 로그인 상태 조회 | ✅ |
| POST | `/api/v1/auth/refresh` | 토큰 재발급 | ❌ |

#### POST /api/v1/auth/signup
**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "name": "홍길동"
}
```

#### POST /api/v1/auth/login
**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```
**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

#### POST /api/v1/auth/refresh
**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

---

### 👤 Users (사용자)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/v1/users/me` | 내 정보 조회 | ✅ |
| GET | `/api/v1/users/me/shorts` | 내 숏폼 목록 | ✅ |
| GET | `/api/v1/users` | 전체 사용자 조회 | ✅ |
| GET | `/api/v1/users/{userId}` | 사용자 조회 | ✅ |
| DELETE | `/api/v1/users/{userId}` | 사용자 삭제 | ✅ |

#### GET /api/v1/users/me
**Response:**
```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "nickname": "길동이"
}
```

---

### 🎬 Shorts (숏폼)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/v1/shorts` | 숏폼 업로드 | ✅ |
| GET | `/api/v1/shorts` | 숏폼 목록 조회 | ❌ |
| GET | `/api/v1/shorts/{shortId}` | 숏폼 상세 조회 | ❌ |
| PATCH | `/api/v1/shorts/{shortId}` | 숏폼 수정 | ✅ |
| DELETE | `/api/v1/shorts/{shortId}` | 숏폼 삭제 | ✅ |

#### POST /api/v1/shorts
**Request Body:**
```json
{
  "userId": 1,
  "categoryId": 1,
  "title": "Spring Boot 시작하기",
  "description": "Spring Boot 입문자를 위한 가이드입니다.",
  "videoUrl": "/uploads/videos/abc123.mp4",
  "thumbnailUrl": "/uploads/thumbnails/abc123.jpg",
  "durationSec": 58,
  "tagNames": ["Java", "Spring", "백엔드"]
}
```
**Response:**
```json
{
  "success": true,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "shortsId": 1,
    "uploaderId": 1,
    "uploaderNickname": "홍길동",
    "categoryId": 1,
    "categoryName": "프로그래밍",
    "title": "Spring Boot 시작하기",
    "description": "Spring Boot 입문자를 위한 가이드입니다.",
    "videoUrl": "/uploads/videos/abc123.mp4",
    "thumbnailUrl": "/uploads/thumbnails/abc123.jpg",
    "durationSec": 58,
    "status": "PUBLIC",
    "createdAt": "2025-01-09T10:30:00",
    "tagNames": ["Java", "Spring", "백엔드"]
  }
}
```

#### GET /api/v1/shorts?page=0&size=8
**Response:**
```json
{
  "success": true,
  "data": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 13,
    "size": 8,
    "number": 0
  }
}
```

#### PATCH /api/v1/shorts/{shortId}
**Request Body:**
```json
{
  "title": "수정된 제목",
  "description": "수정된 설명",
  "categoryId": 2,
  "status": "PRIVATE",
  "tagNames": ["React", "프론트엔드"]
}
```

---

### 📁 Categories (카테고리)

| Method | Endpoint | 설명 | 인증 | 권한 |
|--------|----------|------|------|------|
| POST | `/api/v1/categories` | 카테고리 생성 | ✅ | ADMIN |
| GET | `/api/v1/categories` | 전체 카테고리 조회 | ❌ | - |
| GET | `/api/v1/categories/{categoryId}` | 카테고리 조회 | ❌ | - |
| PUT | `/api/v1/categories/{categoryId}` | 카테고리 수정 | ✅ | ADMIN |
| DELETE | `/api/v1/categories/{categoryId}` | 카테고리 삭제 | ✅ | ADMIN |

#### POST /api/v1/categories
**Request Body:**
```json
{
  "name": "프로그래밍"
}
```
**Response:**
```json
{
  "id": 1,
  "name": "프로그래밍"
}
```

---

### 🏷️ Tags (태그)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/v1/tags` | 태그 생성 | ✅ |
| GET | `/api/v1/tags` | 전체 태그 조회 | ❌ |
| GET | `/api/v1/tags/{tagId}` | 태그 조회 | ❌ |
| PUT | `/api/v1/tags/{tagId}` | 태그 수정 | ✅ |
| DELETE | `/api/v1/tags/{tagId}` | 태그 삭제 | ✅ |

#### POST /api/v1/tags
**Request Body:**
```json
{
  "name": "Java"
}
```

---

### 📤 Files (파일 업로드)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/v1/files/videos` | 비디오 업로드 | ✅ |
| POST | `/api/v1/files/thumbnails` | 썸네일 업로드 | ✅ |

#### POST /api/v1/files/videos
**Request:** `multipart/form-data`
```
file: (비디오 파일 - mp4, webm, mov)
```
**Response:**
```json
{
  "url": "/uploads/videos/abc123.mp4",
  "message": "비디오 업로드 성공"
}
```

#### POST /api/v1/files/thumbnails
**Request:** `multipart/form-data`
```
file: (이미지 파일 - jpg, png, webp)
```
**Response:**
```json
{
  "url": "/uploads/thumbnails/abc123.jpg",
  "message": "썸네일 업로드 성공"
}
```

---

## 🔄 응답 형식

### 성공 응답
```json
{
  "success": true,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": { ... }
}
```

### 에러 응답
```json
{
  "success": false,
  "message": "에러 메시지",
  "data": null
}
```

---

## 📋 상태 코드

| 코드 | 설명 |
|------|------|
| 200 | OK - 성공 |
| 201 | Created - 생성 성공 |
| 204 | No Content - 삭제 성공 |
| 400 | Bad Request - 잘못된 요청 |
| 401 | Unauthorized - 인증 필요 |
| 403 | Forbidden - 권한 없음 |
| 404 | Not Found - 리소스 없음 |
| 409 | Conflict - 중복 |
| 500 | Internal Server Error - 서버 오류 |

---

## 🚀 빠른 시작 (Postman/Frontend)

### 1. 회원가입
```bash
curl -X POST http://localhost:8080/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"test1234","name":"테스터"}'
```

### 2. 로그인
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"test1234"}'
```

### 3. 인증된 요청
```bash
curl -X GET http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer {accessToken}"
```


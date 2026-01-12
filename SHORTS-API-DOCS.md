# 📚 Shorts 도메인 API 명세서

> **Base URL**: `http://localhost:8080`

---

## 🔗 목차

- [쇼츠 관리 (ShortsController)](#-쇼츠-관리-shortscontroller)
- [쇼츠 추천 (RecommendationController)](#-쇼츠-추천-recommendationcontroller)

---

## 🎬 쇼츠 관리 (ShortsController)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/v1/shorts` | 쇼츠 업로드 | ✅ |
| GET | `/api/v1/shorts` | 쇼츠 목록 조회 (페이징) | ❌ |
| GET | `/api/v1/shorts/{shortId}` | 쇼츠 상세 조회 (페이징) | ❌ |
| PATCH | `/api/v1/shorts/{shortId}` | 쇼츠 수정 | ✅ |
| DELETE | `/api/v1/shorts/{shortId}` | 쇼츠 삭제 | ✅ |

---

### 1. 쇼츠 업로드

**Endpoint**: `POST /api/v1/shorts`

**Request Headers**:
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request Body**:
```json
{
  "userId": 1,
  "categoryId": 1,
  "title": "Spring Boot 시작하기",
  "description": "Spring Boot 입문자를 위한 가이드입니다.",
  "videoUrl": "/uploads/videos/abc123.mp4",
  "thumbnailUrl": "/uploads/thumbnails/abc123.jpg",
  "durationSec": 58,
  "keywordNames": ["Java", "Spring", "백엔드"]
}
```

**Field Validation**:
| 필드 | 타입 | 필수 | 제약조건 | 설명 |
|------|------|------|----------|------|
| userId | Long | ✅ | - | 사용자 ID |
| categoryId | Long | ✅ | - | 카테고리 ID |
| title | String | ✅ | 길이 100자 이내 | 쇼츠 제목 |
| description | String | ❌ | - | 쇼츠 설명 |
| videoUrl | String | ✅ | - | 영상 URL |
| thumbnailUrl | String | ❌ | - | 썸네일 URL |
| durationSec | Integer | ❌ | 1 이상 | 영상 길이 (초) |
| keywordNames | List\<String\> | ❌ | - | 키워드 이름 목록 |

**Response** (201 Created):
```json
{
  "success": true,
  "code": "Success",
  "data": {
    "shortsId": "1",
    "title": "Spring Boot 시작하기",
    "description": "Spring Boot 입문자를 위한 가이드입니다.",
    "videoUrl": "/uploads/videos/abc123.mp4",
    "thumbnailUrl": "/uploads/thumbnails/abc123.jpg",
    "durationSec": 58,
    "status": "PUBLISHED",
    "uploader": {
      "userId": 1,
      "nickname": "길동이",
      "profileUrl": "/uploads/profiles/user1.jpg"
    },
    "category": {
      "categoryId": 1,
      "name": "프로그래밍"
    }
  }
}
```

---

### 2. 쇼츠 목록 조회

**Endpoint**: `GET /api/v1/shorts?page=0&size=8&sort=id,asc`

**Query Parameters**:
| 파라미터 | 타입 | 기본값 | 설명 |
|----------|------|--------|------|
| page | Integer | 0 | 페이지 번호 (0부터 시작) |
| size | Integer | 8 | 페이지 크기 |
| sort | String | id,asc | 정렬 조건 (필드,방향) |

**Response** (200 OK):
```json
{
  "success": true,
  "code": "Success",
  "data": {
    "content": [
      {
        "shortsId": "1",
        "title": "Spring Boot 시작하기",
        "description": "Spring Boot 입문자를 위한 가이드입니다.",
        "videoUrl": "/uploads/videos/abc123.mp4",
        "thumbnailUrl": "/uploads/thumbnails/abc123.jpg",
        "durationSec": 58,
        "status": "PUBLISHED",
        "uploader": {
          "userId": 1,
          "nickname": "길동이",
          "profileUrl": "/uploads/profiles/user1.jpg"
        },
        "category": {
          "categoryId": 1,
          "name": "프로그래밍"
        }
      }
    ],
    "totalElements": 100,
    "totalPages": 13,
    "size": 8,
    "number": 0
  }
}
```

---

### 3. 쇼츠 상세 조회 (페이징)

**Endpoint**: `GET /api/v1/shorts/{shortId}?page=0&size=20&sort=id,desc`

**Path Parameters**:
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| shortId | Long | 쇼츠 ID |

**Query Parameters**:
| 파라미터 | 타입 | 기본값 | 설명 |
|----------|------|--------|------|
| page | Integer | 0 | 페이지 번호 (0부터 시작) |
| size | Integer | 20 | 페이지 크기 |
| sort | String | id,desc | 정렬 조건 (필드,방향) |

**Response** (200 OK):
```json
{
  "success": true,
  "code": "Success",
  "data": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 5,
    "size": 20,
    "number": 0
  }
}
```

---

### 4. 쇼츠 수정

**Endpoint**: `PATCH /api/v1/shorts/{shortId}`

**Request Headers**:
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Path Parameters**:
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| shortId | Long | 쇼츠 ID |

**Request Body**:
```json
{
  "title": "수정된 제목",
  "description": "수정된 설명",
  "categoryId": 2,
  "thumbnailUrl": "/uploads/thumbnails/new.jpg",
  "durationSec": 60,
  "status": "PRIVATE",
  "tagNames": ["React", "프론트엔드"]
}
```

**Field Validation**:
| 필드 | 타입 | 필수 | 제약조건 | 설명 |
|------|------|------|----------|------|
| title | String | ❌ | 길이 100자 이내 | 쇼츠 제목 |
| description | String | ❌ | - | 쇼츠 설명 |
| categoryId | Long | ❌ | - | 카테고리 ID |
| thumbnailUrl | String | ❌ | - | 썸네일 URL |
| durationSec | Integer | ❌ | 1 이상 | 영상 길이 (초) |
| status | ShortsStatus | ❌ | - | 쇼츠 상태 (PUBLISHED, PRIVATE) |
| tagNames | List\<String\> | ❌ | - | 키워드 이름 목록 |

**Response** (200 OK):
```json
{
  "success": true,
  "code": "Success",
  "data": {
    "shortsId": "1",
    "title": "수정된 제목",
    "description": "수정된 설명",
    "videoUrl": "/uploads/videos/abc123.mp4",
    "thumbnailUrl": "/uploads/thumbnails/new.jpg",
    "durationSec": 60,
    "status": "PRIVATE",
    "uploader": {
      "userId": 1,
      "nickname": "길동이",
      "profileUrl": "/uploads/profiles/user1.jpg"
    },
    "category": {
      "categoryId": 2,
      "name": "디자인"
    }
  }
}
```

---

### 5. 쇼츠 삭제

**Endpoint**: `DELETE /api/v1/shorts/{shortId}`

**Request Headers**:
```
Authorization: Bearer {accessToken}
```

**Path Parameters**:
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| shortId | Long | 쇼츠 ID |

**Response** (204 No Content):
```json
{
  "success": true,
  "code": "Success",
  "data": null
}
```

---

## 🎯 쇼츠 추천 (RecommendationController)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/v1/recommendations/shorts/{shortsId}` | 쇼츠 기반 추천 | ❌ |

---

### 1. 쇼츠 기반 추천

**Endpoint**: `GET /api/v1/recommendations/shorts/{shortsId}?limit=10`

**Path Parameters**:
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| shortsId | String | 기준 쇼츠 ID |

**Query Parameters**:
| 파라미터 | 타입 | 기본값 | 설명 |
|----------|------|--------|------|
| limit | Integer | 10 | 반환할 추천 개수 (1~20) |

**Response** (200 OK):
```json
{
  "success": true,
  "code": "Success",
  "data": [
    {
      "shortsId": "5",
      "title": "Spring Boot 입문 가이드",
      "thumbnailUrl": "/uploads/thumbnails/xyz.jpg",
      "similarity": 0.667
    },
    {
      "shortsId": "12",
      "title": "Java 기초 강의",
      "thumbnailUrl": "/uploads/thumbnails/abc.jpg",
      "similarity": 0.5
    }
  ]
}
```

---

## 📋 ShortsStatus Enum

| 값 | 설명 |
|----|------|
| PUBLISHED | 게시됨 |
| PRIVATE | 비공개 |

---

## 🔄 공통 응답 포맷

### 성공 응답
```json
{
  "success": true,
  "code": "Success",
  "data": { ... }
}
```

### 에러 응답
```json
{
  "success": false,
  "code": "ERROR_CODE",
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
| 500 | Internal Server Error - 서버 오류 |

---

## 🚀 빠른 시작 (cURL)

### 쇼츠 업로드
```bash
curl -X POST http://localhost:8080/api/v1/shorts \
  -H "Authorization: Bearer {accessToken}" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "categoryId": 1,
    "title": "Spring Boot 시작하기",
    "description": "Spring Boot 입문자를 위한 가이드입니다.",
    "videoUrl": "/uploads/videos/abc123.mp4",
    "thumbnailUrl": "/uploads/thumbnails/abc123.jpg",
    "durationSec": 58,
    "keywordNames": ["Java", "Spring"]
  }'
```

### 쇼츠 목록 조회
```bash
curl -X GET "http://localhost:8080/api/v1/shorts?page=0&size=8"
```

### 쇼츠 기반 추천
```bash
curl -X GET "http://localhost:8080/api/v1/recommendations/shorts/1?limit=10"
```

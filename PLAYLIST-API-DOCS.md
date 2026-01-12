# 📚 Playlist 도메인 API 명세서

> **Base URL**: `http://localhost:8080`

---

## 🔗 목차

- [플레이리스트 관리 (PlaylistController)](#-플레이리스트-관리-playlistcontroller)

---

## 📋 플레이리스트 관리 (PlaylistController)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/v1/playlists` | 공개 플레이리스트 목록 (메인) | ❌ |
| GET | `/api/v1/playlists/{playlistId}` | 플레이리스트 상세 조회 | ❌ |
| GET | `/api/v1/playlists/users/{userId}` | 사용자의 공개 플레이리스트 | ❌ |
| GET | `/api/v1/playlists/me` | 내 전체 플레이리스트 | ✅ |
| POST | `/api/v1/playlists` | 플레이리스트 생성 | ✅ |
| PATCH | `/api/v1/playlists/{playlistId}` | 플레이리스트 수정 | ✅ |
| DELETE | `/api/v1/playlists/{playlistId}` | 플레이리스트 삭제 | ✅ |
| POST | `/api/v1/playlists/{playlistId}/shorts/{shortsId}` | 쇼츠 추가 | ✅ |
| DELETE | `/api/v1/playlists/{playlistId}/shorts/{shortsId}` | 쇼츠 제거 | ✅ |
| PATCH | `/api/v1/playlists/{playlistId}/shorts/reorder` | 쇼츠 순서 변경 | ✅ |

---

### 1. 공개 플레이리스트 목록 (메인)

**Endpoint**: `GET /api/v1/playlists?page=0&size=10`

**Query Parameters**:
| 파라미터 | 타입 | 기본값 | 설명 |
|----------|------|--------|------|
| page | Integer | 0 | 페이지 번호 (0부터 시작) |
| size | Integer | 10 | 페이지 크기 |
| sort | String | - | 정렬 조건 (기본 랜덤) |

**Response** (200 OK):
```json
{
  "success": true,
  "code": "Success",
  "data": {
    "content": [
      {
        "playlistId": "1",
        "name": "내가 좋아하는 코딩 강의",
        "description": "개발자가 꼭 봐야 할 영상들",
        "thumbnailUrl": "/uploads/thumbnails/playlist1.jpg",
        "isPublic": true,
        "owner": {
          "userId": 1,
          "nickname": "길동이",
          "profileUrl": "/uploads/profiles/user1.jpg"
        },
        "shortsCount": 15
      }
    ],
    "totalElements": 100,
    "totalPages": 10,
    "page": 0
  }
}
```

---

### 2. 플레이리스트 상세 조회

**Endpoint**: `GET /api/v1/playlists/{playlistId}`

**Path Parameters**:
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| playlistId | String | 플레이리스트 ID |

**Response** (200 OK):
```json
{
  "success": true,
  "code": "Success",
  "data": {
    "playlistId": "1",
    "name": "내가 좋아하는 코딩 강의",
    "description": "개발자가 꼭 봐야 할 영상들",
    "thumbnailUrl": "/uploads/thumbnails/playlist1.jpg",
    "isPublic": true,
    "owner": {
      "userId": 1,
      "nickname": "길동이",
      "profileUrl": "/uploads/profiles/user1.jpg"
    },
    "shorts": [
      {
        "shortsId": "5",
        "title": "Spring Boot 입문 가이드",
        "thumbnailUrl": "/uploads/thumbnails/xyz.jpg",
        "orderNumber": 1
      },
      {
        "shortsId": "12",
        "title": "Java 기초 강의",
        "thumbnailUrl": "/uploads/thumbnails/abc.jpg",
        "orderNumber": 2
      }
    ]
  }
}
```

---

### 3. 사용자의 공개 플레이리스트

**Endpoint**: `GET /api/v1/playlists/users/{userId}?page=0&size=10`

**Path Parameters**:
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| userId | String | 사용자 ID |

**Query Parameters**:
| 파라미터 | 타입 | 기본값 | 설명 |
|----------|------|--------|------|
| page | Integer | 0 | 페이지 번호 |
| size | Integer | 10 | 페이지 크기 |

**Response** (200 OK):
```json
{
  "success": true,
  "code": "Success",
  "data": {
    "content": [...],
    "totalElements": 5,
    "totalPages": 1,
    "page": 0
  }
}
```

---

### 4. 내 전체 플레이리스트

**Endpoint**: `GET /api/v1/playlists/me?page=0&size=10`

**Request Headers**:
```
X-User-Id: 1
```

**Query Parameters**:
| 파라미터 | 타입 | 기본값 | 설명 |
|----------|------|--------|------|
| page | Integer | 0 | 페이지 번호 |
| size | Integer | 10 | 페이지 크기 |

**Response** (200 OK):
```json
{
  "success": true,
  "code": "Success",
  "data": {
    "content": [
      {
        "playlistId": "1",
        "name": "비공개 플레이리스트",
        "description": "나만 보는 영상",
        "thumbnailUrl": "/uploads/thumbnails/playlist2.jpg",
        "isPublic": false,
        "owner": {
          "userId": 1,
          "nickname": "길동이",
          "profileUrl": "/uploads/profiles/user1.jpg"
        },
        "shortsCount": 8
      }
    ],
    "totalElements": 10,
    "totalPages": 1,
    "page": 0
  }
}
```

---

### 5. 플레이리스트 생성

**Endpoint**: `POST /api/v1/playlists`

**Request Headers**:
```
X-User-Id: 1
Content-Type: application/json
```

**Request Body**:
```json
{
  "name": "내가 좋아하는 코딩 강의",
  "description": "개발자가 꼭 봐야 할 영상들",
  "thumbnailUrl": "/uploads/thumbnails/playlist1.jpg",
  "isPublic": true
}
```

**Field Validation**:
| 필드 | 타입 | 필수 | 제약조건 | 설명 |
|------|------|------|----------|------|
| name | String | ✅ | 길이 100자 이내 | 플레이리스트 이름 |
| description | String | ❌ | - | 플레이리스트 설명 |
| thumbnailUrl | String | ❌ | - | 썸네일 URL |
| isPublic | Boolean | ❌ | - | 공개 여부 (기본 true) |

**Response** (201 Created):
```json
{
  "success": true,
  "code": "Success",
  "data": {
    "playlistId": "1",
    "name": "내가 좋아하는 코딩 강의",
    "description": "개발자가 꼭 봐야 할 영상들",
    "thumbnailUrl": "/uploads/thumbnails/playlist1.jpg",
    "isPublic": true,
    "owner": {
      "userId": 1,
      "nickname": "길동이",
      "profileUrl": "/uploads/profiles/user1.jpg"
    },
    "shortsCount": 0
  }
}
```

---

### 6. 플레이리스트 수정

**Endpoint**: `PATCH /api/v1/playlists/{playlistId}`

**Request Headers**:
```
X-User-Id: 1
Content-Type: application/json
```

**Path Parameters**:
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| playlistId | String | 플레이리스트 ID |

**Request Body**:
```json
{
  "name": "수정된 이름",
  "description": "수정된 설명",
  "thumbnailUrl": "/uploads/thumbnails/new.jpg",
  "isPublic": false
}
```

**Field Validation**:
| 필드 | 타입 | 필수 | 제약조건 | 설명 |
|------|------|------|----------|------|
| name | String | ❌ | 길이 100자 이내 | 플레이리스트 이름 |
| description | String | ❌ | - | 플레이리스트 설명 |
| thumbnailUrl | String | ❌ | - | 썸네일 URL |
| isPublic | Boolean | ❌ | - | 공개 여부 |

**Response** (200 OK):
```json
{
  "success": true,
  "code": "Success",
  "data": {
    "playlistId": "1",
    "name": "수정된 이름",
    "description": "수정된 설명",
    "thumbnailUrl": "/uploads/thumbnails/new.jpg",
    "isPublic": false,
    "owner": {
      "userId": 1,
      "nickname": "길동이",
      "profileUrl": "/uploads/profiles/user1.jpg"
    },
    "shortsCount": 0
  }
}
```

---

### 7. 플레이리스트 삭제

**Endpoint**: `DELETE /api/v1/playlists/{playlistId}`

**Request Headers**:
```
X-User-Id: 1
```

**Path Parameters**:
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| playlistId | String | 플레이리스트 ID |

**Response** (204 No Content):
```json
{
  "success": true,
  "code": "Success",
  "data": null
}
```

---

### 8. 쇼츠 추가

**Endpoint**: `POST /api/v1/playlists/{playlistId}/shorts/{shortsId}`

**Request Headers**:
```
X-User-Id: 1
```

**Path Parameters**:
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| playlistId | String | 플레이리스트 ID |
| shortsId | String | 쇼츠 ID |

**Response** (200 OK):
```json
{
  "success": true,
  "code": "Success",
  "data": {
    "playlistId": "1",
    "name": "내가 좋아하는 코딩 강의",
    "description": "개발자가 꼭 봐야 할 영상들",
    "thumbnailUrl": "/uploads/thumbnails/playlist1.jpg",
    "isPublic": true,
    "owner": {
      "userId": 1,
      "nickname": "길동이",
      "profileUrl": "/uploads/profiles/user1.jpg"
    },
    "shorts": [
      {
        "shortsId": "5",
        "title": "Spring Boot 입문 가이드",
        "thumbnailUrl": "/uploads/thumbnails/xyz.jpg",
        "orderNumber": 1
      }
    ]
  }
}
```

---

### 9. 쇼츠 제거

**Endpoint**: `DELETE /api/v1/playlists/{playlistId}/shorts/{shortsId}`

**Request Headers**:
```
X-User-Id: 1
```

**Path Parameters**:
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| playlistId | String | 플레이리스트 ID |
| shortsId | String | 쇼츠 ID |

**Response** (200 OK):
```json
{
  "success": true,
  "code": "Success",
  "data": null
}
```

---

### 10. 쇼츠 순서 변경

**Endpoint**: `PATCH /api/v1/playlists/{playlistId}/shorts/reorder`

**Request Headers**:
```
X-User-Id: 1
Content-Type: application/json
```

**Path Parameters**:
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| playlistId | String | 플레이리스트 ID |

**Request Body**:
```json
{
  "items": [
    {
      "shortsId": "5",
      "orderNumber": 1
    },
    {
      "shortsId": "12",
      "orderNumber": 2
    }
  ]
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "code": "Success",
  "data": {
    "playlistId": "1",
    "name": "내가 좋아하는 코딩 강의",
    "description": "개발자가 꼭 봐야 할 영상들",
    "thumbnailUrl": "/uploads/thumbnails/playlist1.jpg",
    "isPublic": true,
    "owner": {
      "userId": 1,
      "nickname": "길동이",
      "profileUrl": "/uploads/profiles/user1.jpg"
    },
    "shorts": [
      {
        "shortsId": "5",
        "title": "Spring Boot 입문 가이드",
        "thumbnailUrl": "/uploads/thumbnails/xyz.jpg",
        "orderNumber": 1
      },
      {
        "shortsId": "12",
        "title": "Java 기초 강의",
        "thumbnailUrl": "/uploads/thumbnails/abc.jpg",
        "orderNumber": 2
      }
    ]
  }
}
```

---

## 📋 공통 응답 포맷

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

### 공개 플레이리스트 목록 조회
```bash
curl -X GET "http://localhost:8080/api/v1/playlists?page=0&size=10"
```

### 플레이리스트 생성
```bash
curl -X POST http://localhost:8080/api/v1/playlists \
  -H "X-User-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "내가 좋아하는 코딩 강의",
    "description": "개발자가 꼭 봐야 할 영상들",
    "thumbnailUrl": "/uploads/thumbnails/playlist1.jpg",
    "isPublic": true
  }'
```

### 쇼츠 추가
```bash
curl -X POST http://localhost:8080/api/v1/playlists/1/shorts/5 \
  -H "X-User-Id: 1"
```

### 쇼츠 순서 변경
```bash
curl -X PATCH http://localhost:8080/api/v1/playlists/1/shorts/reorder \
  -H "X-User-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {"shortsId": "5", "orderNumber": 1},
      {"shortsId": "12", "orderNumber": 2}
    ]
  }'
```

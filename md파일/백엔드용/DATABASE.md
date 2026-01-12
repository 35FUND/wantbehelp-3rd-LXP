# 📊 Shortudy 데이터베이스 명세서

## ERD 개요

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│   users     │       │ shorts_form │       │ categories  │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ id (PK)     │──┐    │ id (PK)     │    ┌──│ id (PK)     │
│ email       │  │    │ user_id     │◄───┘  │ parent_id   │
│ password    │  └───►│ category_id │◄──────│ name        │
│ name        │       │ title       │       │ created_at  │
│ nickname    │       │ description │       │ updated_at  │
│ created_at  │       │ video_url   │       └─────────────┘
│ updated_at  │       │ thumbnail_url│
└─────────────┘       │ duration_sec │      ┌─────────────┐
                      │ view_count  │      │    tags     │
                      │ status      │      ├─────────────┤
                      └─────────────┘      │ id (PK)     │
                            │              │ created_at  │
                            │              │ updated_at  │
                            ▼              └─────────────┘
                      ┌─────────────┐            │
                      │  taggings   │            │
                      ├─────────────┤            │
                      │ id (PK)     │            │
                      │ shorts_id   │◄───────────┘  (FK → shorts_form.id)
                      │ tag_id      │
                      └─────────────┘
```

---

## 📋 테이블 명세

### 1. users (회원 정보)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 사용자 ID |
| email | VARCHAR(255) | NOT NULL, UNIQUE | 이메일 (로그인용) |
| password | VARCHAR(255) | NOT NULL | 암호화된 비밀번호 |
| name | VARCHAR(50) | NOT NULL | 이름 |
| nickname | VARCHAR(50) | NULLABLE | 닉네임 |
| profile_url | VARCHAR(500) | NULLABLE | 프로필 이미지 URL |
| created_at | DATETIME | NOT NULL | 생성일시 |
| updated_at | DATETIME | NOT NULL | 수정일시 |

```sql
CREATE TABLE users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    name            VARCHAR(50) NOT NULL,
    nickname        VARCHAR(50),
    profile_url     VARCHAR(500),
    created_at      DATETIME NOT NULL,
    updated_at      DATETIME NOT NULL
);
```

---

### 2. categories (카테고리)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 카테고리 ID |
| parent_id | BIGINT | FK, NULLABLE | 상위 카테고리 ID |
| name | VARCHAR(100) | NOT NULL | 카테고리명 |
| created_at | DATETIME | NOT NULL | 생성일시 |
| updated_at | DATETIME | NOT NULL | 수정일시 |

```sql
CREATE TABLE categories (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id   BIGINT,
    name        VARCHAR(100) NOT NULL,
    created_at  DATETIME NOT NULL,
    updated_at  DATETIME NOT NULL,
    
    FOREIGN KEY (parent_id) REFERENCES categories(id)
);
```

**초기 데이터:**
```sql
INSERT INTO categories (name, created_at, updated_at) VALUES 
('프로그래밍', NOW(), NOW()),
('디자인', NOW(), NOW()),
('마케팅', NOW(), NOW());
```

---

### 3. shorts_form (숏폼 콘텐츠)

> 주의: 현재 JPA 매핑 기준 테이블명은 `shorts_form` 입니다.

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 숏폼 ID(`shortId`) |
| user_id | BIGINT | FK, NOT NULL | 업로더 사용자 ID |
| category_id | BIGINT | FK, NOT NULL | 카테고리 ID |
| title | VARCHAR(100) | NOT NULL | 제목 |
| description | TEXT | NULLABLE | 설명 |
| video_url | VARCHAR(500) | NOT NULL | 영상 URL |
| thumbnail_url | VARCHAR(500) | NULLABLE | 썸네일 URL |
| duration_sec | INT | NULLABLE | 영상 길이(초) |
| view_count | BIGINT | NOT NULL, DEFAULT 0 | 조회수 |
| status | ENUM | DEFAULT 'PUBLISHED' | 상태 |

**status 값(코드 기준):**
- `DRAFT` - 임시 저장
- `PUBLISHED` - 공개
- `ARCHIVED` - 보관

```sql
CREATE TABLE shorts_form (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    category_id     BIGINT NOT NULL,
    title           VARCHAR(100) NOT NULL,
    description     TEXT,
    video_url       VARCHAR(500) NOT NULL,
    thumbnail_url   VARCHAR(500),
    duration_sec    INT,
    view_count      BIGINT NOT NULL DEFAULT 0,
    status          ENUM('DRAFT','PUBLISHED','ARCHIVED') DEFAULT 'PUBLISHED',

    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES categories(id)
);
```

---

### 4. tags (태그)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 태그 ID |
| name | VARCHAR(50) | NOT NULL | 태그명 (정규화됨) |
| display_name | VARCHAR(50) | NOT NULL | 표시명 (원본) |
| created_at | DATETIME | NOT NULL | 생성일시 |
| updated_at | DATETIME | NOT NULL | 수정일시 |

```sql
CREATE TABLE tags (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    display_name    VARCHAR(50) NOT NULL,
    created_at      DATETIME NOT NULL,
    updated_at      DATETIME NOT NULL
);
```

**초기 데이터:**
```sql
INSERT INTO tags (name, display_name, created_at, updated_at) VALUES 
('java', 'Java', NOW(), NOW()),
('spring', 'Spring', NOW(), NOW()),
('react', 'React', NOW(), NOW());
```

---

### 5. taggings (숏폼-태그 연결)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | ID |
| shorts_id | BIGINT | FK, NOT NULL | 숏폼 ID |
| tag_id | BIGINT | FK, NOT NULL | 태그 ID |

```sql
CREATE TABLE taggings (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    shorts_id   BIGINT NOT NULL,
    tag_id      BIGINT NOT NULL,
    
    FOREIGN KEY (shorts_id) REFERENCES shorts_form(id),
    FOREIGN KEY (tag_id) REFERENCES tags(id),
    UNIQUE KEY unique_tagging (shorts_id, tag_id)
);
```

---

### 6. shorts_upload_session (숏폼 업로드 세션)

> 업로드 세션은 쇼츠 엔티티 ID(`shortId`)를 PK로 재사용합니다.

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGINT | PK, FK | 업로드 대상 쇼츠 ID(=shortId) |
| upload_id | VARCHAR(64) | NOT NULL | 완료 알림 검증용 토큰 |
| user_id | BIGINT | NOT NULL | 요청 사용자 ID |
| category_id | BIGINT | NOT NULL | 카테고리 ID |
| title | VARCHAR(100) | NOT NULL | 제목 |
| description | TEXT | NULLABLE | 설명 |
| keywords | TEXT | NULLABLE | 키워드(콤마 구분) |
| file_name | VARCHAR(255) | NOT NULL | 파일명 |
| file_size | BIGINT | NOT NULL | 파일 크기(바이트) |
| content_type | VARCHAR(100) | NOT NULL | Content-Type |
| object_key | VARCHAR(500) | NOT NULL | S3 Object Key |
| expires_in | INT | NOT NULL | 만료(초) |
| status | VARCHAR(20) | NOT NULL | INITIATED/COMPLETED |
| uploaded_at | DATETIME | NULLABLE | 업로드 완료 처리 시각 |
| created_at | DATETIME | NULLABLE | 세션 생성 시각(Auditing) |

```sql
CREATE TABLE shorts_upload_session (
    id              BIGINT PRIMARY KEY,
    upload_id        VARCHAR(64) NOT NULL,
    user_id          BIGINT NOT NULL,
    category_id      BIGINT NOT NULL,
    title            VARCHAR(100) NOT NULL,
    description      TEXT,
    keywords         TEXT,
    file_name        VARCHAR(255) NOT NULL,
    file_size        BIGINT NOT NULL,
    content_type     VARCHAR(100) NOT NULL,
    object_key       VARCHAR(500) NOT NULL,
    expires_in       INT NOT NULL,
    status           VARCHAR(20) NOT NULL,
    uploaded_at      DATETIME,
    created_at       DATETIME,

    FOREIGN KEY (id) REFERENCES shorts_form(id)
);
```

---

### 7. user_roles (사용자 권한)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| user_id | BIGINT | FK, NOT NULL | 사용자 ID |
| role | VARCHAR(50) | NOT NULL | 권한 |

```sql
CREATE TABLE user_roles (
    user_id     BIGINT NOT NULL,
    role        VARCHAR(50) NOT NULL,
    
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

**role 값:**
- `ROLE_USER` - 일반 사용자
- `ROLE_ADMIN` - 관리자

---

## 🔗 관계 요약

| 관계 | 설명 |
|------|------|
| users → shorts_form | 1:N (한 사용자가 여러 숏폼 업로드) |
| categories → shorts_form | 1:N (한 카테고리에 여러 숏폼) |
| shorts_form ↔ tags | N:M (taggings 테이블로 연결) |
| categories → categories | 자기참조 (parent_id로 계층 구조) |

---

## 📝 인덱스 권장사항

```sql
-- 자주 조회되는 컬럼에 인덱스 추가
CREATE INDEX idx_shorts_form_user ON shorts_form(user_id);
CREATE INDEX idx_shorts_form_category ON shorts_form(category_id);
CREATE INDEX idx_shorts_form_status ON shorts_form(status);
CREATE INDEX idx_shorts_form_view_count ON shorts_form(view_count DESC);

CREATE INDEX idx_taggings_shorts ON taggings(shorts_id);
CREATE INDEX idx_taggings_tag ON taggings(tag_id);

CREATE INDEX idx_upload_session_user ON shorts_upload_session(user_id);
CREATE INDEX idx_upload_session_category ON shorts_upload_session(category_id);
```

## 변경 이력
- 2026-01-12: JPA 매핑 기준으로 `shorts_form` 테이블 및 조회수(view_count) 컬럼 반영
- 2026-01-12: Pre-signed 업로드 플로우 반영을 위해 `shorts_upload_session` 테이블 명세 추가
- 2026-01-12: 쇼츠 식별자 `shortId(Long)` 통일 정책에 맞춰 업로드 세션 PK/FK 관계 수정


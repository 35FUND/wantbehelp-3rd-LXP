# 📊 Shortudy 데이터베이스 명세서

## ERD 개요

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│   users     │       │   shorts    │       │ categories  │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ id (PK)     │──┐    │ id (PK)     │    ┌──│ id (PK)     │
│ email       │  │    │ uploader_id │◄───┘  │ parent_id   │
│ password    │  └───►│ category_id │◄──────│ name        │
│ name        │       │ title       │       │ created_at  │
│ nickname    │       │ description │       │ updated_at  │
│ created_at  │       │ video_url   │       └─────────────┘
│ updated_at  │       │ thumbnail_url│
└─────────────┘       │ duration_sec │      ┌─────────────┐
                      │ status       │      │   kewords   │
                      │ created_at   │      ├─────────────┤
                      │ updated_at   │      │ id (PK)     │
                      └─────────────┘       │ name        │
                            │               │ created_at  │
                            │               │ updated_at  │
                            ▼               └─────────────┘
                      ┌─────────────┐            │
                      │ keywordings │            │
                      ├─────────────┤            │
                      │ id (PK)     │            │
                      │ shorts_id   │◄───────────┘
                      │ keyword_id  │
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

### 3. shorts (숏폼 콘텐츠)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 숏폼 ID |
| uploader_id | BIGINT | FK, NOT NULL | 업로더 ID |
| category_id | BIGINT | FK | 카테고리 ID |
| title | VARCHAR(200) | NOT NULL | 제목 |
| description | TEXT | NULLABLE | 설명 |
| video_url | VARCHAR(500) | NOT NULL | 영상 URL |
| thumbnail_url | VARCHAR(500) | NULLABLE | 썸네일 URL |
| duration_sec | INT | NULLABLE | 영상 길이(초) |
| status | ENUM | DEFAULT 'PUBLIC' | 상태 |
| created_at | DATETIME | NOT NULL | 생성일시 |
| updated_at | DATETIME | NOT NULL | 수정일시 |

**status 값:**
- `PUBLIC` - 공개
- `PRIVATE` - 비공개
- `DELETED` - 삭제됨

```sql
CREATE TABLE shorts (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    uploader_id     BIGINT NOT NULL,
    category_id     BIGINT,
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    video_url       VARCHAR(500) NOT NULL,
    thumbnail_url   VARCHAR(500),
    duration_sec    INT,
    status          ENUM('PUBLIC','PRIVATE','DELETED') DEFAULT 'PUBLIC',
    created_at      DATETIME NOT NULL,
    updated_at      DATETIME NOT NULL,
    
    FOREIGN KEY (uploader_id) REFERENCES users(id),
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
    
    FOREIGN KEY (shorts_id) REFERENCES shorts(id),
    FOREIGN KEY (tag_id) REFERENCES tags(id),
    UNIQUE KEY unique_tagging (shorts_id, tag_id)
);
```

---

### 6. user_roles (사용자 권한)

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
| users → shorts | 1:N (한 사용자가 여러 숏폼 업로드) |
| categories → shorts | 1:N (한 카테고리에 여러 숏폼) |
| shorts ↔ tags | N:M (taggings 테이블로 연결) |
| categories → categories | 자기참조 (parent_id로 계층 구조) |

---

## 📝 인덱스 권장사항

```sql
-- 자주 조회되는 컬럼에 인덱스 추가
CREATE INDEX idx_shorts_uploader ON shorts(uploader_id);
CREATE INDEX idx_shorts_category ON shorts(category_id);
CREATE INDEX idx_shorts_status ON shorts(status);
CREATE INDEX idx_shorts_created ON shorts(created_at DESC);
CREATE INDEX idx_taggings_shorts ON taggings(shorts_id);
CREATE INDEX idx_taggings_tag ON taggings(tag_id);
```


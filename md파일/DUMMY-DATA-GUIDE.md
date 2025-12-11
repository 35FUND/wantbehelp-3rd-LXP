# 📊 Shortudy 더미 데이터 가이드

> 생성일: 2025-12-11

---

## 📋 데이터 요약

| 항목 | 개수 | 설명 |
|------|------|------|
| **사용자** | 15명 | 관리자 1명 + 일반 사용자 14명 |
| **카테고리** | 10개 | 프로그래밍, 디자인, 클라우드 등 |
| **태그** | 20개 | Java, Spring, React, Docker 등 |
| **숏폼 영상** | 50개 | 카테고리별로 분산 배치 |
| **태깅** | 70개 | 숏폼-태그 연결 관계 |

---

## 🔐 테스트 계정

### 관리자 계정
```
이메일: admin@shortudy.com
비밀번호: password123
권한: ROLE_ADMIN, ROLE_USER
닉네임: Admin
```

### 일반 사용자 계정 (예시)
```
이메일: hong@example.com
비밀번호: password123
닉네임: 길동이

이메일: kim@example.com
비밀번호: password123
닉네임: 철수코딩

이메일: lee@example.com
비밀번호: password123
닉네임: 영희디자인
```

**💡 모든 사용자 비밀번호: `password123`**

---

## 📁 카테고리 목록

| ID | 이름 | 설명 |
|----|------|------|
| 1 | 프로그래밍 | Spring, JPA, Git 등 (15개 영상) |
| 2 | 디자인 | Figma, UI/UX 등 (10개 영상) |
| 3 | 마케팅 | - |
| 4 | 비즈니스 | - |
| 5 | 데이터 분석 | - |
| 6 | 개발 도구 | - |
| 7 | 클라우드 | AWS, Docker, Kubernetes 등 (10개 영상) |
| 8 | AI/ML | - |
| 9 | 웹 개발 | React, Vue, TypeScript 등 (15개 영상) |
| 10 | 모바일 | - |

---

## 🏷️ 태그 목록

**프로그래밍 관련:**
- Java, Spring, JPA, Git, GitHub

**웹 개발 관련:**
- React, Vue, JavaScript, TypeScript

**인프라 관련:**
- Docker, Kubernetes, AWS, Redis, MySQL, MongoDB

**디자인 관련:**
- Figma, Photoshop

**기타:**
- REST API, OAuth

---

## 🎬 숏폼 영상 데이터

### 카테고리별 분포
```
프로그래밍 (ID 1): 15개
웹 개발 (ID 9): 15개
디자인 (ID 2): 10개
클라우드 (ID 7): 10개
```

### 영상 예시
```sql
ID 1: "Spring Boot 시작하기" (58초, 1일 전)
ID 2: "JPA 연관관계 마스터" (62초, 2일 전)
ID 16: "React Hooks 완벽 가이드" (65초, 1일 전)
ID 31: "Figma 기초 사용법" (62초, 1일 전)
ID 41: "AWS EC2 시작하기" (70초, 2일 전)
```

**📹 영상 URL은 더미 URL입니다.**
- `https://cdn.shortudy.com/videos/*.mp4`
- `https://cdn.shortudy.com/thumbnails/*.jpg`

---

## 💾 데이터 삽입 방법

### 1. MySQL 접속
```bash
mysql -u root -p
```

### 2. 데이터베이스 선택
```sql
USE shortudy;
```

### 3. SQL 스크립트 실행
```bash
# 파일에서 직접 실행
mysql -u root -p shortudy < sql/dummy-data.sql

# 또는 MySQL 내에서
source C:/Users/user/Desktop/프로젝트/wantbehelp-3rd-LXP/sql/dummy-data.sql;
```

### 4. 데이터 확인
```sql
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM shorts_form;
SELECT * FROM users WHERE email = 'admin@shortudy.com';
```

---

## 🧪 테스트 시나리오

### 1. 로그인 테스트
```bash
POST /api/v1/auth/login
{
  "email": "admin@shortudy.com",
  "password": "password123"
}
```

### 2. 숏폼 목록 조회
```bash
GET /api/v1/shorts?page=0&size=10
```

### 3. 특정 카테고리 영상 조회
```bash
GET /api/v1/shorts?categoryId=1&page=0&size=10
```

### 4. 숏폼 상세 조회
```bash
GET /api/v1/shorts/1
```

### 5. 내 숏폼 목록 (로그인 필요)
```bash
GET /api/v1/users/me/shorts
Authorization: Bearer {accessToken}
```

### 6. 카테고리 생성 (관리자 전용)
```bash
POST /api/v1/categories
Authorization: Bearer {adminAccessToken}
{
  "name": "새 카테고리"
}
```

---

## 📌 주의사항

### 1. 비밀번호 해시
- BCrypt 알고리즘 사용
- 원본: `password123`
- 해시: `$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO`

### 2. 프로필 이미지
- `https://i.pravatar.cc/150?img={1-15}` 사용 (랜덤 아바타)
- 실제 서비스에서는 S3 등으로 교체 필요

### 3. 영상 URL
- 더미 URL이므로 실제 영상 없음
- 테스트 시 404 에러 발생 가능
- 필요시 실제 영상 URL로 교체

### 4. 외래키 제약
- 데이터 삭제 시 순서 주의
  1. taggings
  2. shorts_form
  3. user_roles
  4. users, categories, tags

---

## 🔄 데이터 초기화

### 전체 삭제 후 재삽입
```sql
-- 외래키 체크 비활성화
SET FOREIGN_KEY_CHECKS = 0;

-- 테이블 초기화
TRUNCATE TABLE taggings;
TRUNCATE TABLE shorts_form;
TRUNCATE TABLE user_roles;
TRUNCATE TABLE users;
TRUNCATE TABLE categories;
TRUNCATE TABLE tags;

-- 외래키 체크 활성화
SET FOREIGN_KEY_CHECKS = 1;

-- 다시 SQL 스크립트 실행
source sql/dummy-data.sql;
```

---

## 📊 데이터 활용 예시

### 1. 인기 카테고리 분석
```sql
SELECT c.name, COUNT(s.id) AS shorts_count
FROM categories c
LEFT JOIN shorts_form s ON c.id = s.category_id
GROUP BY c.id, c.name
ORDER BY shorts_count DESC;
```

### 2. 가장 많이 사용된 태그
```sql
SELECT t.display_name, COUNT(tg.tag_id) AS usage_count
FROM tags t
LEFT JOIN taggings tg ON t.id = tg.tag_id
GROUP BY t.id, t.display_name
ORDER BY usage_count DESC
LIMIT 10;
```

### 3. 활동적인 사용자
```sql
SELECT u.nickname, COUNT(s.id) AS upload_count
FROM users u
LEFT JOIN shorts_form s ON u.id = s.user_id
GROUP BY u.id, u.nickname
ORDER BY upload_count DESC
LIMIT 5;
```

### 4. 최근 업로드 영상
```sql
SELECT s.title, u.nickname, c.name AS category, s.created_at
FROM shorts_form s
JOIN users u ON s.user_id = u.id
JOIN categories c ON s.category_id = c.id
ORDER BY s.created_at DESC
LIMIT 10;
```

---

## ✅ 체크리스트

- [ ] MySQL 데이터베이스 `shortudy` 생성 완료
- [ ] SQL 스크립트 실행 완료
- [ ] 사용자 데이터 15개 확인
- [ ] 숏폼 영상 50개 확인
- [ ] 관리자 계정으로 로그인 테스트
- [ ] 일반 사용자 계정으로 로그인 테스트
- [ ] 숏폼 목록 API 호출 테스트
- [ ] 카테고리 필터링 테스트

---

## 🎯 다음 단계

1. **프론트엔드 연동**
   - 더미 데이터로 UI 테스트
   - 페이징 동작 확인
   
2. **실제 영상 추가**
   - 샘플 영상 업로드
   - S3 등에 저장
   
3. **추가 기능 테스트**
   - 좋아요, 댓글 기능
   - 조회수 카운팅
   - 검색 기능

---

**✅ 더미 데이터 준비 완료! 테스트를 시작하세요!**


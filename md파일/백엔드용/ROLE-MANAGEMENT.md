# ⚠️ 권한 관리 체크리스트

## 문제 발견 및 해결

### 발견된 문제
- API 명세서에 "관리자 전용"이라고 명시되어 있는데, 실제 코드에서 권한 체크가 안되어 있음

### 해결 방법

#### 1. SecurityConfig 수정
**파일**: `src/main/java/com/example/shortudy/global/config/SecurityConfig.java`

```java
.authorizeHttpRequests(authorize -> authorize
    // 공개 API (인증 불필요)
    .requestMatchers("/api/v1/auth/signup", "/api/v1/auth/login", ...).permitAll()
    
    // 사용자 인증 필요
    .requestMatchers("/api/v1/users/me", "/api/v1/shorts", ...).authenticated()
    
    // 관리자 전용 (ROLE_ADMIN 필요)
    .requestMatchers("POST", "/api/v1/categories", ...).hasRole("ADMIN")
    .requestMatchers("PUT", "/api/v1/categories/**").hasRole("ADMIN")
    .requestMatchers("DELETE", "/api/v1/categories/**").hasRole("ADMIN")
    
    // 나머지 모든 요청
    .anyRequest().authenticated()
)
```

#### 2. User 엔티티의 역할 관리
**파일**: `src/main/java/com/example/shortudy/domain/user/entity/User.java`

```java
// 기본 역할 자동 설정 (회원가입 시)
private User(String email, String password, String name) {
    this.email = email;
    this.password = password;
    this.name = name;
    this.roles.add("ROLE_USER");  // 기본값: 일반 사용자
}
```

---

## 권한 정의

### ROLE_USER
- 기본 회원
- 권한:
  - 영상 업로드/수정/삭제
  - 파일 업로드
  - 내 정보 조회

### ROLE_ADMIN
- 관리자
- 권한:
  - 모든 ROLE_USER 권한 포함
  - 카테고리 CRUD
  - 태그 CRUD (옵션)
  - 사용자 관리

---

## API별 권한 정책

### 공개 (권한 불필요)
```
GET  /api/v1/shorts              - 영상 목록
GET  /api/v1/shorts/{id}         - 영상 상세
GET  /api/v1/categories          - 카테고리 조회
GET  /api/v1/tags                - 태그 조회
POST /api/v1/auth/signup         - 회원가입
POST /api/v1/auth/login          - 로그인
```

### 사용자 인증 필요 (ROLE_USER 이상)
```
POST   /api/v1/shorts            - 영상 업로드
PATCH  /api/v1/shorts/{id}       - 영상 수정
DELETE /api/v1/shorts/{id}       - 영상 삭제
POST   /api/v1/files/videos      - 비디오 업로드
POST   /api/v1/files/thumbnails  - 썸네일 업로드
GET    /api/v1/users/me          - 내 정보
GET    /api/v1/users/me/shorts   - 내 영상
```

### 관리자 전용 (ROLE_ADMIN 필수)
```
POST   /api/v1/categories        - 카테고리 생성
PUT    /api/v1/categories/{id}   - 카테고리 수정
DELETE /api/v1/categories/{id}   - 카테고리 삭제
```

---

## 테스트 방법

### 1. 일반 사용자로 카테고리 생성 시도
```bash
curl -X POST http://localhost:8080/api/v1/categories \
  -H "Authorization: Bearer {USER_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"name":"새 카테고리"}'
```
**결과**: 403 Forbidden (권한 없음)

### 2. 관리자로 카테고리 생성
```bash
curl -X POST http://localhost:8080/api/v1/categories \
  -H "Authorization: Bearer {ADMIN_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"name":"새 카테고리"}'
```
**결과**: 201 Created (성공)

---

## 데이터베이스 초기 설정

### 관리자 계정 생성 SQL
```sql
-- 사용자 생성
INSERT INTO users (email, password, name, created_at, updated_at) 
VALUES ('admin@shortudy.com', '$2a$10$...', 'Admin User', NOW(), NOW());

-- 역할 할당
INSERT INTO user_roles (user_id, role) 
VALUES (1, 'ROLE_ADMIN');
```

> ⚠️ password는 BCrypt로 해시된 값을 사용해야 합니다.
> Spring Boot 에서 자동 생성된 해시값을 얻으려면:
> 
> ```java
> new BCryptPasswordEncoder().encode("password123")
> ```

---

## 프론트 개발자 주의사항

1. **카테고리 CRUD**는 관리자만 가능 → 관리자 페이지에서만 제공
2. **일반 사용자**는 영상 업로드/수정/삭제만 가능
3. 403 Forbidden 에러 발생 시 → 권한 확인 필요
4. 인증 토큰에 실제 권한 정보가 포함됨 (JWT 디코딩 시 확인 가능)

---

## 정리

✅ **권한별 접근 제어 완료**
- SecurityConfig에서 경로별 권한 설정
- User 엔티티에서 역할 관리
- API별 권한 정책 명확히 정의

이제 "관리자 전용" 표시가 실제로 작동합니다!


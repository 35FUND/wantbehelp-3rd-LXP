# ✅ Access Token + Refresh Token 모두 쿠키로 전환 완료!

> 수정일: 2025-12-11

---

## 📋 변경 사항

### 1. 토큰 전달 방식 변경

#### 이전
```
- Access Token: Response Body로 전달
- Refresh Token: HTTP Only 쿠키로 전달
```

#### 이후
```
- Access Token: HTTP Only 쿠키로 전달 (30분)
- Refresh Token: HTTP Only 쿠키로 전달 (7일)
```

---

## 🔐 쿠키 설정

### Access Token 쿠키
```java
Cookie: accessToken
├─ HttpOnly: true       // XSS 공격 방지
├─ Secure: false/true   // 로컬: false, 프로덕션: true (HTTPS)
├─ Path: /
├─ MaxAge: 1800초 (30분)
└─ SameSite: Lax/None   // 로컬: Lax, 프로덕션: None (CORS)
```

### Refresh Token 쿠키
```java
Cookie: refreshToken
├─ HttpOnly: true       // XSS 공격 방지
├─ Secure: false/true   // 로컬: false, 프로덕션: true (HTTPS)
├─ Path: /
├─ MaxAge: 604800초 (7일)
└─ SameSite: Lax/None   // 로컬: Lax, 프로덕션: None (CORS)
```

### ⚠️ SameSite 설정 중요!

**SameSite란?**
- `Lax` (기본값): 같은 도메인에서만 쿠키 전송 (안전)
- `None`: 다른 도메인에서도 쿠키 전송 허용 (프론트/백 분리 시 필요)

**언제 None을 사용하나?**
```
프론트엔드: http://localhost:3000 (React/Vue)
백엔드:     http://localhost:8080 (Spring Boot)
→ 포트가 다르면 "다른 도메인"으로 취급됨!
→ SameSite=None 필수!
```

**주의사항:**
- `SameSite=None`은 반드시 `Secure=true`와 함께 사용해야 함
- `Secure=true`는 HTTPS 환경에서만 동작
- 로컬 개발(HTTP)에서는 `SameSite=Lax`, `Secure=false` 사용

### 환경별 설정

**로컬 개발 (application.properties)**
```properties
app.cookie.same-site=Lax
app.cookie.secure=false
```

**프로덕션 (application-prod.properties)**
```properties
app.cookie.same-site=None
app.cookie.secure=true
```

---

## 📤 API 응답 변경

### POST /api/v1/auth/login

#### 이전 응답
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "accessToken": "eyJ...",      // ❌ 제거됨
    "refreshToken": "eyJ...",     // ❌ 제거됨
    "user": { ... }
  }
}
```

#### 현재 응답
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "user": {
      "id": 1,
      "email": "user@example.com",
      "name": "홍길동",
      "nickname": "길동이",
      "profileUrl": "https://..."
    }
  }
}
```

**쿠키 자동 설정:**
```
Set-Cookie: accessToken=eyJ...; HttpOnly; Path=/; Max-Age=1800
Set-Cookie: refreshToken=eyJ...; HttpOnly; Path=/; Max-Age=604800
```

---

## 🔄 프론트엔드 사용법

### 1. 로그인
```javascript
fetch('/api/v1/auth/login', {
  method: 'POST',
  credentials: 'include',  // ✅ 필수! 쿠키 자동 포함
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'user@example.com',
    password: 'password123'
  })
})
.then(res => res.json())
.then(data => {
  // ✅ user 정보만 사용
  console.log(data.data.user);
  // 토큰은 자동으로 쿠키에 저장됨 (접근 불가)
});
```

### 2. 인증이 필요한 API 호출
```javascript
fetch('/api/v1/users/me', {
  method: 'GET',
  credentials: 'include'  // ✅ 쿠키 자동 포함 (accessToken)
})
.then(res => res.json())
.then(data => {
  console.log(data.data);
});
```

### 3. 토큰 재발급
```javascript
fetch('/api/v1/auth/refresh', {
  method: 'POST',
  credentials: 'include'  // ✅ refreshToken 쿠키 자동 전송
})
.then(res => res.json())
.then(data => {
  // ✅ 새로운 토큰이 자동으로 쿠키에 저장됨
  console.log('토큰 재발급 성공');
});
```

### 4. 로그아웃
```javascript
fetch('/api/v1/auth/logout', {
  method: 'POST',
  credentials: 'include'
})
.then(() => {
  // ✅ 쿠키에서 토큰 자동 삭제됨
  console.log('로그아웃 성공');
  window.location.href = '/login.html';
});
```

---

## 🛠 수정된 파일

### 1. UserLoginResponse.java
```java
// 토큰 필드 제거, user 정보만 반환
public record UserLoginResponse(
    UserResponse user
)
```

### 2. AuthService.java & AuthServiceImpl.java
```java
// 반환 타입 변경: Map으로 토큰과 response 반환
Map<String, Object> login(UserLoginRequest request);
Map<String, Object> refreshToken(TokenRefreshRequest request);
```

### 3. AuthController.java
```java
// ResponseCookie로 sameSite 지원 (권장 방식)
@PostMapping("/login")
public ResponseEntity<UserLoginResponse> login(
        @RequestBody @Valid UserLoginRequest request,
        HttpServletResponse httpResponse) {
    
    Map<String, Object> loginResult = authService.login(request);
    
    // Access Token을 ResponseCookie로 생성 (sameSite 지원!)
    ResponseCookie accessTokenCookie = ResponseCookie
            .from("accessToken", (String) loginResult.get("accessToken"))
            .httpOnly(true)         // XSS 방지
            .secure(cookieSecure)   // 환경별 설정 (로컬: false, 프로덕션: true)
            .path("/")
            .maxAge(30 * 60)        // 30분
            .sameSite(cookieSameSite)  // 환경별 설정 (로컬: Lax, 프로덕션: None)
            .build();
    
    // Refresh Token도 동일하게
    ResponseCookie refreshTokenCookie = ResponseCookie
            .from("refreshToken", (String) loginResult.get("refreshToken"))
            .httpOnly(true)
            .secure(cookieSecure)
            .path("/")
            .maxAge(7 * 24 * 60 * 60)  // 7일
            .sameSite(cookieSameSite)
            .build();
    
    // 응답 헤더에 쿠키 추가
    httpResponse.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
    httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    
    return ResponseEntity.ok((UserLoginResponse) loginResult.get("response"));
}
```

### 4. JwtAuthenticationFilter.java
```java
// 쿠키에서도 Access Token 읽도록 수정
private String resolveToken(HttpServletRequest request) {
    // 1. Authorization 헤더 확인
    // 2. 쿠키 확인 (accessToken)
}
```

---

## 🔒 보안 개선

| 항목 | 이전 | 이후 |
|------|------|------|
| Access Token 저장 | localStorage (XSS 위험) | HTTP Only 쿠키 (안전) |
| Refresh Token 저장 | HTTP Only 쿠키 | ✅ 동일 |
| JavaScript 접근 | Access Token 가능 | ❌ 모든 토큰 불가 |
| XSS 공격 | Access Token 탈취 가능 | ✅ 토큰 탈취 불가 |
| 자동 갱신 | 수동 구현 필요 | ✅ 쿠키 자동 관리 |

---

## ⚠️ 주의사항

### 1. credentials: 'include' 필수
```javascript
// ❌ 잘못된 예
fetch('/api/v1/users/me')

// ✅ 올바른 예
fetch('/api/v1/users/me', { credentials: 'include' })
```

### 2. CORS 설정 확인
```java
// WebConfig.java에서 credentials 허용 확인
.allowCredentials(true)
```

### 3. 토큰 만료 시간
- **Access Token: 30분** - 짧게 유지 (보안)
- **Refresh Token: 7일** - 자동 로그인 유지

### 4. 프로덕션 환경
```java
// Secure 플래그를 true로 변경 (HTTPS에서만 전송)
cookie.setSecure(true);
```

---

## ✨ 장점

1. **보안 강화**
   - XSS 공격으로부터 토큰 보호
   - JavaScript에서 토큰 접근 불가

2. **편의성**
   - 프론트에서 토큰 관리 불필요
   - 쿠키 자동 전송

3. **자동 갱신**
   - Refresh Token으로 자동 재발급
   - 사용자 경험 개선

---

## 🎯 테스트 방법

### 1. 로그인 후 쿠키 확인
```
브라우저 개발자 도구 → Application → Cookies
- accessToken (30분)
- refreshToken (7일)
```

### 2. API 호출 시 쿠키 전송 확인
```
Network 탭 → Request Headers
Cookie: accessToken=eyJ...; refreshToken=eyJ...
```

### 3. 로그아웃 후 쿠키 삭제 확인
```
쿠키 탭에서 accessToken, refreshToken 사라짐
```

---

## ✅ 결론

**✅ 모든 토큰이 안전하게 HTTP Only 쿠키로 관리됩니다!**

- Access Token: 30분 (쿠키)
- Refresh Token: 7일 (쿠키)
- XSS 공격 방지
- 프론트에서 `credentials: 'include'`만 추가하면 자동 작동


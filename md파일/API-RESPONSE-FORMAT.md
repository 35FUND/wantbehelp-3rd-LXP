# ✅ API 응답 성공/실패 명확화 완료

> 수정일: 2025-12-11

---

## 📊 수정 사항

### 1. AuthStatusResponse - 비로그인 상태
```java
// 이전
public static AuthStatusResponse notLoggedIn() {
    return new AuthStatusResponse(false, null, null, null);  // ❌ null 값
}

// 수정 후
public static AuthStatusResponse notLoggedIn() {
    return new AuthStatusResponse(false, "", "", "");  // ✅ 빈 문자열
}
```

### 2. ShortsController - 삭제 응답
```java
// 이전
return ApiResponse.success(null);  // ❌ null 전달

// 수정 후
return ApiResponse.success();  // ✅ 데이터 없음
```

---

## 📤 API 응답 형식

### ✅ 성공 응답 (데이터 있음)
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "shortsId": 1,
    "title": "...",
    ...
  }
}
```

### ✅ 성공 응답 (데이터 없음 - 삭제 등)
```json
{
  "success": true,
  "message": "Success",
  "data": null
}
```

### ❌ 실패 응답
```json
{
  "success": false,
  "message": "에러 메시지",
  "data": null
}
```

---

## 🎯 응답 예시

### 1. 로그인 성공
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
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

### 2. 로그인 실패 (잘못된 비밀번호)
```json
{
  "success": false,
  "message": "잘못된 비밀번호입니다.",
  "data": null
}
```

### 3. 로그인 상태 조회 (비로그인)
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "isLoggedIn": false,
    "email": "",
    "name": "",
    "nickname": ""
  }
}
```

### 4. 숏폼 삭제 성공
```json
{
  "success": true,
  "message": "Success",
  "data": null
}
```

### 5. 리소스 없음 (404)
```json
{
  "success": false,
  "message": "존재하지 않는 숏츠입니다.",
  "data": null
}
```

### 6. 권한 없음 (403)
```json
{
  "success": false,
  "message": "접근 권한이 없습니다.",
  "data": null
}
```

---

## 🔍 프론트엔드 활용법

### JavaScript/TypeScript
```javascript
fetch('/api/v1/shorts/1')
  .then(res => res.json())
  .then(response => {
    if (response.success) {
      // ✅ 성공
      console.log('데이터:', response.data);
      console.log('메시지:', response.message);
    } else {
      // ❌ 실패
      console.error('에러:', response.message);
      alert(response.message);
    }
  });
```

### Axios 예시
```javascript
axios.get('/api/v1/shorts/1')
  .then(({ data }) => {
    if (data.success) {
      // 성공 처리
      setShorts(data.data);
    } else {
      // 실패 처리
      showError(data.message);
    }
  });
```

---

## ✨ 개선 효과

| 항목 | 이전 | 이후 |
|------|------|------|
| 성공 여부 | `success` 필드로 확인 | ✅ 동일 |
| 에러 메시지 | `message` 필드 | ✅ 동일 |
| null 값 | 일부 존재 | ✅ 최소화 |
| 일관성 | 높음 | ✅ 더 높음 |

---

## 📋 전체 응답 구조 정리

### ApiResponse<T> 구조
```java
{
  "success": boolean,    // 성공 여부 (필수)
  "message": string,     // 응답 메시지 (필수)
  "data": T | null      // 실제 데이터 (optional)
}
```

### HTTP 상태 코드와의 관계
| HTTP 코드 | success | 설명 |
|-----------|---------|------|
| 200 OK | true | 조회/수정 성공 |
| 201 Created | true | 생성 성공 |
| 204 No Content | true | 삭제 성공 (data: null) |
| 400 Bad Request | false | 잘못된 요청 |
| 401 Unauthorized | false | 인증 필요 |
| 403 Forbidden | false | 권한 없음 |
| 404 Not Found | false | 리소스 없음 |
| 409 Conflict | false | 중복 (이메일 등) |

---

## ✅ 결론

**모든 API 응답이 일관된 형식으로 성공/실패 여부를 명확히 표시합니다!**

- ✅ `success` 필드로 성공/실패 즉시 확인 가능
- ✅ `message` 필드로 상세 정보 제공
- ✅ null 값 최소화 (빈 문자열 사용)
- ✅ 프론트엔드에서 일관된 에러 처리 가능


# 🌐 Shortudy 웹 페이지 명세서

## 페이지 목록

| # | 파일명 | 경로 | 설명 | 인증 |
|---|--------|------|------|------|
| 1 | index.html | `/` | 메인 홈페이지 | ❌ |
| 2 | login.html | `/login.html` | 로그인 | ❌ |
| 3 | signup.html | `/signup.html` | 회원가입 | ❌ |
| 4 | watch.html | `/watch.html?id={id}` | 영상 시청 | ❌ |
| 5 | upload.html | `/upload.html` | 영상 업로드 | ✅ |
| 6 | edit.html | `/edit.html?id={id}` | 영상 수정 | ✅ |
| 7 | mypage.html | `/mypage.html` | 마이페이지 | ✅ |
| 8 | my-shorts.html | `/my-shorts.html` | 내 영상 관리 | ✅ |
| 9 | trending.html | `/trending.html` | 인기 영상 | ❌ |
| 10 | latest.html | `/latest.html` | 최신 영상 | ❌ |
| 11 | category.html | `/category.html?id={id}` | 카테고리별 영상 | ❌ |
| 12 | search.html | `/search.html?q={query}` | 검색 결과 | ❌ |
| 13 | liked.html | `/liked.html` | 좋아요한 영상 | ✅ |
| 14 | history.html | `/history.html` | 시청 기록 | ✅ |
| 15 | settings.html | `/settings.html` | 설정 | ✅ |
| 16 | admin.html | `/admin.html` | 관리자 페이지 | ✅ |

---

## 📄 페이지별 상세

### 1. index.html (메인 홈)
**기능:**
- 숏폼 영상 그리드 목록
- 사이드바 네비게이션
- 카테고리 필터 칩
- 검색 기능
- 무한 스크롤 (페이징)
- 로그인/로그아웃 상태 처리

**API 호출:**
- `GET /api/v1/shorts` - 숏폼 목록
- `GET /api/v1/auth/stat` - 로그인 상태

---

### 2. login.html (로그인)
**기능:**
- 이메일/비밀번호 입력
- JWT 토큰 저장 (localStorage)
- 에러 메시지 표시

**API 호출:**
- `POST /api/v1/auth/login`

---

### 3. signup.html (회원가입)
**기능:**
- 이메일/이름/비밀번호 입력
- 비밀번호 확인 검증
- 약관 동의 체크박스

**API 호출:**
- `POST /api/v1/auth/signup`

---

### 4. watch.html (영상 시청)
**기능:**
- 세로형 비디오 플레이어 (9:16)
- 좋아요/댓글/공유/저장 버튼
- 영상 상세정보 사이드 패널
- 추천 영상 목록
- 태그 표시

**API 호출:**
- `GET /api/v1/shorts/{id}` - 영상 상세
- `GET /api/v1/shorts` - 추천 영상

---

### 5. upload.html (영상 업로드)
**기능:**
- 드래그 앤 드롭 비디오 업로드
- 비디오 프리뷰
- 제목/설명 입력 (글자수 카운트)
- 카테고리 선택
- 태그 입력 (최대 5개)
- 썸네일 업로드 옵션

**API 호출:**
- `GET /api/v1/users/me` - 사용자 ID 조회
- `GET /api/v1/categories` - 카테고리 목록
- `POST /api/v1/files/videos` - 비디오 업로드
- `POST /api/v1/files/thumbnails` - 썸네일 업로드
- `POST /api/v1/shorts` - 숏폼 등록

---

### 6. edit.html (영상 수정)
**기능:**
- 기존 정보 불러오기
- 제목/설명 수정
- 카테고리/태그 변경
- 공개 상태 변경 (PUBLIC/PRIVATE)
- 영상 삭제

**API 호출:**
- `GET /api/v1/shorts/{id}` - 기존 정보
- `PATCH /api/v1/shorts/{id}` - 수정
- `DELETE /api/v1/shorts/{id}` - 삭제

---

### 7. mypage.html (마이페이지)
**기능:**
- 프로필 정보 표시
- 통계 (업로드 영상, 조회수, 좋아요)
- 프로필 수정 폼
- 로그아웃/계정 삭제

**API 호출:**
- `GET /api/v1/users/me`

---

### 8. my-shorts.html (내 영상)
**기능:**
- 업로드한 영상 리스트
- 공개/비공개 필터 탭
- 수정/삭제 버튼
- 상태 배지 (공개/비공개/삭제됨)

**API 호출:**
- `GET /api/v1/users/me/shorts`
- `DELETE /api/v1/shorts/{id}`

---

### 9. trending.html (인기 영상)
**기능:**
- 인기순 영상 목록
- 순위 배지 (금/은/동)
- 기간 필터 (오늘/이번 주/이번 달)

**API 호출:**
- `GET /api/v1/shorts` (정렬 적용)

---

### 10. latest.html (최신 영상)
**기능:**
- 최신순 영상 목록
- NEW 배지
- 상세 정보 표시 (설명, 태그)

**API 호출:**
- `GET /api/v1/shorts?sort=createdAt,desc`

---

### 11. category.html (카테고리)
**기능:**
- 카테고리별 영상 필터링
- 카테고리 헤더 (아이콘, 설명)
- 정렬 옵션 (최신순/인기순)

**API 호출:**
- `GET /api/v1/shorts?categoryId={id}`

---

### 12. search.html (검색)
**기능:**
- 검색 결과 그리드
- 카테고리 필터
- 빈 결과 상태 표시

**API 호출:**
- `GET /api/v1/shorts` (클라이언트 필터링)

---

### 13. liked.html (좋아요)
**기능:**
- 좋아요한 영상 목록
- 좋아요 취소 버튼
- 전체 삭제

**저장소:**
- localStorage (`liked_videos`)

---

### 14. history.html (시청 기록)
**기능:**
- 시청 기록 목록
- 날짜별 그룹화 (오늘/어제/이번 주)
- 진행률 표시
- 기록 삭제

**저장소:**
- localStorage (`watch_history`)

---

### 15. settings.html (설정)
**기능:**
- 알림 설정 (푸시/이메일)
- 재생 설정 (자동재생/화질/음소거)
- 개인정보 설정 (기록 삭제)
- 테마 설정
- 로그아웃/계정 삭제

**저장소:**
- localStorage (`app_settings`)

---

### 16. admin.html (관리자)
**기능:**
- 카테고리 CRUD
- 태그 CRUD
- 탭 전환 (카테고리/태그)
- 수정 모달

**API 호출:**
- `GET/POST/PUT/DELETE /api/v1/categories`
- `GET/POST/PUT/DELETE /api/v1/tags`

---

## 🎨 디자인 시스템

### 색상 팔레트
```css
--bg-primary: #0f0f0f;      /* 메인 배경 */
--bg-secondary: #1a1a1a;    /* 카드 배경 */
--bg-tertiary: #333;        /* 입력 필드 */
--accent: #ff4757;          /* 포인트 (빨강) */
--success: #2ed573;         /* 성공 (녹색) */
--text-primary: #fff;       /* 메인 텍스트 */
--text-secondary: #888;     /* 보조 텍스트 */
--text-muted: #666;         /* 비활성 텍스트 */
```

### 컴포넌트
- **버튼**: `.btn`, `.btn-primary`, `.btn-secondary`, `.btn-danger`
- **카드**: `.video-card`, `.video-thumbnail`, `.video-info`
- **입력**: `.form-group`, `.form-group input`
- **배지**: `.status-badge`, `.rank-badge`, `.new-badge`
- **모달**: `.modal-overlay`, `.modal`

### 반응형 브레이크포인트
- Desktop: 1200px+
- Tablet: 768px - 1199px
- Mobile: 767px 이하

```css
@media (max-width: 768px) {
    .sidebar { display: none; }
    .main-content { margin-left: 0; }
}
```


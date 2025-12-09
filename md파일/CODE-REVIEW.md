# 🔍 Shortudy 코드 리뷰

## 📊 전체 구조 평가

### ✅ 잘된 점
1. **도메인 분리**: category, shorts, tag, user로 명확하게 분리
2. **DTO 패턴**: Request/Response DTO로 계층 분리
3. **예외 처리**: GlobalExceptionHandler로 중앙 집중식 예외 처리
4. **보안**: JWT 인증, BCrypt 비밀번호 해시, @JsonIgnore로 민감정보 보호
5. **API 문서화**: Swagger 적용

### ⚠️ 개선된 점
1. BaseTimeEntity로 createdAt, updatedAt 자동 관리
2. ShortsResponse에 uploaderId, categoryId 추가
3. User.password에 @JsonIgnore 추가

---

## 📁 도메인별 리뷰

### 1. User 도메인

#### User.java
```java
// ✅ 좋은 점: @JsonIgnore로 password 보호
@JsonIgnore
@Column(nullable = false, length = 100)
private String password;

// ✅ 좋은 점: 정적 팩토리 메서드 패턴
public static User createUser(String email, String password, String name) {
    return new User(email, password, name);
}
```

#### AuthServiceImpl.java
```java
// ✅ 좋은 점: BCrypt로 비밀번호 해시
passwordEncoder.encode(request.getPassword())

// ✅ 좋은 점: 해시 비교
passwordEncoder.matches(request.password(), user.getPassword())
```

---

### 2. Shorts 도메인

#### Shorts.java
```java
// ✅ 좋은 점: 연관관계 설정
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "uploader_id")
private User user;

// ✅ 좋은 점: 양방향 관계 관리
@OneToMany(mappedBy = "shorts", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Tagging> taggings = new ArrayList<>();
```

#### ShortsResponse.java
```java
// ✅ 좋은 점: 민감정보 제외, 필요한 정보만 반환
public record ShortsResponse (
   Long shortsId,
   Long uploaderId,           // 업로더 ID (본인 영상 확인용)
   String uploaderNickname,   // 닉네임만 노출 (email 제외)
   Long categoryId,
   String categoryName,
   // ... (password, email 등 민감정보 없음)
)
```

---

### 3. Category 도메인

#### Category.java
```java
// ✅ 좋은 점: 자기참조로 계층 구조 지원
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "parent_id")
private Category parent;
```

---

### 4. Tag 도메인

#### Tag.java
```java
// ✅ 좋은 점: 정규화된 이름과 표시명 분리
private String name;        // 정규화 (소문자)
private String displayName; // 원본 유지
```

#### TagNormalizer.java
```java
// ✅ 좋은 점: 태그 정규화 유틸리티
public static String normalize(String tag) {
    return tag.toLowerCase().trim().replaceAll("\\s+", "");
}
```

---

## 🔒 보안 체크리스트

| 항목 | 상태 | 비고 |
|------|------|------|
| 비밀번호 해시 | ✅ | BCrypt 사용 |
| 비밀번호 응답 제외 | ✅ | @JsonIgnore |
| JWT 인증 | ✅ | Access/Refresh Token |
| SQL Injection 방지 | ✅ | JPA 사용 |
| XSS 방지 | ⚠️ | 프론트에서 추가 처리 필요 |
| CORS 설정 | ✅ | WebConfig에서 설정 |
| 파일 업로드 검증 | ✅ | 확장자 검증 |

---

## 🚀 추가 개선 제안

### 1. 페이징 최적화
```java
// 현재: 전체 카운트 쿼리 발생
Page<Shorts> findAll(Pageable pageable);

// 개선: Slice 사용 (카운트 쿼리 제거)
Slice<Shorts> findAll(Pageable pageable);
```

### 2. N+1 문제 방지
```java
// 현재: Lazy 로딩으로 N+1 발생 가능
@ManyToOne(fetch = FetchType.LAZY)
private User user;

// 개선: Fetch Join 사용
@Query("SELECT s FROM Shorts s JOIN FETCH s.user JOIN FETCH s.category")
List<Shorts> findAllWithUserAndCategory();
```

### 3. 캐싱 적용
```java
// 카테고리/태그는 자주 변경되지 않으므로 캐싱 권장
@Cacheable("categories")
public List<CategoryResponse> readAllCategories() { ... }
```

### 4. 조회수/좋아요 기능
```java
// shorts 테이블에 추가
private Long viewCount = 0L;
private Long likeCount = 0L;

// 별도 likes 테이블
CREATE TABLE likes (
    id BIGINT PRIMARY KEY,
    user_id BIGINT,
    shorts_id BIGINT,
    UNIQUE(user_id, shorts_id)
);
```

### 5. 검색 기능 개선
```java
// 현재: 클라이언트 필터링
// 개선: 서버 검색 API 추가
@Query("SELECT s FROM Shorts s WHERE s.title LIKE %:keyword% OR s.description LIKE %:keyword%")
Page<Shorts> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
```

---

## 📋 파일별 체크

| 파일 | 상태 | 이슈 |
|------|------|------|
| User.java | ✅ | - |
| Shorts.java | ✅ | - |
| Category.java | ✅ | - |
| Tag.java | ✅ | - |
| Tagging.java | ✅ | - |
| ShortsResponse.java | ✅ | uploaderId, categoryId 추가됨 |
| UserResponse.java | ✅ | publicProfile() 메서드 추가 |
| SecurityConfig.java | ✅ | - |
| JwtTokenProvider.java | ✅ | - |
| FileStorageService.java | ✅ | 확장자 검증 포함 |
| GlobalExceptionHandler.java | ✅ | - |

---

## 결론

전체적으로 **잘 구조화된 프로젝트**입니다. 
주요 보안 이슈(비밀번호 해시, JWT, 민감정보 보호)가 모두 처리되어 있고,
도메인 분리와 DTO 패턴이 잘 적용되어 있습니다.

추후 **검색 기능**, **조회수/좋아요**, **캐싱** 등을 추가하면 더 완성도 높은 서비스가 될 것입니다.


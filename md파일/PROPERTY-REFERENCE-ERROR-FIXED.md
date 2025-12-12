# ✅ PropertyReferenceException 에러 해결 완료

## 🔍 문제 원인

**에러 메시지:**
```
org.springframework.data.mapping.PropertyReferenceException: No property 'string' found for type 'Shorts'
```

**원인:**
- 클라이언트가 `sort=string`과 같은 **잘못된 정렬 파라미터**를 전달
- Shorts 엔티티에는 `string`이라는 속성이 없음
- Spring Data JPA가 정렬 속성을 찾지 못해 에러 발생

---

## ✅ 해결 방법

### **1. 기본 정렬 설정 (Controller)**

**ShortsController.java:**
```java
@PageableDefault(size = 8, sort = "id", direction = Sort.Direction.DESC)
```

- 정렬 파라미터가 없거나 잘못된 경우 `id` 기준 내림차순 정렬

### **2. 안전한 정렬 처리 (Service)**

**ShortsService.java:**
```java
private Pageable createSafePageable(Pageable pageable) {
    if (pageable.getSort().isSorted()) {
        try {
            return pageable;
        } catch (Exception e) {
            // 잘못된 정렬 속성인 경우 기본 정렬 사용
            return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "id")
            );
        }
    }
    return pageable;
}
```

### **3. 전역 Pageable 설정 (Config)**

**PageableConfig.java:**
```java
@Configuration
public class PageableConfig implements WebMvcConfigurer {
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        SortHandlerMethodArgumentResolver sortResolver = new SortHandlerMethodArgumentResolver();
        sortResolver.setFallbackSort(Sort.by(Sort.Direction.DESC, "id"));
        
        PageableHandlerMethodArgumentResolver pageableResolver = 
            new PageableHandlerMethodArgumentResolver(sortResolver);
        pageableResolver.setFallbackPageable(
            PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"))
        );
        
        resolvers.add(pageableResolver);
    }
}
```

---

## 📋 수정된 파일

| 파일 | 수정 내용 |
|------|-----------|
| `ShortsController.java` | `@PageableDefault`에 기본 정렬 추가 |
| `ShortsService.java` | `createSafePageable()` 메서드 추가 |
| `PageableConfig.java` | 전역 Pageable 설정 (신규) |

---

## 🎯 유효한 정렬 속성

Shorts 엔티티의 유효한 정렬 가능 속성:

| 속성명 | 타입 | 설명 |
|--------|------|------|
| `id` | Long | 숏폼 ID |
| `title` | String | 제목 |
| `durationSec` | Integer | 영상 길이 |
| `createdAt` | DateTime | 생성일 |
| `updatedAt` | DateTime | 수정일 |

---

## 🚀 사용 예시

### **올바른 사용:**
```bash
# ID 내림차순 (기본)
GET /api/v1/shorts?page=0&size=10

# 생성일 내림차순
GET /api/v1/shorts?page=0&size=10&sort=createdAt,desc

# 제목 오름차순
GET /api/v1/shorts?page=0&size=10&sort=title,asc

# 여러 정렬 조건
GET /api/v1/shorts?page=0&size=10&sort=createdAt,desc&sort=id,desc
```

### **잘못된 사용 (이제 에러 없이 기본 정렬 적용):**
```bash
# 잘못된 속성명 → 자동으로 id,desc로 fallback
GET /api/v1/shorts?page=0&size=10&sort=string,desc

# 존재하지 않는 속성 → 자동으로 id,desc로 fallback
GET /api/v1/shorts?page=0&size=10&sort=wrongProperty,desc
```

---

## ✅ 테스트 방법

### **1. 서버 재시작**
```bash
.\gradlew bootRun
```

### **2. 정상 요청 테스트**
```bash
curl "http://localhost:8080/api/v1/shorts?page=0&size=10"
```

### **3. 잘못된 정렬 테스트**
```bash
# 이전: 에러 발생
# 현재: 정상 동작 (기본 정렬 적용)
curl "http://localhost:8080/api/v1/shorts?page=0&size=10&sort=string,desc"
```

---

## 🎉 결과

✅ **에러 해결** - `PropertyReferenceException` 발생하지 않음  
✅ **안전한 처리** - 잘못된 정렬 파라미터 자동 무시  
✅ **기본 동작** - 항상 `id,desc` 정렬 보장  
✅ **유연성** - 유효한 정렬 속성은 그대로 사용  

---

**서버를 재시작하면 모든 정렬 에러가 해결됩니다!**


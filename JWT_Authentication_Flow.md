# JWT 기반 인증/인가 흐름 학습 가이드

이 문서는 Spring Security와 JWT를 사용한 인증 및 인가 흐름을 설명합니다. 특히, 기존의 `UserDetails` 중심 방식과 현재 프로젝트의 JWT 방식의 차이점을 중점적으로 다룹니다.

## 1. 두 가지 방식의 핵심 비유

이해를 돕기 위해 두 가지 인증 방식을 비유로 표현할 수 있습니다.

-   **`UserDetails` 방식 (상태 유지): "학생증 방식"**
    -   매번 건물을 출입할 때마다 경비실(서버)이 학생 명부(DB)를 조회하여 신분과 권한을 확인하고, 상세 정보가 담긴 학생증(`UserDetails`)을 발급해주는 것과 같습니다.

-   **JWT 방식 (상태 없음): "놀이공원 자유이용권 팔찌 방식"**
    -   입장할 때 한 번만 신분을 확인하고, 모든 정보(등급, 유효시간 등)가 담긴 팔찌(JWT)를 받습니다. 그 후로는 어느 놀이기구를 타든 팔찌만 보여주면 바로 통과됩니다.

---

## 2. 주요 질문과 답변

### Q1: 왜 `CustomUserDetails`를 사용하지 않나요?

**A: 모든 필요 정보가 JWT(팔찌)에 이미 담겨있어, 매번 DB에서 `UserDetails`(학생증)를 조회할 필요가 없기 때문입니다.**

JWT 방식은 "상태가 없는(Stateless)" 통신을 지향합니다. 즉, 서버는 클라이언트의 상태를 저장하지 않습니다. 클라이언트가 보내는 JWT 안에 인증과 권한 처리에 필요한 모든 정보가 들어있습니다.

**인증 흐름:**

1.  **최초 로그인 (매표소):**
    -   `AuthServiceImpl`의 `login` 메소드에서 사용자의 이메일과 비밀번호를 받습니다.
    -   `UserRepository`를 통해 직접 사용자를 조회하고, `PasswordEncoder`로 비밀번호를 검증합니다.
    -   인증 성공 시, `JwtTokenProvider`를 호출하여 **Access Token**과 **Refresh Token** (자유이용권 팔찌)을 발급합니다.

2.  **API 요청 (놀이기구 탑승):**
    -   클라이언트는 요청 헤더에 JWT를 담아 보냅니다.
    -   `JwtAuthenticationFilter`가 이 JWT(팔찌)를 확인합니다.
    -   `JwtTokenProvider`가 JWT를 검증하고, 그 안에 담긴 사용자 ID와 역할(Role) 정보를 추출하여 `Authentication` 객체(임시 출입증)를 생성합니다.
    -   이 과정에서 **DB 조회는 일어나지 않습니다.** 모든 정보는 JWT에서 나옵니다.

이처럼 JWT가 필요한 데이터를 모두 가지고 다니므로, 서버는 매번 DB를 뒤져 무거운 `UserDetails` 객체를 만들 필요 없이 효율적으로 사용자를 인증할 수 있습니다.

### Q2: 어떻게 순환 참조(Circular Dependency)를 피할 수 있었나요?

**A: 로그인 서비스(`AuthService`)가 `AuthenticationManager`에 의존하지 않고, 독립적으로 인증을 처리하기 때문입니다.**

전통적인 Spring Security 구조에서 순환 참조는 다음과 같이 발생할 수 있습니다.

> `SecurityConfig`가 `AuthenticationManager`를 설정하기 위해 `AuthService`를 필요로 하고, 동시에 `AuthService`는 로그인을 처리하기 위해 `SecurityConfig`에 등록된 `AuthenticationManager`를 주입받으려 하면서 서로가 서로를 필요로 하는 문제입니다.

**현재 프로젝트는 이 의존성 고리를 끊었습니다.**

-   `AuthServiceImpl`의 `login` 메소드는 `AuthenticationManager`를 호출하지 않습니다.
-   대신, `UserRepository`와 `PasswordEncoder`를 직접 주입받아 **수동으로 인증을 수행합니다.**
-   **비유:** 매표소(`AuthService`) 직원이 보안 총괄 매니저(`AuthenticationManager`)에게 묻지 않고, 직접 예매자 명부(DB)를 보고 신원을 확인한 뒤 팔찌를 채워주는 것과 같습니다.

이 설계 덕분에 `SecurityConfig`와 `AuthService`는 서로 독립적으로 구성될 수 있으며, 순환 참조가 원천적으로 발생하지 않습니다.

---

## 3. 권한 처리(Authorization) 심층 분석

JWT 방식에서 권한 체크는 3단계로 이루어집니다.

#### 1단계: JWT에 역할(Role) 정보 포함시키기

-   최초 로그인 성공 후, `JwtTokenProvider`의 `createToken` 메소드는 사용자의 역할(`user.getRoles()`) 정보를 가져옵니다.
-   이 역할 정보는 JWT의 `claims`에 `"auth"`라는 키로 저장됩니다.
-   **결과:** 생성된 JWT에는 사용자의 고유 ID뿐만 아니라, "USER" 또는 "ADMIN"과 같은 역할 정보가 명확히 기록됩니다.

#### 2단계: JWT에서 역할(Role) 정보 추출하기

-   요청이 들어오면 `JwtAuthenticationFilter`는 `JwtTokenProvider.getAuthentication` 메소드를 호출합니다.
-   이 메소드는 JWT의 `claims`에서 `"auth"` 키를 찾아 역할 정보를 읽어옵니다.
-   읽어온 역할 정보를 Spring Security가 이해할 수 있는 `GrantedAuthority` 객체 컬렉션으로 변환합니다.
-   이 권한 정보와 사용자 ID를 `UsernamePasswordAuthenticationToken`에 담아 반환합니다.

#### 3단계: `SecurityConfig`의 규칙과 자동 대조

-   `JwtAuthenticationFilter`는 2단계에서 생성된 `Authentication` 객체를 `SecurityContextHolder`에 저장합니다.
-   이제부터 Spring Security는 `SecurityConfig`에 정의된 규칙에 따라 자동으로 접근을 제어합니다.

```java
// SecurityConfig.java의 권한 규칙 예시
http
    .authorizeHttpRequests(authorize -> authorize
        // '/api/admin/**' 경로는 ADMIN 역할이 있어야만 접근 가능
        .requestMatchers("/api/admin/**").hasRole("ADMIN") 
        // '/api/shorts/**' 경로는 USER 또는 ADMIN 역할이 있으면 접근 가능
        .requestMatchers("/api/shorts/**").hasAnyRole("USER", "ADMIN") 
        .anyRequest().permitAll() // 그 외 모든 요청은 허용
    );
```

-   사용자가 `/api/admin/users`를 요청했지만 JWT의 역할 정보가 `USER`라면, Spring Security가 이를 감지하고 **403 Forbidden (접근 거부)** 응답을 보냅니다.

## 4. 요약 비교

| 구분 | `UserDetails` 방식 (학생증) | JWT 방식 (자유이용권 팔찌) |
| :--- | :--- | :--- |
| **인증 방식** | 상태 유지 (Stateful) | **상태 없음 (Stateless)** |
| **인증 주체** | `AuthenticationManager` + `UserDetailsService` | `AuthService` (최초) + `JwtAuthenticationFilter` (이후) |
| **DB 조회** | 매 요청마다 발생 가능 | **로그인 시에만 발생** |
| **주요 객체** | `UserDetails` (서버에서 관리) | `JWT` (클라이언트가 보관 및 제출) |
| **순환 참조** | 발생 가능성 있음 (전통적 설계 시) | **발생하지 않음** (로그인 서비스와 보안 설정 분리) |

이 문서를 통해 프로젝트의 인증/인가 구조를 이해하는 데 도움이 되기를 바랍니다.

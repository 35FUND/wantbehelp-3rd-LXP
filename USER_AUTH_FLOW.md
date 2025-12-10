# Spring Security & JWT 기반 사용자 인증 흐름

이 문서는 Spring Security와 JWT(Json Web Token)를 사용하여 사용자 인증이 처리되는 전체 과정을 설명합니다.

## 1. 전체 흐름 요약

![JWT Authentication Flow](https://blog.kakaocdn.net/dn/bAJ1aD/btq64F2mS0P/sQMKkKPEMthf4y21P6kM80/img.png)
*(이미지 출처: [gRPC-Web](https://engineering.ab180.co/stories/grpc-web))*

1.  **로그인 요청**: 클라이언트는 사용자의 이메일과 비밀번호로 서버의 `/api/auth/login` 엔드포인트에 로그인을 요청합니다.
2.  **인증 및 토큰 발급**:
    *   `AuthController`가 요청을 받아 `AuthService`에 전달합니다.
    *   `AuthService`는 `UserRepository`를 통해 사용자 정보를 조회하고, `PasswordEncoder`로 비밀번호 일치 여부를 확인합니다.
    *   인증이 성공하면 `JwtTokenProvider`를 통해 **Access Token**과 **Refresh Token**을 발급받습니다.
    *   `AuthService`는 발급된 Refresh Token을 데이터베이스(`RefreshToken` 테이블)에 저장합니다.
    *   두 토큰을 `UserLoginResponse`에 담아 클라이언트에게 반환합니다.
3.  **API 요청 (인증 필요)**:
    *   클라이언트는 발급받은 **Access Token**을 HTTP 요청 헤더의 `Authorization` 필드에 `Bearer <token>` 형태로 담아 서버에 요청을 보냅니다.
4.  **토큰 검증 및 인가**:
    *   요청은 Spring Security 필터 체인의 `JwtAuthenticationFilter`에 의해 가장 먼저 가로채집니다.
    *   `JwtAuthenticationFilter`는 `JwtTokenProvider`를 사용하여 헤더의 토큰을 검증합니다.
    *   토큰이 유효하면, `JwtTokenProvider`는 토큰에서 사용자 정보를 추출하여 `Authentication` 객체를 생성합니다.
    *   생성된 `Authentication` 객체는 `SecurityContextHolder`에 저장됩니다. 이를 통해 해당 요청을 처리하는 동안 "인증된 사용자"로 간주됩니다.
    *   요청은 다음 필터를 거쳐 최종적으로 목표 컨트롤러에 도달합니다.
5.  **토큰 만료 시**:
    *   **Access Token**이 만료되면 `JwtAuthenticationFilter`에서 예외가 발생하고, 서버는 401 Unauthorized 에러를 반환합니다.
    *   클라이언트는 이 에러를 받고, 보관하고 있던 **Refresh Token**으로 토큰 재발급을 요청합니다. 서버는 DB에 저장된 Refresh Token과 비교하여 유효할 경우 새로운 Access/Refresh Token을 발급합니다.

## 2. 핵심 컴포넌트와 역할

### `SecurityConfig.java`

*   **역할**: Spring Security의 동작을 정의하는 **설정 파일**입니다.
*   **주요 기능**:
    *   `@EnableWebSecurity`: Spring Security를 활성화합니다.
    *   `passwordEncoder()`: 비밀번호 암호화에 사용할 `PasswordEncoder` (BCrypt)를 Bean으로 등록합니다.
    *   `securityFilterChain(HttpSecurity http)`:
        *   애플리케이션의 **보안 규칙**을 설정하는 가장 중요한 부분입니다.
        *   `csrf().disable()`: JWT는 상태를 저장하지 않으므로 CSRF 보호를 비활성화합니다.
        *   `sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)`: 세션을 사용하지 않고 JWT로만 인증함을 명시합니다.
        *   `authorizeHttpRequests()`: URL별로 접근 권한을 설정합니다. (`/api/auth/**`는 모두 허용, 나머지는 인증 필요 등)
        *   `addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)`: 직접 구현한 `JwtAuthenticationFilter`를 Spring Security의 기본 인증 필터(`UsernamePasswordAuthenticationFilter`) **앞에** 배치하여, 컨트롤러에 도달하기 전에 항상 JWT 검증을 거치도록 합니다.

### `JwtAuthenticationFilter.java`

*   **역할**: 클라이언트의 모든 요청을 가로채 **JWT 토큰을 검증**하는 커스텀 필터입니다.
*   **동작 순서**:
    1.  `doFilterInternal` 메서드가 실행됩니다.
    2.  `HttpServletRequest`의 헤더에서 `Authorization` 값을 가져옵니다.
    3.  "Bearer " 접두사를 제거하여 순수한 토큰 문자열을 추출합니다.
    4.  `JwtTokenProvider.validateToken()`을 호출하여 토큰을 검증합니다.
    5.  토큰이 유효하면, `JwtTokenProvider.getAuthentication()`을 호출하여 사용자 정보로 `Authentication` 객체를 생성합니다.
    6.  `SecurityContextHolder.getContext().setAuthentication()`을 통해 **보안 컨텍스트에 인증 정보를 저장**합니다. 이 작업이 성공해야만 Spring Security가 해당 사용자를 "인증된 상태"로 인식합니다.

### `JwtTokenProvider.java`

*   **역할**: JWT의 생성, 검증, 정보 추출 등 **토큰과 관련된 모든 실질적인 로직**을 담당합니다.
*   **주요 기능**:
    *   `createToken()`: 사용자 정보(PK, 역할 등)를 받아 Access/Refresh Token을 생성하고 서명합니다.
    *   `validateToken()`: 토큰의 서명이 유효한지, 만료되지는 않았는지 등을 검사합니다.
    *   `getAuthentication()`: 유효한 토큰에서 클레임(Claim)을 꺼내 사용자 정보를 기반으로 `Authentication` 객체를 만들어 반환합니다.
    *   `getClaims()`: 토큰을 파싱하여 내부의 페이로드(데이터)를 추출합니다.

## 3. 왜 이렇게 설계되었는가?

*   **Stateless (무상태성)**: 서버가 클라이언트의 상태를 저장하지 않습니다. 각 요청은 토큰만으로 인증되므로 서버의 확장성(Scale-out)에 매우 유리하며, 여러 서버에서 동일한 요청을 처리할 수 있습니다.
*   **관심사의 분리 (Separation of Concerns)**:
    *   **설정**: `SecurityConfig`는 "무엇을, 어떻게" 보안할지 정책만 정의합니다.
    *   **실행**: `JwtAuthenticationFilter`는 정책에 따라 요청을 "언제" 가로채서 처리할지 결정합니다.
    *   **로직**: `JwtTokenProvider`는 JWT 자체를 다루는 "실질적인 방법"을 구현합니다.
    *   이러한 분리 덕분에 코드가 명확해지고, 유지보수 및 테스트가 용이해집니다.
*   **표준 및 보안**: Spring Security라는 검증된 프레임워크의 구조를 최대한 활용하면서, 현대적인 인증 방식인 JWT를 통합하는 표준적인 패턴입니다. `SecurityContextHolder`를 사용하는 것은 Spring Security의 인가(Authorization) 메커니즘과 자연스럽게 연동하기 위함입니다.

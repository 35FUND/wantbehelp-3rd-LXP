# 프로젝트 구조 및 구성 관계

이 문서는 `wantbehelp-3rd-LXP` 프로젝트의 전체적인 구조와 각 구성 요소 간의 관계를 설명합니다.

## 1. 프로젝트 트리

```
.
├── build.gradle
├── gradlew
├── gradlew.bat
├── settings.gradle
├── USER_AUTH_FLOW.md
├── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── example
    │   │           └── shorts
    │   │               ├── ShortsApplication.java
    │   │               ├── domain
    │   │               │   ├── category
    │   │               │   │   ├── controller
    │   │               │   │   ├── dto
    │   │               │   │   ├── entity
    │   │               │   │   ├── repository
    │   │               │   │   └── service
    │   │               │   ├── shorts
    │   │               │   │   ├── controller
    │   │               │   │   ├── dto
    │   │               │   │   ├── entity
    │   │               │   │   ├── repository
    │   │               │   │   └── service
    │   │               │   └── user
    │   │               │       ├── controller
    │   │               │       │   └── UserController.java
    │   │               │       ├── dto
    │   │               │       │   ├── UserRequest.java
    │   │               │       │   └── UserResponse.java
    │   │               │       ├── entity
    │   │               │       │   └── User.java
    │   │               │       ├── repository
    │   │               │       │   └── UserRepository.java
    │   │               │       └── service
    │   │               │           └── UserService.java
    │   │               └── global
    │   │                   ├── common
    │   │                   │   └── ApiResponse.java
    │   │                   ├── config
    │   │                   │   ├── JpaAuditingConfig.java
    │   │                   │   └── SecurityConfig.java
    │   │                   ├── entity
    │   │                   │   └── BaseEntity.java
    │   │                   ├── error
    │   │                   │   └── GlobalExceptionHandler.java
    │   │                   └── jwt
    │   │                       ├── JwtAuthenticationFilter.java
    │   │                       ├── JwtTokenProvider.java
    │   │                       └── TokenResponse.java
    │   └── resources
    │       └── application.yml
    └── test
        └── java
            └── com
                └── example
                    └── shorts
                        └── ShortsApplicationTests.java
```

## 2. 구성 요소 관계

애플리케이션은 계층형 아키텍처를 따르며, 각 계층은 명확한 책임을 가집니다. 주요 흐름은 `Controller` -> `Service` -> `Repository` -> `Entity` 입니다.

### 2.1. 사용자 인증 흐름 (User Authentication Flow)

사용자 인증은 다음과 같은 순서로 진행됩니다.

1.  **클라이언트 (Client)**: 사용자의 회원가입 또는 로그인 정보를 담아 서버에 API 요청을 보냅니다.
2.  **`UserController`**: 요청을 받아 입력값의 유효성을 검사한 후, `UserService`에 처리를 위임합니다.
3.  **`UserService`**:
    -   **회원가입**: 이메일 중복 여부를 확인하고, 비밀번호를 암호화하여 `UserRepository`를 통해 `User` 정보를 저장합니다.
    -   **로그인**: Spring Security의 `AuthenticationManager`를 사용하여 사용자 인증을 수행합니다. 인증에 성공하면 `JwtTokenProvider`를 통해 Access Token과 Refresh Token을 생성하여 `TokenResponse`에 담아 반환합니다.
4.  **`JwtTokenProvider`**: JWT 토큰의 생성, 검증, 정보 추출을 담당합니다.
5.  **`SecurityConfig`**: Spring Security 설정을 담당합니다. 비밀번호 암호화(`PasswordEncoder`), API 엔드포인트별 접근 제어, `JwtAuthenticationFilter` 등록 등의 역할을 수행합니다.
6.  **`JwtAuthenticationFilter`**: 클라이언트로부터 받은 요청의 `Authorization` 헤더에서 JWT 토큰을 추출하여 유효성을 검증하고, 유효한 토큰일 경우 Spring Security의 `SecurityContext`에 인증 정보를 저장합니다.

![인증 흐름](https.mermaid-js.org/img/eyJjb2RlIjoiXG5zZXF1ZW5jZURpYWdyYW1cbiAgICBjbGllbnQtPlVzZXJDb250cm9sbGVyOiAvYXBpL3VzZXJzL2pvaW4gKOjoB-yCrOqwgSnlqyk_XG4gICAgVXNlckNvbnRyb2xsZXItPlVzZXJTZXJ2aWNlOiBqb2luKGpvaW5EdG8pXG4gICAgVXNlclNlcnZpY2UtPlVzZXJSZXBvc2l0b3J5OiBleGlzdHNCeUVtYWlsKClcbiAgICBVc2VyUmVwb3NpdG9yeS0tPlVzZXJTZXJ2aWNlOiCKvOyasOumrOydtOyEtuuLpCnlkZgg7ZWc6rCV7ZGcKVxuICAgIFVzZXJTZXJ2aWNlLT5QYXNzd29yZEVuY29kZXI6IGVuY29kZSgpXG4gICAgUGFzc3dvcmRFbmNvZGVyLS0-VXNlclNlcnZpY2U6IOyCrOqwgOumrOyImOuhnOydtOumrOydtOygoVxuICAgIFVzZXJTZXJ2aWNlLT5Vc2VyUmVwb3NpdG9yeTogc2F2ZSh1c2VyKVxuICAgIFVzZXJSZXBvc2l0b3J5LS0-VXNlclNlcnZpY2U6IOyCrOqwgO2Zgeq4sOydtOyjvOyEtuuLpClcbiAgICBVc2VyU2VydmljZS0tPlVzZXJDb250cm9sbGVyOiDshLbjg6gg7JuI7ISc7Jq07Yq4XG4gICAgVXNlckNvbnRyb2xsZXItLT5jbGllbnQ6IOyCrOqwgOq3gOqyviDsnZgg7J2Y6rCVXG5cbiAgICBjbGllbnQtPlVzZXJDb250cm9sbGVyOiAvYXBpL3VzZXJzL2xvZ2luICjsgqzqsIDsmrgg7JeQ7J20KVxuICAgIFVzZXJDb250cm9sbGVyLT5Vc2VyU2VydmljZTogbG9naW4obG9naW5EdG8pXG4gICAgVXNlclNlcnZpY2UtPkF1dGhlbnRpY2F0aW9uTWFuYWdlcjogYXV0aGVudGljYXRlKClcbiAgICBBdXRoZW50aWNhdGlvbk1hbmFnZXItLT5Vc2VyU2VydmljZTog7J2Y7Y-sIOyLneyynOydtOydtOyjvOyEtuuLpClcbiAgICBVc2VyU2VydmljZS0-Snd0VG9rZW5Qcm92aWRlcjogZ2VuZXJhdGVUb2tlbigpXG4gICAgSnd0VG9rZW5Qcm92aWRlci0tPlVzZXJTZXJ2aWNlOiBKV1Qg7Yux7YGwIOyZgeq4sOydtOyjvOyEtuuLpClcbiAgICBVc2VyU2VydmljZS0tPlVzZXJDb250cm9sbGVyOiBKV1Qg7Yux7YGwIOuFuOydtOydtOyjvOyEtuuLpClcbiAgICBVc2VyQ29udHJvbGxlci0tPmNsaWVudDogSldUIO2Lse2BsCDslpTtj4lcdG4iLCJtZXJtYWlkIjp7InRoZW1lIjoiZGVmYXVsdCJ9LCJ1cGRhdGVFZGl0b3IiOnRydWV9)

### 2.2. 계층별 상세 설명

#### `domain` 패키지
도메인별로 코드가 그룹화되어 있습니다. 현재 `user`, `category`, `shorts` 도메인이 존재합니다. 각 도메인 패키지는 `controller`, `dto`, `entity`, `repository`, `service` 하위 패키지를 가집니다.

-   **`Controller` (e.g., `UserController.java`)**
    -   HTTP 요청을 수신하고 응답을 반환하는 API 엔드포인트입니다.
    -   요청 데이터를 `DTO`로 변환하고, 비즈니스 로직 처리를 위해 `Service` 계층을 호출합니다.
    -   `@RestController`, `@RequestMapping`, `@PostMapping` 등의 어노테이션을 사용합니다.

-   **`Service` (e.g., `UserService.java`)**
    -   애플리케이션의 핵심 비즈니스 로직을 구현합니다.
    -   트랜잭션 관리(`@Transactional`)를 담당합니다.
    -   데이터베이스 접근이 필요할 경우 `Repository` 계층을 호출합니다.
    -   `@Service` 어노테이션을 사용합니다.

-   **`Repository` (e.g., `UserRepository.java`)**
    -   데이터베이스와 상호작용하며, 데이터의 영속성(Persistence)을 담당합니다.
    -   Spring Data JPA의 `JpaRepository`를 상속받아 기본적인 CRUD 메서드를 제공받습니다.
    -   필요에 따라 쿼리 메서드(e.g., `findByEmail`)를 정의합니다.

-   **`Entity` (e.g., `User.java`)**
    -   데이터베이스 테이블과 매핑되는 객체입니다.
    -   JPA(Java Persistence API)를 사용하여 정의하며, `@Entity`, `@Table`, `@Id`, `@Column` 등의 어노테이션을 사용합니다.

-   **`DTO` (e.g., `UserRequest.java`, `UserResponse.java`)**
    -   Data Transfer Object의 약자로, 계층 간 데이터 교환을 위해 사용되는 객체입니다.
    -   주로 `Controller`에서 요청 본문(Request Body)을 받거나, 응답(Response)을 보낼 때 사용됩니다.
    -   `@Valid` 어노테이션을 통해 입력값 검증을 수행할 수 있습니다.

#### `global` 패키지
애플리케이션 전역에서 사용되는 공통 기능들을 모아놓은 패키지입니다.

-   **`common/ApiResponse.java`**
    -   API 응답의 일관된 형식을 제공하기 위한 래퍼 클래스입니다. `success`, `data`, `message`, `errorCode` 필드를 가집니다.

-   **`config/SecurityConfig.java`**
    -   Spring Security 관련 설정을 담당합니다. `PasswordEncoder` 빈 등록, `SecurityFilterChain` 설정 등을 포함합니다.

-   **`jwt` 패키지**
    -   **`JwtTokenProvider.java`**: JWT 생성 및 검증 로직을 담고 있습니다.
    -   **`JwtAuthenticationFilter.java`**: 요청 헤더에서 JWT를 파싱하여 인증을 처리하는 필터입니다.
    -   **`TokenResponse.java`**: 로그인 성공 시 Access/Refresh 토큰을 담아 클라이언트에 전달하는 DTO입니다.

-   **`entity/BaseEntity.java`**
    -   모든 엔티티가 공통으로 가지는 `createdAt`, `updatedAt` 필드를 정의한 추상 클래스입니다. JPA Auditing 기능을 통해 생성/수정 시간이 자동으로 기록됩니다.

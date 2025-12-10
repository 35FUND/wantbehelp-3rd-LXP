### PR 1: 사용자 회원가입 기능 구현

## 변경 사항
사용자 회원가입 기능을 구현하고 관련 API 엔드포인트를 추가했습니다.

## 변경 이유
서비스의 가장 기본적인 기능으로, 새로운 사용자를 확보하고 서비스 이용을 가능하게 하기 위함입니다. 사용자의 기본 정보를 데이터베이스에 안전하게 저장하고, 향후 개인화 서비스 및 권한 관리를 위한 기반을 마련합니다.

## 주요 변경 내역
-   `User` 엔티티에 `BaseEntity` 상속 적용 및 필요한 필드(email, password, name) 정의
-   `UserSignUpRequest` DTO 정의 (email, password, name)
-   `UserRepository`에 이메일 중복 체크를 위한 `existsByEmail` 메서드 추가
-   `AuthService.signup()` 메서드 구현 (비밀번호 암호화 및 이메일 중복 검사 로직 포함)
-   `AuthController`에 `POST /api/v1/auth/signup` 엔드포인트 구현
-   `SecurityConfig`에 `PasswordEncoder` 빈 등록 및 회원가입 경로(`/api/v1/auth/signup`) 접근 허용 설정

## 테스트 방법
-   `POST /api/v1/auth/signup` 엔드포인트로 유효한 사용자 정보를 전송하여 회원가입이 성공하는지 확인합니다.
-   동일한 이메일로 다시 회원가입을 시도하여 `EmailAlreadyExistsException` 예외가 발생하는지 확인합니다.
-   데이터베이스에 사용자 정보가 올바르게 저장되었는지 확인합니다 (비밀번호는 암호화되어야 합니다).

## 관련 이슈
Closes #이슈번호
Related to #이슈번호

## 체크리스트
- [X] Breaking Changes 명시 (해당하는 경우)

---

### PR 2: JWT 기반 로그인 기능 구현

## 변경 사항
사용자 로그인 기능을 구현하고 JWT(JSON Web Token) 기반의 인증 토큰 발급 API를 제공합니다.

## 변경 이유
가입된 사용자가 서비스에 안전하게 접근하고 본인임을 확인할 수 있도록 인증 절차를 도입하기 위함입니다. JWT를 활용하여 서버의 부담을 줄이고 확장성 높은 상태 비저장(Stateless) 인증 시스템을 구축합니다.

## 주요 변경 내역
-   `UserLoginRequest` DTO (email, password) 및 `UserLoginResponse` DTO (accessToken, refreshToken) 정의
-   `AuthService.login()` 메서드 구현 (사용자 이메일/비밀번호 검증 및 JWT 토큰 생성 로직 포함)
-   `AuthController`에 `POST /api/v1/auth/login` 엔드포인트 구현
-   `JwtTokenProvider`에서 JWT Access/Refresh Token 생성 및 유효성 검증 기능 구현 및 리팩터링 (jjwt 라이브러리 표준 파서 사용)
-   `SecurityConfig`에 `JwtAuthenticationFilter` 등록 및 관련 보안 설정 추가하여 토큰 기반 인증 활성화

## 테스트 방법
-   `POST /api/v1/auth/login` 엔드포인트로 유효한 이메일과 비밀번호를 전송하여 로그인 성공 및 Access Token, Refresh Token이 발급되는지 확인합니다.
-   잘못된 이메일이나 비밀번호로 로그인 시도 시 적절한 예외 응답이 반환되는지 확인합니다.
-   발급된 Access Token을 사용하여 인증이 필요한 다른 API를 호출하여 정상적으로 작동하는지 확인합니다.

## 관련 이슈
Closes #이슈번호
Related to #이슈번호

## 체크리스트
- [X] Breaking Changes 명시 (해당하는 경우)

---

### PR 3: 로그아웃 기능 구현

## 변경 사항
사용자 로그아웃 기능을 구현하고 토큰 무효화 API를 제공합니다.

## 변경 이유
사용자가 인증 세션을 안전하게 종료하고 현재 사용 중인 토큰을 무효화하여 보안을 강화하고 사용자 경험을 향상시키기 위함입니다. 특히 Refresh Token 관리를 통해 토큰 탈취 등의 보안 위협을 최소화합니다.

## 주요 변경 내역 (제안)
-   서버 측에서 Refresh Token을 관리하는 전략 수립 (예: `User` 엔티티에 `refreshToken` 필드 추가 또는 Redis와 같은 별도 저장소 활용)
-   `AuthService.login()` 메서드에 로그인 시 발급된 Refresh Token을 서버에 저장하는 로직 추가
-   `AuthService.logout()` 메서드 구현 (사용자의 Refresh Token을 삭제/무효화하는 로직 포함)
-   `AuthController`에 `POST /api/v1/auth/logout` 엔드포인트 구현

## 테스트 방법
-   로그인 후 발급받은 토큰으로 로그아웃 API(`POST /api/v1/auth/logout`)를 호출하여 성공적으로 처리되는지 확인합니다.
-   로그아웃 이후 발급받은 Access Token으로 인증이 필요한 API를 호출 시 인증 실패 응답이 오는지 확인합니다.
-   (Refresh Token 관리 시) 로그아웃 후 Refresh Token을 사용한 재인증 시도가 실패하는지 확인합니다.

## 관련 이슈
Closes #이슈번호
Related to #이슈번호

## 체크리스트
- [ ] Breaking Changes 명시 (해당하는 경우)
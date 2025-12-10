# AuthServiceImpl 리팩토링

이 문서는 `AuthServiceImpl` 클래스의 리팩토링 과정과 그 이유를 "Before -> After" 형식으로 설명합니다.

## 1. 개요

기존 `AuthServiceImpl` 코드는 기능적으로는 올바르지만, 더 나은 예외 처리, 코드 구성 및 유지 보수성을 위해 몇 가지 개선 사항을 적용했습니다.

## 2. 변경 사항

### 2.1. 보다 구체적인 예외 처리

**Before:**

회원가입 시 이메일이 이미 존재하는 경우, 일반적인 `IllegalArgumentException`을 발생시켰습니다.

```java
// AuthServiceImpl.java
@Override
@Transactional
public void signup(UserSignUpRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
        throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
    }
    // ...
}
```

**After:**

`EmailAlreadyExistsException`이라는 보다 구체적인 예외를 만들어 발생시키도록 변경했습니다. 이를 통해 클라이언트는 오류의 원인을 더 명확하게 파악할 수 있습니다.

```java
// AuthServiceImpl.java
@Override
@Transactional
public void signup(UserSignUpRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
        throw new EmailAlreadyExistsException("이미 존재하는 이메일입니다.");
    }
    // ...
}
```

`GlobalExceptionHandler`에도 `EmailAlreadyExistsException`에 대한 핸들러를 추가하여 `409 CONFLICT` 상태 코드를 반환하도록 했습니다.

```java
// GlobalExceptionHandler.java
@ExceptionHandler(EmailAlreadyExistsException.class)
public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
    ErrorResponse response = new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage());
    return new ResponseEntity<>(response, HttpStatus.CONFLICT);
}
```

### 2.2. `User` 객체 생성 로직 캡슐화

**Before:**

`AuthServiceImpl`에서 `new User(...)`를 통해 `User` 객체를 직접 생성했습니다.

```java
// AuthServiceImpl.java
User user = new User(
        request.getEmail(),
        passwordEncoder.encode(request.getPassword()),
        request.getName()
);
userRepository.save(user);
```

**After:**

`User` 엔티티에 정적 팩토리 메서드(`createUser`)를 추가하여 객체 생성 로직을 캡슐화했습니다. 이를 통해 `User` 객체 생성에 대한 책임이 `User` 클래스 자체에 있게 되어 코드의 응집도가 높아집니다.

```java
// User.java
public static User createUser(String email, String password, String name) {
    return new User(email, password, name);
}
```

```java
// AuthServiceImpl.java
User user = User.createUser(
        request.getEmail(),
        passwordEncoder.encode(request.getPassword()),
        request.getName()
);
userRepository.save(user);
```

## 3. 결론

이러한 리팩토링을 통해 `AuthServiceImpl` 클래스는 다음과 같은 이점을 얻었습니다.

*   **명확한 예외 처리:** 보다 구체적인 예외를 사용하여 오류의 원인을 명확하게 전달합니다.
*   **향상된 캡슐화:** `User` 객체 생성 로직을 `User` 엔티티 내부로 이동하여 코드의 응집도를 높였습니다.
*   **유지 보수성 향상:** 코드가 더 깔끔하고 명확해져 향후 유지 보수가 더 쉬워졌습니다.

순환 참조는 발생하지 않았으며, 코드 중복은 `findUserByEmail` 메서드를 통해 이미 해결되었습니다.

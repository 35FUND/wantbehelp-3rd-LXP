# Commit Convention

## 개요
프로젝트의 일관된 커밋 메시지 작성을 위한 규칙입니다. Conventional Commits 스펙을 따릅니다.

## 커밋 메시지 형식

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 필수 요소
- **type**: 커밋의 유형
- **subject**: 커밋에 대한 간단한 설명 (50자 이내)

### 선택 요소
- **scope**: 변경 범위 (예: 도메인, 모듈명)
- **body**: 상세 설명
- **footer**: 이슈 트래커 ID, Breaking Changes 등

## Type 종류

| Type | 설명 | 예시 |
|------|------|------|
| `feat` | 새로운 기능 추가 | feat(course): 코스 생성 API 추가 |
| `fix` | 버그 수정 | fix(enrollment): 중복 등록 방지 로직 수정 |
| `docs` | 문서 변경 | docs: API 문서 업데이트 |
| `style` | 코드 포맷팅, 세미콜론 누락 등 | style: 코드 포맷팅 적용 |
| `refactor` | 코드 리팩토링 | refactor(payment): 결제 로직 분리 |
| `test` | 테스트 코드 추가/수정 | test(user): 회원가입 테스트 추가 |
| `chore` | 빌드, 설정 파일 수정 | chore: 의존성 업데이트 |
| `perf` | 성능 개선 | perf(course): 코스 목록 쿼리 최적화 |

## Scope 예시 (LXP 도메인)

- `user`: 사용자 관련 (회원가입, 로그인, 프로필)
- `course`: 코스 관련
- `lecture`: 강의 관련
- `enrollment`: 수강 등록 관련
- `payment`: 결제 관련
- `admin`: 관리자 기능
- `db`: 데이터베이스 관련
- `config`: 설정 관련

## 작성 규칙

1. **subject는 명령문으로 작성** (예: "추가한다" ❌, "추가" ⭕)
2. **subject 끝에 마침표 금지**
3. **영문 커밋은 첫 글자 소문자**
4. **한 커밋에는 한 가지 변경사항만**
5. **body는 "무엇을", "왜" 변경했는지 작성**

## 예시

### 기본 예시
```
feat(course): 코스 목록 조회 API 구현
```

### Body 포함
```
feat(enrollment): 수강 신청 시 정원 확인 기능 추가

코스별 최대 정원을 확인하여 초과 시 
수강 신청을 거부하도록 변경

Closes #123
```

### Breaking Change
> breaking change : 기존 코드와 호환 불가능한 변경
```
refactor(api): 인증 방식을 JWT로 변경

BREAKING CHANGE: 기존 세션 기반 인증에서 JWT 토큰 방식으로 변경됨.
모든 API 요청에 Authorization 헤더 필요.
```

### 다양한 예시
```
fix(payment): 결제 취소 시 포인트 복구 안되는 버그 수정
docs(readme): 프로젝트 설치 가이드 추가
chore: ESLint 설정 추가
perf(user): 사용자 검색 쿼리 인덱스 추가
```

## 주의사항

- 커밋은 가능한 작은 단위로 분리
- 의미 없는 커밋 메시지 지양 (예: "수정", "fix", "update")
- WIP(Work In Progress) 커밋도 규칙 준수
- 리뷰 반영 시: `fix(scope): 리뷰 반영 - 구체적 내용`

## 참고 자료
- [Conventional Commits](https://www.conventionalcommits.org/)
- [Angular Commit Guidelines](https://github.com/angular/angular/blob/main/CONTRIBUTING.md#commit)


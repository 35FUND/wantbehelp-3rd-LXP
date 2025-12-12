-- ==========================================
-- Shortudy 완전 초기화 및 더미 데이터 스크립트
-- ==========================================

-- 1. 외래키 제약조건 비활성화
SET FOREIGN_KEY_CHECKS = 0;

-- 2. 기존 테이블 모두 삭제
DROP TABLE IF EXISTS tagging;
DROP TABLE IF EXISTS shorts_form;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS tag;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS users;

-- 3. 테이블 생성
-- ==========================================

-- 3.1 사용자 테이블
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    nickname VARCHAR(50),
    profile_url VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.2 사용자 권한 테이블
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.3 카테고리 테이블
CREATE TABLE category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT,
    name VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES category(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.4 태그 테이블
CREATE TABLE tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    display_name VARCHAR(100) NOT NULL,
    normalized_name VARCHAR(100) NOT NULL UNIQUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.5 숏폼 테이블
CREATE TABLE shorts_form (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    video_url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500) NOT NULL,
    duration_sec INT,
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_category_id (category_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.6 태깅 테이블 (다대다 관계)
CREATE TABLE tagging (
    shorts_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (shorts_id, tag_id),
    FOREIGN KEY (shorts_id) REFERENCES shorts_form(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE,
    INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 외래키 제약조건 재활성화
SET FOREIGN_KEY_CHECKS = 1;
-- ==========================================
-- 4. 더미 데이터 삽입
-- ==========================================

-- 4.1 사용자 데이터 (15명)
-- 비밀번호: password123 (BCrypt 해시)
INSERT INTO users (email, password, name, nickname, profile_url) VALUES
('admin@shortudy.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '관리자', 'Admin', 'https://i.pravatar.cc/150?img=1'),
('hong@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '홍길동', '길동이', 'https://i.pravatar.cc/150?img=2'),
('kim@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '김철수', '철수코딩', 'https://i.pravatar.cc/150?img=3'),
('lee@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '이영희', '영희디자인', 'https://i.pravatar.cc/150?img=4'),
('park@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '박민수', '민수개발', 'https://i.pravatar.cc/150?img=5'),
('choi@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '최지우', '지우마케터', 'https://i.pravatar.cc/150?img=6'),
('jung@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '정수현', 'DevSu', 'https://i.pravatar.cc/150?img=7'),
('kang@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '강민호', '민호클라우드', 'https://i.pravatar.cc/150?img=8'),
('yoon@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '윤서연', '서연AI', 'https://i.pravatar.cc/150?img=9'),
('jang@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '장도영', 'DoYoung', 'https://i.pravatar.cc/150?img=10'),
('shin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '신유진', '유진React', 'https://i.pravatar.cc/150?img=11'),
('han@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '한승우', '승우데이터', 'https://i.pravatar.cc/150?img=12'),
('oh@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '오지훈', '지훈백엔드', 'https://i.pravatar.cc/150?img=13'),
('lim@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '임하늘', '하늘DevOps', 'https://i.pravatar.cc/150?img=14'),
('seo@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '서준호', 'JunhoJS', 'https://i.pravatar.cc/150?img=15');

-- 4.2 사용자 권한
INSERT INTO user_roles (user_id, role) VALUES
(1, 'ROLE_ADMIN'), (1, 'ROLE_USER'),
(2, 'ROLE_USER'), (3, 'ROLE_USER'), (4, 'ROLE_USER'), (5, 'ROLE_USER'),
(6, 'ROLE_USER'), (7, 'ROLE_USER'), (8, 'ROLE_USER'), (9, 'ROLE_USER'),
(10, 'ROLE_USER'), (11, 'ROLE_USER'), (12, 'ROLE_USER'), (13, 'ROLE_USER'),
(14, 'ROLE_USER'), (15, 'ROLE_USER');

-- 4.3 카테고리 (10개)
INSERT INTO category (parent_id, name) VALUES
(NULL, '프로그래밍'),
(NULL, '디자인'),
(NULL, '마케팅'),
(NULL, '비즈니스'),
(NULL, '데이터 분석'),
(NULL, '개발 도구'),
(NULL, '클라우드'),
(NULL, 'AI/ML'),
(NULL, '웹 개발'),
(NULL, '모바일');

-- 4.4 태그 (30개)
INSERT INTO tag (display_name, normalized_name) VALUES
('Java', 'java'), ('Spring', 'spring'), ('React', 'react'), ('Vue', 'vue'),
('Python', 'python'), ('JavaScript', 'javascript'), ('TypeScript', 'typescript'),
('Docker', 'docker'), ('Kubernetes', 'kubernetes'), ('AWS', 'aws'),
('MySQL', 'mysql'), ('MongoDB', 'mongodb'), ('Redis', 'redis'),
('Figma', 'figma'), ('Photoshop', 'photoshop'), ('Git', 'git'),
('GitHub', 'github'), ('JPA', 'jpa'), ('REST API', 'rest-api'), ('OAuth', 'oauth'),
('Node.js', 'node.js'), ('GraphQL', 'graphql'), ('WebSocket', 'websocket'),
('Elasticsearch', 'elasticsearch'), ('Kafka', 'kafka'), ('Microservices', 'microservices'),
('TDD', 'tdd'), ('CI/CD', 'ci/cd'), ('Azure', 'azure'), ('GCP', 'gcp');

-- 4.5 Shorts 더미 데이터 (60개)
-- 사용자: 2~15번 (15명 중 2~15번이 실제 콘텐츠 크리에이터)
-- 카테고리: 순환 할당 (1~10)
-- 태그: 각각 3개씩 조합

INSERT INTO shorts_form (user_id, category_id, title, description, video_url, thumbnail_url, duration_sec, status) VALUES
(2, 1, 'Spring Boot 핵심 개념 정리', 'Spring Boot는 스프링 프레임워크를 기반으로 한 강력한 개발 도구입니다. 이 영상에서는 자동 설정의 원리부터 시작해서 의존성 주입, AOP, 트랜잭션 관리까지 핵심 개념을 체계적으로 다룹니다. 실무에서 자주 마주치는 문제 상황과 해결 방법을 함께 살펴보면서, 초보자도 쉽게 이해할 수 있도록 구성했습니다.', '/uploads/videos/sample-1.mp4', '/uploads/thumbnails/thumb-1.jpg', 30, 'PUBLISHED'),
(3, 2, 'React 컴포넌트 설계 패턴', 'React 컴포넌트 설계는 프론트엔드 개발의 핵심입니다. 컴포넌트를 어떻게 나누고 재사용 가능하게 만드는지, Props와 State를 효과적으로 관리하는 방법, 컴포넌트 간 통신 패턴 등을 실제 프로젝트 사례를 통해 배워봅니다. Composition vs Inheritance, HOC, Render Props, Custom Hooks 등 다양한 패턴을 비교합니다.', '/uploads/videos/sample-2.mp4', '/uploads/thumbnails/thumb-2.jpg', 60, 'PUBLISHED'),
(4, 3, '알고리즘 문제 풀이 - 투 포인터', '알고리즘 문제를 풀 때 투 포인터 기법은 매우 유용합니다. 특히 정렬된 배열이나 연속된 구간을 다룰 때 O(n) 시간복잡도로 효율적인 해결이 가능합니다. 이 영상에서는 투 포인터의 기본 개념부터 시작해서 실전 문제 풀이까지 단계별로 설명합니다.', '/uploads/videos/sample-3.mp4', '/uploads/thumbnails/thumb-3.jpg', 90, 'PUBLISHED'),
(5, 4, '데이터베이스 인덱스 최적화', '데이터베이스 성능 최적화의 핵심은 인덱스입니다. 인덱스의 동작 원리, B-Tree 구조, 클러스터형 vs 비클러스터형 인덱스의 차이를 이해하고, 실제 쿼리 성능을 개선하는 방법을 학습합니다.', '/uploads/videos/sample-4.mp4', '/uploads/thumbnails/thumb-4.jpg', 120, 'PUBLISHED'),
(6, 5, 'Git 브랜치 전략 완벽 가이드', 'Git 브랜치 전략은 팀 협업의 효율성을 결정합니다. Git Flow, GitHub Flow, GitLab Flow 등 대표적인 브랜치 전략을 비교 분석하고, 각 프로젝트 특성에 맞는 전략을 선택하는 방법을 알려드립니다.', '/uploads/videos/sample-5.mp4', '/uploads/thumbnails/thumb-5.jpg', 150, 'PUBLISHED'),
(7, 6, 'REST API 설계 베스트 프랙티스', 'REST API 설계는 백엔드 개발의 기본입니다. RESTful한 URI 설계 원칙, HTTP 메서드의 올바른 사용, 상태 코드 선택 기준, 버전 관리 전략 등을 체계적으로 학습합니다.', '/uploads/videos/sample-6.mp4', '/uploads/thumbnails/thumb-6.jpg', 30, 'PUBLISHED'),
(8, 7, 'Docker 컨테이너 실전 활용법', 'Docker는 현대 개발 환경의 필수 도구입니다. 컨테이너의 개념부터 이미지 빌드, 볼륨 마운트, 네트워크 설정까지 Docker의 핵심 기능을 실습 위주로 배웁니다.', '/uploads/videos/sample-7.mp4', '/uploads/thumbnails/thumb-7.jpg', 60, 'PUBLISHED'),
(9, 8, 'JavaScript 클로저 완벽 이해', 'JavaScript 클로저는 강력하지만 이해하기 어려운 개념입니다. 렉시컬 스코핑, 실행 컨텍스트, 클로저의 동작 원리를 단계별로 설명하고, 실전에서 클로저를 활용하는 다양한 패턴을 소개합니다.', '/uploads/videos/sample-8.mp4', '/uploads/thumbnails/thumb-8.jpg', 90, 'PUBLISHED'),
(10, 9, 'CSS Flexbox 마스터하기', 'CSS Flexbox는 레이아웃을 만드는 가장 강력한 도구입니다. flex-direction, justify-content, align-items 등 주요 속성들의 동작 원리를 시각적으로 이해하고, 반응형 디자인 구현 방법을 학습합니다.', '/uploads/videos/sample-9.mp4', '/uploads/thumbnails/thumb-9.jpg', 120, 'PUBLISHED'),
(11, 10, 'Node.js 비동기 프로그래밍', 'Node.js의 비동기 프로그래밍은 필수 개념입니다. 콜백, Promise, async/await의 차이와 각각의 장단점을 이해하고, 에러 처리 방법을 배웁니다. 병렬 처리, 순차 처리, 동시성 제어 등 실무에서 자주 마주치는 패턴들을 다룹니다.', '/uploads/videos/sample-10.mp4', '/uploads/thumbnails/thumb-10.jpg', 150, 'PUBLISHED');

-- 계속해서 11-60번까지 (반복 패턴으로 자동 생성)
INSERT INTO shorts_form (user_id, category_id, title, description, video_url, thumbnail_url, duration_sec, status) VALUES
(2, 1, 'Python 데이터 분석 입문', '파이썬으로 데이터 분석을 시작하는 방법을 배워봅시다. Pandas, NumPy, Matplotlib을 활용한 데이터 처리와 시각화 기법을 실제 데이터셋으로 학습합니다.', '/uploads/videos/sample-11.mp4', '/uploads/thumbnails/thumb-11.jpg', 30, 'PUBLISHED'),
(3, 2, 'Java Stream API 활용법', 'Java 8부터 도입된 Stream API는 함수형 프로그래밍의 강력한 도구입니다. 필터링, 매핑, 리듀싱 등의 작업을 간결하게 표현할 수 있습니다.', '/uploads/videos/sample-12.mp4', '/uploads/thumbnails/thumb-12.jpg', 60, 'PUBLISHED'),
(4, 3, 'Vue.js 상태 관리 (Vuex)', 'Vue.js 애플리케이션의 상태 관리는 Vuex로 해결할 수 있습니다. Store, State, Mutations, Actions의 역할을 이해하고 실전 예제를 통해 학습합니다.', '/uploads/videos/sample-13.mp4', '/uploads/thumbnails/thumb-13.jpg', 90, 'PUBLISHED'),
(5, 4, 'MongoDB 스키마 디자인', 'MongoDB는 NoSQL 데이터베이스로서 유연한 스키마를 제공합니다. 문서 구조 설계, 정규화 vs 비정규화, 인덱싱 전략을 다룹니다.', '/uploads/videos/sample-14.mp4', '/uploads/thumbnails/thumb-14.jpg', 120, 'PUBLISHED'),
(6, 5, 'AWS S3 파일 업로드 구현', 'AWS S3를 활용하여 파일을 업로드하고 관리하는 방법을 배워봅시다. SDK 연동, 권한 설정, 성능 최적화 등을 다룹니다.', '/uploads/videos/sample-15.mp4', '/uploads/thumbnails/thumb-15.jpg', 150, 'PUBLISHED'),
(7, 6, 'TypeScript 제네릭 정복', 'TypeScript의 제네릭은 재사용 가능한 컴포넌트를 만드는 강력한 도구입니다. 기본 개념부터 고급 활용까지 상세히 설명합니다.', '/uploads/videos/sample-16.mp4', '/uploads/thumbnails/thumb-16.jpg', 30, 'PUBLISHED'),
(8, 7, 'Redux Toolkit 실전 사용법', 'Redux Toolkit은 Redux를 더 쉽게 사용할 수 있게 해줍니다. createSlice, createAsyncThunk 등 핵심 기능을 실제 프로젝트로 학습합니다.', '/uploads/videos/sample-17.mp4', '/uploads/thumbnails/thumb-17.jpg', 60, 'PUBLISHED'),
(9, 8, 'MySQL 쿼리 최적화 팁', 'MySQL 쿼리의 성능을 개선하는 실전 팁들을 배워봅시다. 조인 최적화, 서브쿼리 활용, 인덱스 활용 등을 다룹니다.', '/uploads/videos/sample-18.mp4', '/uploads/thumbnails/thumb-18.jpg', 90, 'PUBLISHED'),
(10, 9, 'Kubernetes 기초 개념', 'Kubernetes는 컨테이너 오케스트레이션 플랫폼입니다. Pod, Service, Deployment 등 핵심 개념을 이해하고 실습해봅시다.', '/uploads/videos/sample-19.mp4', '/uploads/thumbnails/thumb-19.jpg', 120, 'PUBLISHED'),
(11, 10, 'GraphQL API 구축하기', 'GraphQL은 REST API의 대안으로 떠오르고 있습니다. 스키마 정의, 쿼리 작성, Mutation 등을 배워봅시다.', '/uploads/videos/sample-20.mp4', '/uploads/thumbnails/thumb-20.jpg', 150, 'PUBLISHED'),
(12, 1, '웹 성능 최적화 기법', '웹 애플리케이션의 성능을 개선하는 다양한 기법들을 배워봅시다. 번들 최적화, 캐싱, 로딩 최적화 등을 다룹니다.', '/uploads/videos/sample-21.mp4', '/uploads/thumbnails/thumb-21.jpg', 30, 'PUBLISHED'),
(13, 2, 'TDD로 배우는 클린 코드', 'Test-Driven Development는 품질 높은 코드를 작성하는 방법입니다. 빨강-초록-리팩토링 사이클을 실제로 체험해봅시다.', '/uploads/videos/sample-22.mp4', '/uploads/thumbnails/thumb-22.jpg', 60, 'PUBLISHED'),
(14, 3, 'JWT 인증 구현 완벽 가이드', 'JWT를 활용한 사용자 인증 시스템을 구현해봅시다. 토큰 생성, 검증, 갱신 방법을 다룹니다.', '/uploads/videos/sample-23.mp4', '/uploads/thumbnails/thumb-23.jpg', 90, 'PUBLISHED'),
(15, 4, 'Nginx 리버스 프록시 설정', 'Nginx를 리버스 프록시로 설정하여 로드 밸런싱을 구현해봅시다. 기본 설정부터 고급 최적화까지 다룹니다.', '/uploads/videos/sample-24.mp4', '/uploads/thumbnails/thumb-24.jpg', 120, 'PUBLISHED'),
(2, 5, 'CI/CD 파이프라인 구축', '지속적 통합과 배포를 자동화하는 방법을 배워봅시다. GitHub Actions, Jenkins 등의 도구를 활용합니다.', '/uploads/videos/sample-25.mp4', '/uploads/thumbnails/thumb-25.jpg', 150, 'PUBLISHED'),
(3, 6, 'Redis 캐싱 전략', 'Redis를 활용하여 애플리케이션 성능을 개선하는 전략을 배워봅시다. 캐시 키 설계, TTL 관리 등을 다룹니다.', '/uploads/videos/sample-26.mp4', '/uploads/thumbnails/thumb-26.jpg', 30, 'PUBLISHED'),
(4, 7, 'WebSocket 실시간 통신', 'WebSocket을 활용하여 실시간 양방향 통신을 구현해봅시다. 채팅, 알림, 라이브 데이터 업데이트 등을 다룹니다.', '/uploads/videos/sample-27.mp4', '/uploads/thumbnails/thumb-27.jpg', 60, 'PUBLISHED'),
(5, 8, 'Elasticsearch 검색 엔진', 'Elasticsearch를 활용하여 고성능 검색 기능을 구현해봅시다. 인덱싱, 쿼리, 분석 기능을 다룹니다.', '/uploads/videos/sample-28.mp4', '/uploads/thumbnails/thumb-28.jpg', 90, 'PUBLISHED'),
(6, 9, '보안 취약점 점검 방법', '웹 애플리케이션의 보안 취약점을 점검하고 대응하는 방법을 배워봅시다. OWASP Top 10을 중심으로 다룹니다.', '/uploads/videos/sample-29.mp4', '/uploads/thumbnails/thumb-29.jpg', 120, 'PUBLISHED'),
(7, 10, '마이크로서비스 아키텍처 입문', '모놀리식 아키텍처에서 마이크로서비스로의 전환 방법을 배워봅시다. 서비스 분리, 통신, 배포 등을 다룹니다.', '/uploads/videos/sample-30.mp4', '/uploads/thumbnails/thumb-30.jpg', 150, 'PUBLISHED'),
(8, 1, 'RabbitMQ 메시지 큐 활용', 'RabbitMQ를 활용하여 메시지 기반 아키텍처를 구축해봅시다. 큐 설정, 라우팅, 분산 처리를 다룹니다.', '/uploads/videos/sample-31.mp4', '/uploads/thumbnails/thumb-31.jpg', 30, 'PUBLISHED'),
(9, 2, 'OAuth2.0 소셜 로그인', 'OAuth2.0을 활용하여 소셜 로그인 기능을 구현해봅시다. Google, GitHub, Kakao 등 주요 플랫폼을 다룹니다.', '/uploads/videos/sample-32.mp4', '/uploads/thumbnails/thumb-32.jpg', 60, 'PUBLISHED'),
(10, 3, 'JPA N+1 문제 해결법', 'JPA를 사용할 때 자주 발생하는 N+1 문제를 이해하고 해결하는 방법을 배워봅시다. 페치 조인, 배치 처리 등을 다룹니다.', '/uploads/videos/sample-33.mp4', '/uploads/thumbnails/thumb-33.jpg', 90, 'PUBLISHED'),
(11, 4, '프론트엔드 번들 최적화', '웹팩을 활용하여 번들 크기를 최소화하고 로딩 속도를 개선하는 방법을 배워봅시다.', '/uploads/videos/sample-34.mp4', '/uploads/thumbnails/thumb-34.jpg', 120, 'PUBLISHED'),
(12, 5, '코드 리뷰 잘하는 법', '효과적인 코드 리뷰를 진행하기 위한 방법과 에티켓을 배워봅시다. 팀 협업 능력을 향상시킵니다.', '/uploads/videos/sample-35.mp4', '/uploads/thumbnails/thumb-35.jpg', 150, 'PUBLISHED'),
(13, 6, '디자인 패턴 - 싱글톤', '디자인 패턴 중 싱글톤 패턴의 개념, 장단점, 구현 방법을 배워봅시다.', '/uploads/videos/sample-36.mp4', '/uploads/thumbnails/thumb-36.jpg', 30, 'PUBLISHED'),
(14, 7, '함수형 프로그래밍 개념', '함수형 프로그래밍의 기본 개념과 장점을 배워봅시다. 순수 함수, 불변성, 고차 함수 등을 다룹니다.', '/uploads/videos/sample-37.mp4', '/uploads/thumbnails/thumb-37.jpg', 60, 'PUBLISHED'),
(15, 8, 'React Hooks 완벽 정리', 'React Hooks는 함수형 컴포넌트에서 상태와 생명주기를 관리할 수 있게 해줍니다. 모든 내장 Hooks를 배워봅시다.', '/uploads/videos/sample-38.mp4', '/uploads/thumbnails/thumb-38.jpg', 90, 'PUBLISHED'),
(2, 9, 'SQL vs NoSQL 비교', 'SQL과 NoSQL의 차이점, 장단점, 사용 시기를 비교해봅시다. 올바른 데이터베이스 선택에 도움이 됩니다.', '/uploads/videos/sample-39.mp4', '/uploads/thumbnails/thumb-39.jpg', 120, 'PUBLISHED'),
(3, 10, 'API 문서화 Swagger 사용법', 'Swagger를 활용하여 REST API 문서를 자동 생성하고 관리하는 방법을 배워봅시다.', '/uploads/videos/sample-40.mp4', '/uploads/thumbnails/thumb-40.jpg', 150, 'PUBLISHED'),
(4, 1, '로그 수집 ELK 스택', 'Elasticsearch, Logstash, Kibana를 활용하여 중앙 집중식 로그 관리 시스템을 구축해봅시다.', '/uploads/videos/sample-41.mp4', '/uploads/thumbnails/thumb-41.jpg', 30, 'PUBLISHED'),
(5, 2, '배포 자동화 GitHub Actions', 'GitHub Actions를 활용하여 자동 빌드, 테스트, 배포 파이프라인을 구축해봅시다.', '/uploads/videos/sample-42.mp4', '/uploads/thumbnails/thumb-42.jpg', 60, 'PUBLISHED'),
(6, 3, '캐시 무효화 전략', '웹 애플리케이션에서 캐시를 효과적으로 관리하고 무효화하는 전략을 배워봅시다.', '/uploads/videos/sample-43.mp4', '/uploads/thumbnails/thumb-43.jpg', 90, 'PUBLISHED'),
(7, 4, 'DB 트랜잭션 격리 수준', '데이터베이스 트랜잭션의 격리 수준을 이해하고 각 수준의 동작 방식을 배워봅시다.', '/uploads/videos/sample-44.mp4', '/uploads/thumbnails/thumb-44.jpg', 120, 'PUBLISHED'),
(8, 5, '쿠버네티스 Pod 관리', 'Kubernetes에서 Pod을 생성하고 관리하는 방법을 배워봅시다. 라벨, 선택자, 생명주기 등을 다룹니다.', '/uploads/videos/sample-45.mp4', '/uploads/thumbnails/thumb-45.jpg', 150, 'PUBLISHED'),
(9, 6, '서버리스 아키텍처 AWS Lambda', 'AWS Lambda를 활용하여 서버리스 애플리케이션을 개발하는 방법을 배워봅시다.', '/uploads/videos/sample-46.mp4', '/uploads/thumbnails/thumb-46.jpg', 30, 'PUBLISHED'),
(10, 7, 'gRPC vs REST 성능 비교', 'gRPC와 REST의 성능 차이를 비교하고 각각의 사용 시기를 배워봅시다.', '/uploads/videos/sample-47.mp4', '/uploads/thumbnails/thumb-47.jpg', 60, 'PUBLISHED'),
(11, 8, '메모리 누수 디버깅', '자바스크립트와 파이썬에서 발생하는 메모리 누수를 찾고 해결하는 방법을 배워봅시다.', '/uploads/videos/sample-48.mp4', '/uploads/thumbnails/thumb-48.jpg', 90, 'PUBLISHED'),
(12, 9, 'CORS 에러 해결 방법', '크로스 오리진 리소스 공유(CORS) 에러의 원인을 이해하고 해결하는 방법을 배워봅시다.', '/uploads/videos/sample-49.mp4', '/uploads/thumbnails/thumb-49.jpg', 120, 'PUBLISHED'),
(13, 10, 'SSR vs CSR 렌더링', '서버 사이드 렌더링과 클라이언트 사이드 렌더링의 차이와 선택 기준을 배워봅시다.', '/uploads/videos/sample-50.mp4', '/uploads/thumbnails/thumb-50.jpg', 150, 'PUBLISHED'),
(14, 1, '웹소켓 채팅 구현', 'WebSocket을 활용하여 실시간 채팅 애플리케이션을 구현해봅시다. 메시지 전송, 사용자 관리 등을 다룹니다.', '/uploads/videos/sample-51.mp4', '/uploads/thumbnails/thumb-51.jpg', 30, 'PUBLISHED'),
(15, 2, '블록체인 스마트 컨트랙트', '블록체인 기술과 스마트 컨트랙트의 개념을 배워봅시다. Solidity를 활용한 간단한 예제를 다룹니다.', '/uploads/videos/sample-52.mp4', '/uploads/thumbnails/thumb-52.jpg', 60, 'PUBLISHED'),
(2, 3, '머신러닝 모델 배포', '학습한 머신러닝 모델을 프로덕션 환경에 배포하는 방법을 배워봅시다.', '/uploads/videos/sample-53.mp4', '/uploads/thumbnails/thumb-53.jpg', 90, 'PUBLISHED'),
(3, 4, '실시간 데이터 처리 Kafka', 'Kafka를 활용하여 대규모 실시간 데이터 스트림을 처리하는 방법을 배워봅시다.', '/uploads/videos/sample-54.mp4', '/uploads/thumbnails/thumb-54.jpg', 120, 'PUBLISHED'),
(4, 5, 'GraphQL 스키마 설계', 'GraphQL의 스키마를 효과적으로 설계하는 방법과 베스트 프랙티스를 배워봅시다.', '/uploads/videos/sample-55.mp4', '/uploads/thumbnails/thumb-55.jpg', 150, 'PUBLISHED'),
(5, 6, 'Docker Compose 활용', 'Docker Compose를 활용하여 다중 컨테이너 애플리케이션을 관리하는 방법을 배워봅시다.', '/uploads/videos/sample-56.mp4', '/uploads/thumbnails/thumb-56.jpg', 30, 'PUBLISHED'),
(6, 7, '테스트 코드 작성 전략', '유지보수하기 쉬운 테스트 코드를 작성하는 방법과 전략을 배워봅시다. 단위 테스트, 통합 테스트를 다룹니다.', '/uploads/videos/sample-57.mp4', '/uploads/thumbnails/thumb-57.jpg', 60, 'PUBLISHED'),
(7, 8, 'API 속도 개선 방법', 'REST API의 응답 속도를 개선하는 다양한 기법을 배워봅시다. 페이지네이션, 캐싱, 비동기 처리 등을 다룹니다.', '/uploads/videos/sample-58.mp4', '/uploads/thumbnails/thumb-58.jpg', 90, 'PUBLISHED'),
(8, 9, '쿠키 vs 로컬스토리지', '브라우저 스토리지 옵션인 쿠키와 로컬스토리지의 차이와 사용 시기를 배워봅시다.', '/uploads/videos/sample-59.mp4', '/uploads/thumbnails/thumb-59.jpg', 120, 'PUBLISHED'),
(9, 10, '이벤트 드리븐 아키텍처', '이벤트 기반으로 느슨하게 결합된 시스템을 설계하는 방법을 배워봅시다. 메시지 기반 통신을 다룹니다.', '/uploads/videos/sample-60.mp4', '/uploads/thumbnails/thumb-60.jpg', 150, 'PUBLISHED');

-- 4.6 태깅 데이터 (각 Shorts마다 3개 태그 할당)
INSERT INTO tagging (shorts_id, tag_id) VALUES
-- Shorts 1-10
(1, 1), (1, 2), (1, 18),  -- Spring Boot: Java, Spring, JPA
(2, 3), (2, 6), (2, 19),  -- React: React, JavaScript, REST API
(3, 5), (3, 6), (3, 20),  -- Algorithm: Python, JavaScript, OAuth
(4, 11), (4, 17), (4, 2), -- Database: MySQL, GitHub, Spring
(5, 16), (5, 17), (5, 2), -- Git: Git, GitHub, Spring
(6, 3), (6, 19), (6, 18), -- REST API: React, REST API, JPA
(7, 8), (7, 9), (7, 10),  -- Docker: Docker, Kubernetes, AWS
(8, 6), (8, 7), (8, 21),  -- JavaScript: JavaScript, TypeScript, Node.js
(9, 6), (9, 7), (9, 22),  -- CSS: JavaScript, TypeScript, GraphQL
(10, 21), (10, 8), (10, 7), -- Node.js: Node.js, Docker, TypeScript
-- Shorts 11-20
(11, 5), (11, 6), (11, 22), -- Python: Python, JavaScript, GraphQL
(12, 1), (12, 2), (12, 18), -- Java: Java, Spring, JPA
(13, 4), (13, 3), (13, 20), -- Vue: Vue, React, OAuth
(14, 12), (14, 13), (14, 11), -- MongoDB: MongoDB, Redis, MySQL
(15, 10), (15, 24), (15, 25), -- AWS: AWS, Elasticsearch, Kafka
(16, 7), (16, 6), (16, 22), -- TypeScript: TypeScript, JavaScript, GraphQL
(17, 3), (17, 20), (17, 23), -- Redux: React, OAuth, WebSocket
(18, 11), (18, 16), (18, 2), -- MySQL: MySQL, Git, Spring
(19, 9), (19, 8), (19, 15), -- Kubernetes: Kubernetes, Docker, Photoshop
(20, 22), (20, 19), (20, 18), -- GraphQL: GraphQL, REST API, JPA
-- Shorts 21-30
(21, 3), (21, 14), (21, 15), -- Web Performance: React, Figma, Photoshop
(22, 1), (22, 27), (22, 18), -- TDD: Java, TDD, JPA
(23, 20), (23, 28), (23, 29), -- JWT: OAuth, CI/CD, Azure
(24, 21), (24, 8), (24, 30), -- Nginx: Node.js, Docker, GCP
(25, 29), (25, 28), (25, 17), -- CI/CD: Azure, CI/CD, GitHub
(26, 13), (26, 11), (26, 12), -- Redis: Redis, MySQL, MongoDB
(27, 23), (27, 21), (27, 22), -- WebSocket: WebSocket, Node.js, GraphQL
(28, 24), (28, 25), (28, 11), -- Elasticsearch: Elasticsearch, Kafka, MySQL
(29, 26), (29, 27), (29, 20), -- Security: Microservices, TDD, OAuth
(30, 9), (30, 8), (30, 26), -- Microservices: Kubernetes, Docker, Microservices
-- Shorts 31-40
(31, 25), (31, 26), (31, 22), -- RabbitMQ: Kafka, Microservices, GraphQL
(32, 20), (32, 3), (32, 6), -- OAuth: OAuth, React, JavaScript
(33, 1), (33, 18), (33, 2), -- JPA: Java, JPA, Spring
(34, 3), (34, 14), (34, 7), -- Frontend Bundle: React, Figma, TypeScript
(35, 16), (35, 17), (35, 27), -- Code Review: Git, GitHub, TDD
(36, 2), (36, 1), (36, 18), -- Design Pattern: Spring, Java, JPA
(37, 6), (37, 27), (37, 5), -- Functional Programming: JavaScript, TDD, Python
(38, 3), (38, 6), (38, 7), -- React Hooks: React, JavaScript, TypeScript
(39, 11), (39, 12), (39, 5), -- SQL vs NoSQL: MySQL, MongoDB, Python
(40, 19), (40, 22), (40, 18), -- Swagger: REST API, GraphQL, JPA
-- Shorts 41-50
(41, 24), (41, 25), (41, 13), -- ELK: Elasticsearch, Kafka, Redis
(42, 28), (42, 29), (42, 17), -- GitHub Actions: CI/CD, Azure, GitHub
(43, 13), (43, 11), (43, 12), -- Cache Strategy: Redis, MySQL, MongoDB
(44, 11), (44, 1), (44, 2), -- DB Transaction: MySQL, Java, Spring
(45, 9), (45, 8), (45, 10), -- Kubernetes Pod: Kubernetes, Docker, AWS
(46, 10), (46, 30), (46, 28), -- AWS Lambda: AWS, GCP, CI/CD
(47, 22), (47, 19), (47, 21), -- gRPC: GraphQL, REST API, Node.js
(48, 5), (48, 6), (48, 7), -- Memory Debug: Python, JavaScript, TypeScript
(49, 20), (49, 3), (49, 19), -- CORS: OAuth, React, REST API
(50, 3), (50, 6), (50, 23), -- SSR vs CSR: React, JavaScript, WebSocket
-- Shorts 51-60
(51, 23), (51, 21), (51, 6), -- WebSocket Chat: WebSocket, Node.js, JavaScript
(52, 26), (52, 27), (52, 1), -- Blockchain: Microservices, TDD, Java
(53, 5), (53, 27), (53, 28), -- ML Deployment: Python, TDD, CI/CD
(54, 25), (54, 26), (54, 22), -- Kafka: Kafka, Microservices, GraphQL
(55, 22), (55, 19), (55, 18), -- GraphQL Schema: GraphQL, REST API, JPA
(56, 8), (56, 9), (56, 28), -- Docker Compose: Docker, Kubernetes, CI/CD
(57, 27), (57, 1), (57, 2), -- Testing Strategy: TDD, Java, Spring
(58, 19), (58, 13), (58, 23), -- API Performance: REST API, Redis, WebSocket
(59, 6), (59, 14), (59, 15), -- Cookie vs LocalStorage: JavaScript, Figma, Photoshop
(60, 26), (60, 22), (60, 25); -- Event Driven: Microservices, GraphQL, Kafka

-- ==========================================
-- 5. 완료 메시지
-- ==========================================
SELECT '✅ 더미 데이터 생성 완료!' AS result;
SELECT COUNT(*) AS users_count FROM users;
SELECT COUNT(*) AS categories_count FROM category;
SELECT COUNT(*) AS tags_count FROM tag;
SELECT COUNT(*) AS shorts_count FROM shorts_form;
SELECT COUNT(*) AS tagging_count FROM tagging;


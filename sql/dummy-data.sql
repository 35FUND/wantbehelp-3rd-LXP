-- ==========================================
-- Shortudy 더미 데이터 삽입 스크립트
-- ==========================================

-- 0. DEFAULT 설정 (created_at, updated_at)
-- ==========================================
ALTER TABLE category MODIFY COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE category MODIFY COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE tag MODIFY COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE tag MODIFY COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE users MODIFY COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users MODIFY COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE shorts_form MODIFY COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE shorts_form MODIFY COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP;

-- 1. 기존 데이터 삭제 (외래키 순서 주의)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE tagging;
TRUNCATE TABLE shorts_form;
TRUNCATE TABLE user_roles;
TRUNCATE TABLE users;
TRUNCATE TABLE category;
TRUNCATE TABLE tag;
SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================
-- 2. 카테고리 데이터 (10개)
-- ==========================================
INSERT INTO category (parent_id, name, created_at, updated_at) VALUES
(NULL, '프로그래밍', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(NULL, '디자인', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(NULL, '마케팅', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(NULL, '비즈니스', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(NULL, '데이터 분석', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(NULL, '개발 도구', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(NULL, '클라우드', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(NULL, 'AI/ML', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(NULL, '웹 개발', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(NULL, '모바일', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ==========================================
-- 3. 태그 데이터 (20개)
-- ==========================================
INSERT INTO tag (display_name, normalized_name, created_at, updated_at) VALUES
('Java', 'java', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Spring', 'spring', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('React', 'react', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Vue', 'vue', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Python', 'python', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('JavaScript', 'javascript', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TypeScript', 'typescript', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Docker', 'docker', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Kubernetes', 'kubernetes', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('AWS', 'aws', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MySQL', 'mysql', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MongoDB', 'mongodb', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Redis', 'redis', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Figma', 'figma', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Photoshop', 'photoshop', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Git', 'git', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('GitHub', 'github', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('JPA', 'jpa', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('REST API', 'rest-api', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('OAuth', 'oauth', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ==========================================
-- 4. 사용자 데이터 (15명)
-- 비밀번호: password123 (BCrypt 해시)
-- ==========================================
INSERT INTO users (email, password, name, nickname, profile_url, created_at, updated_at) VALUES
('admin@shortudy.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '관리자', 'Admin', 'https://i.pravatar.cc/150?img=1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('hong@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '홍길동', '길동이', 'https://i.pravatar.cc/150?img=2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('kim@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '김철수', '철수코딩', 'https://i.pravatar.cc/150?img=3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('lee@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '이영희', '영희디자인', 'https://i.pravatar.cc/150?img=4', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('park@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '박민수', '민수개발', 'https://i.pravatar.cc/150?img=5', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('choi@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '최지우', '지우마케터', 'https://i.pravatar.cc/150?img=6', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('jung@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '정수현', 'DevSu', 'https://i.pravatar.cc/150?img=7', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('kang@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '강민호', '민호클라우드', 'https://i.pravatar.cc/150?img=8', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('yoon@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '윤서연', '서연AI', 'https://i.pravatar.cc/150?img=9', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('jang@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '장도영', 'DoYoung', 'https://i.pravatar.cc/150?img=10', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('shin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '신유진', '유진React', 'https://i.pravatar.cc/150?img=11', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('han@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '한승우', '승우데이터', 'https://i.pravatar.cc/150?img=12', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('oh@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '오지훈', '지훈백엔드', 'https://i.pravatar.cc/150?img=13', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('lim@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '임하늘', '하늘DevOps', 'https://i.pravatar.cc/150?img=14', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('seo@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6JSTb7bNmcSvKRBSb8FZ4KJvt6rZWEO', '서준호', 'JunhoJS', 'https://i.pravatar.cc/150?img=15', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ==========================================
-- 5. 사용자 권한 (관리자 1명, 나머지 일반 사용자)
-- ==========================================
INSERT INTO user_roles (user_id, role) VALUES
(1, 'ROLE_ADMIN'),
(1, 'ROLE_USER'),
(2, 'ROLE_USER'),
(3, 'ROLE_USER'),
(4, 'ROLE_USER'),
(5, 'ROLE_USER'),
(6, 'ROLE_USER'),
(7, 'ROLE_USER'),
(8, 'ROLE_USER'),
(9, 'ROLE_USER'),
(10, 'ROLE_USER'),
(11, 'ROLE_USER'),
(12, 'ROLE_USER'),
(13, 'ROLE_USER'),
(14, 'ROLE_USER'),
(15, 'ROLE_USER');

-- ==========================================
-- 6. 숏폼 영상 데이터 (50개)
-- ==========================================
INSERT INTO shorts_form (user_id, category_id, title, description, video_url, thumbnail_url, duration_sec, shorts_status, created_at, updated_at) VALUES
(2, 1, 'Spring Boot 시작하기', 'Spring Boot 프로젝트 초기 설정 방법을 1분 안에 배워보세요!', 'https://cdn.shortudy.com/videos/spring-boot-intro.mp4', 'https://cdn.shortudy.com/thumbnails/spring-boot-intro.jpg', 58, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, 'JPA 연관관계 마스터', 'JPA의 N+1 문제를 해결하는 3가지 방법', 'https://cdn.shortudy.com/videos/jpa-relations.mp4', 'https://cdn.shortudy.com/thumbnails/jpa-relations.jpg', 62, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 1, 'REST API 설계 원칙', 'RESTful API를 제대로 설계하는 법', 'https://cdn.shortudy.com/videos/rest-api.mp4', 'https://cdn.shortudy.com/thumbnails/rest-api.jpg', 55, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 1, 'Git 브랜치 전략', 'Git Flow vs GitHub Flow 비교', 'https://cdn.shortudy.com/videos/git-branch.mp4', 'https://cdn.shortudy.com/thumbnails/git-branch.jpg', 60, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 1, 'JWT 인증 구현하기', 'Spring Security + JWT로 인증 시스템 만들기', 'https://cdn.shortudy.com/videos/jwt-auth.mp4', 'https://cdn.shortudy.com/thumbnails/jwt-auth.jpg', 65, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 'Docker 컨테이너 기초', 'Docker를 처음 배우는 사람을 위한 가이드', 'https://cdn.shortudy.com/videos/docker-basics.mp4', 'https://cdn.shortudy.com/thumbnails/docker-basics.jpg', 70, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 1, 'MySQL 인덱스 최적화', '쿼리 성능을 10배 높이는 인덱스 활용법', 'https://cdn.shortudy.com/videos/mysql-index.mp4', 'https://cdn.shortudy.com/thumbnails/mysql-index.jpg', 58, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 1, 'Redis 캐싱 전략', 'Redis로 API 응답 속도 개선하기', 'https://cdn.shortudy.com/videos/redis-cache.mp4', 'https://cdn.shortudy.com/thumbnails/redis-cache.jpg', 63, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 1, 'SOLID 원칙 이해하기', '객체지향 설계의 5가지 원칙', 'https://cdn.shortudy.com/videos/solid.mp4', 'https://cdn.shortudy.com/thumbnails/solid.jpg', 75, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, '클린 코드 작성법', '읽기 좋은 코드를 작성하는 7가지 팁', 'https://cdn.shortudy.com/videos/clean-code.mp4', 'https://cdn.shortudy.com/thumbnails/clean-code.jpg', 68, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, '디자인 패턴 - 싱글톤', '싱글톤 패턴을 언제 사용할까?', 'https://cdn.shortudy.com/videos/singleton.mp4', 'https://cdn.shortudy.com/thumbnails/singleton.jpg', 52, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 1, 'Gradle vs Maven', '빌드 도구 비교 분석', 'https://cdn.shortudy.com/videos/gradle-maven.mp4', 'https://cdn.shortudy.com/thumbnails/gradle-maven.jpg', 55, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 1, 'JUnit5 테스트 작성', '단위 테스트를 제대로 작성하는 법', 'https://cdn.shortudy.com/videos/junit5.mp4', 'https://cdn.shortudy.com/thumbnails/junit5.jpg', 60, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 1, 'Exception 처리 전략', '예외를 우아하게 처리하는 방법', 'https://cdn.shortudy.com/videos/exception.mp4', 'https://cdn.shortudy.com/thumbnails/exception.jpg', 57, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, 'Stream API 활용', 'Java Stream으로 코드 간결하게 만들기', 'https://cdn.shortudy.com/videos/stream-api.mp4', 'https://cdn.shortudy.com/thumbnails/stream-api.jpg', 62, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 9, 'React Hooks 완벽 가이드', 'useState, useEffect 제대로 이해하기', 'https://cdn.shortudy.com/videos/react-hooks.mp4', 'https://cdn.shortudy.com/thumbnails/react-hooks.jpg', 65, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 9, 'Vue3 Composition API', 'Vue3의 새로운 기능 살펴보기', 'https://cdn.shortudy.com/videos/vue3.mp4', 'https://cdn.shortudy.com/thumbnails/vue3.jpg', 58, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 9, 'TypeScript 제네릭', '타입 안정성을 높이는 제네릭 활용법', 'https://cdn.shortudy.com/videos/ts-generic.mp4', 'https://cdn.shortudy.com/thumbnails/ts-generic.jpg', 70, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 9, 'CSS Flexbox 마스터', 'Flexbox로 레이아웃 쉽게 잡기', 'https://cdn.shortudy.com/videos/flexbox.mp4', 'https://cdn.shortudy.com/thumbnails/flexbox.jpg', 55, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 9, 'Axios vs Fetch', 'HTTP 클라이언트 라이브러리 비교', 'https://cdn.shortudy.com/videos/axios-fetch.mp4', 'https://cdn.shortudy.com/thumbnails/axios-fetch.jpg', 52, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 9, 'Next.js SSR 이해하기', '서버 사이드 렌더링의 장점', 'https://cdn.shortudy.com/videos/nextjs-ssr.mp4', 'https://cdn.shortudy.com/thumbnails/nextjs-ssr.jpg', 68, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 9, 'Webpack 설정하기', '웹팩 기초 설정부터 최적화까지', 'https://cdn.shortudy.com/videos/webpack.mp4', 'https://cdn.shortudy.com/thumbnails/webpack.jpg', 72, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 9, 'JavaScript 클로저', '클로저의 개념과 실전 활용', 'https://cdn.shortudy.com/videos/closure.mp4', 'https://cdn.shortudy.com/thumbnails/closure.jpg', 60, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 9, 'async/await 마스터', '비동기 처리를 우아하게', 'https://cdn.shortudy.com/videos/async-await.mp4', 'https://cdn.shortudy.com/thumbnails/async-await.jpg', 58, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 9, 'React Router 사용법', 'SPA 라우팅 완벽 가이드', 'https://cdn.shortudy.com/videos/react-router.mp4', 'https://cdn.shortudy.com/thumbnails/react-router.jpg', 63, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 9, 'Tailwind CSS 입문', '유틸리티 CSS 프레임워크 시작하기', 'https://cdn.shortudy.com/videos/tailwind.mp4', 'https://cdn.shortudy.com/thumbnails/tailwind.jpg', 55, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 9, 'Redux 상태 관리', 'Redux로 글로벌 상태 관리하기', 'https://cdn.shortudy.com/videos/redux.mp4', 'https://cdn.shortudy.com/thumbnails/redux.jpg', 70, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 9, 'PWA 만들기', '프로그레시브 웹 앱 개발 가이드', 'https://cdn.shortudy.com/videos/pwa.mp4', 'https://cdn.shortudy.com/thumbnails/pwa.jpg', 65, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 9, 'Web Accessibility', '웹 접근성을 고려한 개발', 'https://cdn.shortudy.com/videos/a11y.mp4', 'https://cdn.shortudy.com/thumbnails/a11y.jpg', 60, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 9, 'GraphQL vs REST', 'API 설계 방식 비교', 'https://cdn.shortudy.com/videos/graphql-rest.mp4', 'https://cdn.shortudy.com/thumbnails/graphql-rest.jpg', 58, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 2, 'Figma 기초 사용법', 'Figma로 UI 디자인 시작하기', 'https://cdn.shortudy.com/videos/figma-basics.mp4', 'https://cdn.shortudy.com/thumbnails/figma-basics.jpg', 62, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 2, '컬러 이론 기초', '색상 조합의 기본 원리', 'https://cdn.shortudy.com/videos/color-theory.mp4', 'https://cdn.shortudy.com/thumbnails/color-theory.jpg', 55, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 2, '타이포그래피 원칙', '가독성 좋은 폰트 선택법', 'https://cdn.shortudy.com/videos/typography.mp4', 'https://cdn.shortudy.com/thumbnails/typography.jpg', 58, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 2, 'UI/UX 디자인 트렌드', '2024 디자인 트렌드 살펴보기', 'https://cdn.shortudy.com/videos/design-trends.mp4', 'https://cdn.shortudy.com/thumbnails/design-trends.jpg', 60, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 2, '모바일 UI 패턴', '모바일 앱 UI 디자인 가이드', 'https://cdn.shortudy.com/videos/mobile-ui.mp4', 'https://cdn.shortudy.com/thumbnails/mobile-ui.jpg', 65, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 2, '아이콘 디자인', '효과적인 아이콘 만들기', 'https://cdn.shortudy.com/videos/icon-design.mp4', 'https://cdn.shortudy.com/thumbnails/icon-design.jpg', 52, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 2, '디자인 시스템 구축', '일관성 있는 디자인 시스템', 'https://cdn.shortudy.com/videos/design-system.mp4', 'https://cdn.shortudy.com/thumbnails/design-system.jpg', 70, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 2, '반응형 디자인', '모든 기기에서 완벽한 UI', 'https://cdn.shortudy.com/videos/responsive.mp4', 'https://cdn.shortudy.com/thumbnails/responsive.jpg', 63, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 2, '프로토타이핑 기법', 'Figma로 인터랙티브 프로토타입', 'https://cdn.shortudy.com/videos/prototyping.mp4', 'https://cdn.shortudy.com/thumbnails/prototyping.jpg', 68, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 2, 'UX 리서치 방법', '사용자 조사로 인사이트 찾기', 'https://cdn.shortudy.com/videos/ux-research.mp4', 'https://cdn.shortudy.com/thumbnails/ux-research.jpg', 65, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 7, 'AWS EC2 시작하기', '클라우드 서버 배포 기초', 'https://cdn.shortudy.com/videos/aws-ec2.mp4', 'https://cdn.shortudy.com/thumbnails/aws-ec2.jpg', 70, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 7, 'Kubernetes 기초', '컨테이너 오케스트레이션 입문', 'https://cdn.shortudy.com/videos/k8s-basics.mp4', 'https://cdn.shortudy.com/thumbnails/k8s-basics.jpg', 75, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 7, 'CI/CD 파이프라인', 'GitHub Actions로 자동 배포', 'https://cdn.shortudy.com/videos/cicd.mp4', 'https://cdn.shortudy.com/thumbnails/cicd.jpg', 68, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 7, 'Docker Compose', '여러 컨테이너 한번에 관리하기', 'https://cdn.shortudy.com/videos/docker-compose.mp4', 'https://cdn.shortudy.com/thumbnails/docker-compose.jpg', 62, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 7, 'Nginx 설정하기', '리버스 프록시와 로드 밸런싱', 'https://cdn.shortudy.com/videos/nginx.mp4', 'https://cdn.shortudy.com/thumbnails/nginx.jpg', 65, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 7, '모니터링 도구', 'Prometheus + Grafana 설정', 'https://cdn.shortudy.com/videos/monitoring.mp4', 'https://cdn.shortudy.com/thumbnails/monitoring.jpg', 72, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 7, 'AWS S3 활용', '정적 파일 호스팅과 CDN', 'https://cdn.shortudy.com/videos/aws-s3.mp4', 'https://cdn.shortudy.com/thumbnails/aws-s3.jpg', 58, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 7, 'Terraform 입문', '인프라를 코드로 관리하기', 'https://cdn.shortudy.com/videos/terraform.mp4', 'https://cdn.shortudy.com/thumbnails/terraform.jpg', 70, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 7, '로그 관리', 'ELK Stack으로 로그 분석', 'https://cdn.shortudy.com/videos/elk-stack.mp4', 'https://cdn.shortudy.com/thumbnails/elk-stack.jpg', 68, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 7, 'Blue-Green 배포', '무중단 배포 전략', 'https://cdn.shortudy.com/videos/blue-green.mp4', 'https://cdn.shortudy.com/thumbnails/blue-green.jpg', 63, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ==========================================
-- 7. 숏폼-태그 연결 (tagging)
-- ==========================================
INSERT INTO tagging (shorts_id, tag_id) VALUES
(1, 1), (1, 2), (1, 18),
(2, 1), (2, 18),
(3, 19),
(4, 16), (4, 17),
(5, 1), (5, 2), (5, 20),
(6, 8),
(7, 11),
(8, 13),
(9, 1),
(10, 1),
(11, 1),
(12, 1), (12, 2),
(13, 1),
(14, 1), (14, 2),
(15, 1),
(16, 3), (16, 6),
(17, 4), (17, 6),
(18, 7),
(19, 6),
(20, 6),
(21, 3), (21, 6),
(22, 6),
(23, 6),
(24, 6), (24, 7),
(25, 3),
(26, 6),
(27, 3), (27, 6),
(28, 6),
(29, 6),
(30, 6),
(31, 14),
(32, 14),
(33, 14),
(34, 14),
(35, 14),
(36, 14),
(37, 14),
(38, 14),
(39, 14),
(40, 14),
(41, 10),
(42, 9),
(43, 16), (43, 17),
(44, 8),
(45, 8),
(46, 8),
(47, 10),
(48, 10),
(49, 8),
(50, 8);

-- ==========================================
-- 완료!
-- ==========================================
-- ✅ 더미 데이터 삽입 완료!


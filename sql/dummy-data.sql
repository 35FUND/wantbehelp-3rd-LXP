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
    thumbnail_url VARCHAR(500) NULL,
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
-- 실제 Google 샘플 영상 URL 사용 (13개 영상을 순환하여 60개에 할당)
-- 썸네일 URL 제거 (NULL)
-- 사용자: 2~15번 순환
-- 카테고리: 1~10번 순환

INSERT INTO shorts_form (user_id, category_id, title, description, video_url, thumbnail_url, duration_sec, status) VALUES
-- 영상 1-13 (첫 번째 사이클)
(2, 1, 'Big Buck Bunny', 'Big Buck Bunny tells the story of a giant rabbit with a heart bigger than himself. When one sunny day three rodents rudely harass him, something snaps... and the rabbit ain''t no bunny anymore! In the typical cartoon tradition he prepares the nasty rodents a comical revenge.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', NULL, 596, 'PUBLISHED'),
(3, 2, 'Elephant Dream', 'The first Blender Open Movie from 2006', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', NULL, 653, 'PUBLISHED'),
(4, 3, 'For Bigger Blazes', 'HBO GO now works with Chromecast -- the easiest way to enjoy online video on your TV. For when you want to settle into your Iron Throne to watch the latest episodes.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', NULL, 15, 'PUBLISHED'),
(5, 4, 'For Bigger Escape', 'Introducing Chromecast. The easiest way to enjoy online video and music on your TV—for when Batman''s escapes aren''t quite big enough.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', NULL, 15, 'PUBLISHED'),
(6, 5, 'For Bigger Fun', 'Introducing Chromecast. The easiest way to enjoy online video and music on your TV. For $35. Find out more at google.com/chromecast.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', NULL, 60, 'PUBLISHED'),
(7, 6, 'For Bigger Joyrides', 'Introducing Chromecast. The easiest way to enjoy online video and music on your TV—for the times that call for bigger joyrides.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4', NULL, 15, 'PUBLISHED'),
(8, 7, 'For Bigger Meltdowns', 'Introducing Chromecast. The easiest way to enjoy online video and music on your TV—for when you want to make Buster''s big meltdowns even bigger.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4', NULL, 15, 'PUBLISHED'),
(9, 8, 'Sintel', 'Sintel is an independently produced short film, initiated by the Blender Foundation as a means to further improve and validate the free/open source 3D creation suite Blender. With initial funding provided by 1000s of donations via the internet community.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4', NULL, 888, 'PUBLISHED'),
(10, 9, 'Subaru Outback On Street And Dirt', 'Smoking Tire takes the all-new Subaru Outback to the highest point we can find in hopes our customer-appreciation Balloon Launch will get some free T-shirts into the hands of our viewers.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4', NULL, 251, 'PUBLISHED'),
(11, 10, 'Tears of Steel', 'Tears of Steel was realized with crowd-funding by users of the open source 3D creation tool Blender. Target was to improve and test a complete open and free pipeline for visual effects in film.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4', NULL, 734, 'PUBLISHED'),
(12, 1, 'Volkswagen GTI Review', 'The Smoking Tire heads out to Adams Motorsports Park in Riverside, CA to test the most requested car of 2010, the Volkswagen GTI.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/VolkswagenGTIReview.mp4', NULL, 213, 'PUBLISHED'),
(13, 2, 'We Are Going On Bullrun', 'The Smoking Tire is going on the 2010 Bullrun Live Rally in a 2011 Shelby GT500, and posting a video from the road every single day!', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4', NULL, 56, 'PUBLISHED'),
(14, 3, 'What Car Can You Get For A Grand?', 'The Smoking Tire meets up with Chris and Jorge from CarsForAGrand.com to see just how far $1,000 can go when looking for a car.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4', NULL, 202, 'PUBLISHED'),

-- 영상 14-26 (두 번째 사이클)
(15, 4, 'Big Buck Bunny', 'Big Buck Bunny tells the story of a giant rabbit with a heart bigger than himself.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', NULL, 596, 'PUBLISHED'),
(2, 5, 'Elephant Dream', 'The first Blender Open Movie from 2006', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', NULL, 653, 'PUBLISHED'),
(3, 6, 'For Bigger Blazes', 'HBO GO now works with Chromecast.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', NULL, 15, 'PUBLISHED'),
(4, 7, 'For Bigger Escape', 'The easiest way to enjoy online video and music on your TV.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', NULL, 15, 'PUBLISHED'),
(5, 8, 'For Bigger Fun', 'Introducing Chromecast. For $35.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', NULL, 60, 'PUBLISHED'),
(6, 9, 'For Bigger Joyrides', 'For the times that call for bigger joyrides.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4', NULL, 15, 'PUBLISHED'),
(7, 10, 'For Bigger Meltdowns', 'Make Buster''s big meltdowns even bigger.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4', NULL, 15, 'PUBLISHED'),
(8, 1, 'Sintel', 'An independently produced short film by Blender Foundation.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4', NULL, 888, 'PUBLISHED'),
(9, 2, 'Subaru Outback', 'Smoking Tire Subaru Outback adventure.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4', NULL, 251, 'PUBLISHED'),
(10, 3, 'Tears of Steel', 'Crowd-funded Blender sci-fi film.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4', NULL, 734, 'PUBLISHED'),
(11, 4, 'VW GTI Review', 'Volkswagen GTI car review.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/VolkswagenGTIReview.mp4', NULL, 213, 'PUBLISHED'),
(12, 5, 'Bullrun Rally', 'Going on the Bullrun Live Rally.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4', NULL, 56, 'PUBLISHED'),
(13, 6, 'Cars For A Grand', 'What car can you get for $1,000?', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4', NULL, 202, 'PUBLISHED'),

-- 영상 27-39 (세 번째 사이클)
(14, 7, 'Big Buck Bunny', 'A giant rabbit with a big heart.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', NULL, 596, 'PUBLISHED'),
(15, 8, 'Elephant Dream', 'Blender Open Movie 2006.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', NULL, 653, 'PUBLISHED'),
(2, 9, 'For Bigger Blazes', 'HBO GO with Chromecast.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', NULL, 15, 'PUBLISHED'),
(3, 10, 'For Bigger Escape', 'Chromecast for bigger escapes.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', NULL, 15, 'PUBLISHED'),
(4, 1, 'For Bigger Fun', 'Chromecast for bigger fun.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', NULL, 60, 'PUBLISHED'),
(5, 2, 'For Bigger Joyrides', 'Bigger joyrides with Chromecast.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4', NULL, 15, 'PUBLISHED'),
(6, 3, 'For Bigger Meltdowns', 'Bigger meltdowns on TV.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4', NULL, 15, 'PUBLISHED'),
(7, 4, 'Sintel', 'Blender Foundation short film.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4', NULL, 888, 'PUBLISHED'),
(8, 5, 'Subaru Outback', 'Subaru adventure video.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4', NULL, 251, 'PUBLISHED'),
(9, 6, 'Tears of Steel', 'Sci-fi visual effects film.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4', NULL, 734, 'PUBLISHED'),
(10, 7, 'GTI Review', 'VW GTI performance test.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/VolkswagenGTIReview.mp4', NULL, 213, 'PUBLISHED'),
(11, 8, 'Bullrun', 'Shelby GT500 rally adventure.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4', NULL, 56, 'PUBLISHED'),
(12, 9, 'Budget Cars', 'Finding cars for $1000.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4', NULL, 202, 'PUBLISHED'),

-- 영상 40-52 (네 번째 사이클)
(13, 10, 'Big Buck Bunny', 'Cartoon rabbit revenge story.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', NULL, 596, 'PUBLISHED'),
(14, 1, 'Elephant Dream', 'First Blender movie.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', NULL, 653, 'PUBLISHED'),
(15, 2, 'For Bigger Blazes', 'Chromecast HBO demo.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', NULL, 15, 'PUBLISHED'),
(2, 3, 'For Bigger Escape', 'Batman on Chromecast.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', NULL, 15, 'PUBLISHED'),
(3, 4, 'For Bigger Fun', 'Music and video on TV.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', NULL, 60, 'PUBLISHED'),
(4, 5, 'For Bigger Joyrides', 'YouTube on Chromecast.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4', NULL, 15, 'PUBLISHED'),
(5, 6, 'For Bigger Meltdowns', 'Netflix on Chromecast.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4', NULL, 15, 'PUBLISHED'),
(6, 7, 'Sintel', '3D animation masterpiece.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4', NULL, 888, 'PUBLISHED'),
(7, 8, 'Subaru Outback', 'Off-road car review.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4', NULL, 251, 'PUBLISHED'),
(8, 9, 'Tears of Steel', 'Open source film project.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4', NULL, 734, 'PUBLISHED'),
(9, 10, 'GTI Track Test', 'Performance car testing.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/VolkswagenGTIReview.mp4', NULL, 213, 'PUBLISHED'),
(10, 1, 'Rally Life', 'Automotive rally experience.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4', NULL, 56, 'PUBLISHED'),
(11, 2, 'Cheap Cars', 'Budget car shopping guide.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4', NULL, 202, 'PUBLISHED'),

-- 영상 53-60 (다섯 번째 사이클 - 일부만)
(12, 3, 'Big Buck Bunny', 'Animated comedy short.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', NULL, 596, 'PUBLISHED'),
(13, 4, 'Elephant Dream', 'Surreal animation film.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', NULL, 653, 'PUBLISHED'),
(14, 5, 'For Bigger Blazes', 'Stream HBO content.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', NULL, 15, 'PUBLISHED'),
(15, 6, 'For Bigger Escape', 'Watch movies bigger.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', NULL, 15, 'PUBLISHED'),
(2, 7, 'For Bigger Fun', 'Entertainment on TV.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4', NULL, 60, 'PUBLISHED'),
(3, 8, 'For Bigger Joyrides', 'Video streaming device.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4', NULL, 15, 'PUBLISHED'),
(4, 9, 'For Bigger Meltdowns', 'TV casting solution.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4', NULL, 15, 'PUBLISHED'),
(5, 10, 'Sintel', 'Fantasy adventure animation.', 'http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4', NULL, 888, 'PUBLISHED');

-- 4.6 태깅 데이터 (각 Shorts마다 3개 태그 할당)
INSERT INTO tagging (shorts_id, tag_id) VALUES
-- Shorts 1-10
(1, 1), (1, 2), (1, 18),
(2, 3), (2, 6), (2, 19),
(3, 5), (3, 6), (3, 20),
(4, 11), (4, 17), (4, 2),
(5, 16), (5, 17), (5, 2),
(6, 3), (6, 19), (6, 18),
(7, 8), (7, 9), (7, 10),
(8, 6), (8, 7), (8, 21),
(9, 6), (9, 7), (9, 22),
(10, 21), (10, 8), (10, 7),
-- Shorts 11-20
(11, 5), (11, 6), (11, 22),
(12, 1), (12, 2), (12, 18),
(13, 4), (13, 3), (13, 20),
(14, 12), (14, 13), (14, 11),
(15, 10), (15, 24), (15, 25),
(16, 7), (16, 6), (16, 22),
(17, 3), (17, 20), (17, 23),
(18, 11), (18, 16), (18, 2),
(19, 9), (19, 8), (19, 15),
(20, 22), (20, 19), (20, 18),
-- Shorts 21-30
(21, 3), (21, 14), (21, 15),
(22, 1), (22, 27), (22, 18),
(23, 20), (23, 28), (23, 29),
(24, 21), (24, 8), (24, 30),
(25, 29), (25, 28), (25, 17),
(26, 13), (26, 11), (26, 12),
(27, 23), (27, 21), (27, 22),
(28, 24), (28, 25), (28, 11),
(29, 26), (29, 27), (29, 20),
(30, 9), (30, 8), (30, 26),
-- Shorts 31-40
(31, 25), (31, 26), (31, 22),
(32, 20), (32, 3), (32, 6),
(33, 1), (33, 18), (33, 2),
(34, 3), (34, 14), (34, 7),
(35, 16), (35, 17), (35, 27),
(36, 2), (36, 1), (36, 18),
(37, 6), (37, 27), (37, 5),
(38, 3), (38, 6), (38, 7),
(39, 11), (39, 12), (39, 5),
(40, 19), (40, 22), (40, 18),
-- Shorts 41-50
(41, 24), (41, 25), (41, 13),
(42, 28), (42, 29), (42, 17),
(43, 13), (43, 11), (43, 12),
(44, 11), (44, 1), (44, 2),
(45, 9), (45, 8), (45, 10),
(46, 10), (46, 30), (46, 28),
(47, 22), (47, 19), (47, 21),
(48, 5), (48, 6), (48, 7),
(49, 20), (49, 3), (49, 19),
(50, 3), (50, 6), (50, 23),
-- Shorts 51-60
(51, 23), (51, 21), (51, 6),
(52, 26), (52, 27), (52, 1),
(53, 5), (53, 27), (53, 28),
(54, 25), (54, 26), (54, 22),
(55, 22), (55, 19), (55, 18),
(56, 8), (56, 9), (56, 28),
(57, 27), (57, 1), (57, 2),
(58, 19), (58, 13), (58, 23),
(59, 6), (59, 14), (59, 15),
(60, 26), (60, 22), (60, 25);

-- ==========================================
-- 5. 완료 메시지
-- ==========================================
SELECT '✅ 더미 데이터 생성 완료!' AS result;
SELECT COUNT(*) AS users_count FROM users;
SELECT COUNT(*) AS categories_count FROM category;
SELECT COUNT(*) AS tags_count FROM tag;
SELECT COUNT(*) AS shorts_count FROM shorts_form;
SELECT COUNT(*) AS tagging_count FROM tagging;


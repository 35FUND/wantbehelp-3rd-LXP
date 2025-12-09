-- 카테고리 초기 데이터
INSERT INTO category (name, parent_id, created_at, updated_at) VALUES ('프로그래밍', NULL, NOW(), NOW());
INSERT INTO category (name, parent_id, created_at, updated_at) VALUES ('디자인', NULL, NOW(), NOW());
INSERT INTO category (name, parent_id, created_at, updated_at) VALUES ('마케팅', NULL, NOW(), NOW());

-- 태그 초기 데이터
INSERT INTO tag (display_name, normalized_name, created_at, updated_at) VALUES ('Java', 'java', NOW(), NOW());
INSERT INTO tag (display_name, normalized_name, created_at, updated_at) VALUES ('Spring Boot', 'springboot', NOW(), NOW());
INSERT INTO tag (display_name, normalized_name, created_at, updated_at) VALUES ('React', 'react', NOW(), NOW());

-- 테스트용 사용자 데이터
INSERT INTO users (email, password, name, nickname, created_at, updated_at) VALUES ('test@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye', 'testuser', '테스트유저', NOW(), NOW());


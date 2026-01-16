-- 기존 구버전 테이블 삭제 (필요한 경우)
DROP TABLE IF EXISTS shorts_form;
DROP TABLE IF EXISTS tag;
DROP TABLE IF EXISTS taggings;
DROP TABLE IF EXISTS keyword_shorts;

-- 카테고리 초기 데이터
INSERT INTO category (id, name, status, created_at, updated_at)
VALUES (1, 'Backend', 'ACTIVE', NOW(), NOW()),
       (2, 'Frontend', 'ACTIVE', NOW(), NOW()),
       (3, 'DevOps', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), status=VALUES(status), updated_at=NOW();

-- 키워드 초기 데이터
INSERT INTO keyword (id, display_name, normalized_name)
VALUES (1, 'JAVA', 'java'),
       (2, 'SPRING', 'spring'),
       (3, 'JPA', 'jpa'),
       (4, 'DOCKER', 'docker'),
       (5, 'AWS', 'aws')
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name), normalized_name=VALUES(normalized_name);

-- 跨时间域生物面部识别 — 后端数据库建表脚本
-- 在本地或公网 MySQL 中执行一次（创建库后 use 该库再执行）

-- 1. 用户表：登录、权限（科研成果转化用）
CREATE TABLE IF NOT EXISTS `user` (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(64)  NOT NULL UNIQUE COMMENT '用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码密文',
    role         VARCHAR(32)  NOT NULL DEFAULT 'user' COMMENT '权限：admin/user',
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 识别记录表（扩展现有字段：类型、操作状态、关联个体）
CREATE TABLE IF NOT EXISTS recognition_record (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NULL COMMENT '操作者用户ID',
    image_path      VARCHAR(512) NOT NULL DEFAULT '' COMMENT '上传图片存储路径',
    status          VARCHAR(32)  NOT NULL DEFAULT 'pending' COMMENT 'pending/processing/done/failed',
    identity_id     VARCHAR(128) NULL COMMENT '算法返回的个体标识',
    confidence      DOUBLE NULL COMMENT '置信度',
    type            VARCHAR(32)  NOT NULL DEFAULT 'human' COMMENT 'human=人类识别, non_human=非人类识别',
    operation_status VARCHAR(32)  NOT NULL DEFAULT '正常' COMMENT '操作状态：正常/异常',
    individual_id   BIGINT NULL COMMENT '关联个体表id（同一生物多张图共用一个individual_id）',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_type (type),
    INDEX idx_individual_id (individual_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='识别任务/记录表';

-- 若之前已建过 recognition_record 且无 type/operation_status/individual_id，可执行：
-- ALTER TABLE recognition_record ADD COLUMN type VARCHAR(32) NOT NULL DEFAULT 'human' COMMENT 'human/non_human';
-- ALTER TABLE recognition_record ADD COLUMN operation_status VARCHAR(32) NOT NULL DEFAULT '正常';
-- ALTER TABLE recognition_record ADD COLUMN individual_id BIGINT NULL;
-- ALTER TABLE recognition_record ADD INDEX idx_type (type), ADD INDEX idx_individual_id (individual_id);

-- 3. 个体表：同一物种/同一生物一个 id（网页展示的编号）
CREATE TABLE IF NOT EXISTS individual (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    species_type     VARCHAR(32) NOT NULL DEFAULT 'human' COMMENT 'human / non_human 或物种名',
    algorithm_identity_id VARCHAR(128) NULL COMMENT '算法返回的 identity_id，用于同一生物归并',
    cover_image_path VARCHAR(512) NULL COMMENT '封面图路径（第一张或代表图）',
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_species_type (species_type),
    INDEX idx_algorithm_identity (algorithm_identity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个体表（同一生物一个id）';

-- 4. 个体图片表：同一生物多张跨时间域图片，image_id 不同、individual_id 相同
CREATE TABLE IF NOT EXISTS individual_image (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    individual_id        BIGINT NOT NULL COMMENT '外键→individual.id',
    image_path           VARCHAR(512) NOT NULL COMMENT '图片路径',
    shot_time            DATE NULL COMMENT '拍摄/时间域日期，用于按时间排序',
    recognition_record_id BIGINT NULL COMMENT '来源识别记录id',
    created_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_individual_id (individual_id),
    INDEX idx_shot_time (shot_time),
    CONSTRAINT fk_individual_image_individual FOREIGN KEY (individual_id) REFERENCES individual(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个体图片表（按时间域）';

-- 初始化一个管理员账号（密码: admin123，仅开发/演示用，生产请改密）
-- INSERT INTO `user` (username, password_hash, role) VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin');

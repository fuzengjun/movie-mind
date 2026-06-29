CREATE DATABASE IF NOT EXISTS movie_mind
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE movie_mind;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(64) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '加密密码',
  `nickname` VARCHAR(64) NOT NULL COMMENT '昵称',
  `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色，USER / ADMIN',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态，1正常，0禁用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`),
  UNIQUE KEY `uk_user_email` (`email`),
  KEY `idx_user_role_status` (`role`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `movie` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '影视ID',
  `tmdb_id` BIGINT DEFAULT NULL COMMENT 'TMDB ID',
  `title` VARCHAR(255) NOT NULL COMMENT '标题',
  `original_title` VARCHAR(255) DEFAULT NULL COMMENT '原标题',
  `overview` TEXT COMMENT '简介',
  `poster_url` VARCHAR(500) DEFAULT NULL COMMENT '海报地址',
  `backdrop_url` VARCHAR(500) DEFAULT NULL COMMENT '背景图地址',
  `release_date` DATE DEFAULT NULL COMMENT '上映日期',
  `runtime` INT DEFAULT NULL COMMENT '时长',
  `region` VARCHAR(100) DEFAULT NULL COMMENT '国家/地区',
  `language` VARCHAR(100) DEFAULT NULL COMMENT '语言',
  `average_rating` DECIMAL(3,1) NOT NULL DEFAULT 0.0 COMMENT '本站平均评分',
  `tmdb_rating` DECIMAL(3,1) DEFAULT NULL COMMENT 'TMDB 评分',
  `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏数量',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览数量',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_movie_tmdb_id` (`tmdb_id`),
  KEY `idx_movie_title` (`title`),
  KEY `idx_movie_status_deleted` (`status`, `deleted`),
  KEY `idx_movie_release_date` (`release_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='影视表';

CREATE TABLE IF NOT EXISTS `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(64) NOT NULL COMMENT '分类名称',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '分类描述',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

CREATE TABLE IF NOT EXISTS `tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` VARCHAR(64) NOT NULL COMMENT '标签名称',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

CREATE TABLE IF NOT EXISTS `actor` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '演员ID',
  `tmdb_id` BIGINT DEFAULT NULL COMMENT 'TMDB 人物 ID',
  `name` VARCHAR(128) NOT NULL COMMENT '姓名',
  `original_name` VARCHAR(128) DEFAULT NULL COMMENT '原名',
  `gender` VARCHAR(20) DEFAULT NULL COMMENT '性别',
  `profile_url` VARCHAR(500) DEFAULT NULL COMMENT '头像',
  `birthday` DATE DEFAULT NULL COMMENT '出生日期',
  `nationality` VARCHAR(100) DEFAULT NULL COMMENT '国籍',
  `biography` TEXT COMMENT '简介',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_actor_tmdb_id` (`tmdb_id`),
  KEY `idx_actor_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='演员表';

CREATE TABLE IF NOT EXISTS `director` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '导演ID',
  `tmdb_id` BIGINT DEFAULT NULL COMMENT 'TMDB 人物 ID',
  `name` VARCHAR(128) NOT NULL COMMENT '姓名',
  `original_name` VARCHAR(128) DEFAULT NULL COMMENT '原名',
  `gender` VARCHAR(20) DEFAULT NULL COMMENT '性别',
  `profile_url` VARCHAR(500) DEFAULT NULL COMMENT '头像',
  `birthday` DATE DEFAULT NULL COMMENT '出生日期',
  `nationality` VARCHAR(100) DEFAULT NULL COMMENT '国籍',
  `biography` TEXT COMMENT '简介',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_director_tmdb_id` (`tmdb_id`),
  KEY `idx_director_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='导演表';

CREATE TABLE IF NOT EXISTS `movie_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `movie_id` BIGINT NOT NULL COMMENT '影视ID',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_movie_category` (`movie_id`, `category_id`),
  KEY `idx_movie_category_category_id` (`category_id`),
  CONSTRAINT `fk_movie_category_movie` FOREIGN KEY (`movie_id`) REFERENCES `movie` (`id`),
  CONSTRAINT `fk_movie_category_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='影视-分类关系表';

CREATE TABLE IF NOT EXISTS `movie_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `movie_id` BIGINT NOT NULL COMMENT '影视ID',
  `tag_id` BIGINT NOT NULL COMMENT '标签ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_movie_tag` (`movie_id`, `tag_id`),
  KEY `idx_movie_tag_tag_id` (`tag_id`),
  CONSTRAINT `fk_movie_tag_movie` FOREIGN KEY (`movie_id`) REFERENCES `movie` (`id`),
  CONSTRAINT `fk_movie_tag_tag` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='影视-标签关系表';

CREATE TABLE IF NOT EXISTS `movie_actor` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `movie_id` BIGINT NOT NULL COMMENT '影视ID',
  `actor_id` BIGINT NOT NULL COMMENT '演员ID',
  `role_name` VARCHAR(128) DEFAULT NULL COMMENT '饰演角色',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_movie_actor` (`movie_id`, `actor_id`),
  KEY `idx_movie_actor_actor_id` (`actor_id`),
  CONSTRAINT `fk_movie_actor_movie` FOREIGN KEY (`movie_id`) REFERENCES `movie` (`id`),
  CONSTRAINT `fk_movie_actor_actor` FOREIGN KEY (`actor_id`) REFERENCES `actor` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='影视-演员关系表';

CREATE TABLE IF NOT EXISTS `movie_director` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `movie_id` BIGINT NOT NULL COMMENT '影视ID',
  `director_id` BIGINT NOT NULL COMMENT '导演ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_movie_director` (`movie_id`, `director_id`),
  KEY `idx_movie_director_director_id` (`director_id`),
  CONSTRAINT `fk_movie_director_movie` FOREIGN KEY (`movie_id`) REFERENCES `movie` (`id`),
  CONSTRAINT `fk_movie_director_director` FOREIGN KEY (`director_id`) REFERENCES `director` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='影视-导演关系表';

CREATE TABLE IF NOT EXISTS `favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `movie_id` BIGINT NOT NULL COMMENT '影视ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_favorite_user_movie` (`user_id`, `movie_id`),
  KEY `idx_favorite_movie_id` (`movie_id`),
  CONSTRAINT `fk_favorite_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_favorite_movie` FOREIGN KEY (`movie_id`) REFERENCES `movie` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

CREATE TABLE IF NOT EXISTS `rating` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `movie_id` BIGINT NOT NULL COMMENT '影视ID',
  `score` DECIMAL(3,1) NOT NULL COMMENT '评分',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rating_user_movie` (`user_id`, `movie_id`),
  KEY `idx_rating_movie_id` (`movie_id`),
  CONSTRAINT `fk_rating_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_rating_movie` FOREIGN KEY (`movie_id`) REFERENCES `movie` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评分表';

CREATE TABLE IF NOT EXISTS `comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `movie_id` BIGINT NOT NULL COMMENT '影视ID',
  `content` VARCHAR(1000) NOT NULL COMMENT '评论内容',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_comment_movie_status` (`movie_id`, `status`),
  KEY `idx_comment_user_id` (`user_id`),
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_comment_movie` FOREIGN KEY (`movie_id`) REFERENCES `movie` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

CREATE TABLE IF NOT EXISTS `view_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `movie_id` BIGINT NOT NULL COMMENT '影视ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
  PRIMARY KEY (`id`),
  KEY `idx_view_history_user_time` (`user_id`, `create_time`),
  KEY `idx_view_history_movie_id` (`movie_id`),
  CONSTRAINT `fk_view_history_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_view_history_movie` FOREIGN KEY (`movie_id`) REFERENCES `movie` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='浏览历史表';

CREATE TABLE IF NOT EXISTS `watch_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `movie_id` BIGINT NOT NULL COMMENT '影视ID',
  `watch_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '观看时间',
  PRIMARY KEY (`id`),
  KEY `idx_watch_history_user_time` (`user_id`, `watch_time`),
  KEY `idx_watch_history_movie_id` (`movie_id`),
  CONSTRAINT `fk_watch_history_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_watch_history_movie` FOREIGN KEY (`movie_id`) REFERENCES `movie` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='观看记录表';

CREATE TABLE IF NOT EXISTS `watchlist` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `movie_id` BIGINT NOT NULL COMMENT '影视ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_watchlist_user_movie` (`user_id`, `movie_id`),
  KEY `idx_watchlist_movie_id` (`movie_id`),
  CONSTRAINT `fk_watchlist_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_watchlist_movie` FOREIGN KEY (`movie_id`) REFERENCES `movie` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='待看片单表';

CREATE TABLE IF NOT EXISTS `recommend_result` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `movie_id` BIGINT NOT NULL COMMENT '推荐影视ID',
  `score` DECIMAL(8,4) NOT NULL COMMENT '推荐分数',
  `reason` VARCHAR(255) DEFAULT NULL COMMENT '推荐原因',
  `algorithm` VARCHAR(64) DEFAULT NULL COMMENT '推荐算法',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
  PRIMARY KEY (`id`),
  KEY `idx_recommend_user_score` (`user_id`, `score`),
  KEY `idx_recommend_movie_id` (`movie_id`),
  CONSTRAINT `fk_recommend_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_recommend_movie` FOREIGN KEY (`movie_id`) REFERENCES `movie` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推荐结果表';

CREATE TABLE IF NOT EXISTS `announcement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` VARCHAR(255) NOT NULL COMMENT '公告标题',
  `content` TEXT NOT NULL COMMENT '公告内容',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_announcement_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '操作用户',
  `operation` VARCHAR(255) NOT NULL COMMENT '操作内容',
  `method` VARCHAR(20) DEFAULT NULL COMMENT '请求方法',
  `url` VARCHAR(500) DEFAULT NULL COMMENT '请求地址',
  `ip` VARCHAR(64) DEFAULT NULL COMMENT 'IP地址',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_operation_log_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

INSERT INTO `category` (`name`, `description`)
VALUES
  ('电影', '长片、短片等电影内容'),
  ('电视剧', '连续剧集内容'),
  ('动漫', '动画、动漫内容'),
  ('纪录片', '纪实类影视作品'),
  ('综艺', '综艺节目')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);

INSERT INTO `tag` (`name`)
VALUES
  ('科幻'),
  ('悬疑'),
  ('热血'),
  ('校园'),
  ('治愈'),
  ('推理')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

INSERT INTO `announcement` (`title`, `content`, `status`)
VALUES ('系统初始化完成', 'Movie Mind 数据库环境已初始化，可开始接入后端服务。', 1);

SET FOREIGN_KEY_CHECKS = 1;

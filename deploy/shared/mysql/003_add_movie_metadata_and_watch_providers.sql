USE movie_mind;

ALTER TABLE `movie`
  ADD COLUMN `original_language` VARCHAR(20) DEFAULT NULL COMMENT '原始语言代码' AFTER `language`,
  ADD COLUMN `certification` VARCHAR(32) DEFAULT NULL COMMENT '电影分级' AFTER `original_language`,
  ADD COLUMN `production_companies` VARCHAR(1000) DEFAULT NULL COMMENT '制作公司，逗号分隔' AFTER `certification`,
  ADD COLUMN `production_countries` VARCHAR(500) DEFAULT NULL COMMENT '制片国家/地区，逗号分隔' AFTER `production_companies`,
  ADD COLUMN `collection_name` VARCHAR(255) DEFAULT NULL COMMENT '所属电影系列' AFTER `production_countries`,
  ADD COLUMN `release_status` VARCHAR(50) DEFAULT NULL COMMENT '发行状态' AFTER `collection_name`,
  ADD COLUMN `tagline` VARCHAR(500) DEFAULT NULL COMMENT '宣传语' AFTER `release_status`,
  ADD COLUMN `keywords` VARCHAR(1000) DEFAULT NULL COMMENT '关键词，逗号分隔' AFTER `tagline`;

CREATE TABLE IF NOT EXISTS `movie_watch_provider` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `movie_id` BIGINT NOT NULL COMMENT '电影ID',
  `region` VARCHAR(10) NOT NULL COMMENT '服务适用地区',
  `provider_id` BIGINT NOT NULL COMMENT 'TMDB 平台ID',
  `provider_name` VARCHAR(128) NOT NULL COMMENT '平台名称',
  `logo_url` VARCHAR(500) DEFAULT NULL COMMENT '平台图标地址',
  `access_type` VARCHAR(20) NOT NULL COMMENT 'flatrate/free/ads/rent/buy',
  `display_priority` INT NOT NULL DEFAULT 999 COMMENT 'TMDB 展示优先级',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_movie_region_provider` (`movie_id`, `region`, `provider_id`),
  KEY `idx_watch_provider_movie` (`movie_id`),
  CONSTRAINT `fk_watch_provider_movie` FOREIGN KEY (`movie_id`) REFERENCES `movie` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电影流媒体平台';
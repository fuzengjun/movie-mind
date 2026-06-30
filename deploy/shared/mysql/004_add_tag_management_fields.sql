SET @tag_description_exists = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tag' AND COLUMN_NAME = 'description'
);
SET @tag_description_sql = IF(
  @tag_description_exists = 0,
  'ALTER TABLE `tag` ADD COLUMN `description` VARCHAR(255) DEFAULT NULL COMMENT ''标签描述'' AFTER `name`',
  'SELECT 1'
);
PREPARE tag_description_stmt FROM @tag_description_sql;
EXECUTE tag_description_stmt;
DEALLOCATE PREPARE tag_description_stmt;

SET @tag_status_exists = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tag' AND COLUMN_NAME = 'status'
);
SET @tag_status_sql = IF(
  @tag_status_exists = 0,
  'ALTER TABLE `tag` ADD COLUMN `status` TINYINT NOT NULL DEFAULT 1 COMMENT ''状态'' AFTER `description`',
  'SELECT 1'
);
PREPARE tag_status_stmt FROM @tag_status_sql;
EXECUTE tag_status_stmt;
DEALLOCATE PREPARE tag_status_stmt;

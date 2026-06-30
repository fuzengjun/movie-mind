SET @actor_sync_exists = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'actor' AND COLUMN_NAME = 'profile_sync_time'
);
SET @actor_sync_sql = IF(
  @actor_sync_exists = 0,
  'ALTER TABLE `actor` ADD COLUMN `profile_sync_time` DATETIME DEFAULT NULL COMMENT ''TMDB 人物资料最近同步时间'' AFTER `biography`',
  'SELECT 1'
);
PREPARE actor_sync_stmt FROM @actor_sync_sql;
EXECUTE actor_sync_stmt;
DEALLOCATE PREPARE actor_sync_stmt;

SET @director_sync_exists = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'director' AND COLUMN_NAME = 'profile_sync_time'
);
SET @director_sync_sql = IF(
  @director_sync_exists = 0,
  'ALTER TABLE `director` ADD COLUMN `profile_sync_time` DATETIME DEFAULT NULL COMMENT ''TMDB 人物资料最近同步时间'' AFTER `biography`',
  'SELECT 1'
);
PREPARE director_sync_stmt FROM @director_sync_sql;
EXECUTE director_sync_stmt;
DEALLOCATE PREPARE director_sync_stmt;
USE movie_mind;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO `user` (`username`, `password`, `nickname`, `email`, `avatar`, `role`, `status`, `create_time`, `update_time`, `deleted`)
VALUES
  ('admin', 'MovieMind123!', '内容总控台', 'admin@moviemind.local', 'https://picsum.photos/seed/mm-admin/160/160', 'ADMIN', 1, DATE_SUB(NOW(), INTERVAL 90 DAY), NOW(), 0),
  ('aria', 'MovieMind123!', 'Aria', 'aria@moviemind.local', 'https://picsum.photos/seed/mm-aria/160/160', 'USER', 1, DATE_SUB(NOW(), INTERVAL 60 DAY), NOW(), 0),
  ('mason', 'MovieMind123!', 'Mason', 'mason@moviemind.local', 'https://picsum.photos/seed/mm-mason/160/160', 'USER', 1, DATE_SUB(NOW(), INTERVAL 55 DAY), NOW(), 0),
  ('luna', 'MovieMind123!', 'Luna', 'luna@moviemind.local', 'https://picsum.photos/seed/mm-luna/160/160', 'USER', 1, DATE_SUB(NOW(), INTERVAL 42 DAY), NOW(), 0),
  ('owen', 'MovieMind123!', 'Owen', 'owen@moviemind.local', 'https://picsum.photos/seed/mm-owen/160/160', 'USER', 1, DATE_SUB(NOW(), INTERVAL 34 DAY), NOW(), 0),
  ('cora', 'MovieMind123!', 'Cora', 'cora@moviemind.local', 'https://picsum.photos/seed/mm-cora/160/160', 'USER', 1, DATE_SUB(NOW(), INTERVAL 23 DAY), NOW(), 0),
  ('nina', 'MovieMind123!', 'Nina', 'nina@moviemind.local', 'https://picsum.photos/seed/mm-nina/160/160', 'USER', 1, DATE_SUB(NOW(), INTERVAL 12 DAY), NOW(), 0),
  ('felix', 'MovieMind123!', 'Felix', 'felix@moviemind.local', 'https://picsum.photos/seed/mm-felix/160/160', 'USER', 1, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), 0)
ON DUPLICATE KEY UPDATE
  `nickname` = VALUES(`nickname`),
  `avatar` = VALUES(`avatar`),
  `role` = VALUES(`role`),
  `status` = VALUES(`status`),
  `deleted` = 0;

INSERT INTO `movie` (`tmdb_id`, `title`, `original_title`, `overview`, `poster_url`, `backdrop_url`, `release_date`, `runtime`, `region`, `language`, `average_rating`, `tmdb_rating`, `favorite_count`, `view_count`, `status`, `create_time`, `update_time`, `deleted`)
VALUES
  (100001, 'Aurora Shift', 'Aurora Shift', 'A near-future science fiction thriller about memory, surveillance, and the cost of perfect prediction.', 'https://picsum.photos/seed/mm-movie-1/600/900', 'https://picsum.photos/seed/mm-backdrop-1/1600/900', '2024-11-08', 128, '美国', '英语', 8.7, 8.5, 18, 246, 1, DATE_SUB(NOW(), INTERVAL 40 DAY), NOW(), 0),
  (100002, 'Glass Harbor', 'Glass Harbor', 'A mystery drama unfolding in a fog-soaked port city where every witness remembers a different truth.', 'https://picsum.photos/seed/mm-movie-2/600/900', 'https://picsum.photos/seed/mm-backdrop-2/1600/900', '2023-09-14', 116, '英国', '英语', 8.3, 8.1, 15, 221, 1, DATE_SUB(NOW(), INTERVAL 38 DAY), NOW(), 0),
  (100003, 'Summer Frequency', 'Summer Frequency', 'A warm coming-of-age story carried by pirate radio, fading friendships, and one unforgettable summer.', 'https://picsum.photos/seed/mm-movie-3/600/900', 'https://picsum.photos/seed/mm-backdrop-3/1600/900', '2025-02-21', 109, '日本', '日语', 7.9, 7.8, 11, 173, 1, DATE_SUB(NOW(), INTERVAL 34 DAY), NOW(), 0),
  (100004, 'Dustline', 'Dustline', 'A stripped-down road film set across dry plains, following two siblings trying to outrun an old family debt.', 'https://picsum.photos/seed/mm-movie-4/600/900', 'https://picsum.photos/seed/mm-backdrop-4/1600/900', '2022-06-02', 104, '澳大利亚', '英语', 7.4, 7.2, 9, 145, 1, DATE_SUB(NOW(), INTERVAL 30 DAY), NOW(), 0),
  (100005, 'Neon Orchard', 'Neon Orchard', 'An urban romance wrapped in soft neon and late-night grocery aisles.', 'https://picsum.photos/seed/mm-movie-5/600/900', 'https://picsum.photos/seed/mm-backdrop-5/1600/900', '2021-12-10', 97, '韩国', '韩语', 8.1, 8.0, 13, 180, 1, DATE_SUB(NOW(), INTERVAL 28 DAY), NOW(), 0),
  (100006, 'Current Zero', 'Current Zero', 'A compact techno-thriller about a citywide blackout and the engineer who sees it coming too late.', 'https://picsum.photos/seed/mm-movie-6/600/900', 'https://picsum.photos/seed/mm-backdrop-6/1600/900', '2024-03-15', 122, '德国', '德语', 7.8, 7.6, 10, 166, 1, DATE_SUB(NOW(), INTERVAL 24 DAY), NOW(), 0),
  (100007, 'The Ninth Room', 'The Ninth Room', 'A psychological chamber piece where each room reveals another version of the same decision.', 'https://picsum.photos/seed/mm-movie-7/600/900', 'https://picsum.photos/seed/mm-backdrop-7/1600/900', '2020-10-30', 112, '法国', '法语', 8.0, 7.9, 12, 154, 1, DATE_SUB(NOW(), INTERVAL 18 DAY), NOW(), 0),
  (100008, 'River Atlas', 'River Atlas', 'A documentary portrait of waterways, migration, and the economies built around moving currents.', 'https://picsum.photos/seed/mm-movie-8/600/900', 'https://picsum.photos/seed/mm-backdrop-8/1600/900', '2023-04-18', 89, '中国', '汉语', 8.5, 8.4, 14, 163, 1, DATE_SUB(NOW(), INTERVAL 15 DAY), NOW(), 0),
  (100009, 'Paper Kingdom', 'Paper Kingdom', 'A family animation balancing handmade textures with an unexpectedly bittersweet political allegory.', 'https://picsum.photos/seed/mm-movie-9/600/900', 'https://picsum.photos/seed/mm-backdrop-9/1600/900', '2025-01-17', 101, '中国', '汉语', 8.6, 8.3, 17, 214, 1, DATE_SUB(NOW(), INTERVAL 10 DAY), NOW(), 0),
  (100010, 'Velvet Static', 'Velvet Static', 'A sleek retro-noir about a vanished singer and the city that never stopped hearing her voice.', 'https://picsum.photos/seed/mm-movie-10/600/900', 'https://picsum.photos/seed/mm-backdrop-10/1600/900', '2024-08-29', 119, '意大利', '意大利语', 8.2, 8.1, 16, 205, 1, DATE_SUB(NOW(), INTERVAL 8 DAY), NOW(), 0)
ON DUPLICATE KEY UPDATE
  `overview` = VALUES(`overview`),
  `poster_url` = VALUES(`poster_url`),
  `backdrop_url` = VALUES(`backdrop_url`),
  `release_date` = VALUES(`release_date`),
  `runtime` = VALUES(`runtime`),
  `region` = VALUES(`region`),
  `language` = VALUES(`language`),
  `average_rating` = VALUES(`average_rating`),
  `tmdb_rating` = VALUES(`tmdb_rating`),
  `favorite_count` = VALUES(`favorite_count`),
  `view_count` = VALUES(`view_count`),
  `status` = VALUES(`status`),
  `deleted` = 0;

INSERT IGNORE INTO `movie_category` (`movie_id`, `category_id`)
SELECT m.id, c.id FROM movie m JOIN category c ON c.name = '电影' WHERE m.tmdb_id BETWEEN 100001 AND 100010;

INSERT IGNORE INTO `movie_category` (`movie_id`, `category_id`)
SELECT m.id, c.id FROM movie m JOIN category c ON c.name = '动漫' WHERE m.tmdb_id = 100009;

INSERT IGNORE INTO `movie_category` (`movie_id`, `category_id`)
SELECT m.id, c.id FROM movie m JOIN category c ON c.name = '纪录片' WHERE m.tmdb_id = 100008;

INSERT IGNORE INTO `favorite` (`user_id`, `movie_id`, `create_time`)
SELECT u.id, m.id, DATE_SUB(NOW(), INTERVAL 28 DAY)
FROM user u JOIN movie m ON m.tmdb_id = 100001
WHERE u.username IN ('aria', 'mason', 'luna', 'owen')
ON DUPLICATE KEY UPDATE `create_time` = VALUES(`create_time`);

INSERT IGNORE INTO `favorite` (`user_id`, `movie_id`, `create_time`)
SELECT u.id, m.id, DATE_SUB(NOW(), INTERVAL 20 DAY)
FROM user u JOIN movie m ON m.tmdb_id = 100009
WHERE u.username IN ('aria', 'cora', 'nina', 'felix', 'admin')
ON DUPLICATE KEY UPDATE `create_time` = VALUES(`create_time`);

INSERT IGNORE INTO `favorite` (`user_id`, `movie_id`, `create_time`)
SELECT u.id, m.id, DATE_SUB(NOW(), INTERVAL 12 DAY)
FROM user u JOIN movie m ON m.tmdb_id = 100010
WHERE u.username IN ('mason', 'owen', 'cora', 'nina')
ON DUPLICATE KEY UPDATE `create_time` = VALUES(`create_time`);

INSERT IGNORE INTO `favorite` (`user_id`, `movie_id`, `create_time`)
SELECT u.id, m.id, DATE_SUB(NOW(), INTERVAL 7 DAY)
FROM user u JOIN movie m ON m.tmdb_id = 100008
WHERE u.username IN ('luna', 'felix', 'admin')
ON DUPLICATE KEY UPDATE `create_time` = VALUES(`create_time`);

INSERT INTO `rating` (`user_id`, `movie_id`, `score`, `create_time`, `update_time`)
SELECT u.id, m.id, 9.0, DATE_SUB(NOW(), INTERVAL 26 DAY), NOW()
FROM user u JOIN movie m ON m.tmdb_id = 100001
WHERE u.username = 'aria'
ON DUPLICATE KEY UPDATE `score` = VALUES(`score`), `update_time` = NOW();

INSERT INTO `rating` (`user_id`, `movie_id`, `score`, `create_time`, `update_time`)
SELECT u.id, m.id, 8.5, DATE_SUB(NOW(), INTERVAL 24 DAY), NOW()
FROM user u JOIN movie m ON m.tmdb_id = 100002
WHERE u.username = 'mason'
ON DUPLICATE KEY UPDATE `score` = VALUES(`score`), `update_time` = NOW();

INSERT INTO `rating` (`user_id`, `movie_id`, `score`, `create_time`, `update_time`)
SELECT u.id, m.id, 8.8, DATE_SUB(NOW(), INTERVAL 19 DAY), NOW()
FROM user u JOIN movie m ON m.tmdb_id = 100009
WHERE u.username = 'cora'
ON DUPLICATE KEY UPDATE `score` = VALUES(`score`), `update_time` = NOW();

INSERT INTO `rating` (`user_id`, `movie_id`, `score`, `create_time`, `update_time`)
SELECT u.id, m.id, 7.9, DATE_SUB(NOW(), INTERVAL 14 DAY), NOW()
FROM user u JOIN movie m ON m.tmdb_id = 100006
WHERE u.username = 'owen'
ON DUPLICATE KEY UPDATE `score` = VALUES(`score`), `update_time` = NOW();

INSERT INTO `rating` (`user_id`, `movie_id`, `score`, `create_time`, `update_time`)
SELECT u.id, m.id, 8.2, DATE_SUB(NOW(), INTERVAL 9 DAY), NOW()
FROM user u JOIN movie m ON m.tmdb_id = 100010
WHERE u.username = 'nina'
ON DUPLICATE KEY UPDATE `score` = VALUES(`score`), `update_time` = NOW();

INSERT INTO `comment` (`user_id`, `movie_id`, `content`, `status`, `create_time`, `update_time`, `deleted`)
SELECT u.id, m.id, 'The pacing is sharp and the world-building feels premium.', 1, DATE_SUB(NOW(), INTERVAL 25 DAY), NOW(), 0
FROM user u JOIN movie m ON m.tmdb_id = 100001
WHERE u.username = 'aria'
  AND NOT EXISTS (
    SELECT 1 FROM comment c WHERE c.user_id = u.id AND c.movie_id = m.id AND c.content = 'The pacing is sharp and the world-building feels premium.'
  );

INSERT INTO `comment` (`user_id`, `movie_id`, `content`, `status`, `create_time`, `update_time`, `deleted`)
SELECT u.id, m.id, 'Loved the atmosphere, especially the harbor scenes at night.', 1, DATE_SUB(NOW(), INTERVAL 18 DAY), NOW(), 0
FROM user u JOIN movie m ON m.tmdb_id = 100002
WHERE u.username = 'mason'
  AND NOT EXISTS (
    SELECT 1 FROM comment c WHERE c.user_id = u.id AND c.movie_id = m.id AND c.content = 'Loved the atmosphere, especially the harbor scenes at night.'
  );

INSERT INTO `comment` (`user_id`, `movie_id`, `content`, `status`, `create_time`, `update_time`, `deleted`)
SELECT u.id, m.id, 'The ending lands much harder than the poster suggests.', 1, DATE_SUB(NOW(), INTERVAL 13 DAY), NOW(), 0
FROM user u JOIN movie m ON m.tmdb_id = 100009
WHERE u.username = 'cora'
  AND NOT EXISTS (
    SELECT 1 FROM comment c WHERE c.user_id = u.id AND c.movie_id = m.id AND c.content = 'The ending lands much harder than the poster suggests.'
  );

INSERT INTO `comment` (`user_id`, `movie_id`, `content`, `status`, `create_time`, `update_time`, `deleted`)
SELECT u.id, m.id, 'A compact thriller with a surprisingly emotional final act.', 1, DATE_SUB(NOW(), INTERVAL 6 DAY), NOW(), 0
FROM user u JOIN movie m ON m.tmdb_id = 100006
WHERE u.username = 'owen'
  AND NOT EXISTS (
    SELECT 1 FROM comment c WHERE c.user_id = u.id AND c.movie_id = m.id AND c.content = 'A compact thriller with a surprisingly emotional final act.'
  );

INSERT INTO `view_history` (`user_id`, `movie_id`, `create_time`)
SELECT u.id, m.id, DATE_SUB(NOW(), INTERVAL 9 DAY)
FROM user u JOIN movie m ON m.tmdb_id = 100001
WHERE u.username = 'admin'
  AND NOT EXISTS (
    SELECT 1 FROM view_history vh WHERE vh.user_id = u.id AND vh.movie_id = m.id AND vh.create_time = DATE_SUB(NOW(), INTERVAL 9 DAY)
  );

INSERT INTO `view_history` (`user_id`, `movie_id`, `create_time`)
SELECT u.id, m.id, DATE_SUB(NOW(), INTERVAL 5 DAY)
FROM user u JOIN movie m ON m.tmdb_id = 100009
WHERE u.username = 'felix'
  AND NOT EXISTS (
    SELECT 1 FROM view_history vh WHERE vh.user_id = u.id AND vh.movie_id = m.id AND vh.create_time = DATE_SUB(NOW(), INTERVAL 5 DAY)
  );

SET FOREIGN_KEY_CHECKS = 1;

DELETE older
FROM view_history older
JOIN view_history newer
  ON older.user_id = newer.user_id
 AND older.movie_id = newer.movie_id
 AND (
      older.create_time < newer.create_time
      OR (older.create_time = newer.create_time AND older.id < newer.id)
 );
ALTER TABLE view_history
  ADD UNIQUE KEY uk_view_history_user_movie (user_id, movie_id);
# Demo Seed

1. Run `deploy/shared/mysql/001_init_movie_mind.sql` to create the schema and base dictionaries.
2. Run `deploy/shared/mysql/002_seed_dashboard_demo.sql` to load demo users, movies, favorites, ratings, comments, and view history.

The seed is idempotent and is intended for local dashboard and UI presentation only.

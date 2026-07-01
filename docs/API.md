### TMDB API 本地配置说明

出于安全原因，仓库中不保存真实的 TMDB 读令牌或 API Key。

请将以下信息写入本地忽略文件 `backend/src/main/resources/application-local-secret.yml`，或配置为环境变量：

```yml
tmdb:
  read-access-token: YOUR_TMDB_READ_ACCESS_TOKEN
  base-url: https://api.themoviedb.org/3
  image-base-url: https://image.tmdb.org/t/p/w780
```

对应环境变量：

- `TMDB_READ_ACCESS_TOKEN`
- `TMDB_BASE_URL`
- `TMDB_IMAGE_BASE_URL`

完成后可调用后端接口导入真实数据：

- `POST /api/admin/tmdb/import/popular?limit=50`

## 电影详情扩展字段

`GET /api/movies/{id}` 除基础电影信息外，还返回：

- `originalLanguage`：TMDB 原始语言代码。
- `certification`：优先中国大陆、其次美国地区的电影分级。
- `productionCompanies`：制作公司数组。
- `productionCountries`：制片国家/地区数组。
- `collectionName`：所属电影系列。
- `releaseStatus`：TMDB 发行状态。
- `tagline`：电影宣传语。
- `keywords`：TMDB 关键词数组。
- `watchProviderRegion`：观看平台适用地区，当前优先 `CN`，无数据时回退 `US`。
- `watchProviders`：平台列表，包含 `providerId`、`name`、`logoUrl`、`region` 和 `accessType`。

观看平台信息来自 TMDB/JustWatch 的地区性数据，仅表示对应地区的可用信息，不表示本站拥有播放权。

已有数据库必须先执行 `deploy/shared/mysql/003_add_movie_metadata_and_watch_providers.sql`，随后重新调用导入接口才能填充这些字段。

## 个性化推荐接口

`GET /api/recommend/me?limit=12` 根据当前 JWT 用户生成个性化影片列表，`limit` 范围为 1 到 30。

推荐算法按 PRD 使用基于用户的协同过滤与余弦相似度，行为权重如下：

- 评分：最高 5 分（用户 10 分制评分归一化）。
- 收藏：4 分。
- 标记已观看：3 分。
- 加入待看：2.5 分。
- 浏览详情：1 分。

用户有效行为少于 3 条、没有足够的共同评分影片或没有候选影片时，接口自动使用热门度、评分和上映时间混合的冷启动策略。响应中的 `recommendScore`、`reason` 和 `algorithm` 分别表示推荐分数、推荐原因和算法类型。结果会保存到 `recommend_result`；开启 Redis 时使用 `recommend:user:{userId}:{limit}` 缓存 30 分钟。
## 用户观影管理

- GET /api/movies/{id}/interaction：当前用户收藏、评分、想看和看过状态。
- POST|DELETE /api/favorites/{id}：收藏或取消收藏。
- POST /api/ratings：新增或更新 1–10 分评分。
- POST|DELETE /api/watchlist/{id}：标记或取消“想看”。
- POST|DELETE /api/watch-history/{id}：标记或取消“看过”；标记后自动移出想看。
- GET /api/user/records/{favorites|ratings|watchlist|watched|views}：个人记录。
- GET /api/movies/rankings：排行榜。
## 影片检索与 TMDB 单片导入

- GET /api/movies/page：参数 keyword、category、region、year、sort、pageNum、pageSize。
- GET /api/movies/filters：返回可用类型、地区与年份。
- GET /api/admin/tmdb/search?query={name}&page=1：按名称搜索 TMDB，结果包含 imported 状态。
- POST /api/admin/tmdb/import/{tmdbId}：导入指定影片；已导入影片拒绝重复导入。

## Redis 影片缓存

启用 app.cache.redis.enabled 后缓存首页片单、热门影片、影片详情、各类型排行榜和个性化推荐。影片导入、后台影片/分类变更以及收藏、评分、浏览行为会清理对应缓存；Redis 不可用时自动回退数据库查询。
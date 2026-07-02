# Movie Mind API 补充说明

本文只记录 Movie Mind 后端向前端开放的项目接口。TMDB 和 DeepSeek 的外部服务配置、鉴权及上游调用方式见 [第三方 API 接入说明](./THIRD_PARTY_APIS.md)。完整接口列表和在线调试以 Swagger 为准。

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
- GET /api/movies/rankings：分页排行榜，参数为 `type`、`pageNum`、`pageSize`。
- GET /api/movies/{id}/similar?limit=8：按共同类型、标签、导演和演员获取相似影片。
## 影片检索与 TMDB 数据管理

- GET /api/movies/page：参数 keyword、category、region、year、sort、pageNum、pageSize。
- GET /api/movies/filters：返回可用类型、地区与年份。
- GET /api/admin/tmdb/search?query={name}&page=1：按名称搜索 TMDB，结果包含 imported 状态。
- POST /api/admin/tmdb/import/{tmdbId}：导入指定影片；已导入影片拒绝重复导入。
以上 `/api/admin/**` 接口需要在请求头携带管理员 JWT：`Authorization: Bearer <token>`。

## AI 影片助手接口

`POST /api/assistant/chat` 将对话消息和可选的用户观影上下文发送给影片助手。该接口需要登录并携带 JWT。

请求示例：

```json
{
  "messages": [
    {"role": "user", "content": "推荐几部科幻电影"}
  ],
  "userContext": {
    "username": "示例用户",
    "favorites": [{"id": 1, "title": "星际穿越"}],
    "ratings": [{"id": 2, "title": "盗梦空间", "score": 9}],
    "watched": [],
    "watchlist": []
  }
}
```

响应的 `content` 是助手回答，`links` 是根据回答中的书名号片名匹配到的站内影片。未配置 DeepSeek 或上游请求失败时，接口会返回服务暂时不可用的提示文本。

## Redis 影片缓存

启用 app.cache.redis.enabled 后缓存首页片单、热门影片、影片详情、各类型排行榜和个性化推荐。影片导入、后台影片/分类变更以及收藏、评分、浏览行为会清理对应缓存；Redis 不可用时自动回退数据库查询。

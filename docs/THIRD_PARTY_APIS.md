# 第三方 API 接入说明

本文记录 Movie Mind 后端调用的外部服务。项目自身提供给前端的 REST 接口见 [项目 API 补充说明](./API.md)。

## 本地密钥配置

复制 `backend/src/main/resources/application-local-secret.example.yml` 为 `application-local-secret.yml`，并填写真实凭证：

```yaml
tmdb:
  read-access-token: YOUR_TMDB_READ_ACCESS_TOKEN

deepseek:
  api-key: YOUR_DEEPSEEK_API_KEY
```

`application-local-secret.yml` 已被 Git 忽略，不要把真实 Token 或 API Key 提交到仓库。也可以使用下文列出的环境变量。

## TMDB API

TMDB 用于搜索和导入影片、演职人员、关键词、海报及观看平台等数据。只需申请 TMDB Read Access Token，后端通过 Bearer Token 调用 TMDB API。

| 环境变量 | 默认值 | 说明 |
|----------|--------|------|
| `TMDB_READ_ACCESS_TOKEN` | 无 | TMDB Read Access Token，使用导入功能时必填 |
| `TMDB_BASE_URL` | `https://api.themoviedb.org/3` | 主 API 地址 |
| `TMDB_FALLBACK_BASE_URL` | `https://api.tmdb.org/3` | 主地址不可用时的备用地址 |
| `TMDB_IMAGE_BASE_URL` | `https://image.tmdb.org/t/p/w780` | 图片基础地址 |

后端发出的请求包含：

```http
Authorization: Bearer <TMDB_READ_ACCESS_TOKEN>
Accept: application/json
```

连接超时为 3 秒，单次请求超时为 6 秒。某个域名失败后会暂停使用 2 分钟并尝试可用的备用地址；主、备用地址均失败时，项目接口返回上游服务不可用错误。

观看平台数据来自 TMDB/JustWatch，只表示相应地区的可用信息，不表示本站拥有播放权。系统优先读取中国大陆地区数据，无数据时回退至美国地区。

## DeepSeek API

DeepSeek 为 AI 影片助手提供对话能力。当前实现使用兼容 OpenAI Chat Completions 格式的接口，由后端调用，前端不会直接接触 API Key。

| 环境变量 | 默认值 | 说明 |
|----------|--------|------|
| `DEEPSEEK_API_KEY` | 无 | DeepSeek API Key，启用 AI 助手时必填 |
| `DEEPSEEK_API_URL` | `https://api.deepseek.com/v1/chat/completions` | 对话接口地址 |
| `DEEPSEEK_MODEL` | `deepseek-chat` | 使用的模型 |

后端向 DeepSeek 发送 Bearer 鉴权和 JSON 请求：

```http
POST /v1/chat/completions
Authorization: Bearer <DEEPSEEK_API_KEY>
Content-Type: application/json
```

核心请求参数与当前代码保持一致：

```json
{
  "model": "deepseek-chat",
  "messages": [
    {"role": "system", "content": "影视助手系统提示词"},
    {"role": "user", "content": "推荐几部科幻电影"}
  ],
  "stream": false,
  "temperature": 0.7,
  "max_tokens": 1024
}
```

连接超时为 5 秒，请求超时为 30 秒。上游返回非 2xx、超时或解析失败时，系统记录错误日志并向用户返回友好的降级提示，不影响影片浏览等核心功能。

登录用户的收藏、评分、看过和想看记录可作为个性化上下文加入系统提示词，每类最多传入 20 条。助手回答中使用 `《》` 标记的片名会与本地影片库匹配，并作为站内影片链接返回。

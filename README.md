# Movie Mind — 智能影视信息管理系统

> 基于 Vue 3 + Spring Boot 3 的全栈影视信息平台，集成 TMDB 数据导入、协同过滤推荐与 AI 影片助手。

---

## 目录

- [项目简介](#项目简介)
- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [数据库初始化](#数据库初始化)
- [TMDB 数据导入](#tmdb-数据导入)
- [API 文档](#api-文档)

---

## 项目简介

Movie Mind 是一个面向**普通用户**和**后台管理员**两类角色的智能影视信息管理系统。

**普通用户**可以浏览影视作品、搜索影片、查看详情（含演职人员、流媒体平台、关键词等）、收藏影片、评分评论、记录观影历史、维护待看片单，并接收个性化推荐。

**管理员**可以登录后台，对影视数据、用户、评论以及分类、标签、演职人员等基础资料进行管理，通过 TMDB 接口导入影视信息，并查看系统数据统计图表。

---

## 功能特性

### 用户端

| 功能模块 | 说明 |
|----------|------|
| 🏠 首页 | 沉浸式巨幕 Banner + 热门/高分/最新/个性化内容轨 |
| 🔍 影片浏览 | 列表筛选（类型、地区、年份、语言、评分区间）、搜索 |
| 🎬 影片详情 | 海报、评分、简介、流媒体平台、关键词、相似影片、演职人员 |
| 👥 演职人员 | 演员/导演个人页（经历、照片、作品列表） |
| 📊 排行榜 | 高分榜 / 收藏榜 / 浏览榜 / 最新榜 / 用户评分榜 |
| ⭐ 用户交互 | 收藏、评分（1–10 分）、文字评论、待看与观影记录 |
| 📋 个人中心 | 资料修改、我的收藏、观影记录、待看清单、我的评论 |
| 🤖 个性化推荐 | 基于协同过滤算法（User-CF 余弦相似度）的个性化推荐 |
| 💬 AI 影片助手 | 悬浮聊天窗，接入 DeepSeek API，支持个性化上下文 |

### 管理员后台

| 功能模块 | 说明 |
|----------|------|
| 📈 数据仪表盘 | 用户增长趋势、日活互动、分类/评分/年份/地区分布（ECharts） |
| 🎬 影片管理 | 增删改查、上下架、类型/标签管理 |
| 👤 用户管理 | 用户列表、启用/禁用、重置密码 |
| 💬 评论管理 | 查看/删除评论 |
| 🗂️ 基础资料 | 分类、标签、演员与导演资料管理 |
| 🌐 TMDB 导入 | 按名称或 ID 导入影片，自动同步演职人员、关键词、流媒体平台等完整数据 |

---

## 技术栈

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | ^3.5 | 前端框架（Composition API） |
| Vite | ^7.0 | 构建工具 |
| Vue Router | ^4.5 | 路由管理 |
| Pinia | ^3.0 | 全局状态管理 |
| Element Plus | ^2.11 | UI 组件库（管理后台） |
| ECharts | ^6.0 | 数据可视化图表 |
| Axios | ^1.11 | HTTP 请求 |
| Plain CSS + CSS Variables | — | 主题与样式系统 |

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.5.x | 后端主框架 |
| JDK | 21 | 运行环境 |
| Spring Security | — | 认证与权限 |
| JWT (jjwt) | 0.12.6 | 无状态登录凭证 |
| MyBatis-Plus | 3.5.12 | ORM / CRUD |
| MySQL | >= 8.4| 核心数据存储 |
| Redis（可选） | >= 7.0 | 热门数据、推荐结果与统计数据缓存 |
| Lombok | — | 简化实体代码 |
| SpringDoc / Swagger | 2.8.9 | 接口文档 |

### 第三方服务

| 服务 | 说明 |
|------|------|
| TMDB API | 影视基础数据、演职人员、海报、流媒体平台 |
| DeepSeek API | AI 影片助手对话能力（可选） |

---

## 项目结构

```
movie-mind/
├── frontend/                  # Vue 3 前端
│   └── src/
│       ├── api/               # Axios 接口封装
│       ├── assets/            # 全局 CSS 与静态资源
│       ├── components/        # 通用组件
│       │   ├── AiAssistant.vue    # AI 悬浮助手
│       │   ├── CastRail.vue       # 演职人员横向轨道
│       │   ├── ContentRail.vue    # 影片内容横向轨道
│       │   ├── MovieCard.vue      # 影片卡片
│       │   ├── MovieActions.vue   # 收藏/评分/待看操作
│       │   └── Navbar.vue         # 顶部导航栏
│       ├── layout/            # 布局容器
│       ├── router/            # 路由配置
│       ├── stores/            # Pinia 状态
│       ├── utils/             # 工具函数（主题、请求等）
│       └── views/
│           ├── front/         # 用户端页面
│           └── admin/         # 管理后台页面
│
├── backend/                   # Spring Boot 后端
│   └── src/main/java/com/example/movie/
│       ├── controller/        # REST 控制器
│       │   └── admin/         # 管理员专属接口
│       ├── service/           # 业务逻辑层
│       ├── mapper/            # MyBatis-Plus Mapper
│       ├── entity/            # 数据库实体
│       ├── dto/               # 请求数据对象
│       ├── vo/                # 响应数据对象
│       ├── config/            # Spring 配置类
│       ├── security/          # Security 过滤器与 JWT
│       └── utils/             # 工具类
│
├── deploy/                    # 部署配置
│   └── shared/mysql/          # 数据库初始化 SQL（按序号执行）
│
└── docs/                      # 项目文档
    ├── PRD.md                 # 产品需求文档
    ├── API.md                 # 项目接口补充说明
    └── THIRD_PARTY_APIS.md    # TMDB 与 DeepSeek 接入说明
```

---

## 快速开始

### 环境要求

- **JDK**: 21
- **Node.js**: >= 20.19（或 >= 22.12）
- **npm**: Node.js 内置（前端包管理器）
- **MySQL**: >= 8.4
- **Redis**: >= 7.0（可选）
- **Maven**: >= 3.9

---

### 第一步：准备数据库

请先准备 MySQL 实例。Redis 为可选组件，可在本机环境支持时启用。Windows 本地环境的详细步骤见 `deploy/windows/README.md`。

| 参数 | 默认值 |
|------|--------|
| MySQL Host | `127.0.0.1` |
| MySQL Port | `3306` |
| Database | `movie_mind` |
| Redis Host | `127.0.0.1` |
| Redis Port | `6379` |

---

### 第二步：初始化数据库


若使用已有数据库，请按顺序手动执行 `deploy/shared/mysql/` 下的迁移脚本：

```
001_init_movie_mind.sql                          ← 建库建表
002_seed_dashboard_demo.sql                      ← 演示数据（可选）
003_add_movie_metadata_and_watch_providers.sql   ← 影片元数据扩展
004_add_tag_management_fields.sql
005_add_person_profile_sync_time.sql
006_unique_watch_history.sql
007_normalize_region_names.sql
008_unique_view_history.sql
```

---

### 第三步：配置后端

后端通过环境变量或本地配置文件读取数据库、TMDB、DeepSeek 等参数。参考 `application.yml` 中的变量名，在运行环境中配置以下内容：

| 配置项 | 说明 | 是否必填 |
|--------|------|----------|
| `DB_HOST` / `DB_PORT` / `DB_NAME` | 数据库连接地址 | 建议配置 |
| `DB_USERNAME` / `DB_PASSWORD` | 数据库账号密码 | 建议配置 |
| `TMDB_READ_ACCESS_TOKEN` | TMDB 只读令牌，从 [themoviedb.org](https://www.themoviedb.org/) 申请 | 必填（导入功能） |
| `DEEPSEEK_API_KEY` | DeepSeek API 密钥，从 [platform.deepseek.com](https://platform.deepseek.com/) 申请 | 可选（AI 助手） |
| `REDIS_ENABLED` | 是否启用 Redis 缓存，设为 `true` 开启 | 可选 |

> **说明**：项目提供了数据库连接默认值，但不同电脑的账号和密码通常不同，建议使用本地密钥配置文件覆盖。DeepSeek Key 不填写时 AI 助手不可用，其余功能正常运行。

复制 `backend/src/main/resources/application-local-secret.example.yml` 为 `application-local-secret.yml`，然后填写本机数据库账号、密码和所需的第三方密钥。

---

### 第四步：启动后端

```bash
cd backend

# 使用已安装的 Maven
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8080`。

---

### 第五步：启动前端

你可以选择在**项目根目录**使用快捷脚本启动，或直接**进入前端目录**启动：

#### 方式一：在项目根目录（快捷启动）
```bash
# 安装前端依赖
npm run install:frontend

# 运行前端
npm run dev
```

#### 方式二：在前端目录启动
```bash
cd frontend

# 安装依赖
npm install

# 运行前端
npm run dev
```

前端默认运行在 `http://localhost:5173`，请求自动代理到后端 `8080` 端口。

---

## 配置说明

### Redis 缓存（可选）

Redis 不属于核心功能依赖。当前仓库的本地开发公共配置 `application-local.yml` 默认启用 Redis；本机没有 Redis 时，请在 `application-local-secret.yml` 中覆盖为关闭：

```yaml
app:
  cache:
    redis:
      enabled: false
```

需要启用时，将该值改为 `true`，并正确配置 `REDIS_HOST` 和 `REDIS_PORT`。Redis 可用于热门影视、推荐结果和后台统计等高频数据的缓存。

---

### 环境变量（生产部署）

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `DB_HOST` | MySQL 主机 | `127.0.0.1` |
| `DB_PORT` | MySQL 端口 | `3306` |
| `DB_NAME` | 数据库名 | `movie_mind` |
| `REDIS_HOST` | Redis 主机 | `127.0.0.1` |
| `REDIS_PORT` | Redis 端口 | `6379` |
| `REDIS_ENABLED` | 是否启用 Redis 缓存 | `true` |
| `TMDB_READ_ACCESS_TOKEN` | TMDB 只读访问令牌 | — |
| `DEEPSEEK_API_KEY` | DeepSeek AI API 密钥 | — |

---

## 数据库初始化

数据库名称为 `movie_mind`，字符集 `utf8mb4`，排序规则 `utf8mb4_unicode_ci`。

主要数据表一览：

| 表名 | 描述 |
|------|------|
| `user` | 用户表（含角色、状态、逻辑删除） |
| `movie` | 影片表（含 TMDB ID、评分、收藏数、浏览数） |
| `actor` / `director` | 演员 / 导演表 |
| `category` / `tag` | 分类 / 标签表 |
| `movie_category` / `movie_tag` | 影片与分类/标签的多对多关联 |
| `movie_actor` / `movie_director` | 影片与演职人员的多对多关联 |
| `rating` | 用户评分表 |
| `comment` | 用户评论表 |
| `favorite` | 收藏表 |
| `watch_history` | 观影记录表 |
| `watchlist` | 待看片单表 |
| `watch_provider` | 流媒体平台表 |
| `movie_watch_provider` | 影片与平台关联 |
| `keyword` / `movie_keyword` | 关键词及关联 |

---

## TMDB 数据导入

系统支持两种导入方式，均需管理员权限。

### 批量导入热门影片

```powershell
# 导入 50 部热门影片（需先登录获取管理员 Token 并附加到请求头）
$headers = @{ Authorization = "Bearer <管理员登录后获得的 Token>" }
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/admin/tmdb/import/popular?limit=50" -Headers $headers
```

### 按名称或 ID 导入

在管理后台 → **TMDB 管理** 页面，通过名称搜索或填入 TMDB ID 进行精确导入。

导入内容包括：电影基础信息、演职人员、影片分级、制作公司、制片国家、关键词、流媒体平台（优先中国大陆地区，无数据时回退至美国地区）。

---

## API 文档

后端启动后，无需登录即可访问以下地址查看 Swagger 接口文档：

```
http://localhost:8080/swagger-ui/index.html
```

主要接口路径：

| 前缀 | 描述 |
|------|------|
| `/api/auth/` | 注册、普通用户登录 |
| `/api/movies/` | 影片列表、详情、搜索、排行榜 |
| `/api/favorites/`、`/api/ratings`、`/api/comments` | 收藏、评分与评论 |
| `/api/watchlist/`、`/api/watch-history/` | 待看列表与观影记录 |
| `/api/recommend/` | 个性化推荐 |
| `/api/people/` | 演员/导演详情与作品 |
| `/api/user/` | 个人资料管理 |
| `/api/admin/` | 管理员专属接口（需 ADMIN 角色） |
| `/api/assistant/` | AI 影片助手对话接口 |

---

## 注意事项

- 生产部署前，请修改数据库密码、JWT 密钥等所有默认敏感配置，切勿使用默认值。
- 所有密钥与凭证请通过环境变量注入，不要硬编码在代码或配置文件中提交至版本库。

---

## 许可证

本项目为课程设计作品，仅供学习与参考使用。





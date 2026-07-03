# Movie Mind 项目结构分析与成员分工说明

> 本说明以当前仓库的目录、Vue Router 路由、前端 API 调用、Spring Controller/Service、实体与 Mapper、数据库脚本及配置文件为依据，并以“项目已经完成”为前提整理，可用于课程设计报告或答辩中的项目分工章节。

## 一、项目整体结构分析

### 1. 前端工程

前端位于 `frontend/`，采用 Vue 3、Vite、Vue Router、Pinia、Element Plus、ECharts、Axios 和 CSS Variables。

- `src/views/front/`：用户端页面，包括首页、影视库、排行榜、影片详情、演职人员、登录注册、个性化推荐和个人中心。
- `src/views/admin/`：后台页面，包括数据仪表盘、影片管理、用户管理、评论管理、基础资料管理和 TMDB 管理。
- `src/components/`：影片卡片、影片列表、内容轨道、演职人员轨道、影片交互区、导航栏、AI 助手等复用组件。
- `src/layout/`：用户端和管理端布局。
- `src/api/`：对后端 REST 接口的 Axios 封装。
- `src/router/`：前后台路由、登录校验和管理员角色校验。
- `src/stores/`、`src/utils/`：用户登录状态、请求拦截、主题切换等公共能力。

当前路由实际挂载的后台页面为 `DashboardView.vue`、`MovieManagementView.vue`、`UserManagementView.vue`、`CommentManageView.vue`、`ContentManageView.vue` 和 `TmdbManageView.vue`。仓库中的 `MovieManageView.vue`、`UserManageView.vue` 未被当前路由引用，因此不作为正式功能模块或成员工作量的主要依据。

### 2. 后端工程

后端位于 `backend/`，采用 Spring Boot 3、Spring Security、JWT、MyBatis-Plus、JdbcTemplate、MySQL、可选 Redis 和 SpringDoc。

- `controller/`、`controller/admin/`：普通用户接口与管理员接口。
- `service/`、`service/impl/`：认证、影视查询、交互、人物信息、推荐、统计、后台管理、TMDB 导入和 AI 助手等业务逻辑。
- `mapper/`：现有 `UserMapper`、`MovieMapper`，负责用户和影片实体的 MyBatis-Plus 数据访问。
- `entity/`：`User`、`Movie`、`Actor`、`Director`、`Favorite`、`Rating` 等实体。部分关联表和后台管理查询没有单独实体或 Mapper，而是在 Service 中通过 `JdbcTemplate` 完成，这是当前代码的实际实现方式。
- `dto/`、`vo/`：登录注册、AI 对话、后台影片维护、统计和推荐等请求或响应模型。
- `security/`、`config/`：JWT 过滤、用户/管理员权限规则、跨域、MyBatis-Plus、Redis、TMDB 和 DeepSeek 配置。
- `common/`：统一响应 `Result`、分页响应 `PageResult` 和全局异常处理。
- `utils/`：Redis Key、地区标准化、统计区间解析等工具。

### 3. 数据库与初始化脚本

数据库脚本位于 `deploy/shared/mysql/`：

- `001_init_movie_mind.sql`：创建数据库、核心表、关联表及初始分类、标签和公告。
- `002_seed_dashboard_demo.sql`：提供后台统计演示数据。
- `003_add_movie_metadata_and_watch_providers.sql`：扩充影片元数据并增加流媒体平台表。
- `004_add_tag_management_fields.sql`：补充标签描述和状态字段。
- `005_add_person_profile_sync_time.sql`：增加演员、导演资料同步时间。
- `006_unique_watch_history.sql`、`008_unique_view_history.sql`：清理重复记录并增加唯一约束。
- `007_normalize_region_names.sql`：统一影片地区名称。

核心表包括 `user`、`movie`、`category`、`tag`、`actor`、`director`、`movie_category`、`movie_tag`、`movie_actor`、`movie_director`、`favorite`、`rating`、`comment`、`view_history`、`watch_history`、`watchlist`、`recommend_result`、`movie_watch_provider`，另有 `announcement` 和 `operation_log` 基础表。

### 4. 配置、部署与文档

- `backend/src/main/resources/application*.yml`：数据库、Redis、TMDB、DeepSeek、端口和本地密钥覆盖配置。
- `frontend/vite.config.js`：前端构建和开发代理配置。
- `deploy/README.md`、`deploy/windows/README.md`、`deploy/shared/mysql/README.md`：部署与数据库执行说明。
- `README.md`：项目总览、技术栈、启动和配置说明。
- `docs/PRD.md`、`docs/API.md`、`docs/THIRD_PARTY_APIS.md`：需求、接口和第三方服务文档。

## 二、功能模块划分

### 1. 用户注册、登录、资料与权限认证

系统支持普通用户注册、普通用户登录、管理员登录、JWT 无状态认证、前端路由守卫、个人资料修改和密码修改。后端由 `AuthController`、`AdminAuthController`、`UserController`、`AuthServiceImpl`、JWT 与 Security 配置共同实现；前端对应 `LoginView.vue`、`RegisterView.vue`、`ProfileView.vue`、用户 Store 和请求拦截器。主要关联 `user` 表。

### 2. 影视浏览、搜索、筛选、排行与详情

系统提供首页内容展示、影视库分页、关键字搜索、类型/地区/年份组合筛选、排序、热门影片、排行榜、影片详情和相似影片。主要由 `MovieController`、`RankingController`、`MovieServiceImpl`、`MovieCatalogService`、`SimilarMovieService` 和 `MovieCacheService` 实现；前端对应首页、影视库、排行榜、详情页以及影片卡片、列表和内容轨道组件。主要关联 `movie`、`category`、`movie_category`、`tag`、`movie_tag` 和 `movie_watch_provider`。

### 3. 演职人员信息与作品展示

影片详情可展示演员、导演信息，并进入人物详情页和完整作品页；人物资料不足或过期时，`PersonServiceImpl` 会结合 TMDB 人物接口补全资料。对应 `PersonController`、`PersonServiceImpl`、`PersonDetailVO`，以及 `MovieCastView.vue`、`PersonDetailView.vue`、`PersonWorksView.vue`、`CastRail.vue`。关联 `actor`、`director`、`movie_actor`、`movie_director` 和 `movie`。

### 4. 收藏、评分、浏览、观看、待看与评论

`InteractionController` 和 `InteractionService` 提供影片交互状态、收藏、评分、浏览记录、已看记录、待看片单、评论发布/删除/分页查询以及个人记录查询。前端由 `MovieActions.vue`、`MovieDetailView.vue` 和 `ProfileView.vue` 调用。关联 `favorite`、`rating`、`view_history`、`watch_history`、`watchlist`、`comment`、`movie` 和 `user`。

### 5. 个性化推荐

`RecommendationServiceImpl` 从评分、收藏、观看、待看和浏览行为构建用户—影片偏好矩阵，采用余弦相似度 User-CF 生成推荐；交互不足时使用评分、热度、收藏量和新鲜度组成的冷启动策略。结果写入 `recommend_result`，并可写入 Redis 缓存。前端通过 `RecommendationView.vue` 和首页个性化内容调用 `/api/recommend/me`。

### 6. AI 影片助手

`AiAssistant.vue` 提供全局悬浮对话界面，可读取用户记录形成上下文；`AiAssistantController`、`AiAssistantServiceImpl` 调用 DeepSeek 兼容接口，并将回复中识别到的本地影片关联为可展示结果。`AiRateLimitInterceptor` 负责接口限流。关联 `AiChatRequest`、DeepSeek 配置，以及用于上下文和影片匹配的用户行为表、`movie` 表。

### 7. 后台内容与用户管理

后台包含影片增删改查、上下架、批量删除、影片分类/标签/演员/导演关联维护，用户列表、启禁用、详情和密码重置，评论查询和逻辑删除，以及分类、标签、演员、导演基础资料维护。核心代码为 `AdminManagementController`、`AdminManagementServiceImpl`、后台管理 DTO 和 `MovieManagementView.vue`、`UserManagementView.vue`、`CommentManageView.vue`、`ContentManageView.vue`。

### 8. 后台数据统计

`AdminStatisticsController` 和 `AdminStatisticsServiceImpl` 统计用户数、影片数、评论数、收藏数、评分数、当日新增、用户增长、活跃互动、影片分类/评分/年份/地区分布等数据；`DashboardView.vue` 与 `DashboardChart.vue` 使用 ECharts 展示，并支持时间范围切换和 Redis 缓存。关联 `user`、`movie`、`comment`、`favorite`、`rating`、`view_history`、`watch_history`、`watchlist` 及分类关联表。

### 9. TMDB 第三方数据导入与同步

管理员可搜索 TMDB 影片、按 TMDB ID 导入、导入热门影片、补充新影片和刷新本地影片。`TmdbApiClient` 负责第三方请求，`TmdbImportServiceImpl` 负责影片、类型、演职人员、关键词文本、制作信息和流媒体平台的落库与关联同步；`PersonServiceImpl` 还负责人物详情补全。前端对应 `TmdbManageView.vue`。主要关联 `movie`、`category`、`movie_category`、`actor`、`director`、`movie_actor`、`movie_director` 和 `movie_watch_provider`。

### 10. 数据库、缓存、统一响应与部署配置

该模块包括数据库初始化和迁移、逻辑删除及唯一约束设计、MyBatis-Plus 配置、可选 Redis 缓存、统一响应和异常处理、前端代理、环境变量、本地密钥示例和 Windows 部署说明。它是各业务模块运行和集成的基础。

## 三、五名成员分工建议

### 成员一：用户体系、认证授权与个人中心

**主要负责模块**：注册登录、JWT 认证、角色权限、用户资料与密码维护。

**具体负责功能**：

- 普通用户注册、登录和管理员登录。
- JWT 生成、解析、认证过滤与 Spring Security 权限规则。
- Pinia 登录状态、Axios Token 注入、前端登录/管理员路由守卫。
- 个人资料查询与修改、密码校验与修改。

**前端页面或组件**：`LoginView.vue`、`RegisterView.vue`、`ProfileView.vue` 中的账户资料与密码区域、`Navbar.vue` 的登录状态展示、`stores/user.js`、`api/auth.js`、`api/user.js`、`utils/request.js`、相关路由守卫。

**后端代码**：`AuthController`、`AdminAuthController`、`UserController`、`AuthService`、`AuthServiceImpl`、`UserService`、`UserServiceImpl`、`JwtUtil`、`JwtAuthenticationFilter`、`SecurityConfig`、`AdminSecurityConfig`、`LoginRequest`、`RegisterRequest`、`User`、`UserMapper`。

**数据库与脚本**：以 `user` 表为主，涉及 `001_init_movie_mind.sql` 中用户字段、角色、状态和逻辑删除设计。

**技术重点**：BCrypt 密码加密、JWT 无状态认证、普通用户与管理员权限隔离、前后端登录状态一致性、用户输入校验和账号状态控制。

### 成员二：用户端影视内容、检索与演职人员展示

**主要负责模块**：首页、影视库、排行榜、影片详情、相似影片、演职人员详情与作品展示。

**具体负责功能**：

- 首页热门、高分、最新等内容轨道。
- 影视关键字搜索、多条件筛选、排序与分页。
- 多种排行榜、影片详情、类别标签、流媒体平台和相似影片。
- 演员/导演列表、人物详情及参演/导演作品展示。

**前端页面或组件**：`HomeView.vue`、`MovieListView.vue`、`RankingView.vue`、`MovieDetailView.vue`、`MovieCastView.vue`、`PersonDetailView.vue`、`PersonWorksView.vue`、`MovieCard.vue`、`MovieList.vue`、`ContentRail.vue`、`CastRail.vue`、`api/movie.js`、`api/person.js`。

**后端代码**：`MovieController`、`RankingController`、`PersonController`、`MovieService`、`MovieServiceImpl`、`MovieCatalogService`、`SimilarMovieService`、`PersonService`、`PersonServiceImpl`、`MovieCacheService`、`MovieVO`、`PersonDetailVO`、`Movie`、`Actor`、`Director`、`MovieMapper`。

**数据库与脚本**：`movie`、`category`、`tag`、`movie_category`、`movie_tag`、`actor`、`director`、`movie_actor`、`movie_director`、`movie_watch_provider`；涉及 `001`、`003`、`005`、`007` 号脚本。

**技术重点**：复杂筛选 SQL、分页与排序、详情聚合查询、相似影片计算、内容组件复用、人物资料缓存及过期刷新、响应式用户界面。

### 成员三：用户交互、个性化推荐与 AI 助手

**主要负责模块**：收藏/评分/记录/评论、协同过滤推荐、AI 影片助手。

**具体负责功能**：

- 收藏、评分、浏览记录、观看记录、待看片单的新增、更新、删除和状态回显。
- 用户评论发布、本人删除、影片评论分页和个人记录分页。
- User-CF 余弦相似度推荐、冷启动补充、推荐结果持久化和 Redis 缓存。
- AI 助手对话、用户行为上下文组装、影片结果关联和接口限流。

**前端页面或组件**：`MovieActions.vue`、`RecommendationView.vue`、`AiAssistant.vue`、`MovieDetailView.vue` 的交互与评论区域、`ProfileView.vue` 的收藏/评分/观看/待看/评论记录区域、`api/interaction.js`、`api/recommend.js`、`api/assistant.js`。

**后端代码**：`InteractionController`、`RecommendController`、`AiAssistantController`、`InteractionService`、`RecommendationService`、`RecommendationServiceImpl`、`AiAssistantService`、`AiAssistantServiceImpl`、`AiRateLimitInterceptor`、`AiProperties`、`AiChatRequest`、`RecommendationVO`、`Favorite`、`Rating`，以及缓存 Key 工具。

**数据库与脚本**：`favorite`、`rating`、`comment`、`view_history`、`watch_history`、`watchlist`、`recommend_result`、`movie`、`user`；重点涉及 `001`、`006`、`008` 号脚本。

**技术重点**：事务一致性、唯一约束与幂等写入、评分和计数回算、User-CF 偏好矩阵与余弦相似度、冷启动策略、Redis 容错、外部大模型调用与限流。

### 成员四：后台运营管理与数据可视化

**主要负责模块**：后台影片、用户、评论、基础资料管理和统计仪表盘。

**具体负责功能**：

- 影片增删改查、上下架、批量逻辑删除及分类/标签/人物关联维护。
- 用户分页查询、详情查看、启禁用和密码重置。
- 评论筛选与逻辑删除。
- 分类、标签、演员、导演基础资料的增删改查和状态控制。
- 汇总指标、增长趋势、活跃趋势和影片分布统计图表。

**前端页面或组件**：`AdminLayout.vue`、`DashboardView.vue`、`MovieManagementView.vue`、`UserManagementView.vue`、`CommentManageView.vue`、`ContentManageView.vue`、`DashboardChart.vue`、`AdminPage.vue`、`AdminPagination.vue`、`api/admin.js`、`api/adminManagement.js` 中非 TMDB 部分、`assets/admin.css`。

**后端代码**：`AdminManagementController`、`AdminMovieController`、`AdminUserController`、`AdminStatisticsController`、`AdminManagementService`、`AdminManagementServiceImpl`、`AdminStatisticsService`、`AdminStatisticsServiceImpl`、`AdminMovieRequest`、`AdminMovieListItemDTO`、`AdminUserListItemDTO`、`DashboardStatisticsDTO`、`DashboardRangeResolver` 及对应测试。

**数据库与脚本**：后台管理覆盖 `movie`、`user`、`comment`、`category`、`tag`、`actor`、`director` 及影片关联表；统计还涉及 `favorite`、`rating`、`view_history`、`watch_history`、`watchlist`。`002_seed_dashboard_demo.sql` 可归入该成员的统计演示数据工作。

**技术重点**：后台通用分页与表单复用、逻辑删除、关联数据维护、参数化查询、ECharts 可视化、多时间范围统计、统计缓存与接口测试。

### 成员五：TMDB 数据集成、数据库与运行环境

**主要负责模块**：TMDB 导入同步、数据库总体设计与迁移、缓存和环境部署配置、技术文档。

**具体负责功能**：

- TMDB 影片搜索、按 ID 导入、热门批量导入、增量补片和已有影片刷新。
- 影片基础数据、类型、演职人员、制作信息、关键词文本和流媒体平台的转换与落库。
- 数据库初始化、演示数据、字段扩展、地区标准化、历史记录去重和唯一约束。
- MySQL、Redis、TMDB、DeepSeek、本地密钥、前后端端口与代理配置。
- 项目启动、数据库脚本顺序、第三方接口和 API 文档整理。

**前端页面或组件**：`TmdbManageView.vue`、`api/tmdb.js`、`api/adminManagement.js` 中 TMDB 导入方法；同时协助维护前端 `vite.config.js` 的代理与构建配置。

**后端代码**：`AdminTmdbController`、`TmdbImportService`、`TmdbImportServiceImpl`、`TmdbApiClient`、`TmdbProperties`、`TmdbImportResultDTO`、`RedisConfig`、`MybatisPlusConfig`、`WebConfig`、应用入口和 `application*.yml`。人物资料同步与成员二的 `PersonServiceImpl` 存在接口协作，但 TMDB 客户端和同步规则由本成员统一维护。

**数据库与脚本**：负责 `deploy/shared/mysql/001` 至 `008` 全部脚本的总体设计和执行顺序，重点覆盖 `movie`、分类/标签、演职人员及关联表、`movie_watch_provider` 和用户行为表约束。

**技术重点**：第三方 API 鉴权与网络异常处理、外部数据到本地模型的映射、批量与增量同步、重复数据控制、事务边界、数据库外键/索引/逻辑删除、环境变量和可选 Redis 降级配置。

## 四、分工合理性说明

1. **采用业务模块纵向分工，而不是机械地把五人拆成纯前端或纯后端。** 每名成员均能从页面、API、Controller/Service 到数据表说明完整业务链路，更符合课程设计答辩对个人贡献的陈述需求。
2. **核心复杂模块均有明确负责人。** 用户认证、影视内容、用户行为与算法、后台运营、第三方同步与数据库分别独立，避免重要模块无人主责。
3. **减少同一文件的并行修改。** 成员一主要维护认证和个人账户文件；成员二维护用户端内容页面；成员三维护交互、推荐和 AI；成员四维护后台非 TMDB 页面；成员五维护 TMDB 和配置脚本。共同文件主要只有 `MovieDetailView.vue`、`ProfileView.vue`、`api/adminManagement.js` 和少量配置，联调时可通过约定接口边界控制冲突。
4. **工作量按复杂度而非文件数量平衡。** 成员三文件数不一定最多，但包含推荐算法、交互一致性和 AI 外部服务；成员五页面较少，但承担数据同步、数据库迁移和运行环境；成员四承担后台多业务页面和统计 SQL；因此整体负担较均衡。
5. **数据库由成员五总体把关，各业务成员维护自身表语义。** 这样既保证数据库结构、脚本顺序和约束统一，又能让成员一至四准确说明本模块的数据来源和业务规则。
6. **便于答辩展示。** 五人可分别演示“登录与个人中心”“浏览检索与人物详情”“交互、推荐与 AI”“后台管理与统计”“TMDB 导入与数据库部署”，演示边界清楚且能够覆盖系统完整流程。

## 五、归属推断与边界说明

- `MovieDetailView.vue` 同时包含影片内容展示和用户交互区域：建议由成员二负责页面主体与内容数据，由成员三负责 `MovieActions.vue`、评论和交互接口联调。推断依据是该页面同时调用影视详情接口和交互接口。
- `ProfileView.vue` 同时包含账号设置与个人行为记录：建议成员一负责资料/密码区域，成员三负责收藏、评分、观看、待看和评论记录区域。推断依据是页面分别调用 `api/user.js` 与 `api/interaction.js`。
- `PersonServiceImpl` 会调用 TMDB 补全人物资料：人物展示业务归成员二，TMDB 请求协议、密钥和同步规范由成员五负责。推断依据是人物服务属于前台人物功能，但外部客户端和第三方配置属于统一数据集成能力。
- `api/adminManagement.js` 同时封装后台 CRUD 和 TMDB 批量操作：非 TMDB 方法归成员四，TMDB 方法归成员五，必要时由成员四维护文件结构、成员五提交接口变更说明。
- 当前后端只有 `UserMapper`、`MovieMapper` 两个 Mapper；交互、推荐、后台统计、后台管理和 TMDB 同步大量使用 `JdbcTemplate`。因此分工中应按现有 Service 和 SQL 职责陈述，不应虚构不存在的 CommentMapper、RatingMapper 或 RecommendMapper。
- `announcement`、`operation_log` 在初始化脚本中存在，但当前没有对应 Controller、Service 或已挂载前端页面，故不列为已完成的独立“公告管理”或“操作日志管理”模块，仅作为数据库预留基础表说明。


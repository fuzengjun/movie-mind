# Windows 本地数据库环境

这套方案适用于直接在 Windows 本机安装 MySQL 和 Redis 的同学，不需要 WSL，也不需要 Docker。

## 推荐版本

- MySQL 8.4 LTS 或更新稳定版
- Redis 7.x 或更新稳定版

## 启动步骤

### 1. 安装 MySQL

安装完成后，创建数据库：

```sql
CREATE DATABASE movie_mind DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 导入初始化 SQL

使用 MySQL 客户端、Navicat、DataGrip 或命令行执行下面的 SQL 文件：

- `deploy/shared/mysql/001_init_movie_mind.sql`

命令行示例：

```bash
mysql -u root -p movie_mind < deploy/shared/mysql/001_init_movie_mind.sql
```

### 3. 安装并启动 Redis

确保 Redis 服务启动成功，默认端口建议使用 `6379`。

### 4. 配置后端数据库连接

方式一：直接设置环境变量

- `DB_HOST=127.0.0.1`
- `DB_PORT=3306`
- `DB_NAME=movie_mind`
- `DB_USERNAME=movie_mind`
- `DB_PASSWORD=你的数据库密码`
- `REDIS_HOST=127.0.0.1`
- `REDIS_PORT=6379`

方式二：复制后端配置模板

将下面文件复制为 `application-local.yml` 并按自己机器环境修改：

- `backend/src/main/resources/application-local.example.yml`

### 5. 启动后端

在 `backend/` 下运行 Spring Boot 项目。

### 6. 启动前端

在 `frontend/` 下使用 `pnpm`：

```bash
pnpm install
pnpm dev
```

## 注意事项

- 如果 MySQL 账号不是 `movie_mind`，请同步修改后端连接配置。
- 如果 Redis 不是默认端口，也要同步修改后端配置。
- 初始化 SQL 与 WSL 方案共用，避免两套表结构不一致。


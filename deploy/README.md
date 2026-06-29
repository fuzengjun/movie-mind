# Deploy 说明

## 目录说明

- `shared/`：团队共享的初始化 SQL 与演示数据。
- `wsl-docker/`：WSL2 Docker Compose 与容器初始化 SQL。
- `windows/`：Windows 环境说明。

## 后端连接方式

后端默认读取以下环境变量：

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`

也可以参考 `backend/src/main/resources/application-local.example.yml` 创建本地忽略的 `application-local.yml`。

## 数据库升级

已有数据库需要先执行一次：

```text
deploy/shared/mysql/003_add_movie_metadata_and_watch_providers.sql
```

该迁移增加电影分级、制作公司、制片国家、系列、发行状态、宣传语、关键词和流媒体平台表。新建 Docker 数据卷时，`deploy/wsl-docker/mysql/init/` 中的脚本会自动执行。

## TMDB 导入数据

完成迁移并重启 Spring Boot 后，重新同步 50 条电影数据：

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/admin/tmdb/import/popular?limit=50"
```

导入会同步电影详情、演职人员、分级、制作信息、关键词和观看平台。观看平台优先保存中国大陆数据，无数据时回退到美国地区，并在前端明确标注地区。
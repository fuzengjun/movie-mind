# Deploy 说明

## 目录说明

- shared/：团队共享的初始化 SQL 等公共部署资料。

- `windows/`：适用于直接在 Windows 本机安装 MySQL 和 Redis 的同学。

## 后端连接方式

后端默认读取环境变量：

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`

如果不想配环境变量，也可以参考 `backend/src/main/resources/application-local.example.yml`，复制成 `application-local.yml` 后按本机环境修改。

## TMDB 导入数据

```shell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/admin/tmdb/import/popular?limit=50"

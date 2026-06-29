# Deploy 说明

`deploy/` 目录提供两套本地数据库环境方案，团队成员按自己的开发方式选择即可。

## 目录说明

- shared/：团队共享的初始化 SQL 等公共部署资料。

- `windows/`：适用于直接在 Windows 本机安装 MySQL 和 Redis 的同学。
- `wsl-docker/`：适用于在 WSL 中通过 Docker 启动 MySQL 和 Redis 的同学。

## 选择建议

- 大部分同学：优先使用 `windows/` 方案，安装和排查更直接。
- 使用 WSL 的同学：可以使用 `wsl-docker/` 方案做容器化本地环境。

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




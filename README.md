# 企业级进销存 / 库存管理系统

SpringBoot 3.2.5 + Java 21 + MyBatis-Plus + MySQL 8 + Redis 7 + Vue 3 + Docker + Nginx 的**单体模块化**企业级实现。
覆盖 RBAC（用户-角色-菜单-接口权限）、数据权限、统一响应/异常、参数校验、全链路日志与审计、缓存一致性、
消息通知、定时任务、Excel 导入导出、幂等与并发控制、分页搜索、报表统计、单元测试与容器化部署。

> 规模：30 张业务表（Flyway 3 个迁移）、248 个后端 Java 类、28 个 Vue 页面/组件、12 个单元测试用例；
> 已完成本机真实启动与接口冒烟验证（见第 10 节）。

---

## 1. 技术栈

| 层次 | 选型 |
| --- | --- |
| 后端 | Java 21 / Spring Boot 3.2.5 / Spring Security 6 / MyBatis-Plus 3.5.7 / Flyway |
| 存储 | MySQL 8.0（utf8mb4）、Redis 7（缓存、分布式锁、幂等键） |
| 前端 | Vue 3.5 + TypeScript + Vite 5 + Element Plus 2 + Pinia + Vue Router 4 + ECharts 5 + Axios |
| 文档 | Knife4j / SpringDoc OpenAPI 3 |
| 部署 | Docker 多阶段构建、Docker Compose、Nginx 反向代理 |

---

## 2. 工程结构（Maven 多模块）

```
inventory-system/
├── pom.xml                           # 父 POM：统一依赖版本、Java 21、插件管理
├── mvnw / mvnw.cmd / .mvn/wrapper    # Maven Wrapper 3.9.16（无需预装 Maven）
├── inventory-common/                 # 基础能力层（8 个模块，可被任意业务模块复用）
│   ├── common-core                   # Result / ResultCode / BizException / PageQuery / 上下文 / 校验分组
│   ├── common-web                    # 全局异常处理、Jackson(Long→String)、OpenAPI、MVC、异步线程池
│   ├── common-security               # JWT 过滤器、RBAC 注解与切面、接口权限过滤器、权限缓存 SPI
│   ├── common-mybatis                # 分页/乐观锁插件、数据权限拦截器、自动填充、BaseEntity
│   ├── common-redis                  # CacheService / CacheConsistency / 幂等 / Redisson 分布式锁
│   ├── common-log                    # TraceId 过滤器、WebLogAspect、AuditLog 注解与落库
│   ├── common-excel                  # EasyExcel 封装、批量导入监听器、错误行收集
│   └── common-notify                 # 站内信 / 邮件 / Webhook 多通道发布
├── inventory-modules/                # 业务域（6 个模块）
│   ├── module-system                 # 认证、用户/角色/菜单/部门/字典/日志/通知/参数
│   ├── module-inventory              # 分类/商品/仓库/供应商/客户、库存、流水、盘点、调拨
│   ├── module-purchase               # 采购订单状态机 + 入库
│   ├── module-sale                   # 销售订单状态机 + 出库
│   ├── module-report                 # 经营看板、近 N 日趋势、仓库分布、进销存报表
│   └── module-job                    # 超时提醒、低库存预警、日报预计算、过期清理
├── inventory-admin/                  # 启动装配模块：Application、配置、Flyway 迁移、单元测试
├── web/                              # Vue3 前端（Vite + TS）
├── deploy/                           # 运行时挂载目录（logs / data，已 gitignore）
├── Dockerfile                        # 后端多阶段构建
├── web/Dockerfile + web/nginx.conf   # 前端多阶段构建 + Nginx
├── docker-compose.yml                # MySQL + Redis + backend + web 全栈编排
├── .env.example                      # 环境变量模板
└── scripts/build.sh / build.bat      # 本地构建脚本（优先使用 mvnw）
```

依赖方向严格单向：`inventory-admin → inventory-modules → inventory-common`，模块间不产生循环依赖。

---

## 3. 核心能力实现

### 3.1 RBAC 与数据权限

- **四层模型**：`sys_user` ↔ `sys_user_role` ↔ `sys_role` ↔ `sys_role_menu` ↔ `sys_menu`；
  `sys_menu.menu_type = M/C/F`，其中 `F` 记录 `permission + api_path + api_method`，即接口权限点。
- **双通道校验**：`@RequirePermission("xxx")` 就近声明 + `ApiPermissionFilter` 按集中登记的 URL 规则二次校验；
  超级管理员（`role_key = admin`）与 `*:*:*` 直通。
- **数据权限**：`sys_role.data_scope` = 1 全部 / 2 自定义部门（`sys_role_dept`）/ 3 本部门 / 4 仅本人；
  `DataPermissionHandlerImpl` 基于 MyBatis-Plus 数据权限插件在 SQL 上追加 `create_by / create_dept` 条件，
  非法条件解析失败降级为 `1 = 0`（拒绝而非放行）；`@IgnoreDataScope` 用于登录、字典等特殊场景。
- **缓存一致性**：权限缓存在 Redis，角色/菜单授权变更在**事务提交后**失效用户权限缓存，避免脏权限。

### 3.2 统一响应、异常、校验与日志

- 统一响应体 `Result<T>{code,message,data,traceId,timestamp}`，`GlobalExceptionHandler` 收敛
  业务异常、参数校验异常、鉴权异常、系统异常。
- 所有接口使用 Jakarta Validation + `Create/Update` 分组；分页基类 `PageQuery` 对 `orderBy`
  做正则 + 白名单校验，杜绝 ORDER BY 注入。
- `TraceIdFilter` 生成/透传 `X-Trace-Id` 写入 MDC，响应体携带同一 traceId；
  `WebLogAspect` 记录请求耗时，`AuditLogAspect` 通过 `@AuditLog` 落库「谁、何时、对什么、做了什么、
  请求/响应、IP、UA、成功与否」，密码类字段可通过 `saveRequest = false` 屏蔽。
- 雪花 ID 为 19 位 Long，Jackson 全局序列化为字符串，避免前端 Number 精度丢失。

### 3.3 缓存

- `CacheService`：TTL 抖动防雪崩、空值缓存防穿透、互斥重建防击穿、JSON 泛型反序列化。
- `CacheConsistency`：**先更新 DB，事务提交后删缓存 + 延迟双删**，字典/权限/热点商品缓存统一走此通道。
- 典型缓存对象：字典（`sys_dict_*`）、用户权限与菜单、商品详情、统计看板。

### 3.4 幂等与并发控制（重点）

- **库存扣减**：`UPDATE biz_stock SET quantity = quantity + #{delta}
  WHERE product_id = ? AND warehouse_id = ? AND quantity + #{delta} >= 0` 原子 CAS，
  Redisson 分布式锁按「商品+仓库」维度串行化，事务内写 `biz_stock_flow` 流水（变动前/变动量/变动后）。
- **重复提交**：`@Idempotent(module, key, expire)` 基于 Redis `SETNX` + SpEL 键解析，
  前端 Axios 对所有写请求自动生成 `Idempotency-Key` 头，重复请求返回首次结果。
- **订单状态机**：`待审核(1) → 已审核(2) → 已入库/已出库(3)`，另有 `已取消(4)` / `已驳回(5)`；
  审核与出入库均使用带 `status` 条件的 CAS UPDATE，杜绝并发重复审核与重复出入库。
- **审核类操作**使用 `@Idempotent(key = "#id")` 按单据维度去重。

### 3.5 通知、定时任务、Excel

- `NotificationPublisher` 异步分发 `IN_APP / EMAIL / WEBHOOK` 三通道（各自开关、失败隔离）。
- 定时任务（`@Scheduled` + Redis leader 锁保证集群单实例执行）：
  `order-timeout` 每小时、`low-stock` 每 30 分钟（每日去重）、`daily-stat` 00:10、`cleanup` 03:00；
  `GET /api/jobs`、`POST /api/jobs/{name}/run` 可查看与手动补跑。
- `ExcelUtils` 基于 EasyExcel：基础资料、库存、流水、订单、用户、日志、进销存报表均支持导出；
  商品/基础资料支持导入，逐行校验并返回 `{total, successCount, failCount, errors[]}`。

### 3.6 前端

- Axios 统一解包 `Result`，自动携带 JWT、`X-Trace-Id`、`Idempotency-Key`；401/403 统一处理；blob 下载自动读取文件名。
- 动态菜单由后端 `sys_menu` 生成（`GET /api/auth/info`），静态路由表仅做组件映射与权限兜底；
  按钮级权限用 `user.hasPerm()` 控制显隐，与后端权限同源。
- 通用 `CrudPage` 配置化页面 + 复杂的库存/订单/报表/系统管理独立页面；ECharts 趋势与分布图。

---

## 4. 数据库模型（30 张表，Flyway 自动迁移）

| 分组 | 表 |
| --- | --- |
| 权限 | `sys_user` `sys_role` `sys_menu` `sys_user_role` `sys_role_menu` `sys_role_dept` `sys_dept` |
| 系统 | `sys_dict_type` `sys_dict_data` `sys_oper_log` `sys_login_log` `sys_notice` `sys_notice_read` `sys_config` |
| 基础资料 | `biz_category` `biz_product` `biz_warehouse` `biz_supplier` `biz_customer` |
| 库存 | `biz_stock` `biz_stock_flow` `biz_inventory_check` `biz_inventory_check_item` `biz_transfer` |
| 单据 | `biz_purchase_order` `biz_purchase_item` `biz_sale_order` `biz_sale_item` |
| 报表/运维 | `biz_daily_stat` `biz_idempotent_record` |

迁移脚本位于 `inventory-admin/src/main/resources/db/migration/`：

| 脚本 | 内容 |
| --- | --- |
| `V1__init_schema.sql` | 30 张表：索引、逻辑删除、乐观锁、审计字段、唯一约束 |
| `V2__init_data.sql` | 部门/用户/角色/菜单与接口权限/字典/演示商品与库存 |
| `V3__seed_recent_daily_stats.sql` | 相对日期生成最近 7 天经营统计，让趋势图首次打开即有数据（`INSERT IGNORE`，可重复执行） |

实体的参数校验注解由生成脚本从 V1 中「`NOT NULL` 且无 `DEFAULT`」的列自动推导，
保证接口校验与数据库约束严格一致（避免空值落到 DB 才报笼统的 409）。

---

## 5. 快速开始（Docker 全栈，推荐）

前置：Docker 20.10+ 且带 Compose v2、可用网络（首次构建需拉取基础镜像与依赖）。

```bash
cd inventory-system
cp .env.example .env          # Windows: copy .env.example .env
# 建议修改 JWT_SECRET / 各类密码
docker compose --env-file .env up -d --build
docker compose ps
docker compose logs -f backend
```

启动顺序由 healthcheck 保证：`mysql/redis 健康 → backend 健康 → web 就绪`。
Flyway 在后端启动时自动建库表并灌入演示数据，无需手工执行 SQL。

| 入口 | 地址 |
| --- | --- |
| 前端（统一入口） | http://localhost/ |
| 后端健康检查 | http://localhost/actuator/health （或 127.0.0.1:8080） |
| 接口文档 Knife4j | http://localhost/doc.html |
| OpenAPI JSON | http://localhost/v3/api-docs |

```bash
docker compose down          # 停止并移除容器（保留数据卷）
docker compose down -v       # 连同数据卷一起删除（会清空演示数据，谨慎）
```

> 生产环境请把 `KNIFE4J_ENABLE=false`，并移除 compose 中 `127.0.0.1:...` 的调试端口映射，
> 仅由 Nginx 暴露 80/443。

---

## 6. 本地开发

### 6.1 后端

前置：JDK 21、MySQL 8、Redis 7（也可只起依赖容器）：
```bash
docker compose up -d mysql redis
```

```bash
./mvnw -B clean package -DskipTests          # Windows: mvnw.cmd -B clean package -DskipTests
java -jar inventory-admin/target/inventory-admin.jar --spring.profiles.active=dev
# 或
./mvnw -pl inventory-admin -am spring-boot:run -Dspring-boot.run.profiles=dev
```
默认连接 `localhost:3306/inventory`（root/root123456）、`localhost:6379`，
可通过 `MYSQL_*` / `REDIS_*` 环境变量覆盖。也可用 `scripts/build.sh` / `scripts/build.bat` 一键打包。

本机已有 MySQL / Redis、不启动 Docker 时：先创建空库，再用环境变量注入凭据（**不要把密码写进配置文件**）：

```sql
CREATE DATABASE inventory DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```
```bash
export MYSQL_HOST=127.0.0.1 MYSQL_DATABASE=inventory MYSQL_USER=<user> MYSQL_PASSWORD=<password>
export REDIS_HOST=127.0.0.1 REDIS_PORT=6379 SPRING_PROFILES_ACTIVE=dev
java -jar inventory-admin/target/inventory-admin.jar
curl http://127.0.0.1:8080/actuator/health     # {"status":"UP"}
```

启动日志应出现 `Successfully applied 3 migrations`；健康检查包含 db / redis / diskSpace，
其中 Redis 使用自定义 `PING` 指示器（见 `RedisHealthConfig`），兼容各版本且不受 `INFO` 解析差异影响；
未配置 SMTP 时邮件健康指示已关闭，避免整体健康被误判为 `DOWN`。

### 6.2 前端

```bash
cd web
npm ci
npm run dev          # http://localhost:5173，Vite 代理 /api → http://localhost:8080
npm run typecheck    # vue-tsc --noEmit
npm run build        # 产物 web/dist
```

环境变量：`.env.development` 的 `VITE_API_BASE=/api`、`VITE_PROXY_TARGET` 指向后端地址；
生产由 Nginx 反代 `/api`，无需跨域配置。

---

## 7. 默认账号

| 账号 | 密码 | 角色 | 数据权限 |
| --- | --- | --- | --- |
| `admin` | `Admin@123` | 超级管理员 | 全部数据 |
| `purchaser` | `Admin@123` | 采购经理 | 自定义部门 |
| `seller` | `Admin@123` | 销售专员 | 仅本人 |
| `keeper` | `Admin@123` | 仓库管理员 | 本部门 |

> 首次上线请立即修改默认密码与 `JWT_SECRET`。密码以 BCrypt 存储，SQL 中不含明文。

---

## 8. 主要接口（完整清单见 Knife4j）

| 模块 | 前缀 | 说明 |
| --- | --- | --- |
| 认证 | `/api/auth` | `POST /login`、`GET /info`、`POST /logout` |
| 系统 | `/api/system` | `/users` `/roles` `/menus` `/depts` `/dict` `/oper-logs` `/notices` `/configs` |
| 基础资料 | `/api/base` | `/categories` `/products` `/warehouses` `/suppliers` `/customers`（分页/CRUD/导出/导入） |
| 库存 | `/api/inventory` | `/stocks` `/stocks/summary` `/stocks/adjust` `/flows` `/checks` `/transfers` |
| 采购 / 销售 | `/api/purchase/orders`、`/api/sale/orders` | 分页、明细、创建、审核、入库/出库、取消、导出 |
| 报表 | `/api/report` | `/dashboard` `/trend` `/warehouse-distribution` `/pss` `/pss/export` |
| 运维 | `/api/jobs` | 任务清单、手动触发 |

所有业务接口统一返回 `Result<T>`；分页返回 `PageResult<T>{total,records,pageNum,pageSize,pages}`。

---

## 9. 配置项

主要环境变量（完整模板见 `.env.example`）：

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `dev` | Compose 中为 `prod` |
| `MYSQL_HOST/PORT/DATABASE/USER/PASSWORD` | localhost / 3306 / inventory / root / root123456 | 数据源 |
| `REDIS_HOST/PORT/PASSWORD/DB` | localhost / 6379 / 空 / 0 | 缓存与锁 |
| `JWT_SECRET` / `JWT_TTL` | 内置开发值 / 8h | 生产必须替换 |
| `KNIFE4J_ENABLE` | `false`(prod) | 是否开放接口文档 |
| `NOTIFY_EMAIL_ENABLED` / `NOTIFY_WEBHOOK_ENABLED` | false | 通知通道开关 |
| `LOG_HOME` | `logs` | 日志目录（容器内 `/app/logs`，挂载 `deploy/logs`） |

应用级配置见 `inventory-admin/src/main/resources/application*.yml`：
`inventory.security.ignore-urls`、`inventory.cache.*`（TTL/空值/双删延迟）、
`inventory.job.*-cron`（定时任务表达式）。

---

## 10. 测试与验证

```bash
./mvnw -B test             # 后端单元测试（12 个用例）
cd web && npm run typecheck && npm run build
```

覆盖：统一响应与异常契约、数据权限 SQL 条件解析、BCrypt 口令、库存扣减（CAS/锁/流水/幂等/余额不足）、
部门树工具。CI 可直接复用这两条命令。

### 10.1 本机端到端验证记录（MySQL 8.0.40 + Redis，真实启动）

| 验证项 | 结果 |
| --- | --- |
| `./mvnw clean package` | BUILD SUCCESS，12 个单元测试全绿，产出可执行 jar |
| `vue-tsc --noEmit` / `npm run build` | 0 类型错误，生产构建成功 |
| Flyway 真实迁移 | V1/V2/V3 全部成功，31 张表（含 `flyway_schema_history`），初始化数据列与值形状一致 |
| 实体 ↔ 表结构 | 30 个实体逐字段比对，0 个字段不存在于数据库 |
| 应用启动 | Spring Boot 3.2.5 / Java 21，Tomcat 8080，Lettuce + Redisson 均连通 |
| `/actuator/health` | `UP`（db / redis / diskSpace 全部 UP，mail 指示已关闭） |
| 认证与 RBAC | 未登录 401、伪造 token 401；seller 访问 `system:user:list` 返回 403，有权限接口 200 |
| 参数校验 | 空商品创建返回 HTTP 400 与「商品名称不能为空; SKU不能为空」 |
| CRUD 与备注 | 创建/详情/更新/删除正常，19 个实体的 `remark` 真实落库，雪花 ID 以字符串返回 |
| 幂等 | 相同 `Idempotency-Key` 重放回放首次结果，两次响应类型一致（字符串，不丢精度） |
| 库存并发 | 原子扣减成功；超卖返回业务码 1007 与可用量提示；台账分页/搜索/低库存过滤均正常 |
| 缓存 | Redis 中真实产生字典、权限、配置、幂等、任务锁等 14 个键 |
| 报表与任务 | 趋势图 7 个数据点、经营看板、进销存报表、手动触发定时任务均正常 |
| 审计与文档 | 操作日志真实落库；Excel 导出 xlsx 文件头正确；`/doc.html`、`/v3/api-docs` 可访问 |

---

## 11. 排障提示

- **后端起不来**：`docker compose logs -f backend`，多数为 MySQL/Redis 未就绪；healthcheck 已覆盖，
  若手动启动请先确认依赖可用。
- **接口 403**：确认角色在「角色管理 → 分配权限」中被授予对应 `F` 权限点，授权后权限缓存自动失效。
- **前端 ID 精度**：后端 Long 一律序列化为字符串，前端下拉与表单均以字符串传递 ID，不能 `Number()` 转换。
- **Excel 导入失败**：查看导入结果弹窗的行号与原因；单次上限 20MB。
- **健康检查 DOWN**：确认 MySQL/Redis 可达；本项目已禁用未配置 SMTP 时的邮件健康指示，
  并用 `PING` 覆盖默认 Redis 健康指示，避免旧版 Redis / 代理的 `INFO` 解析差异导致容器被误判重启。
- **端口冲突**：修改 `.env` 中 `WEB_PORT` / `*_EXPOSE_PORT`。
- **忘记管理员密码**：用 BCrypt 生成新哈希更新 `sys_user.password`，或直接调用用户管理接口的「重置密码」。

---

## 12. 生产化建议

1. 全站 HTTPS（Nginx 证书 + HSTS），`CORS_ORIGINS` 收敛到具体域名。
2. `JWT_SECRET` 使用 32 字节以上随机值并由密钥管理服务注入，缩短 `JWT_TTL` 并接入刷新令牌。
3. MySQL 主从/读写分离 + 定期备份，Redis 哨兵/集群；`sys_oper_log` 按周期归档。
4. 关闭 Knife4j、Actuator 细节，移除调试端口映射；为 `@Idempotent` 记录配置独立过期策略。
5. 大报表导出改为异步任务 + 文件服务，避免长连接；定时任务接入分布式调度与告警。

---

## 13. 云上部署（GitHub Actions + TiDB Cloud + Render 免费档）

### 13.1 架构与流水线

| 组件 | 平台 | 说明 |
|------|------|------|
| 代码托管 | GitHub（公开仓库） | push `main` 即触发 Actions 构建镜像 |
| 数据库 | TiDB Cloud Serverless | MySQL 协议兼容；JDBC 强制 TLS（`sslMode=REQUIRED`） |
| 缓存 | Render Key Value（Valkey 8） | Redis 协议兼容；免费档 25MB 无持久化，仅存短 TTL 数据 |
| 后端 | Render Web Service（镜像部署） | `ghcr.io/<owner>/inventory-backend:main` |
| 前端 | Render Web Service（镜像部署） | Nginx，反代后端 `*.onrender.com` 公网地址 |

镜像为什么在 GitHub Actions 构建：Render 免费档实例仅 512MB 内存，
本仓库的多模块 Maven / Vite 构建会 OOM；GHCR 公开镜像 Render 可直接拉取部署。

```
git push main ─→ Actions 构建前后端镜像 ─→ 推送 GHCR ─→ (可选) Deploy Hook 触发 Render 滚动更新
```

### 13.2 前置准备

1. **TiDB Cloud**：注册 [tidb.cloud](https://tidbcloud.com) → 免费创建 Serverless 集群（建议东京/新加坡），
   在 SQL Editor 执行：`CREATE DATABASE inventory CHARACTER SET utf8mb4;`
2. **GHCR 镜像可见性**：首次 Actions 构建成功后，到 GitHub 仓库 → Packages →
   `inventory-backend` / `inventory-web` → Package settings → Change visibility → **Public**
   （仓库本身公开时镜像公开即可拉取，无需在 Render 配置注册表凭据）。
3. **Render**：注册 [render.com](https://render.com)（可用 GitHub 账号登录）。

### 13.3 一键 Blueprint 部署

仓库根目录的 `render.yaml` 定义了全部三个服务。操作：

1. Render 控制台 → **New → Blueprint** → 授权并选择本仓库；
2. Blueprint 会提示填写三个密钥（`sync: false` 项，只存在 Render，不进 Git）：

| 环境变量 | 值（示例，换成你的集群信息） |
|----------|------------------------------|
| `MYSQL_JDBC_URL` | `jdbc:mysql://gateway01.ap-northeast-1.prod.aws.tidbcloud.com:4000/inventory?sslMode=REQUIRED&useUnicode=true&characterEncoding=utf8&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true` |
| `MYSQL_USER` | `<前缀>.root`（在集群 Connect 弹窗复制，形如 `qTMwxxxxx.root`） |
| `MYSQL_PASSWORD` | 你的 TiDB 集群密码 |

   `JWT_SECRET` 由 Render `generateValue` 自动生成，无需填写。
3. 点 **Apply**：Render 创建 Key Value → 拉取两个镜像启动。首次启动时 Flyway 自动在 TiDB
   上完成 V1/V2/V3 迁移（约 2–5 分钟），健康检查 `/actuator/health` 变绿即部署成功。
4. 访问 `https://inventory-web.onrender.com`，用 `admin / Admin@123` 登录后**立即改密**。

### 13.4 与本机部署的差异

- **时区**：TiDB 服务端为 UTC，JDBC 层通过 `MYSQL_INIT_SQL`（默认 `SET time_zone = '+08:00'`）
  将每个连接的会话时区统一为东八区，`CURRENT_TIMESTAMP` 行为与本机 MySQL 一致。
- **连接池**：免费实例 512MB + Serverless 云库，`MYSQL_MAX_POOL_SIZE=8`、`REDIS_MAX_ACTIVE=16`，
  超出本地默认（20/32）；`render.yaml` 已配好。
- **休眠**：免费 Web 服务 15 分钟无流量会休眠，首个请求约 30–60 秒冷启动；
  通过前端 Nginx 访问时表现为页面请求变慢，属正常现象。
- **内网限制**：Render 免费档 Web 服务**只能发内网请求、不能收**，因此前端 Nginx 走后端
  `*.onrender.com` 公网地址反代（`BACKEND_*` 环境变量，见 `web/nginx.conf.template`）。
- **缓存无持久化**：Key Value 免费档重启丢数据。系统中的缓存/幂等/验证码均为短 TTL 数据，
  应用层已按“缓存可失效”设计，无需处理。

### 13.5 更新部署与排障

- **更新代码**：push `main` → Actions 重新构建镜像 → 到 Render 手动 **Manual Deploy → Deploy latest reference**，
  或配置仓库 Secret `RENDER_DEPLOY_HOOK_BACKEND` / `RENDER_DEPLOY_HOOK_WEB`
  （各服务 Settings → Deploy Hook 复制 URL）实现推送后自动滚动更新。
- **后端起不来**：Render 服务 → Logs 查看；常见为 `MYSQL_JDBC_URL` 密码错误、
  TiDB 未建 `inventory` 库、GHCR 镜像未设为 Public。
- **首次部署健康检查超时**：Flyway 远端迁移耗时较长属正常，可在 Render 日志确认
  `Successfully applied` 后再观察；若失败检查 TiDB 连通性。
- **Redisson 与 Valkey 8**：单机模式协议兼容；若未来使用其集群/新特性，
  需升级 Redisson ≥ 3.42 并启用对应能力开关。
- **内存吃紧**：免费档 512MB 下 JVM 以容器感知方式运行（`MaxRAMPercentage=70`），
  若遇 OOM 可减小连接池或升级实例。

---

© 2026 inventory-system · 以学习和企业内网交付为目的的参考实现

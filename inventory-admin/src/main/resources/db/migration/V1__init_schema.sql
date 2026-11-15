-- ============================================================================
--  V1__init_schema.sql  企业级进销存 / 库存管理系统 全量建表
--  MySQL 8.0 / utf8mb4 / InnoDB
--  约定：
--    * id 由应用层 MyBatis-Plus 雪花算法生成（IdType.ASSIGN_ID）
--    * deleted   逻辑删除（@TableLogic）
--    * version   乐观锁（@Version）
--    * create_by / create_dept / create_time / update_by / update_time 自动填充
--    * create_dept + create_by 是「数据权限」的 SQL 过滤依据
-- ============================================================================
SET NAMES utf8mb4;

-- ---------------------------------------------------------------------------
-- 一、系统域：RBAC / 数据权限 / 审计 / 通知 / 幂等
-- ---------------------------------------------------------------------------
CREATE TABLE sys_dept (
    id            BIGINT       NOT NULL COMMENT '部门ID',
    parent_id     BIGINT       NOT NULL DEFAULT 0 COMMENT '父部门ID',
    ancestors     VARCHAR(500) NOT NULL DEFAULT '' COMMENT '祖级列表(逗号分隔)',
    dept_name     VARCHAR(100) NOT NULL COMMENT '部门名称',
    dept_code     VARCHAR(64)  NOT NULL COMMENT '部门编码',
    leader        VARCHAR(64)           DEFAULT NULL COMMENT '负责人',
    phone         VARCHAR(32)           DEFAULT NULL COMMENT '联系电话',
    email         VARCHAR(128)          DEFAULT NULL COMMENT '邮箱',
    sort          INT          NOT NULL DEFAULT 0 COMMENT '显示顺序',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 1正常 0停用',
    create_by     BIGINT                DEFAULT NULL,
    create_dept   BIGINT                DEFAULT NULL,
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by     BIGINT                DEFAULT NULL,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    version       INT          NOT NULL DEFAULT 0,
    remark        VARCHAR(500)          DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_dept_parent (parent_id),
    UNIQUE KEY uk_dept_code (dept_code, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';

CREATE TABLE sys_user (
    id              BIGINT       NOT NULL COMMENT '用户ID',
    dept_id         BIGINT                DEFAULT NULL COMMENT '部门ID',
    username        VARCHAR(64)  NOT NULL COMMENT '登录账号',
    nickname        VARCHAR(64)  NOT NULL COMMENT '用户昵称',
    password        VARCHAR(120) NOT NULL COMMENT 'BCrypt 密码',
    email           VARCHAR(128)          DEFAULT NULL,
    phone           VARCHAR(32)           DEFAULT NULL,
    avatar          VARCHAR(255)          DEFAULT NULL,
    sex             TINYINT      NOT NULL DEFAULT 0 COMMENT '0未知 1男 2女',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '1正常 0停用',
    last_login_ip   VARCHAR(64)           DEFAULT NULL,
    last_login_time DATETIME              DEFAULT NULL,
    create_by       BIGINT                DEFAULT NULL,
    create_dept     BIGINT                DEFAULT NULL,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by       BIGINT                DEFAULT NULL,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    version         INT          NOT NULL DEFAULT 0,
    remark          VARCHAR(500)          DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_username (username, deleted),
    KEY idx_user_dept (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

CREATE TABLE sys_role (
    id          BIGINT       NOT NULL COMMENT '角色ID',
    role_name   VARCHAR(64)  NOT NULL COMMENT '角色名称',
    role_key    VARCHAR(64)  NOT NULL COMMENT '角色标识',
    sort        INT          NOT NULL DEFAULT 0,
    -- 数据权限：1全部 2本部门及以下 3本部门 4本人 5自定义部门
    data_scope  TINYINT      NOT NULL DEFAULT 4 COMMENT '数据范围',
    status      TINYINT      NOT NULL DEFAULT 1,
    create_by   BIGINT                DEFAULT NULL,
    create_dept BIGINT                DEFAULT NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by   BIGINT                DEFAULT NULL,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    version     INT          NOT NULL DEFAULT 0,
    remark      VARCHAR(500)          DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_key (role_key, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

CREATE TABLE sys_menu (
    id          BIGINT       NOT NULL COMMENT '菜单/权限ID',
    parent_id   BIGINT       NOT NULL DEFAULT 0,
    menu_name   VARCHAR(64)  NOT NULL,
    menu_type   CHAR(1)      NOT NULL COMMENT 'M目录 C菜单 F按钮/接口权限',
    path        VARCHAR(200)          DEFAULT NULL COMMENT '路由地址',
    component   VARCHAR(255)          DEFAULT NULL COMMENT '前端组件',
    route_name  VARCHAR(64)           DEFAULT NULL COMMENT '路由名称',
    icon        VARCHAR(64)           DEFAULT NULL,
    permission  VARCHAR(128)          DEFAULT NULL COMMENT '权限标识，如 inventory:stock:list',
    api_path    VARCHAR(255)          DEFAULT NULL COMMENT '接口权限：Ant 路径，可选二次校验',
    api_method  VARCHAR(16)           DEFAULT NULL COMMENT '接口权限：HTTP Method',
    sort        INT          NOT NULL DEFAULT 0,
    visible     TINYINT      NOT NULL DEFAULT 1,
    status      TINYINT      NOT NULL DEFAULT 1,
    is_frame    TINYINT      NOT NULL DEFAULT 0,
    create_by   BIGINT                DEFAULT NULL,
    create_dept BIGINT                DEFAULT NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by   BIGINT                DEFAULT NULL,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    version     INT          NOT NULL DEFAULT 0,
    remark      VARCHAR(500)          DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_menu_parent (parent_id),
    KEY idx_menu_permission (permission)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单与接口权限表';

CREATE TABLE sys_user_role (
    id      BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_ur_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联';

CREATE TABLE sys_role_menu (
    id      BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    KEY idx_rm_menu (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色菜单/接口权限关联';

CREATE TABLE sys_role_dept (
    id      BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_dept (role_id, dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色自定义数据权限部门';

CREATE TABLE sys_dict_type (
    id          BIGINT      NOT NULL,
    dict_name   VARCHAR(100) NOT NULL,
    dict_type   VARCHAR(100) NOT NULL,
    status      TINYINT     NOT NULL DEFAULT 1,
    create_by   BIGINT       DEFAULT NULL,
    create_dept BIGINT       DEFAULT NULL,
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by   BIGINT       DEFAULT NULL,
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT     NOT NULL DEFAULT 0,
    version     INT         NOT NULL DEFAULT 0,
    remark      VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_dict_type (dict_type, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典类型';

CREATE TABLE sys_dict_data (
    id          BIGINT       NOT NULL,
    dict_type   VARCHAR(100) NOT NULL,
    dict_label  VARCHAR(100) NOT NULL,
    dict_value  VARCHAR(100) NOT NULL,
    css_class   VARCHAR(100)          DEFAULT NULL,
    list_class  VARCHAR(100)          DEFAULT NULL,
    is_default  TINYINT      NOT NULL DEFAULT 0,
    sort        INT          NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 1,
    create_by   BIGINT                DEFAULT NULL,
    create_dept BIGINT                DEFAULT NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by   BIGINT                DEFAULT NULL,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    version     INT          NOT NULL DEFAULT 0,
    remark      VARCHAR(500)          DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典数据';

CREATE TABLE sys_config (
    id            BIGINT       NOT NULL,
    config_name   VARCHAR(100) NOT NULL,
    config_key    VARCHAR(100) NOT NULL,
    config_value  VARCHAR(500) NOT NULL,
    config_type   TINYINT      NOT NULL DEFAULT 1 COMMENT '1系统内置 0非内置',
    create_by     BIGINT       DEFAULT NULL,
    create_dept   BIGINT       DEFAULT NULL,
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by     BIGINT       DEFAULT NULL,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    version       INT          NOT NULL DEFAULT 0,
    remark        VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统参数(预警阈值等)';

CREATE TABLE sys_oper_log (
    id              BIGINT       NOT NULL,
    trace_id        VARCHAR(64)           DEFAULT NULL COMMENT '链路ID',
    title           VARCHAR(128)          DEFAULT NULL COMMENT '业务标题',
    module          VARCHAR(64)           DEFAULT NULL COMMENT '模块',
    business_type   VARCHAR(32)           DEFAULT NULL COMMENT 'INSERT/UPDATE/DELETE/EXPORT/IMPORT/LOGIN/OTHER',
    method          VARCHAR(255)          DEFAULT NULL COMMENT '执行方法',
    request_method  VARCHAR(16)           DEFAULT NULL,
    request_url     VARCHAR(500)          DEFAULT NULL,
    request_param   TEXT                  DEFAULT NULL,
    response_result TEXT                  DEFAULT NULL,
    oper_id         BIGINT                DEFAULT NULL,
    oper_name       VARCHAR(64)           DEFAULT NULL,
    oper_dept       BIGINT                DEFAULT NULL,
    oper_ip         VARCHAR(64)           DEFAULT NULL,
    user_agent      VARCHAR(500)          DEFAULT NULL,
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '1成功 0失败',
    error_msg       TEXT                  DEFAULT NULL,
    cost_ms         BIGINT       NOT NULL DEFAULT 0,
    oper_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_log_time (oper_time),
    KEY idx_log_user (oper_id),
    KEY idx_log_trace (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审计/操作日志';

CREATE TABLE sys_login_log (
    id         BIGINT      NOT NULL,
    trace_id   VARCHAR(64)  DEFAULT NULL,
    username   VARCHAR(64)  DEFAULT NULL,
    ip         VARCHAR(64)  DEFAULT NULL,
    browser    VARCHAR(64)  DEFAULT NULL,
    os         VARCHAR(64)  DEFAULT NULL,
    status     TINYINT     NOT NULL DEFAULT 1,
    message    VARCHAR(255) DEFAULT NULL,
    login_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录日志';

CREATE TABLE sys_notice (
    id          BIGINT       NOT NULL,
    title       VARCHAR(200) NOT NULL,
    content     TEXT                  DEFAULT NULL,
    notice_type TINYINT      NOT NULL DEFAULT 1 COMMENT '1通知 2公告 3预警',
    level       TINYINT      NOT NULL DEFAULT 1 COMMENT '1普通 2重要 3紧急',
    target_type TINYINT      NOT NULL DEFAULT 1 COMMENT '1全体 2指定用户 3指定角色',
    target_ids  VARCHAR(500)          DEFAULT NULL COMMENT '逗号分隔',
    sender_id   BIGINT                DEFAULT NULL,
    create_by   BIGINT                DEFAULT NULL,
    create_dept BIGINT                DEFAULT NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by   BIGINT                DEFAULT NULL,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    version     INT          NOT NULL DEFAULT 0,
    remark      VARCHAR(500)          DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_notice_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='站内信/通知公告';

CREATE TABLE sys_notice_read (
    id        BIGINT NOT NULL,
    notice_id BIGINT NOT NULL,
    user_id   BIGINT NOT NULL,
    read_flag TINYINT NOT NULL DEFAULT 0,
    read_time DATETIME DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_notice_user (notice_id, user_id),
    KEY idx_nr_user (user_id, read_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='站内信阅读状态';

CREATE TABLE biz_idempotent_record (
    id              BIGINT       NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL COMMENT '幂等键(请求头 Idempotency-Key 或业务键)',
    module          VARCHAR(64)  NOT NULL,
    request_hash    VARCHAR(128)          DEFAULT NULL,
    response_snapshot TEXT                 DEFAULT NULL COMMENT '首次成功响应快照，重复请求直接回放',
    status          TINYINT      NOT NULL DEFAULT 0 COMMENT '0处理中 1成功 2失败',
    expire_time     DATETIME              DEFAULT NULL,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_idempotency_key (idempotency_key, module),
    KEY idx_idempotent_expire (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='幂等记录(防重复提交)';

-- ---------------------------------------------------------------------------
-- 二、基础资料域
-- ---------------------------------------------------------------------------
CREATE TABLE biz_category (
    id            BIGINT       NOT NULL,
    parent_id     BIGINT       NOT NULL DEFAULT 0,
    category_name VARCHAR(100) NOT NULL,
    category_code VARCHAR(64)  NOT NULL,
    sort          INT          NOT NULL DEFAULT 0,
    status        TINYINT      NOT NULL DEFAULT 1,
    create_by     BIGINT                DEFAULT NULL,
    create_dept   BIGINT                DEFAULT NULL,
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by     BIGINT                DEFAULT NULL,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    version       INT          NOT NULL DEFAULT 0,
    remark        VARCHAR(500)          DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_code (category_code, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类';

CREATE TABLE biz_product (
    id             BIGINT        NOT NULL,
    category_id    BIGINT                 DEFAULT NULL,
    product_code   VARCHAR(64)   NOT NULL COMMENT 'SKU',
    product_name   VARCHAR(200)  NOT NULL,
    spec           VARCHAR(128)           DEFAULT NULL,
    unit           VARCHAR(32)   NOT NULL DEFAULT '件',
    brand          VARCHAR(64)            DEFAULT NULL,
    barcode        VARCHAR(64)            DEFAULT NULL,
    purchase_price DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    sale_price     DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    safety_stock   BIGINT        NOT NULL DEFAULT 0 COMMENT '安全库存(低库存预警阈值)',
    status         TINYINT       NOT NULL DEFAULT 1,
    image_url      VARCHAR(255)           DEFAULT NULL,
    create_by      BIGINT                 DEFAULT NULL,
    create_dept    BIGINT                 DEFAULT NULL,
    create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by      BIGINT                 DEFAULT NULL,
    update_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted        TINYINT       NOT NULL DEFAULT 0,
    version        INT           NOT NULL DEFAULT 0,
    remark         VARCHAR(500)           DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_code (product_code, deleted),
    KEY idx_product_category (category_id),
    KEY idx_product_name (product_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品/物料';

CREATE TABLE biz_warehouse (
    id             BIGINT       NOT NULL,
    warehouse_code VARCHAR(64)  NOT NULL,
    warehouse_name VARCHAR(128) NOT NULL,
    address        VARCHAR(255)          DEFAULT NULL,
    manager_id     BIGINT                DEFAULT NULL,
    phone          VARCHAR(32)           DEFAULT NULL,
    status         TINYINT      NOT NULL DEFAULT 1,
    create_by      BIGINT                DEFAULT NULL,
    create_dept    BIGINT                DEFAULT NULL,
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by      BIGINT                DEFAULT NULL,
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted        TINYINT      NOT NULL DEFAULT 0,
    version        INT          NOT NULL DEFAULT 0,
    remark         VARCHAR(500)          DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_warehouse_code (warehouse_code, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='仓库';

CREATE TABLE biz_supplier (
    id            BIGINT       NOT NULL,
    supplier_code VARCHAR(64)  NOT NULL,
    supplier_name VARCHAR(200) NOT NULL,
    contact       VARCHAR(64)           DEFAULT NULL,
    phone         VARCHAR(32)           DEFAULT NULL,
    email         VARCHAR(128)          DEFAULT NULL,
    address       VARCHAR(255)          DEFAULT NULL,
    status        TINYINT      NOT NULL DEFAULT 1,
    create_by     BIGINT                DEFAULT NULL,
    create_dept   BIGINT                DEFAULT NULL,
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by     BIGINT                DEFAULT NULL,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    version       INT          NOT NULL DEFAULT 0,
    remark        VARCHAR(500)          DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_supplier_code (supplier_code, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='供应商';

CREATE TABLE biz_customer (
    id            BIGINT       NOT NULL,
    customer_code VARCHAR(64)  NOT NULL,
    customer_name VARCHAR(200) NOT NULL,
    contact       VARCHAR(64)           DEFAULT NULL,
    phone         VARCHAR(32)           DEFAULT NULL,
    email         VARCHAR(128)          DEFAULT NULL,
    address       VARCHAR(255)          DEFAULT NULL,
    status        TINYINT      NOT NULL DEFAULT 1,
    create_by     BIGINT                DEFAULT NULL,
    create_dept   BIGINT                DEFAULT NULL,
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by     BIGINT                DEFAULT NULL,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    version       INT          NOT NULL DEFAULT 0,
    remark        VARCHAR(500)          DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_customer_code (customer_code, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户';

-- ---------------------------------------------------------------------------
-- 三、库存域
-- ---------------------------------------------------------------------------
CREATE TABLE biz_stock (
    id                 BIGINT NOT NULL,
    product_id         BIGINT NOT NULL,
    warehouse_id       BIGINT NOT NULL,
    quantity           BIGINT NOT NULL DEFAULT 0 COMMENT '在库总量',
    locked_quantity    BIGINT NOT NULL DEFAULT 0 COMMENT '锁定量(已下单未出库)',
    available_quantity BIGINT NOT NULL DEFAULT 0 COMMENT '可用量 = quantity - locked_quantity',
    create_by          BIGINT      DEFAULT NULL,
    create_dept        BIGINT      DEFAULT NULL,
    create_time        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by          BIGINT      DEFAULT NULL,
    update_time        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted            TINYINT  NOT NULL DEFAULT 0,
    version            INT      NOT NULL DEFAULT 0 COMMENT '乐观锁',
    remark             VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_stock_product_warehouse (product_id, warehouse_id, deleted),
    KEY idx_stock_warehouse (warehouse_id),
    KEY idx_stock_available (available_quantity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存主表(行级并发扣减)';

CREATE TABLE biz_stock_flow (
    id              BIGINT      NOT NULL,
    flow_no         VARCHAR(64) NOT NULL COMMENT '流水号',
    product_id      BIGINT      NOT NULL,
    warehouse_id    BIGINT      NOT NULL,
    biz_type        VARCHAR(32) NOT NULL COMMENT 'PURCHASE_IN/SALE_OUT/CHECK_GAIN/CHECK_LOSS/TRANSFER_IN/TRANSFER_OUT/LOCK/UNLOCK/INIT',
    biz_no          VARCHAR(64)           DEFAULT NULL COMMENT '关联业务单号',
    quantity_before BIGINT      NOT NULL DEFAULT 0,
    quantity_change BIGINT      NOT NULL DEFAULT 0 COMMENT '变动量(可正可负)',
    quantity_after  BIGINT      NOT NULL DEFAULT 0,
    operator_id     BIGINT                DEFAULT NULL,
    operator_name   VARCHAR(64)           DEFAULT NULL,
    oper_time       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT                DEFAULT NULL,
    create_dept     BIGINT                DEFAULT NULL,
    create_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by       BIGINT                DEFAULT NULL,
    update_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT     NOT NULL DEFAULT 0,
    version         INT         NOT NULL DEFAULT 0,
    remark          VARCHAR(500)          DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_flow_no (flow_no),
    KEY idx_flow_product (product_id, warehouse_id),
    KEY idx_flow_biz_no (biz_no),
    KEY idx_flow_time (oper_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存流水(可追溯台账)';

CREATE TABLE biz_inventory_check (
    id           BIGINT      NOT NULL,
    check_no     VARCHAR(64) NOT NULL,
    warehouse_id BIGINT      NOT NULL,
    check_date   DATE                  DEFAULT NULL,
    status       TINYINT     NOT NULL DEFAULT 0 COMMENT '0草稿 1待审核 2已完成 3已取消',
    audit_by     BIGINT                DEFAULT NULL,
    audit_time   DATETIME              DEFAULT NULL,
    create_by    BIGINT                DEFAULT NULL,
    create_dept  BIGINT                DEFAULT NULL,
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by    BIGINT                DEFAULT NULL,
    update_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted      TINYINT     NOT NULL DEFAULT 0,
    version      INT         NOT NULL DEFAULT 0,
    remark       VARCHAR(500)          DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_check_no (check_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='盘点单';

CREATE TABLE biz_inventory_check_item (
    id              BIGINT NOT NULL,
    check_id        BIGINT NOT NULL,
    product_id      BIGINT NOT NULL,
    system_quantity BIGINT NOT NULL DEFAULT 0,
    actual_quantity BIGINT NOT NULL DEFAULT 0,
    diff_quantity   BIGINT NOT NULL DEFAULT 0,
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         TINYINT  NOT NULL DEFAULT 0,
    remark          VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_check_item (check_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='盘点明细';

CREATE TABLE biz_transfer (
    id                BIGINT      NOT NULL,
    transfer_no       VARCHAR(64) NOT NULL,
    from_warehouse_id BIGINT      NOT NULL,
    to_warehouse_id   BIGINT      NOT NULL,
    product_id        BIGINT      NOT NULL,
    quantity          BIGINT      NOT NULL,
    status            TINYINT     NOT NULL DEFAULT 0 COMMENT '0草稿 1待审核 2已完成 3已取消',
    audit_by          BIGINT      DEFAULT NULL,
    audit_time        DATETIME    DEFAULT NULL,
    create_by         BIGINT      DEFAULT NULL,
    create_dept       BIGINT      DEFAULT NULL,
    create_time       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by         BIGINT      DEFAULT NULL,
    update_time       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted           TINYINT     NOT NULL DEFAULT 0,
    version           INT         NOT NULL DEFAULT 0,
    remark            VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_transfer_no (transfer_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='仓库调拨单';

-- ---------------------------------------------------------------------------
-- 四、采购 / 销售域
-- ---------------------------------------------------------------------------
CREATE TABLE biz_purchase_order (
    id              BIGINT        NOT NULL,
    order_no        VARCHAR(64)   NOT NULL COMMENT '采购单号 CGyyyyMMddxxxx',
    supplier_id     BIGINT        NOT NULL,
    warehouse_id    BIGINT        NOT NULL,
    total_amount    DECIMAL(16,2) NOT NULL DEFAULT 0.00,
    status          TINYINT       NOT NULL DEFAULT 0 COMMENT '0草稿 1待审核 2已审核 3已入库 4已取消 5驳回',
    expect_date     DATE                    DEFAULT NULL,
    audit_by        BIGINT                  DEFAULT NULL,
    audit_time      DATETIME                DEFAULT NULL,
    timeout_notified TINYINT      NOT NULL DEFAULT 0 COMMENT '超时提醒已发送',
    create_by       BIGINT                  DEFAULT NULL,
    create_dept     BIGINT                  DEFAULT NULL,
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by       BIGINT                  DEFAULT NULL,
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT       NOT NULL DEFAULT 0,
    version         INT           NOT NULL DEFAULT 0,
    remark          VARCHAR(500)            DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_purchase_no (order_no),
    KEY idx_purchase_status_time (status, create_time),
    KEY idx_purchase_supplier (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购订单';

CREATE TABLE biz_purchase_item (
    id                BIGINT        NOT NULL,
    order_id          BIGINT        NOT NULL,
    product_id        BIGINT        NOT NULL,
    quantity          BIGINT        NOT NULL DEFAULT 0,
    received_quantity BIGINT        NOT NULL DEFAULT 0,
    unit_price        DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    amount            DECIMAL(16,2) NOT NULL DEFAULT 0.00,
    create_time       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted           TINYINT       NOT NULL DEFAULT 0,
    remark            VARCHAR(500)            DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_pi_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购明细';

CREATE TABLE biz_sale_order (
    id               BIGINT        NOT NULL,
    order_no         VARCHAR(64)   NOT NULL COMMENT '销售单号 XSyyyyMMddxxxx',
    customer_id      BIGINT        NOT NULL,
    warehouse_id     BIGINT        NOT NULL,
    total_amount     DECIMAL(16,2) NOT NULL DEFAULT 0.00,
    status           TINYINT       NOT NULL DEFAULT 0 COMMENT '0草稿 1待审核 2已审核 3已出库 4已取消 5驳回',
    expect_date      DATE                    DEFAULT NULL,
    audit_by         BIGINT                  DEFAULT NULL,
    audit_time       DATETIME                DEFAULT NULL,
    timeout_notified TINYINT       NOT NULL DEFAULT 0,
    create_by        BIGINT                  DEFAULT NULL,
    create_dept      BIGINT                  DEFAULT NULL,
    create_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by        BIGINT                  DEFAULT NULL,
    update_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted          TINYINT       NOT NULL DEFAULT 0,
    version          INT           NOT NULL DEFAULT 0,
    remark           VARCHAR(500)            DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sale_no (order_no),
    KEY idx_sale_status_time (status, create_time),
    KEY idx_sale_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='销售订单';

CREATE TABLE biz_sale_item (
    id                 BIGINT        NOT NULL,
    order_id           BIGINT        NOT NULL,
    product_id         BIGINT        NOT NULL,
    quantity           BIGINT        NOT NULL DEFAULT 0,
    delivered_quantity BIGINT        NOT NULL DEFAULT 0,
    unit_price         DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    amount             DECIMAL(16,2) NOT NULL DEFAULT 0.00,
    create_time        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted            TINYINT       NOT NULL DEFAULT 0,
    remark             VARCHAR(500)            DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_si_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='销售明细';

-- ---------------------------------------------------------------------------
-- 五、报表域
-- ---------------------------------------------------------------------------
CREATE TABLE biz_daily_stat (
    id              BIGINT        NOT NULL,
    stat_date       DATE          NOT NULL,
    purchase_amount DECIMAL(16,2) NOT NULL DEFAULT 0.00,
    sale_amount     DECIMAL(16,2) NOT NULL DEFAULT 0.00,
    purchase_count  INT           NOT NULL DEFAULT 0,
    sale_count      INT           NOT NULL DEFAULT 0,
    stock_total     BIGINT        NOT NULL DEFAULT 0,
    low_stock_count INT           NOT NULL DEFAULT 0,
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='每日经营统计(定时任务预计算)';

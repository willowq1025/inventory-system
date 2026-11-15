package com.enterprise.common.core;

/** Redis 缓存键集中管理，统一前缀便于批量清理与运维。 */
public interface CacheKey {

    String PREFIX = "inv:";

    /** 用户权限集合 */
    String PERM_USER = PREFIX + "auth:perm:";
    /** 用户角色标识 */
    String ROLE_USER = PREFIX + "auth:role:";
    /** 用户基本信息快照 */
    String USER_SNAPSHOT = PREFIX + "auth:user:";
    /** 已登记的接口权限规则 */
    String API_RULES = PREFIX + "auth:api:rules";
    /** 字典（按类型） */
    String DICT_TYPE = PREFIX + "dict:";
    /** 系统参数 */
    String CONFIG = PREFIX + "config:";
    /** 商品热点缓存 */
    String PRODUCT = PREFIX + "product:";
    /** 商品分类树 */
    String CATEGORY_TREE = PREFIX + "category:tree";
    /** 部门树 / 子部门集合 */
    String DEPT_CHILDREN = PREFIX + "dept:children:";
    /** 幂等键 */
    String IDEMPOTENT = PREFIX + "idem:";
    /** 幂等响应快照 */
    String IDEMPOTENT_SNAPSHOT = PREFIX + "idem:snapshot:";
    /** 库存分布式锁 */
    String LOCK_STOCK = PREFIX + "lock:stock:";
    /** 业务单分布式锁 */
    String LOCK_ORDER = PREFIX + "lock:order:";
    /** 定时任务集群互斥锁 */
    String LOCK_JOB = PREFIX + "lock:job:";
    /** 防缓存穿透的空值标记 */
    String NULL_VALUE = "##NULL##";
}

-- ============================================================================
--  V2__init_data.sql  初始化数据
--  默认管理员：admin / Admin@123 （BCrypt 加密，首次登录请立即修改）
-- ============================================================================
SET NAMES utf8mb4;

-- 部门 -----------------------------------------------------------------------
INSERT INTO sys_dept (id,parent_id,ancestors,dept_name,dept_code,leader,sort,create_by,create_dept) VALUES
(100,0,'0','星辰科技','ROOT','管理员',1,1,100),
(101,100,'0,100','采购部','PURCHASE','王采',1,1,100),
(102,100,'0,100','销售部','SALE','李销',2,1,100),
(103,100,'0,100','仓储部','WAREHOUSE','张仓',3,1,100),
(104,100,'0,100','财务部','FINANCE','赵财',4,1,100);

-- 用户（密码均为 Admin@123）---------------------------------------------------
INSERT INTO sys_user (id,dept_id,username,nickname,password,email,phone,status,create_by,create_dept) VALUES
(1,100,'admin','超级管理员','$2a$10$rd4XatbmKsrFyaygIEIqueVvH0czYGbVZASiSD4lXcy8moS1Tvi0S','admin@example.com','13800000000',1,1,100),
(2,101,'purchaser','采购专员','$2a$10$rd4XatbmKsrFyaygIEIqueVvH0czYGbVZASiSD4lXcy8moS1Tvi0S','purchase@example.com','13800000001',1,1,100),
(3,102,'seller','销售专员','$2a$10$rd4XatbmKsrFyaygIEIqueVvH0czYGbVZASiSD4lXcy8moS1Tvi0S','sale@example.com','13800000002',1,1,100),
(4,103,'keeper','仓库管理员','$2a$10$rd4XatbmKsrFyaygIEIqueVvH0czYGbVZASiSD4lXcy8moS1Tvi0S','warehouse@example.com','13800000003',1,1,100);

-- 角色 -----------------------------------------------------------------------
INSERT INTO sys_role (id,role_name,role_key,sort,data_scope,status,create_by,create_dept,remark) VALUES
(1,'超级管理员','admin',1,1,1,1,100,'拥有全部数据与接口权限'),
(2,'采购经理','purchase_manager',2,2,1,1,100,'本部门及以下数据'),
(3,'仓库管理员','warehouse_keeper',3,3,1,1,100,'本部门数据'),
(4,'销售专员','seller',4,4,1,1,100,'仅本人数据');

INSERT INTO sys_user_role (id,user_id,role_id) VALUES (1,1,1),(2,2,2),(3,3,4),(4,4,3);

-- 菜单 / 接口权限 -------------------------------------------------------------
-- 目录
INSERT INTO sys_menu (id,parent_id,menu_name,menu_type,path,component,route_name,icon,sort,create_by,create_dept) VALUES
(1,0,'系统管理','M','/system',NULL,'System','Setting',1,1,100),
(2,0,'基础资料','M','/base',NULL,'Base','Files',2,1,100),
(3,0,'库存管理','M','/inventory',NULL,'Inventory','Box',3,1,100),
(4,0,'采购管理','M','/purchase',NULL,'Purchase','ShoppingCart',4,1,100),
(5,0,'销售管理','M','/sale',NULL,'Sale','Sell',5,1,100),
(6,0,'报表统计','M','/report',NULL,'Report','DataAnalysis',6,1,100),
(7,0,'消息中心','M','/message',NULL,'Message','Bell',7,1,100);
-- 菜单页
INSERT INTO sys_menu (id,parent_id,menu_name,menu_type,path,component,route_name,icon,sort,permission,api_path,api_method,create_by,create_dept) VALUES
(101,1,'用户管理','C','user','system/user/index','SysUser','User',1,'system:user:list','/api/system/users','GET',1,100),
(102,1,'角色管理','C','role','system/role/index','SysRole','UserFilled',2,'system:role:list','/api/system/roles','GET',1,100),
(103,1,'菜单管理','C','menu','system/menu/index','SysMenu','Menu',3,'system:menu:list','/api/system/menus','GET',1,100),
(104,1,'部门管理','C','dept','system/dept/index','SysDept','OfficeBuilding',4,'system:dept:list','/api/system/depts','GET',1,100),
(105,1,'字典管理','C','dict','system/dict/index','SysDict','Collection',5,'system:dict:list','/api/system/dict/types','GET',1,100),
(106,1,'审计日志','C','log','system/log/index','SysLog','Document',6,'system:log:list','/api/system/oper-logs','GET',1,100),
(107,1,'通知公告','C','notice','system/notice/index','SysNotice','Notification',7,'system:notice:list','/api/system/notices','GET',1,100),
(108,1,'系统参数','C','config','system/config/index','SysConfig','Tools',8,'system:config:list','/api/system/configs','GET',1,100),
(201,2,'商品分类','C','category','base/category/index','Category','FolderOpened',1,'base:category:list','/api/base/categories','GET',1,100),
(202,2,'商品管理','C','product','base/product/index','Product','Goods',2,'base:product:list','/api/base/products','GET',1,100),
(203,2,'仓库管理','C','warehouse','base/warehouse/index','Warehouse','House',3,'base:warehouse:list','/api/base/warehouses','GET',1,100),
(204,2,'供应商','C','supplier','base/supplier/index','Supplier','Truck',4,'base:supplier:list','/api/base/suppliers','GET',1,100),
(205,2,'客户管理','C','customer','base/customer/index','Customer','Avatar',5,'base:customer:list','/api/base/customers','GET',1,100),
(301,3,'库存查询','C','stock','inventory/stock/index','Stock','Coin',1,'inventory:stock:list','/api/inventory/stocks','GET',1,100),
(302,3,'库存流水','C','flow','inventory/flow/index','StockFlow','Tickets',2,'inventory:flow:list','/api/inventory/flows','GET',1,100),
(303,3,'库存盘点','C','check','inventory/check/index','InventoryCheck','Checked',3,'inventory:check:list','/api/inventory/checks','GET',1,100),
(304,3,'仓库调拨','C','transfer','inventory/transfer/index','Transfer','Sort',4,'inventory:transfer:list','/api/inventory/transfers','GET',1,100),
(401,4,'采购订单','C','order','purchase/order/index','PurchaseOrder','ShoppingCartFull',1,'purchase:order:list','/api/purchase/orders','GET',1,100),
(501,5,'销售订单','C','order','sale/order/index','SaleOrder','Sell',1,'sale:order:list','/api/sale/orders','GET',1,100),
(601,6,'经营看板','C','dashboard','report/dashboard/index','ReportDashboard','TrendCharts',1,'report:dashboard:view','/api/report/dashboard','GET',1,100),
(602,6,'进销存报表','C','pss','report/pss/index','ReportPss','DataLine',2,'report:pss:list','/api/report/pss','GET',1,100),
(701,7,'我的消息','C','mine','message/mine/index','MessageMine','Message',1,'message:mine:list','/api/system/notices/mine','GET',1,100);
-- 按钮 / 接口权限点（F 类型，@RequirePermission 与 ApiPermissionFilter 共同校验）
INSERT INTO sys_menu (id,parent_id,menu_name,menu_type,permission,api_path,api_method,sort,create_by,create_dept) VALUES
(1001,101,'用户新增','F','system:user:create','/api/system/users','POST',1,1,100),
(1002,101,'用户修改','F','system:user:update','/api/system/users/*','PUT',2,1,100),
(1003,101,'用户删除','F','system:user:delete','/api/system/users/*','DELETE',3,1,100),
(1004,101,'用户导出','F','system:user:export','/api/system/users/export','GET',4,1,100),
(1011,102,'角色新增','F','system:role:create','/api/system/roles','POST',1,1,100),
(1012,102,'角色修改','F','system:role:update','/api/system/roles/*','PUT',2,1,100),
(1013,102,'角色删除','F','system:role:delete','/api/system/roles/*','DELETE',3,1,100),
(1014,102,'分配权限','F','system:role:grant','/api/system/roles/*/menus','PUT',4,1,100),
(2001,202,'商品新增','F','base:product:create','/api/base/products','POST',1,1,100),
(2002,202,'商品修改','F','base:product:update','/api/base/products/*','PUT',2,1,100),
(2003,202,'商品删除','F','base:product:delete','/api/base/products/*','DELETE',3,1,100),
(2004,202,'商品导入','F','base:product:import','/api/base/products/import','POST',4,1,100),
(2005,202,'商品导出','F','base:product:export','/api/base/products/export','GET',5,1,100),
(3001,301,'库存调整','F','inventory:stock:adjust','/api/inventory/stocks/adjust','POST',1,1,100),
(3002,301,'库存导出','F','inventory:stock:export','/api/inventory/stocks/export','GET',2,1,100),
(3003,303,'盘点审核','F','inventory:check:audit','/api/inventory/checks/*/audit','POST',3,1,100),
(3004,304,'调拨审核','F','inventory:transfer:audit','/api/inventory/transfers/*/audit','POST',4,1,100),
(4001,401,'采购单创建','F','purchase:order:create','/api/purchase/orders','POST',1,1,100),
(4002,401,'采购单审核','F','purchase:order:audit','/api/purchase/orders/*/audit','POST',2,1,100),
(4003,401,'采购入库','F','purchase:order:stock-in','/api/purchase/orders/*/stock-in','POST',3,1,100),
(5001,501,'销售单创建','F','sale:order:create','/api/sale/orders','POST',1,1,100),
(5002,501,'销售单审核','F','sale:order:audit','/api/sale/orders/*/audit','POST',2,1,100),
(5003,501,'销售出库','F','sale:order:stock-out','/api/sale/orders/*/stock-out','POST',3,1,100),
(6001,602,'报表导出','F','report:pss:export','/api/report/pss/export','GET',1,1,100);
-- 补齐其余按钮 / 接口权限点（与 @RequirePermission 一一对应）
INSERT INTO sys_menu (id,parent_id,menu_name,menu_type,permission,api_path,api_method,sort,create_by,create_dept) VALUES
(8100,201,'商品分类新增','F','base:category:create','/api/base/categories','POST',1,1,100),
(8101,201,'商品分类修改','F','base:category:update','/api/base/categories/*','PUT',2,1,100),
(8102,201,'商品分类删除','F','base:category:delete','/api/base/categories/*','DELETE',3,1,100),
(8103,201,'商品分类导出','F','base:category:export','/api/base/categories/export','GET',4,1,100),
(8104,201,'商品分类导入','F','base:category:import','/api/base/categories/import','POST',5,1,100),
(8105,203,'仓库新增','F','base:warehouse:create','/api/base/warehouses','POST',1,1,100),
(8106,203,'仓库修改','F','base:warehouse:update','/api/base/warehouses/*','PUT',2,1,100),
(8107,203,'仓库删除','F','base:warehouse:delete','/api/base/warehouses/*','DELETE',3,1,100),
(8108,203,'仓库导出','F','base:warehouse:export','/api/base/warehouses/export','GET',4,1,100),
(8109,203,'仓库导入','F','base:warehouse:import','/api/base/warehouses/import','POST',5,1,100),
(8110,204,'供应商新增','F','base:supplier:create','/api/base/suppliers','POST',1,1,100),
(8111,204,'供应商修改','F','base:supplier:update','/api/base/suppliers/*','PUT',2,1,100),
(8112,204,'供应商删除','F','base:supplier:delete','/api/base/suppliers/*','DELETE',3,1,100),
(8113,204,'供应商导出','F','base:supplier:export','/api/base/suppliers/export','GET',4,1,100),
(8114,204,'供应商导入','F','base:supplier:import','/api/base/suppliers/import','POST',5,1,100),
(8115,205,'客户新增','F','base:customer:create','/api/base/customers','POST',1,1,100),
(8116,205,'客户修改','F','base:customer:update','/api/base/customers/*','PUT',2,1,100),
(8117,205,'客户删除','F','base:customer:delete','/api/base/customers/*','DELETE',3,1,100),
(8118,205,'客户导出','F','base:customer:export','/api/base/customers/export','GET',4,1,100),
(8119,205,'客户导入','F','base:customer:import','/api/base/customers/import','POST',5,1,100),
(8120,302,'库存流水导出','F','inventory:flow:export','/api/inventory/flows/export','GET',1,1,100),
(8121,303,'创建盘点单','F','inventory:check:create','/api/inventory/checks','POST',1,1,100),
(8122,304,'创建调拨单','F','inventory:transfer:create','/api/inventory/transfers','POST',1,1,100),
(8123,401,'采购单导出','F','purchase:order:export','/api/purchase/orders/export','GET',4,1,100),
(8124,501,'销售单导出','F','sale:order:export','/api/sale/orders/export','GET',4,1,100),
(8125,108,'修改系统参数','F','system:config:update','/api/system/configs/*','PUT',1,1,100),
(8126,108,'手动执行任务','F','system:job:run','/api/jobs/*/run','POST',2,1,100),
(8127,104,'部门新增','F','system:dept:create','/api/system/depts','POST',1,1,100),
(8128,104,'部门修改','F','system:dept:update','/api/system/depts/*','PUT',2,1,100),
(8129,104,'部门删除','F','system:dept:delete','/api/system/depts/*','DELETE',3,1,100),
(8130,105,'字典项新增/修改','F','system:dict:update','/api/system/dict/data','POST',1,1,100),
(8131,105,'字典项删除','F','system:dict:update','/api/system/dict/data/*','DELETE',2,1,100),
(8132,106,'审计日志导出','F','system:log:export','/api/system/oper-logs/export','GET',1,1,100),
(8133,103,'菜单新增','F','system:menu:create','/api/system/menus','POST',1,1,100),
(8134,103,'菜单修改','F','system:menu:update','/api/system/menus/*','PUT',2,1,100),
(8135,103,'菜单删除','F','system:menu:delete','/api/system/menus/*','DELETE',3,1,100),
(8136,107,'发布通知','F','system:notice:create','/api/system/notices/publish','POST',1,1,100);
-- 超级管理员授予全部菜单；其余角色按职责授权
INSERT INTO sys_role_menu (id,role_id,menu_id) SELECT 30000 + id, 1, id FROM sys_menu;
INSERT INTO sys_role_menu (id,role_id,menu_id) VALUES
(20001,2,2),(20002,2,201),(20003,2,202),(20004,2,203),(20005,2,204),(20006,2,4),(20007,2,401),
(20008,2,4001),(20009,2,4002),(20010,2,4003),(20011,2,3),(20012,2,301),(20013,2,601),
(20021,3,2),(20022,3,202),(20023,3,203),(20024,3,3),(20025,3,301),(20026,3,302),(20027,3,303),
(20028,3,304),(20029,3,3001),(20030,3,3003),(20031,3,3004),
(20041,4,5),(20042,4,501),(20043,4,5001),(20044,4,3),(20045,4,301),(20046,4,2),(20047,4,202),
(20048,2,701),(20049,3,701),(20050,4,701);
INSERT INTO sys_role_dept (id,role_id,dept_id) VALUES (1,2,101),(2,3,103),(3,4,102);

-- 字典 -----------------------------------------------------------------------
INSERT INTO sys_dict_type (id,dict_name,dict_type,status,create_by,create_dept) VALUES
(1,'通用状态','sys_common_status',1,1,100),
(2,'采购订单状态','biz_purchase_status',1,1,100),
(3,'销售订单状态','biz_sale_status',1,1,100),
(4,'库存变动类型','biz_stock_flow_type',1,1,100),
(5,'数据权限范围','sys_data_scope',1,1,100),
(6,'通知类型','sys_notice_type',1,1,100);
INSERT INTO sys_dict_data (id,dict_type,dict_label,dict_value,list_class,sort,create_by,create_dept) VALUES
(1,'sys_common_status','正常','1','success',1,1,100),(2,'sys_common_status','停用','0','danger',2,1,100),
(3,'biz_purchase_status','草稿','0','info',1,1,100),(4,'biz_purchase_status','待审核','1','warning',2,1,100),
(5,'biz_purchase_status','已审核','2','primary',3,1,100),(6,'biz_purchase_status','已入库','3','success',4,1,100),
(7,'biz_purchase_status','已取消','4','info',5,1,100),(8,'biz_purchase_status','驳回','5','danger',6,1,100),
(9,'biz_sale_status','草稿','0','info',1,1,100),(10,'biz_sale_status','待审核','1','warning',2,1,100),
(11,'biz_sale_status','已审核','2','primary',3,1,100),(12,'biz_sale_status','已出库','3','success',4,1,100),
(13,'biz_sale_status','已取消','4','info',5,1,100),(14,'biz_sale_status','驳回','5','danger',6,1,100),
(15,'biz_stock_flow_type','采购入库','PURCHASE_IN','success',1,1,100),(16,'biz_stock_flow_type','销售出库','SALE_OUT','warning',2,1,100),
(17,'biz_stock_flow_type','盘盈','CHECK_GAIN','success',3,1,100),(18,'biz_stock_flow_type','盘亏','CHECK_LOSS','danger',4,1,100),
(19,'biz_stock_flow_type','调拨入','TRANSFER_IN','primary',5,1,100),(20,'biz_stock_flow_type','调拨出','TRANSFER_OUT','primary',6,1,100),
(21,'sys_data_scope','全部数据','1','danger',1,1,100),(22,'sys_data_scope','本部门及以下','2','warning',2,1,100),
(23,'sys_data_scope','本部门','3','primary',3,1,100),(24,'sys_data_scope','仅本人','4','success',4,1,100),
(25,'sys_notice_type','通知','1','primary',1,1,100),(26,'sys_notice_type','公告','2','success',2,1,100),
(27,'sys_notice_type','预警','3','danger',3,1,100);

-- 系统参数 -------------------------------------------------------------------
INSERT INTO sys_config (id,config_name,config_key,config_value,config_type,create_by,create_dept,remark) VALUES
(1,'订单审核超时小时数','order.audit.timeout.hours','24',1,1,100,'超过该时长未审核的待审核订单触发提醒'),
(2,'默认低库存预警阈值','stock.safety.threshold','10',1,1,100,'商品未单独设置安全库存时使用该值'),
(3,'定时任务集群锁秒数','job.lock.seconds','300',1,1,100,'分布式锁持有时间'),
(4,'Webhook 通知地址','notify.webhook.url','',1,1,100,'留空则不发送 Webhook'),
(5,'Webhook 签名密钥','notify.webhook.secret','change-me',1,1,100,'HmacSHA256 签名密钥');

-- 基础资料 -------------------------------------------------------------------
INSERT INTO biz_category (id,parent_id,category_name,category_code,sort,create_by,create_dept) VALUES
(1,0,'电子产品','ELEC',1,1,100),(2,0,'办公用品','OFFICE',2,1,100),
(11,1,'电脑整机','ELEC-PC',1,1,100),(12,1,'外设配件','ELEC-PERI',2,1,100),
(21,2,'纸品','OFFICE-PAPER',1,1,100);

INSERT INTO biz_warehouse (id,warehouse_code,warehouse_name,address,manager_id,phone,create_by,create_dept) VALUES
(1,'WH001','华东主仓','上海市青浦区华新镇 88 号',4,'021-60000001',1,100),
(2,'WH002','华南分仓','深圳市龙岗区坂田街道 12 号',4,'0755-60000002',1,100);

INSERT INTO biz_product (id,category_id,product_code,product_name,spec,unit,brand,purchase_price,sale_price,safety_stock,create_by,create_dept) VALUES
(1,11,'SKU-PC-0001','商务笔记本电脑','14寸 / 16G / 512G','台','ThinkPad',5200.00,6999.00,10,1,100),
(2,11,'SKU-PC-0002','轻薄本','13寸 / 16G / 1T','台','MateBook',6100.00,7999.00,8,1,100),
(3,12,'SKU-PERI-0001','无线机械键盘','87键 / 茶轴','个','Keychron',320.00,499.00,20,1,100),
(4,12,'SKU-PERI-0002','人体工学鼠标','无线 / 蓝牙','个','Logitech',210.00,329.00,20,1,100),
(5,21,'SKU-OFF-0001','A4 复印纸','70g / 500张','包','Double A',18.50,26.00,50,1,100);

INSERT INTO biz_supplier (id,supplier_code,supplier_name,contact,phone,email,address,create_by,create_dept) VALUES
(1,'SUP001','联想（上海）信息技术有限公司','陈经理','021-51000001','supply1@example.com','上海市浦东新区',1,100),
(2,'SUP002','深圳外设供应链有限公司','刘经理','0755-51000002','supply2@example.com','深圳市南山区',1,100);

INSERT INTO biz_customer (id,customer_code,customer_name,contact,phone,email,address,create_by,create_dept) VALUES
(1,'CUS001','华东贸易有限公司','周总','021-52000001','buy1@example.com','上海市静安区',1,100),
(2,'CUS002','华南智造科技有限公司','吴总','020-52000002','buy2@example.com','广州市天河区',1,100);

-- 初始库存 -------------------------------------------------------------------
INSERT INTO biz_stock (id,product_id,warehouse_id,quantity,locked_quantity,available_quantity,create_by,create_dept,remark) VALUES
(1,1,1,120,0,120,1,100,'初始化'),(2,2,1,80,0,80,1,100,'初始化'),(3,3,1,300,0,300,1,100,'初始化'),
(4,4,1,260,0,260,1,100,'初始化'),(5,5,1,800,0,800,1,100,'初始化'),
(6,1,2,40,0,40,1,100,'初始化'),(7,3,2,150,0,150,1,100,'初始化');

INSERT INTO biz_stock_flow (id,flow_no,product_id,warehouse_id,biz_type,biz_no,quantity_before,quantity_change,quantity_after,operator_id,operator_name,create_by,create_dept,remark) VALUES
(1,'INIT-000001',1,1,'INIT',NULL,0,120,120,1,'超级管理员',1,100,'系统初始化'),
(2,'INIT-000002',2,1,'INIT',NULL,0,80,80,1,'超级管理员',1,100,'系统初始化'),
(3,'INIT-000003',3,1,'INIT',NULL,0,300,300,1,'超级管理员',1,100,'系统初始化'),
(4,'INIT-000004',4,1,'INIT',NULL,0,260,260,1,'超级管理员',1,100,'系统初始化'),
(5,'INIT-000005',5,1,'INIT',NULL,0,800,800,1,'超级管理员',1,100,'系统初始化');

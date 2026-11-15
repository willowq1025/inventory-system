import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import Layout from '@/layout/Layout.vue'
import { useUserStore } from '@/stores/user'

/**
 * 静态可分析的路由组件映射。
 * 不能用 import(`../views/${path}.vue`)：Vite 对「变量 + 多级目录」的动态导入无法展开，
 * 会生成空的模块表，构建能过但运行时报 Unknown variable dynamic import。
 * import.meta.glob 由 Vite 在构建期展开为每个页面独立的 lazy chunk。
 */
const modules = import.meta.glob('../views/**/*.vue')
const view = (path: string) => {
  const key = `../views/${path}.vue`
  const loader = modules[key]
  if (!loader) {
    return () => Promise.reject(new Error(`页面组件不存在: ${key}`))
  }
  return loader as () => Promise<{ default: unknown }>
}

/**
 * 静态路由表（组件映射）；可见性由后端返回的菜单树决定，
 * 接口权限由后端 @RequirePermission + sys_menu.api_path 双重校验。
 */
export const pageRoutes: RouteRecordRaw[] = [
  { path: 'dashboard', name: 'Dashboard', component: view('dashboard/index'), meta: { title: '首页概览', icon: 'House' } },
  { path: 'system/user', name: 'SysUser', component: view('system/user'), meta: { title: '用户管理', permission: 'system:user:list' } },
  { path: 'system/role', name: 'SysRole', component: view('system/role'), meta: { title: '角色管理', permission: 'system:role:list' } },
  { path: 'system/menu', name: 'SysMenu', component: view('system/menu'), meta: { title: '菜单管理', permission: 'system:menu:list' } },
  { path: 'system/dept', name: 'SysDept', component: view('system/dept'), meta: { title: '部门管理', permission: 'system:dept:list' } },
  { path: 'system/dict', name: 'SysDict', component: view('system/dict'), meta: { title: '字典管理', permission: 'system:dict:list' } },
  { path: 'system/log', name: 'SysLog', component: view('system/log'), meta: { title: '审计日志', permission: 'system:log:list' } },
  { path: 'system/notice', name: 'SysNotice', component: view('system/notice'), meta: { title: '通知公告', permission: 'system:notice:list' } },
  { path: 'base/category', name: 'Category', component: view('base/category'), meta: { title: '商品分类', permission: 'base:category:list' } },
  { path: 'base/product', name: 'Product', component: view('base/product'), meta: { title: '商品管理', permission: 'base:product:list' } },
  { path: 'base/warehouse', name: 'Warehouse', component: view('base/warehouse'), meta: { title: '仓库管理', permission: 'base:warehouse:list' } },
  { path: 'base/supplier', name: 'Supplier', component: view('base/supplier'), meta: { title: '供应商', permission: 'base:supplier:list' } },
  { path: 'base/customer', name: 'Customer', component: view('base/customer'), meta: { title: '客户管理', permission: 'base:customer:list' } },
  { path: 'inventory/stock', name: 'Stock', component: view('inventory/stock'), meta: { title: '库存查询', permission: 'inventory:stock:list' } },
  { path: 'inventory/flow', name: 'StockFlow', component: view('inventory/flow'), meta: { title: '库存流水', permission: 'inventory:flow:list' } },
  { path: 'inventory/check', name: 'InventoryCheck', component: view('inventory/check'), meta: { title: '库存盘点', permission: 'inventory:check:list' } },
  { path: 'inventory/transfer', name: 'Transfer', component: view('inventory/transfer'), meta: { title: '仓库调拨', permission: 'inventory:transfer:list' } },
  { path: 'purchase/order', name: 'PurchaseOrder', component: view('purchase/order'), meta: { title: '采购订单', permission: 'purchase:order:list' } },
  { path: 'sale/order', name: 'SaleOrder', component: view('sale/order'), meta: { title: '销售订单', permission: 'sale:order:list' } },
  { path: 'report/dashboard', name: 'ReportDashboard', component: view('report/dashboard'), meta: { title: '经营看板', permission: 'report:dashboard:view' } },
  { path: 'report/pss', name: 'ReportPss', component: view('report/pss'), meta: { title: '进销存报表', permission: 'report:pss:list' } },
  { path: 'message/mine', name: 'MessageMine', component: view('message/mine'), meta: { title: '我的消息' } },
  { path: 'system/config', name: 'SysConfig', component: view('system/config'), meta: { title: '系统参数' } }
]

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'Login', component: view('login/index'), meta: { public: true } },
    {
      path: '/',
      component: Layout,
      redirect: '/dashboard',
      children: [
        ...pageRoutes,
        { path: ':pathMatch(.*)*', name: 'NotFound', component: view('error/404') }
      ]
    }
  ],
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach(async (to, _from, next) => {
  const user = useUserStore()
  if (to.meta.public) return next()
  if (!user.isLogged) return next({ path: '/login', query: { redirect: to.fullPath } })
  if (!user.loaded) {
    try { await user.loadInfo() } catch { user.reset(); return next('/login') }
  }
  const perm = to.meta.permission as string | undefined
  if (perm && !user.hasPerm(perm)) return next({ path: '/dashboard' })
  next()
})

export default router

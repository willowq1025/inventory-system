export interface LoginCmd { username: string; password: string }

export interface LoginToken {
  accessToken: string
  tokenType: string
  expiresIn: number
  userId: string
  nickname: string
}

export interface RouteMeta {
  title: string
  icon?: string
  permission?: string
  hidden?: boolean
}

export interface RouteVO {
  id: string
  path: string
  name: string
  component: string
  redirect?: string
  meta: RouteMeta
  children: RouteVO[]
}

export interface UserVO {
  id: string
  username: string
  nickname: string
  email?: string
  phone?: string
  deptId?: string
  deptName?: string
  status: number
  roleIds?: string[]
  createTime?: string
}

export interface LoginVO {
  token: LoginToken
  user: UserVO
  permissions: string[]
  roles: string[]
  menus: RouteVO[]
}

export interface DictVO { id?: string; label: string; value: string; listClass?: string; cssClass?: string }

export interface StockVO {
  id: string
  productId: string
  warehouseId: string
  quantity: number
  lockedQuantity: number
  availableQuantity: number
  productCode: string
  productName: string
  unit: string
  safetyStock: number
  warehouseName: string
  lowStockText?: string
  updateTime?: string
}

export interface DashboardVO {
  todayPurchase: number
  todaySale: number
  monthPurchase: number
  monthSale: number
  todayPurchaseCount: number
  todaySaleCount: number
  pendingPurchase: number
  pendingSale: number
  stockTotal: number
  lowStockCount: number
  trend: TrendPoint[]
  warehouseDistribution: Record<string, unknown>[]
}

export interface TrendPoint { date: string; purchaseAmount: number; saleAmount: number }

export interface PssVO {
  productId: string
  productCode: string
  productName: string
  unit: string
  warehouseId: string
  warehouseName: string
  openingQty: number
  inQty: number
  outQty: number
  closingQty: number
}

export interface ImportResult {
  total: number
  successCount: number
  failCount: number
  errors: { rowIndex: number; message: string }[]
}

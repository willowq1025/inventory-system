import dayjs from 'dayjs'

export function fmtDate(value?: string | null): string {
  if (!value) return '-'
  return dayjs(value).format('YYYY-MM-DD HH:mm:ss')
}

export function fmtMoney(value?: number | string | null): string {
  if (value === null || value === undefined) return '¥0.00'
  const num = Number(value)
  return '¥' + num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

export function fmtNumber(value?: number | string | null): string {
  if (value === null || value === undefined) return '0'
  return Number(value).toLocaleString('zh-CN')
}

export interface TreeNode { id: string; parentId: string; children?: TreeNode[]; [key: string]: unknown }

export function buildTree<T extends TreeNode>(rows: T[], rootId = '0'): T[] {
  const map = new Map<string, T>()
  rows.forEach((r) => map.set(String(r.id), { ...r, children: [] } as T))
  const roots: T[] = []
  map.forEach((node) => {
    const parent = map.get(String(node.parentId))
    if (parent && String(node.parentId) !== rootId) parent.children!.push(node)
    else roots.push(node)
  })
  return roots
}

/** 菜单树 → 可见的侧边栏条目（拼出完整路由路径） */
export function flattenMenus(menus: { path: string; name?: string; meta?: Record<string, unknown>; children?: unknown[] }[],
                              parentPath = ''): { path: string; title: string; icon?: string; children?: unknown[] }[] {
  const result: { path: string; title: string; icon?: string; children?: unknown[] }[] = []
  for (const m of menus) {
    if (m.meta?.hidden) continue
    const path = m.path.startsWith('/') ? m.path : `${parentPath}/${m.path}`.replace(/\/+/g, '/')
    const item = {
      path,
      title: String(m.meta?.title || m.name || m.path),
      icon: m.meta?.icon as string | undefined,
      children: undefined as unknown[] | undefined
    }
    const children = (m.children || []) as typeof menus
    if (children.length) item.children = flattenMenus(children, path) as unknown[]
    result.push(item)
  }
  return result
}

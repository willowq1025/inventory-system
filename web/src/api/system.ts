import { del, download, get, post, put } from './request'
import type { PageQuery, PageResult } from './request'
import type { DictVO, UserVO } from './types'

export const systemApi = {
  users: (q: PageQuery) => get<PageResult<Record<string, unknown>>>('/system/users', q as Record<string, unknown>),
  user: (id: string) => get<UserVO>(`/system/users/${id}`),
  createUser: (data: Record<string, unknown>) => post<string>('/system/users', data),
  updateUser: (id: string, data: Record<string, unknown>) => put<void>(`/system/users/${id}`, data),
  deleteUser: (id: string) => del<void>(`/system/users/${id}`),
  resetPassword: (id: string, password: string) => put<void>(`/system/users/${id}/password`, { password }),
  exportUsers: (q: PageQuery) => download('/system/users/export', q as Record<string, unknown>, '用户列表.xlsx'),

  roles: (q: PageQuery) => get<PageResult<Record<string, unknown>>>('/system/roles', q as Record<string, unknown>),
  allRoles: () => get<Record<string, unknown>[]>('/system/roles/all'),
  role: (id: string) => get<Record<string, unknown>>(`/system/roles/${id}`),
  createRole: (data: Record<string, unknown>) => post<string>('/system/roles', data),
  updateRole: (id: string, data: Record<string, unknown>) => put<void>(`/system/roles/${id}`, data),
  deleteRole: (id: string) => del<void>(`/system/roles/${id}`),
  grant: (id: string, menuIds: string[], deptIds: string[]) =>
    put<void>(`/system/roles/${id}/menus`, { menuIds, deptIds }),

  menus: () => get<Record<string, unknown>[]>('/system/menus'),
  createMenu: (data: Record<string, unknown>) => post<string>('/system/menus', data),
  updateMenu: (id: string, data: Record<string, unknown>) => put<void>(`/system/menus/${id}`, data),
  deleteMenu: (id: string) => del<void>(`/system/menus/${id}`),

  depts: () => get<Record<string, unknown>[]>('/system/depts'),
  createDept: (data: Record<string, unknown>) => post<string>('/system/depts', data),
  updateDept: (id: string, data: Record<string, unknown>) => put<void>(`/system/depts/${id}`, data),
  deleteDept: (id: string) => del<void>(`/system/depts/${id}`),

  dictTypes: () => get<Record<string, unknown>[]>('/system/dict/types'),
  dictByType: (type: string) => get<DictVO[]>(`/system/dict/type/${type}`),
  allDict: () => get<Record<string, DictVO[]>>('/system/dict/all'),
  saveDictData: (data: Record<string, unknown>) => post<void>('/system/dict/data', data),
  deleteDictData: (id: string) => del<void>(`/system/dict/data/${id}`),

  operLogs: (q: PageQuery) => get<PageResult<Record<string, unknown>>>('/system/oper-logs', q as Record<string, unknown>),
  exportLogs: (q: PageQuery) => download('/system/oper-logs/export', q as Record<string, unknown>, '审计日志.xlsx'),

  notices: (q: PageQuery) => get<PageResult<Record<string, unknown>>>('/system/notices', q as Record<string, unknown>),
  mine: () => get<Record<string, unknown>[]>('/system/notices/mine'),
  unreadCount: () => get<number>('/system/notices/unread-count'),
  markRead: (id: string) => post<void>(`/system/notices/read/${id}`),
  markAllRead: () => post<void>('/system/notices/read-all'),
  publish: (data: Record<string, unknown>) => post<void>('/system/notices/publish', data),

  configs: () => get<Record<string, unknown>[]>('/system/configs'),
  updateConfig: (id: string, configValue: string) => put<void>(`/system/configs/${id}`, { configValue }),

  saveDictType: (data: Record<string, unknown>) => post<void>('/system/dict/type', data),
  permissions: () => get<Record<string, unknown>[]>('/system/menus/permissions'),
  jobs: () => get<Record<string, string>>('/jobs'),
  runJob: (name: string) => post<boolean>(`/jobs/${name}/run`)
}

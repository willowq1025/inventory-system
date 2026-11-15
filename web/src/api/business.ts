import { del, download, get, post, put, upload } from './request'
import type { PageQuery, PageResult } from './request'
import type { DashboardVO, DictVO, ImportResult, PssVO, StockVO, TrendPoint } from './types'

type Row = Record<string, unknown>

export const baseApi = {
  list: (resource: string, q: PageQuery) => get<PageResult<Row>>(`/base/${resource}`, q as Record<string, unknown>),
  /** 下拉选择用的全量数据（后端分页最大 500 条） */
  all: (resource: string) =>
    get<PageResult<Row>>(`/base/${resource}`, { pageNum: 1, pageSize: 500 }).then((r) => r.records || []),
  create: (resource: string, data: Row) => post<string>(`/base/${resource}`, data),
  update: (resource: string, id: string, data: Row) => put<void>(`/base/${resource}/${id}`, data),
  remove: (resource: string, id: string) => del<void>(`/base/${resource}/${id}`),
  export: (resource: string, q: PageQuery, name: string) =>
    download(`/base/${resource}/export`, q as Record<string, unknown>, `${name}.xlsx`),
  import: (resource: string, file: File) => upload<ImportResult>(`/base/${resource}/import`, file)
}

export const inventoryApi = {
  stocks: (q: PageQuery) => get<PageResult<StockVO>>('/inventory/stocks', q as Record<string, unknown>),
  summary: () => get<Row>('/inventory/stocks/summary'),
  adjust: (data: Row) => post<number>('/inventory/stocks/adjust', data),
  exportStocks: (q: PageQuery) => download('/inventory/stocks/export', q as Record<string, unknown>, '库存台账.xlsx'),
  flows: (q: PageQuery) => get<PageResult<Row>>('/inventory/flows', q as Record<string, unknown>),
  exportFlows: (q: PageQuery) => download('/inventory/flows/export', q as Record<string, unknown>, '库存流水.xlsx'),
  checks: (q: PageQuery) => get<PageResult<Row>>('/inventory/checks', q as Record<string, unknown>),
  checkItems: (id: string) => get<Row[]>(`/inventory/checks/${id}/items`),
  createCheck: (data: Row) => post<string>('/inventory/checks', data),
  auditCheck: (id: string) => post<void>(`/inventory/checks/${id}/audit`),
  transfers: (q: PageQuery) => get<PageResult<Row>>('/inventory/transfers', q as Record<string, unknown>),
  createTransfer: (data: Row) => post<string>('/inventory/transfers', data),
  auditTransfer: (id: string) => post<void>(`/inventory/transfers/${id}/audit`)
}

export const orderApi = {
  list: (type: 'purchase' | 'sale', q: PageQuery) =>
    get<PageResult<Row>>(`/${type}/orders`, q as Record<string, unknown>),
  detail: (type: 'purchase' | 'sale', id: string) => get<Row>(`/${type}/orders/${id}`),
  create: (type: 'purchase' | 'sale', data: Row) => post<string>(`/${type}/orders`, data),
  audit: (type: 'purchase' | 'sale', id: string, pass: boolean) =>
    post<void>(`/${type}/orders/${id}/audit`, { pass }),
  execute: (type: 'purchase' | 'sale', id: string) =>
    post<void>(`/${type}/orders/${id}/${type === 'purchase' ? 'stock-in' : 'stock-out'}`),
  cancel: (type: 'purchase' | 'sale', id: string) => post<void>(`/${type}/orders/${id}/cancel`),
  export: (type: 'purchase' | 'sale', q: PageQuery) =>
    download(`/${type}/orders/export`, q as Record<string, unknown>, `${type === 'purchase' ? '采购' : '销售'}订单.xlsx`)
}

export const reportApi = {
  dashboard: (days = 7) => get<DashboardVO>(`/report/dashboard?days=${days}`),
  trend: (days = 7) => get<TrendPoint[]>(`/report/trend?days=${days}`),
  warehouseDistribution: () => get<Row[]>('/report/warehouse-distribution'),
  pss: (q: Row) => get<PssVO[]>('/report/pss', q),
  exportPss: (q: Row) => download('/report/pss/export', q, '进销存报表.xlsx')
}

export const dictApi = {
  all: () => get<Record<string, DictVO[]>>('/system/dict/all')
}

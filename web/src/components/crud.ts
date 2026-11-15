export interface CrudColumn {
  prop: string
  label: string
  width?: number
  type?: 'text' | 'money' | 'status' | 'date' | 'number'
}

export type RemoteResource = 'categories' | 'products' | 'warehouses' | 'suppliers' | 'customers'

export interface CrudField {
  prop: string
  label: string
  type?: 'text' | 'number' | 'select' | 'textarea'
  options?: { label: string; value: string | number }[]
  /** 从基础资料接口拉取下拉选项；雪花 ID 以字符串传递，避免 JS 精度丢失 */
  remote?: RemoteResource
  required?: boolean
  hint?: string
}

export interface CrudConfig {
  title: string
  resource: string
  perm: string
  columns: CrudColumn[]
  fields: CrudField[]
  exportable?: boolean
  importable?: boolean
}

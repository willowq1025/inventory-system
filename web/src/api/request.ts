import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

export interface Result<T> {
  code: number
  message: string
  data: T
  traceId?: string
  timestamp: number
}

export interface PageResult<T> {
  total: number
  records: T[]
  pageNum: number
  pageSize: number
  pages: number
}

export interface PageQuery {
  pageNum: number
  pageSize: number
  keyword?: string
  startTime?: string
  endTime?: string
  orderBy?: string
  asc?: boolean
  [key: string]: unknown
}

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 30000
})

function uuid(): string {
  return typeof crypto !== 'undefined' && crypto.randomUUID
    ? crypto.randomUUID().replace(/-/g, '')
    : `${Date.now()}${Math.random().toString(16).slice(2)}`
}

instance.interceptors.request.use((config) => {
  const token = localStorage.getItem('inv_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  config.headers['X-Trace-Id'] = uuid()
  // 写操作自动生成幂等键；也可由业务显式传入覆盖
  const method = (config.method || 'get').toLowerCase()
  if (['post', 'put', 'delete'].includes(method) && !config.headers['Idempotency-Key']) {
    config.headers['Idempotency-Key'] = uuid()
  }
  return config
})

instance.interceptors.response.use(
  (response) => {
    if (response.config.responseType === 'blob') return response
    const body = response.data as Result<unknown>
    if (body && body.code === 200) return body as unknown as typeof response
    ElMessage.error(body?.message || '请求失败')
    return Promise.reject(new Error(body?.message || '请求失败'))
  },
  (error) => {
    const status = error?.response?.status
    const body = error?.response?.data as Result<unknown> | undefined
    const message = body?.message || error?.message || '网络异常'
    if (status === 401) {
      localStorage.removeItem('inv_token')
      localStorage.removeItem('inv_user')
      ElMessage.error('登录已失效，请重新登录')
      setTimeout(() => { window.location.href = '/login' }, 600)
    } else if (status === 403) {
      ElMessage.error('没有权限执行该操作')
    } else {
      ElMessage.error(message + (body?.traceId ? `（traceId: ${body.traceId}）` : ''))
    }
    return Promise.reject(error)
  }
)

/** 统一返回 data 部分，业务层不再感知 Result 外壳 */
export async function http<T = unknown>(config: AxiosRequestConfig): Promise<T> {
  const response = await instance.request(config)
  return (response as unknown as { data: T }).data
}

export const get = <T>(url: string, params?: Record<string, unknown>) =>
  http<T>({ url, method: 'GET', params })
export const post = <T>(url: string, data?: unknown, config?: AxiosRequestConfig) =>
  http<T>({ url, method: 'POST', data, ...config })
export const put = <T>(url: string, data?: unknown) =>
  http<T>({ url, method: 'PUT', data })
export const del = <T>(url: string, params?: Record<string, unknown>) =>
  http<T>({ url, method: 'DELETE', params })

/** Excel / 文件下载：后端返回 blob，自动读取 Content-Disposition 文件名 */
export async function download(url: string, params?: Record<string, unknown>, fallbackName = '导出数据.xlsx') {
  const response = await instance.get(url, { params, responseType: 'blob' })
  const disposition = response.headers?.['content-disposition'] as string | undefined
  let filename = fallbackName
  if (disposition) {
    const match = /filename\*?=(?:UTF-8''|"?)([^";]+)/i.exec(disposition)
    if (match) filename = decodeURIComponent(match[1]).replace(/\.xlsx$/, '') + '.xlsx'
  }
  const blob = new Blob([response.data as BlobPart], { type: 'application/vnd.ms-excel' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = filename
  link.click()
  URL.revokeObjectURL(link.href)
}

export async function upload<T>(url: string, file: File): Promise<T> {
  const form = new FormData()
  form.append('file', file)
  return http<T>({ url, method: 'POST', data: form, headers: { 'Content-Type': 'multipart/form-data' } })
}

export default instance

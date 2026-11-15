import { ref } from 'vue'
import { baseApi } from '@/api/business'
import { systemApi } from '@/api/system'
import type { RemoteResource } from '@/components/crud'

export interface Option { label: string; value: string }

const LABEL_KEY: Record<RemoteResource, string> = {
  categories: 'categoryName', products: 'productName', warehouses: 'warehouseName',
  suppliers: 'supplierName', customers: 'customerName'
}

/** 统一加载基础资料下拉项。雪花 ID 一律以字符串传递，避免 JS Number 精度丢失。 */
export function useOptions() {
  const products = ref<Option[]>([])
  const warehouses = ref<Option[]>([])
  const suppliers = ref<Option[]>([])
  const customers = ref<Option[]>([])
  const categories = ref<Option[]>([])
  const roles = ref<Option[]>([])
  const depts = ref<Option[]>([])

  const buckets: Record<RemoteResource, typeof products> = {
    products, warehouses, suppliers, customers, categories
  }

  async function load(resources: RemoteResource[] = []) {
    await Promise.all(resources.map(async (r) => {
      try {
        const rows = await baseApi.all(r)
        buckets[r].value = rows.map((row) => ({
          label: String(row[LABEL_KEY[r]] ?? row.id), value: String(row.id)
        }))
      } catch { buckets[r].value = [] }
    }))
  }

  async function loadRoles() {
    try {
      const rows = await systemApi.allRoles()
      roles.value = rows.map((r) => ({ label: String(r.roleName || r.roleKey), value: String(r.id) }))
    } catch { roles.value = [] }
  }

  async function loadDepts() {
    try {
      const rows = await systemApi.depts()
      depts.value = rows.map((r) => ({ label: String(r.deptName), value: String(r.id) }))
    } catch { depts.value = [] }
  }

  return { products, warehouses, suppliers, customers, categories, roles, depts, load, loadRoles, loadDepts }
}

export const statusTag = (v: unknown) => (v === 1 || v === '1' ? 'success' : 'info')
export const statusText = (v: unknown) => (v === 1 || v === '1' ? '正常' : '停用')

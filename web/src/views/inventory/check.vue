<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-toolbar">
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width:140px">
          <el-option label="待审核" :value="1" /><el-option label="已审核" :value="2" />
        </el-select>
        <el-button type="primary" @click="reload"><el-icon><Search /></el-icon> 查询</el-button>
        <div class="spacer" />
        <el-button v-if="user.hasPerm('inventory:check:create')" type="primary" @click="openCreate"><el-icon><Plus /></el-icon> 新建盘点单</el-button>
      </div>
      <el-table :data="rows" v-loading="loading" border stripe>
        <el-table-column prop="checkNo" label="盘点单号" width="200" />
        <el-table-column label="仓库" width="170"><template #default="{ row }">{{ warehouseName(row.warehouseId) }}</template></el-table-column>
        <el-table-column prop="checkDate" label="盘点日期" width="140" />
        <el-table-column label="状态" width="110"><template #default="{ row }"><el-tag size="small" :type="row.status === 1 ? 'warning' : 'success'">{{ row.status === 1 ? '待审核' : '已审核' }}</el-tag></template></el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column label="审核时间" width="175"><template #default="{ row }">{{ fmtDate(row.auditTime) }}</template></el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showItems(row)">明细</el-button>
            <el-button v-if="row.status === 1 && user.hasPerm('inventory:check:audit')" link type="warning" @click="audit(row)">审核</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total"
                       layout="total, prev, pager, next" @change="load" />
      </div>
    </div>

    <el-dialog v-model="visible" title="新建盘点单" width="720px" destroy-on-close>
      <el-form :model="form" label-width="100px" inline>
        <el-form-item label="盘点仓库">
          <el-select v-model="form.warehouseId" filterable style="width:200px">
            <el-option v-for="o in warehouses" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="盘点日期"><el-date-picker v-model="form.checkDate" type="date" value-format="YYYY-MM-DD" style="width:200px" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" style="width:220px" /></el-form-item>
      </el-form>
      <div class="page-toolbar mt-12">
        <b>盘点明细</b><div class="spacer" />
        <el-button size="small" @click="addItem"><el-icon><Plus /></el-icon> 添加一行</el-button>
      </div>
      <el-table :data="form.items" border size="small">
        <el-table-column label="商品" min-width="220">
          <template #default="{ row }">
            <el-select v-model="row.productId" filterable class="full-width">
              <el-option v-for="o in products" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="实盘数量" width="160">
          <template #default="{ row }"><el-input-number v-model="row.actualQuantity" :min="0" size="small" /></template>
        </el-table-column>
        <el-table-column label="备注" min-width="160">
          <template #default="{ row }"><el-input v-model="row.remark" size="small" /></template>
        </el-table-column>
        <el-table-column label="操作" width="80"><template #default="{ $index }"><el-button link type="danger" @click="form.items.splice($index, 1)">移除</el-button></template></el-table-column>
      </el-table>
      <template #footer><el-button @click="visible = false">取消</el-button><el-button type="primary" :loading="saving" @click="submit">提交盘点</el-button></template>
    </el-dialog>

    <el-dialog v-model="itemsVisible" title="盘点明细" width="680px">
      <el-table :data="items" border size="small">
        <el-table-column label="商品" min-width="180"><template #default="{ row }">{{ productName(row.productId) }}</template></el-table-column>
        <el-table-column prop="systemQuantity" label="账面数量" width="110" />
        <el-table-column prop="actualQuantity" label="实盘数量" width="110" />
        <el-table-column label="差异" width="100">
          <template #default="{ row }"><span :class="row.diffQuantity < 0 ? 'text-danger' : 'text-success'">{{ row.diffQuantity > 0 ? '+' : '' }}{{ row.diffQuantity }}</span></template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import { inventoryApi } from '@/api/business'
import { useUserStore } from '@/stores/user'
import { useOptions } from '@/composables/useOptions'
import { fmtDate } from '@/utils/format'
import type { PageQuery } from '@/api/request'

const user = useUserStore()
const { products, warehouses, load: loadOptions } = useOptions()
const rows = ref<Record<string, any>[]>([])
const items = ref<Record<string, any>[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const itemsVisible = ref(false)
const query = reactive<PageQuery>({ pageNum: 1, pageSize: 20 })
const form = reactive<Record<string, any>>({ items: [] })

const productName = (id: unknown) => products.value.find((o) => o.value === String(id))?.label || String(id ?? '-')
const warehouseName = (id: unknown) => warehouses.value.find((o) => o.value === String(id))?.label || String(id ?? '-')

async function load() {
  loading.value = true
  try {
    const data = await inventoryApi.checks(query)
    rows.value = data.records || []
    total.value = Number(data.total || 0)
  } finally { loading.value = false }
}
function reload() { query.pageNum = 1; load() }
function openCreate() {
  Object.assign(form, { warehouseId: '', checkDate: dayjs().format('YYYY-MM-DD'), remark: '', items: [] })
  addItem(); visible.value = true
}
function addItem() { form.items.push({ productId: '', actualQuantity: 0, remark: '' }) }
async function submit() {
  if (!form.warehouseId || !form.items.some((i: Record<string, any>) => i.productId)) {
    ElMessage.warning('请选择仓库并至少填写一条盘点明细'); return
  }
  saving.value = true
  try {
    await inventoryApi.createCheck({ ...form })
    ElMessage.success('盘点单已创建'); visible.value = false; load()
  } finally { saving.value = false }
}
async function audit(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认审核盘点单「${row.checkNo}」？审核后将按差异生成出入库流水。`, '提示', { type: 'warning' })
  await inventoryApi.auditCheck(String(row.id))
  ElMessage.success('审核完成'); load()
}
async function showItems(row: Record<string, any>) {
  items.value = await inventoryApi.checkItems(String(row.id))
  itemsVisible.value = true
}
onMounted(async () => { await loadOptions(['products', 'warehouses']); load() })
</script>

<style scoped>
.pager { display: flex; justify-content: flex-end; margin-top: 14px; }
</style>

<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-toolbar">
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width:140px">
          <el-option label="待审核" :value="1" /><el-option label="已审核" :value="2" />
        </el-select>
        <el-button type="primary" @click="reload"><el-icon><Search /></el-icon> 查询</el-button>
        <div class="spacer" />
        <el-button v-if="user.hasPerm('inventory:transfer:create')" type="primary" @click="openCreate"><el-icon><Plus /></el-icon> 新建调拨单</el-button>
      </div>
      <el-table :data="rows" v-loading="loading" border stripe>
        <el-table-column prop="transferNo" label="调拨单号" width="200" />
        <el-table-column label="调出仓库" width="160"><template #default="{ row }">{{ warehouseName(row.fromWarehouseId) }}</template></el-table-column>
        <el-table-column label="调入仓库" width="160"><template #default="{ row }">{{ warehouseName(row.toWarehouseId) }}</template></el-table-column>
        <el-table-column label="商品" min-width="170"><template #default="{ row }">{{ productName(row.productId) }}</template></el-table-column>
        <el-table-column prop="quantity" label="数量" width="110" />
        <el-table-column label="状态" width="110"><template #default="{ row }"><el-tag size="small" :type="row.status === 1 ? 'warning' : 'success'">{{ row.status === 1 ? '待审核' : '已审核' }}</el-tag></template></el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1 && user.hasPerm('inventory:transfer:audit')" link type="warning" @click="audit(row)">审核</el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total"
                       layout="total, prev, pager, next" @change="load" />
      </div>
    </div>

    <el-dialog v-model="visible" title="新建调拨单" width="560px" destroy-on-close>
      <el-alert type="info" :closable="false" class="mb-12" title="审核通过后在同一事务内完成调出扣减与调入增加，并生成两条库存流水。" />
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="调出仓库" prop="fromWarehouseId">
          <el-select v-model="form.fromWarehouseId" filterable class="full-width"><el-option v-for="o in warehouses" :key="o.value" :label="o.label" :value="o.value" /></el-select>
        </el-form-item>
        <el-form-item label="调入仓库" prop="toWarehouseId">
          <el-select v-model="form.toWarehouseId" filterable class="full-width"><el-option v-for="o in warehouses" :key="o.value" :label="o.label" :value="o.value" /></el-select>
        </el-form-item>
        <el-form-item label="商品" prop="productId">
          <el-select v-model="form.productId" filterable class="full-width"><el-option v-for="o in products" :key="o.value" :label="o.label" :value="o.value" /></el-select>
        </el-form-item>
        <el-form-item label="调拨数量" prop="quantity"><el-input-number v-model="form.quantity" :min="1" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="visible = false">取消</el-button><el-button type="primary" :loading="saving" @click="submit">提交</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { inventoryApi } from '@/api/business'
import { useUserStore } from '@/stores/user'
import { useOptions } from '@/composables/useOptions'
import type { PageQuery } from '@/api/request'

const user = useUserStore()
const { products, warehouses, load: loadOptions } = useOptions()
const rows = ref<Record<string, any>[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const formRef = ref<FormInstance>()
const query = reactive<PageQuery>({ pageNum: 1, pageSize: 20 })
const form = reactive<Record<string, any>>({})
const rules = {
  fromWarehouseId: [{ required: true, message: '请选择调出仓库', trigger: 'change' }],
  toWarehouseId: [{ required: true, message: '请选择调入仓库', trigger: 'change' }],
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }]
}
const productName = (id: unknown) => products.value.find((o) => o.value === String(id))?.label || String(id ?? '-')
const warehouseName = (id: unknown) => warehouses.value.find((o) => o.value === String(id))?.label || String(id ?? '-')

async function load() {
  loading.value = true
  try {
    const data = await inventoryApi.transfers(query)
    rows.value = data.records || []
    total.value = Number(data.total || 0)
  } finally { loading.value = false }
}
function reload() { query.pageNum = 1; load() }
function openCreate() {
  Object.assign(form, { fromWarehouseId: '', toWarehouseId: '', productId: '', quantity: 1, remark: '' })
  visible.value = true
}
async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (form.fromWarehouseId === form.toWarehouseId) { ElMessage.warning('调出与调入仓库不能相同'); return }
  saving.value = true
  try {
    await inventoryApi.createTransfer({ ...form })
    ElMessage.success('调拨单已创建'); visible.value = false; load()
  } finally { saving.value = false }
}
async function audit(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认审核调拨单「${row.transferNo}」？`, '提示', { type: 'warning' })
  await inventoryApi.auditTransfer(String(row.id))
  ElMessage.success('审核完成'); load()
}
onMounted(async () => { await loadOptions(['products', 'warehouses']); load() })
</script>

<style scoped>
.pager { display: flex; justify-content: flex-end; margin-top: 14px; }
</style>

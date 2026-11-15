<template>
  <div class="page-container">
    <el-row :gutter="12" class="mb-12">
      <el-col :xs="12" :md="6"><div class="stat-card"><div class="label">库存总量</div><div class="value">{{ fmtNumber(summary.totalQuantity) }}</div></div></el-col>
      <el-col :xs="12" :md="6"><div class="stat-card"><div class="label">低库存品项</div><div class="value text-danger">{{ fmtNumber(summary.lowStockCount) }}</div></div></el-col>
      <el-col :xs="12" :md="6"><div class="stat-card"><div class="label">商品品项</div><div class="value">{{ products.length }}</div></div></el-col>
      <el-col :xs="12" :md="6"><div class="stat-card"><div class="label">仓库数</div><div class="value">{{ warehouses.length }}</div></div></el-col>
    </el-row>

    <div class="page-card">
      <div class="page-toolbar">
        <el-input v-model="query.keyword" placeholder="SKU / 商品名称" clearable style="width:210px" @keyup.enter="reload" />
        <el-select v-model="query.warehouseId" placeholder="全部仓库" clearable filterable style="width:170px">
          <el-option v-for="o in warehouses" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
        <el-select v-model="query.productId" placeholder="全部商品" clearable filterable style="width:190px">
          <el-option v-for="o in products" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
        <el-checkbox v-model="lowOnly" @change="reload">仅看低库存</el-checkbox>
        <el-button type="primary" @click="reload"><el-icon><Search /></el-icon> 查询</el-button>
        <div class="spacer" />
        <el-button v-if="user.hasPerm('inventory:stock:adjust')" type="primary" @click="openAdjust"><el-icon><EditPen /></el-icon> 库存调整</el-button>
        <el-button v-if="user.hasPerm('inventory:stock:export')" @click="inventoryApi.exportStocks(query)"><el-icon><Download /></el-icon> 导出</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" border stripe>
        <el-table-column type="index" label="#" width="55" />
        <el-table-column prop="productCode" label="SKU" width="150" />
        <el-table-column prop="productName" label="商品名称" min-width="170" show-overflow-tooltip />
        <el-table-column prop="warehouseName" label="仓库" width="150" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="quantity" label="现有库存" width="110" />
        <el-table-column prop="lockedQuantity" label="锁定量" width="100" />
        <el-table-column label="可用量" width="110">
          <template #default="{ row }"><b :class="row.availableQuantity <= 0 ? 'text-danger' : ''">{{ row.availableQuantity }}</b></template>
        </el-table-column>
        <el-table-column prop="safetyStock" label="安全库存" width="110" />
        <el-table-column label="预警" width="110">
          <template #default="{ row }"><el-tag v-if="row.lowStockText" size="small" type="danger">低于安全线</el-tag><span v-else>-</span></template>
        </el-table-column>
        <el-table-column label="更新时间" width="175"><template #default="{ row }">{{ fmtDate(row.updateTime) }}</template></el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total"
                       :page-sizes="[10,20,50,100]" layout="total, sizes, prev, pager, next" @change="load" />
      </div>
    </div>

    <el-dialog v-model="visible" title="库存调整" width="520px" destroy-on-close>
      <el-alert type="warning" :closable="false" class="mb-12"
                title="正数入库、负数出库。调整走幂等键 + 分布式锁 + 原子 CAS，并写入库存流水与审计日志。" />
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="商品" prop="productId">
          <el-select v-model="form.productId" filterable class="full-width">
            <el-option v-for="o in products" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="form.warehouseId" filterable class="full-width">
            <el-option v-for="o in warehouses" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="变动数量" prop="quantity"><el-input-number v-model="form.quantity" :min="-999999" :max="999999" :step="1" /><span class="hint">负数为出库</span></el-form-item>
        <el-form-item label="业务类型" prop="bizType">
          <el-select v-model="form.bizType" class="full-width" filterable allow-create default-first-option>
            <el-option v-for="o in flowTypes" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务单号"><el-input v-model="form.bizNo" placeholder="可选，便于追溯" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="visible = false">取消</el-button><el-button type="primary" :loading="saving" @click="submit">确认调整</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { inventoryApi } from '@/api/business'
import { systemApi } from '@/api/system'
import { useUserStore } from '@/stores/user'
import { useOptions } from '@/composables/useOptions'
import { fmtDate, fmtNumber } from '@/utils/format'
import type { PageQuery } from '@/api/request'
import type { StockVO } from '@/api/types'

const user = useUserStore()
const { products, warehouses, load } = useOptions()
const rows = ref<StockVO[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const lowOnly = ref(false)
const summary = reactive<Record<string, any>>({ totalQuantity: 0, lowStockCount: 0 })
const flowTypes = ref<{ label: string; value: string }[]>([])
const formRef = ref<FormInstance>()
const query = reactive<PageQuery>({ pageNum: 1, pageSize: 20, keyword: '' })
const form = reactive<Record<string, any>>({})
const rules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入变动数量', trigger: 'blur' }],
  bizType: [{ required: true, message: '请选择业务类型', trigger: 'change' }]
}

async function loadStocks() {
  loading.value = true
  try {
    query.lowStockOnly = lowOnly.value
    const data = await inventoryApi.stocks(query)
    rows.value = data.records || []
    total.value = Number(data.total || 0)
    Object.assign(summary, await inventoryApi.summary())
  } finally { loading.value = false }
}
function reload() { query.pageNum = 1; loadStocks() }
function openAdjust() {
  Object.keys(form).forEach((k) => delete form[k])
  Object.assign(form, { quantity: 1, bizType: 'MANUAL_ADJUST', bizNo: '', remark: '' })
  visible.value = true
}
async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!form.quantity) { ElMessage.warning('变动数量不能为 0'); return }
  saving.value = true
  try {
    await inventoryApi.adjust({ ...form })
    ElMessage.success('调整成功'); visible.value = false; loadStocks()
  } finally { saving.value = false }
}
onMounted(async () => {
  await load(['products', 'warehouses'])
  try {
    flowTypes.value = (await systemApi.dictByType('biz_stock_flow_type')).map((d) => ({ label: d.label, value: d.value }))
  } catch { flowTypes.value = [] }
  loadStocks()
})
</script>

<style scoped>
.pager { display: flex; justify-content: flex-end; margin-top: 14px; }
.hint { margin-left: 10px; color: #9ca3af; font-size: 12px; }
</style>

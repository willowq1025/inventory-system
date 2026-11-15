<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-toolbar">
        <el-input v-model="query.keyword" placeholder="流水号 / 业务单号" clearable style="width:220px" @keyup.enter="reload" />
        <el-select v-model="query.bizType" placeholder="变动类型" clearable filterable style="width:180px">
          <el-option v-for="o in flowTypes" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
        <el-select v-model="query.warehouseId" placeholder="全部仓库" clearable filterable style="width:170px">
          <el-option v-for="o in warehouses" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
        <el-date-picker v-model="range" type="datetimerange" value-format="YYYY-MM-DD HH:mm:ss"
                        start-placeholder="开始" end-placeholder="结束" style="width:350px" />
        <el-button type="primary" @click="reload"><el-icon><Search /></el-icon> 查询</el-button>
        <div class="spacer" />
        <el-button v-if="user.hasPerm('inventory:flow:export')" @click="inventoryApi.exportFlows(query)"><el-icon><Download /></el-icon> 导出</el-button>
      </div>
      <el-table :data="rows" v-loading="loading" border stripe>
        <el-table-column prop="flowNo" label="流水号" width="190" show-overflow-tooltip />
        <el-table-column label="商品" min-width="170"><template #default="{ row }">{{ productName(row.productId) }}</template></el-table-column>
        <el-table-column label="仓库" width="150"><template #default="{ row }">{{ warehouseName(row.warehouseId) }}</template></el-table-column>
        <el-table-column label="业务类型" width="140"><template #default="{ row }"><el-tag size="small">{{ flowLabel(row.bizType) }}</el-tag></template></el-table-column>
        <el-table-column prop="bizNo" label="业务单号" width="180" show-overflow-tooltip />
        <el-table-column prop="quantityBefore" label="变动前" width="100" />
        <el-table-column label="变动量" width="110">
          <template #default="{ row }"><span :class="row.quantityChange > 0 ? 'text-success' : 'text-danger'">{{ row.quantityChange > 0 ? '+' : '' }}{{ row.quantityChange }}</span></template>
        </el-table-column>
        <el-table-column prop="quantityAfter" label="变动后" width="100" />
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column label="时间" width="175"><template #default="{ row }">{{ fmtDate(row.operTime) }}</template></el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total"
                       :page-sizes="[10,20,50,100]" layout="total, sizes, prev, pager, next" @change="load" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { inventoryApi } from '@/api/business'
import { systemApi } from '@/api/system'
import { useUserStore } from '@/stores/user'
import { useOptions } from '@/composables/useOptions'
import { fmtDate } from '@/utils/format'
import type { PageQuery } from '@/api/request'

const user = useUserStore()
const { products, warehouses, load: loadOptions } = useOptions()
const rows = ref<Record<string, any>[]>([])
const total = ref(0)
const loading = ref(false)
const range = ref<[string, string] | null>(null)
const flowTypes = ref<{ label: string; value: string }[]>([])
const query = reactive<PageQuery>({ pageNum: 1, pageSize: 20, keyword: '' })

watch(range, (v) => { query.startTime = v?.[0]; query.endTime = v?.[1] })
const productName = (id: unknown) => products.value.find((o) => o.value === String(id))?.label || String(id ?? '-')
const warehouseName = (id: unknown) => warehouses.value.find((o) => o.value === String(id))?.label || String(id ?? '-')
const flowLabel = (v: string) => flowTypes.value.find((o) => o.value === v)?.label || v

async function load() {
  loading.value = true
  try {
    const data = await inventoryApi.flows(query)
    rows.value = data.records || []
    total.value = Number(data.total || 0)
  } finally { loading.value = false }
}
function reload() { query.pageNum = 1; load() }
onMounted(async () => {
  await loadOptions(['products', 'warehouses'])
  try { flowTypes.value = (await systemApi.dictByType('biz_stock_flow_type')).map((d) => ({ label: d.label, value: d.value })) } catch { /* 忽略 */ }
  load()
})
</script>

<style scoped>
.pager { display: flex; justify-content: flex-end; margin-top: 14px; }
</style>

<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-toolbar">
        <el-date-picker v-model="range" type="daterange" value-format="YYYY-MM-DD"
                        start-placeholder="起始日期" end-placeholder="结束日期" style="width:280px" />
        <el-select v-model="queryObj.warehouseId" placeholder="全部仓库" clearable filterable style="width:170px">
          <el-option v-for="o in warehouses" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
        <el-select v-model="queryObj.productId" placeholder="全部商品" clearable filterable style="width:190px">
          <el-option v-for="o in products" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
        <el-button type="primary" @click="load"><el-icon><Search /></el-icon> 统计</el-button>
        <div class="spacer" />
        <el-button v-if="user.hasPerm('report:pss:export')" @click="reportApi.exportPss(queryObj)"><el-icon><Download /></el-icon> 导出 Excel</el-button>
      </div>
      <el-alert type="info" :closable="false" class="mb-12"
                title="期初 = 期末现存 - 期间入库 + 期间出库；数据来源于库存流水聚合，按 商品 + 仓库 分组。" />
      <el-table :data="rows" v-loading="loading" border stripe show-summary :summary-method="summaryRow">
        <el-table-column type="index" label="#" width="55" />
        <el-table-column prop="productCode" label="SKU" width="150" />
        <el-table-column prop="productName" label="商品名称" min-width="170" show-overflow-tooltip />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="warehouseName" label="仓库" width="160" />
        <el-table-column prop="openingQty" label="期初" width="120" />
        <el-table-column prop="inQty" label="期间入库" width="120" />
        <el-table-column prop="outQty" label="期间出库" width="120" />
        <el-table-column prop="closingQty" label="期末" width="120" />
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { reportApi } from '@/api/business'
import { useUserStore } from '@/stores/user'
import { useOptions } from '@/composables/useOptions'
import type { PssVO } from '@/api/types'

const user = useUserStore()
const { products, warehouses, load: loadOptions } = useOptions()
const rows = ref<PssVO[]>([])
const loading = ref(false)
const range = ref<[string, string]>([dayjs().subtract(30, 'day').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')])
const queryObj = reactive<Record<string, unknown>>({})

watch(range, (v) => { queryObj.startDate = v?.[0]; queryObj.endDate = v?.[1] }, { immediate: true })

function summaryRow(param: any): (string | number)[] {
  const columns = param.columns as { property: string }[]
  const data = param.data as Record<string, number>[]
  return columns.map((col, index) => {
    if (index === 0) return '合计'
    if (['openingQty', 'inQty', 'outQty', 'closingQty'].includes(col.property)) {
      return data.reduce((s, r) => s + Number(r[col.property] || 0), 0)
    }
    return ''
  })
}
async function load() {
  loading.value = true
  try { rows.value = await reportApi.pss(queryObj) } finally { loading.value = false }
}
onMounted(async () => { await loadOptions(['products', 'warehouses']); load() })
</script>

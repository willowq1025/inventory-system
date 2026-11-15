<template>
  <div class="page-container">
    <el-row :gutter="14">
      <el-col v-for="card in cards" :key="card.label" :xs="12" :sm="8" :md="6" :lg="4">
        <div class="stat-card mb-12">
          <div class="label">{{ card.label }}</div>
          <div class="value" :class="card.accent">{{ card.value }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="14">
      <el-col :xs="24" :lg="15">
        <div class="page-card mb-12">
          <div class="page-toolbar">
            <b>近 7 日进销趋势</b>
            <div class="spacer" />
            <el-button-group size="small">
              <el-button v-for="d in [7, 14, 30]" :key="d" :type="days === d ? 'primary' : ''"
                         @click="days = d; load()">{{ d }}天</el-button>
            </el-button-group>
          </div>
          <div ref="trendEl" style="height: 300px" />
        </div>
      </el-col>
      <el-col :xs="24" :lg="9">
        <div class="page-card mb-12">
          <b>仓库库存分布</b>
          <div ref="pieEl" style="height: 300px" />
        </div>
      </el-col>
    </el-row>

    <div class="page-card">
      <b>快捷入口</b>
      <div class="flex gap-12 mt-12" style="flex-wrap:wrap">
        <el-button v-for="s in shortcuts" :key="s.path" @click="$router.push(s.path)">{{ s.label }}</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { echarts, type EChartsInstance } from '@/utils/echarts'
import { ElMessage } from 'element-plus'
import { reportApi } from '@/api/business'
import { fmtMoney, fmtNumber } from '@/utils/format'
import type { DashboardVO } from '@/api/types'

const days = ref(7)
const data = ref<DashboardVO | null>(null)
const trendEl = ref<HTMLElement>()
const pieEl = ref<HTMLElement>()
let trendChart: EChartsInstance | null = null
let pieChart: EChartsInstance | null = null

const cards = computed(() => {
  const d = data.value
  return [
    { label: '今日采购额', value: fmtMoney(d?.todayPurchase), accent: '' },
    { label: '今日销售额', value: fmtMoney(d?.todaySale), accent: 'text-danger' },
    { label: '本月采购', value: fmtMoney(d?.monthPurchase), accent: '' },
    { label: '本月销售', value: fmtMoney(d?.monthSale), accent: 'text-danger' },
    { label: '库存总量', value: fmtNumber(d?.stockTotal), accent: '' },
    { label: '低库存品项', value: fmtNumber(d?.lowStockCount), accent: 'text-danger' }
  ]
})

const shortcuts = [
  { label: '库存查询', path: '/inventory/stock' },
  { label: '采购下单', path: '/purchase/order' },
  { label: '销售下单', path: '/sale/order' },
  { label: '进销存报表', path: '/report/pss' },
  { label: '审计日志', path: '/system/log' }
]

function renderTrend() {
  if (!trendEl.value || !trendChart) return
  const trend = data.value?.trend || []
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['采购额', '销售额'] },
    grid: { left: 60, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: trend.map((t) => t.date) },
    yAxis: { type: 'value', name: '金额(元)' },
    series: [
      { name: '采购额', type: 'line', smooth: true, data: trend.map((t) => t.purchaseAmount), itemStyle: { color: '#2563eb' }, areaStyle: { opacity: .08 } },
      { name: '销售额', type: 'line', smooth: true, data: trend.map((t) => t.saleAmount), itemStyle: { color: '#dc2626' }, areaStyle: { opacity: .08 } }
    ]
  })
}

function renderPie() {
  if (!pieEl.value || !pieChart) return
  const rows = data.value?.warehouseDistribution || []
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie', radius: ['42%', '68%'],
      data: rows.map((r) => ({ name: String(r.name || '未知'), value: Number(r.total_quantity || 0) })),
      emphasis: { itemStyle: { shadowBlur: 10 } }
    }]
  })
}

async function load() {
  try {
    data.value = await reportApi.dashboard(days.value)
    await nextTick()
    renderTrend()
    renderPie()
  } catch {
    ElMessage.warning('统计接口暂不可用，请确认后端已启动')
  }
}

onMounted(() => {
  if (trendEl.value) trendChart = echarts.init(trendEl.value)
  if (pieEl.value) pieChart = echarts.init(pieEl.value)
  load()
  window.addEventListener('resize', () => { trendChart?.resize(); pieChart?.resize() })
})
</script>

<template>
  <div class="page-container">
    <div class="page-toolbar">
      <b>经营看板</b><div class="spacer" />
      <el-button-group size="small">
        <el-button v-for="d in [7, 14, 30]" :key="d" :type="days === d ? 'primary' : ''" @click="days = d; load()">{{ d }} 天</el-button>
      </el-button-group>
      <el-button size="small" @click="load"><el-icon><Refresh /></el-icon> 刷新</el-button>
    </div>

    <el-row :gutter="12" class="mb-12">
      <el-col v-for="c in cards" :key="c.label" :xs="12" :sm="8" :md="6" :lg="3">
        <div class="stat-card mb-12"><div class="label">{{ c.label }}</div><div class="value" :class="c.accent">{{ c.value }}</div></div>
      </el-col>
    </el-row>

    <el-row :gutter="12">
      <el-col :xs="24" :lg="16"><div class="page-card mb-12"><b>进销趋势</b><div ref="trendEl" style="height:320px" /></div></el-col>
      <el-col :xs="24" :lg="8"><div class="page-card mb-12"><b>仓库库存分布</b><div ref="pieEl" style="height:320px" /></div></el-col>
    </el-row>

    <div class="page-card">
      <b>经营指标说明</b>
      <el-descriptions :column="3" border size="small" class="mt-12">
        <el-descriptions-item label="数据口径">已审核 / 已出入库订单计入金额统计</el-descriptions-item>
        <el-descriptions-item label="刷新时机">每日 00:10 由定时任务预计算日报</el-descriptions-item>
        <el-descriptions-item label="缓存策略">热点统计读 Redis，日切后失效重建</el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
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
    { label: '今日采购', value: fmtMoney(d?.todayPurchase), accent: '' },
    { label: '今日销售', value: fmtMoney(d?.todaySale), accent: 'text-danger' },
    { label: '本月采购', value: fmtMoney(d?.monthPurchase), accent: '' },
    { label: '本月销售', value: fmtMoney(d?.monthSale), accent: 'text-danger' },
    { label: '今日采购单', value: fmtNumber(d?.todayPurchaseCount), accent: '' },
    { label: '今日销售单', value: fmtNumber(d?.todaySaleCount), accent: '' },
    { label: '待审采购单', value: fmtNumber(d?.pendingPurchase), accent: 'text-danger' },
    { label: '低库存品项', value: fmtNumber(d?.lowStockCount), accent: 'text-danger' }
  ]
})

function renderTrend() {
  if (!trendChart) return
  const trend = data.value?.trend || []
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['采购额', '销售额'] },
    grid: { left: 70, right: 24, top: 44, bottom: 36 },
    xAxis: { type: 'category', data: trend.map((t) => t.date) },
    yAxis: { type: 'value', name: '金额(元)' },
    series: [
      { name: '采购额', type: 'bar', data: trend.map((t) => t.purchaseAmount), itemStyle: { color: '#2563eb' } },
      { name: '销售额', type: 'line', smooth: true, data: trend.map((t) => t.saleAmount), itemStyle: { color: '#dc2626' }, areaStyle: { opacity: .1 } }
    ]
  }, true)
}
function renderPie() {
  if (!pieChart) return
  const rows = data.value?.warehouseDistribution || []
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0 },
    series: [{ type: 'pie', radius: ['40%', '68%'],
      data: rows.map((r) => ({ name: String(r.name || '未知'), value: Number(r.total_quantity || 0) })) }]
  }, true)
}
async function load() {
  try { data.value = await reportApi.dashboard(days.value); renderTrend(); renderPie() }
  catch { ElMessage.warning('统计接口暂不可用，请确认后端已启动') }
}
onMounted(() => {
  if (trendEl.value) trendChart = echarts.init(trendEl.value)
  if (pieEl.value) pieChart = echarts.init(pieEl.value)
  load()
  window.addEventListener('resize', () => { trendChart?.resize(); pieChart?.resize() })
})
</script>

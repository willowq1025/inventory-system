<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-toolbar">
        <el-input v-model="query.keyword" placeholder="标题 / 操作人 / URL" clearable style="width:220px" @keyup.enter="reload" />
        <el-select v-model="query.businessType" placeholder="业务类型" clearable style="width:140px">
          <el-option v-for="t in ['INSERT','UPDATE','DELETE','AUDIT','EXPORT','IMPORT','LOGIN','LOGOUT','SELECT']" :key="t" :label="t" :value="t" />
        </el-select>
        <el-select v-model="query.status" placeholder="结果" clearable style="width:110px">
          <el-option label="成功" :value="1" /><el-option label="失败" :value="0" />
        </el-select>
        <el-date-picker v-model="range" type="datetimerange" value-format="YYYY-MM-DD HH:mm:ss"
                        start-placeholder="开始时间" end-placeholder="结束时间" style="width:360px" />
        <el-button type="primary" @click="reload"><el-icon><Search /></el-icon> 查询</el-button>
        <div class="spacer" />
        <el-button v-if="user.hasPerm('system:log:export')" @click="systemApi.exportLogs(query)"><el-icon><Download /></el-icon> 导出</el-button>
      </div>
      <el-table :data="rows" v-loading="loading" border stripe>
        <el-table-column prop="title" label="操作标题" width="150" show-overflow-tooltip />
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="businessType" label="类型" width="100" />
        <el-table-column prop="operName" label="操作人" width="110" />
        <el-table-column prop="operIp" label="IP" width="130" />
        <el-table-column prop="requestMethod" label="方法" width="80" />
        <el-table-column prop="requestUrl" label="请求地址" min-width="200" show-overflow-tooltip />
        <el-table-column label="结果" width="90">
          <template #default="{ row }"><el-tag size="small" :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '成功' : '失败' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="costMs" label="耗时(ms)" width="100" />
        <el-table-column label="时间" width="175"><template #default="{ row }">{{ fmtDate(row.operTime) }}</template></el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }"><el-button link type="primary" @click="detail(row)">详情</el-button></template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total"
                       :page-sizes="[10,20,50,100]" layout="total, sizes, prev, pager, next" @change="load" />
      </div>
    </div>

    <el-drawer v-model="drawer" title="审计日志详情" size="560px">
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="traceId">{{ current.traceId }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ current.operName }}（{{ current.operId }}）</el-descriptions-item>
        <el-descriptions-item label="部门">{{ current.operDept }}</el-descriptions-item>
        <el-descriptions-item label="IP / UA">{{ current.operIp }} / {{ current.userAgent }}</el-descriptions-item>
        <el-descriptions-item label="方法签名">{{ current.method }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ current.costMs }} ms</el-descriptions-item>
        <el-descriptions-item label="错误信息">{{ current.errorMsg || '-' }}</el-descriptions-item>
      </el-descriptions>
      <h4 class="mt-12">请求参数</h4><pre class="code">{{ pretty(current.requestParam) }}</pre>
      <h4 class="mt-12">响应结果</h4><pre class="code">{{ pretty(current.responseResult) }}</pre>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { systemApi } from '@/api/system'
import { useUserStore } from '@/stores/user'
import { fmtDate } from '@/utils/format'
import type { PageQuery } from '@/api/request'

const user = useUserStore()
const rows = ref<Record<string, any>[]>([])
const total = ref(0)
const loading = ref(false)
const drawer = ref(false)
const range = ref<[string, string] | null>(null)
const current = ref<Record<string, any>>({})
const query = reactive<PageQuery>({ pageNum: 1, pageSize: 20, keyword: '' })

watch(range, (v) => {
  query.startTime = v?.[0]
  query.endTime = v?.[1]
})
function pretty(v?: string) {
  if (!v) return '-'
  try { return JSON.stringify(JSON.parse(v), null, 2) } catch { return v }
}
async function load() {
  loading.value = true
  try {
    const data = await systemApi.operLogs(query)
    rows.value = data.records || []
    total.value = Number(data.total || 0)
  } finally { loading.value = false }
}
function reload() { query.pageNum = 1; load() }
function detail(row: Record<string, any>) { current.value = row; drawer.value = true }
onMounted(load)
</script>

<style scoped>
.pager { display: flex; justify-content: flex-end; margin-top: 14px; }
.code { background: #f6f8fa; border: 1px solid #e5e7eb; border-radius: 6px; padding: 12px; max-height: 240px; overflow: auto; font-size: 12px; white-space: pre-wrap; word-break: break-all; }
</style>

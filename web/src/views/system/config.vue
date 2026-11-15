<template>
  <div class="page-container">
    <div class="page-card mb-12">
      <div class="page-toolbar"><b>系统参数</b><span class="sub">变更后即时生效，写操作全部计入审计日志</span></div>
      <el-table :data="configs" v-loading="loadingConfig" border>
        <el-table-column prop="configName" label="参数名称" width="220" />
        <el-table-column prop="configKey" label="参数键" width="240" />
        <el-table-column label="参数值">
          <template #default="{ row }"><el-input v-model="row.configValue" size="small" /></template>
        </el-table-column>
        <el-table-column label="类型" width="100"><template #default="{ row }">{{ row.configType === 1 ? '系统内置' : '自定义' }}</template></el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }"><el-button link type="primary" @click="save(row)">保存</el-button></template>
        </el-table-column>
      </el-table>
    </div>

    <div class="page-card">
      <div class="page-toolbar"><b>定时任务</b><span class="sub">集群环境下由 Redis 抢占 leader 锁，保证单实例执行</span></div>
      <el-table :data="jobRows" v-loading="loadingJob" border>
        <el-table-column prop="name" label="任务标识" width="180" />
        <el-table-column prop="desc" label="说明" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :loading="running === row.name" @click="run(row.name)">手动执行</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { systemApi } from '@/api/system'

const configs = ref<Record<string, any>[]>([])
const jobs = ref<Record<string, string>>({})
const loadingConfig = ref(false)
const loadingJob = ref(false)
const running = ref('')
const jobRows = computed(() => Object.entries(jobs.value).map(([name, desc]) => ({ name, desc })))

async function loadConfigs() {
  loadingConfig.value = true
  try { configs.value = await systemApi.configs() } finally { loadingConfig.value = false }
}
async function loadJobs() {
  loadingJob.value = true
  try { jobs.value = await systemApi.jobs() } finally { loadingJob.value = false }
}
async function save(row: Record<string, any>) {
  await systemApi.updateConfig(String(row.id), String(row.configValue))
  ElMessage.success('参数已保存'); loadConfigs()
}
async function run(name: string) {
  running.value = name
  try {
    const ok = await systemApi.runJob(name)
    if (ok) ElMessage.success('任务已触发')
    else ElMessage.warning('当前实例未抢到 leader 锁，跳过执行')
  } finally { running.value = '' }
}
onMounted(() => { loadConfigs(); loadJobs() })
</script>

<style scoped>
.sub { color: #9ca3af; font-size: 12px; margin-left: 12px; }
</style>

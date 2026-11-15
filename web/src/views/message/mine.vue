<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-toolbar">
        <b>我的消息</b>
        <div class="spacer" />
        <el-button @click="readAll"><el-icon><Check /></el-icon> 全部标为已读</el-button>
      </div>
      <el-empty v-if="!rows.length" description="暂无消息" />
      <el-timeline v-else>
        <el-timeline-item v-for="n in rows" :key="String(n.id)" :timestamp="fmtDate(n.createTime)" placement="top"
                          :type="n.read ? 'info' : 'primary'">
          <el-card shadow="never">
            <div class="msg-title">
              <el-tag v-if="!n.read" size="small" type="danger">未读</el-tag>
              <b>{{ n.title }}</b>
              <el-button v-if="!n.read" link type="primary" @click="read(n)">标为已读</el-button>
            </div>
            <p class="msg-content">{{ n.content }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { systemApi } from '@/api/system'
import { fmtDate } from '@/utils/format'

const rows = ref<Record<string, any>[]>([])
async function load() { rows.value = await systemApi.mine() }
async function read(n: Record<string, any>) {
  await systemApi.markRead(String(n.id)); n.read = true; ElMessage.success('已标记为已读')
}
async function readAll() {
  await systemApi.markAllRead()
  rows.value.forEach((n) => { n.read = true })
  ElMessage.success('已全部标为已读')
}
onMounted(load)
</script>

<style scoped>
.msg-title { display: flex; align-items: center; gap: 10px; }
.msg-content { color: #4b5563; line-height: 1.7; margin: 10px 0 0; white-space: pre-wrap; }
</style>

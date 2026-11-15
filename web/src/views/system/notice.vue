<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-toolbar">
        <el-input v-model="query.keyword" placeholder="标题关键字" clearable style="width:220px" @keyup.enter="reload" />
        <el-button type="primary" @click="reload"><el-icon><Search /></el-icon> 查询</el-button>
        <div class="spacer" />
        <el-button v-if="user.hasPerm('system:notice:create')" type="primary" @click="openPublish"><el-icon><Plus /></el-icon> 发布通知</el-button>
      </div>
      <el-table :data="rows" v-loading="loading" border stripe>
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="类型" width="110"><template #default="{ row }">{{ row.noticeType === 1 ? '通知' : '公告' }}</template></el-table-column>
        <el-table-column label="级别" width="100"><template #default="{ row }"><el-tag size="small" :type="row.level === 3 ? 'danger' : row.level === 2 ? 'warning' : 'info'">{{ ['普通','重要','紧急'][(row.level || 1) - 1] }}</el-tag></template></el-table-column>
        <el-table-column label="范围" width="110"><template #default="{ row }">{{ row.targetType === 1 ? '全体' : '指定对象' }}</template></el-table-column>
        <el-table-column label="发布时间" width="175"><template #default="{ row }">{{ fmtDate(row.createTime) }}</template></el-table-column>
        <el-table-column label="内容" min-width="220" show-overflow-tooltip><template #default="{ row }">{{ row.content }}</template></el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total"
                       layout="total, prev, pager, next" @change="load" />
      </div>
    </div>

    <el-dialog v-model="visible" title="发布通知" width="580px" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="类型"><el-radio-group v-model="form.noticeType"><el-radio :value="1">通知</el-radio><el-radio :value="2">公告</el-radio></el-radio-group></el-form-item>
        <el-form-item label="级别"><el-radio-group v-model="form.level"><el-radio :value="1">普通</el-radio><el-radio :value="2">重要</el-radio><el-radio :value="3">紧急</el-radio></el-radio-group></el-form-item>
        <el-form-item label="发送范围"><el-radio-group v-model="form.targetType"><el-radio :value="1">全体用户</el-radio><el-radio :value="2">指定用户</el-radio></el-radio-group></el-form-item>
        <el-form-item v-if="form.targetType === 2" label="接收人ID"><el-input v-model="targetText" placeholder="多个 ID 用英文逗号分隔" /></el-form-item>
        <el-form-item label="发送渠道">
          <el-checkbox-group v-model="form.channels">
            <el-checkbox v-for="c in ['IN_APP','EMAIL','WEBHOOK']" :key="c" :value="c" :label="c">{{ channelText(c) }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="visible = false">取消</el-button><el-button type="primary" :loading="saving" @click="publish">发布</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { systemApi } from '@/api/system'
import { useUserStore } from '@/stores/user'
import { fmtDate } from '@/utils/format'
import type { PageQuery } from '@/api/request'

const user = useUserStore()
const channelText = (c: string) => ({ IN_APP: '站内信', EMAIL: '邮件', WEBHOOK: 'Webhook' } as Record<string, string>)[c] || c
const rows = ref<Record<string, any>[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const targetText = ref('')
const query = reactive<PageQuery>({ pageNum: 1, pageSize: 20, keyword: '' })
const form = reactive<Record<string, any>>({})

async function load() {
  loading.value = true
  try {
    const data = await systemApi.notices(query)
    rows.value = data.records || []
    total.value = Number(data.total || 0)
  } finally { loading.value = false }
}
function reload() { query.pageNum = 1; load() }
function openPublish() {
  Object.assign(form, { title: '', content: '', noticeType: 1, level: 1, targetType: 1, channels: ['IN_APP'] })
  targetText.value = ''
  visible.value = true
}
async function publish() {
  saving.value = true
  try {
    const payload: Record<string, unknown> = { ...form }
    if (form.targetType === 2) {
      payload.targetUserIds = targetText.value.split(',').map((s) => s.trim()).filter(Boolean)
    }
    await systemApi.publish(payload)
    ElMessage.success('通知已发布'); visible.value = false; load()
  } finally { saving.value = false }
}
onMounted(load)
</script>

<style scoped>
.pager { display: flex; justify-content: flex-end; margin-top: 14px; }
</style>

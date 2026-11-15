<template>
  <div class="page-container">
    <el-row :gutter="14">
      <el-col :xs="24" :md="8">
        <div class="page-card">
          <div class="page-toolbar">
            <b>字典类型</b><div class="spacer" />
            <el-button v-if="can" size="small" type="primary" @click="openType">新增</el-button>
          </div>
          <el-table :data="types" v-loading="loadingType" highlight-current-row border size="small"
                    @current-change="onTypeChange">
            <el-table-column prop="dictName" label="名称" />
            <el-table-column prop="dictType" label="标识" show-overflow-tooltip />
          </el-table>
        </div>
      </el-col>
      <el-col :xs="24" :md="16">
        <div class="page-card">
          <div class="page-toolbar">
            <b>{{ currentType ? `${currentType.dictName}（${currentType.dictType}）` : '字典项' }}</b>
            <div class="spacer" />
            <el-button v-if="can && currentType" size="small" type="primary" @click="openData()">新增字典项</el-button>
          </div>
          <el-alert type="info" :closable="false" class="mb-12"
                    title="字典读操作命中 Redis 缓存；变更在事务提交后失效缓存，避免脏读。" />
          <el-table :data="items" v-loading="loadingItem" border size="small">
            <el-table-column prop="label" label="标签" />
            <el-table-column prop="value" label="值" />
            <el-table-column prop="listClass" label="样式" width="110" />
            <el-table-column prop="cssClass" label="CSS 类" width="130" />
            <el-table-column label="操作" width="140">
              <template #default="{ row }">
                <el-button link type="primary" @click="openData(row)">编辑</el-button>
                <el-button link type="danger" @click="removeData(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>

    <el-dialog v-model="typeVisible" title="新增字典类型" width="440px">
      <el-form :model="typeForm" label-width="90px">
        <el-form-item label="名称"><el-input v-model="typeForm.dictName" /></el-form-item>
        <el-form-item label="标识"><el-input v-model="typeForm.dictType" placeholder="如 order_status" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="typeForm.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="typeVisible = false">取消</el-button><el-button type="primary" @click="saveType">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="dataVisible" title="字典项" width="480px">
      <el-form :model="dataForm" label-width="90px">
        <el-form-item label="标签"><el-input v-model="dataForm.dictLabel" /></el-form-item>
        <el-form-item label="值"><el-input v-model="dataForm.dictValue" /></el-form-item>
        <el-form-item label="样式"><el-input v-model="dataForm.listClass" placeholder="primary / success / danger" /></el-form-item>
        <el-form-item label="CSS 类"><el-input v-model="dataForm.cssClass" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="dataForm.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dataVisible = false">取消</el-button><el-button type="primary" @click="saveData">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { systemApi } from '@/api/system'
import { useUserStore } from '@/stores/user'
import type { DictVO } from '@/api/types'

const user = useUserStore()
const can = user.hasPerm('system:dict:update')
const types = ref<Record<string, any>[]>([])
const items = ref<DictVO[]>([])
const currentType = ref<Record<string, any> | null>(null)
const loadingType = ref(false)
const loadingItem = ref(false)
const typeVisible = ref(false)
const dataVisible = ref(false)
const typeForm = reactive<Record<string, any>>({ status: 1 })
const dataForm = reactive<Record<string, any>>({ sort: 0 })

async function loadTypes() {
  loadingType.value = true
  try {
    types.value = await systemApi.dictTypes()
    if (types.value.length && !currentType.value) onTypeChange(types.value[0])
  } finally { loadingType.value = false }
}
async function onTypeChange(row: Record<string, any> | null) {
  if (!row) return
  currentType.value = row
  loadingItem.value = true
  try { items.value = await systemApi.dictByType(String(row.dictType)) } finally { loadingItem.value = false }
}
function openType() {
  Object.assign(typeForm, { dictName: '', dictType: '', status: 1 })
  typeVisible.value = true
}
async function saveType() {
  await systemApi.saveDictType({ ...typeForm })
  ElMessage.success('已保存'); typeVisible.value = false; currentType.value = null; loadTypes()
}
function openData(row?: DictVO) {
  Object.keys(dataForm).forEach((k) => delete dataForm[k])
  Object.assign(dataForm, { sort: 0, status: 1, dictType: currentType.value?.dictType })
  if (row) Object.assign(dataForm, {
    id: row.id, dictType: currentType.value?.dictType,
    dictLabel: row.label, dictValue: row.value, listClass: row.listClass, cssClass: row.cssClass
  })
  dataVisible.value = true
}
async function saveData() {
  await systemApi.saveDictData({ ...dataForm })
  ElMessage.success('已保存，缓存已失效'); dataVisible.value = false
  if (currentType.value) onTypeChange(currentType.value)
}
async function removeData(row: DictVO) {
  await ElMessageBox.confirm(`确认删除字典项「${row.label}」？`, '提示', { type: 'warning' })
  await systemApi.deleteDictData(String(row.id))
  ElMessage.success('删除成功')
  if (currentType.value) onTypeChange(currentType.value)
}
onMounted(loadTypes)
</script>

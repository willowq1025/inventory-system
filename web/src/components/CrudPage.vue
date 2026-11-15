<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-toolbar">
        <el-input v-model="query.keyword" :placeholder="`搜索${config.title}名称 / 编码`" clearable
                  style="width: 240px" @keyup.enter="reload" />
        <el-button type="primary" @click="reload"><el-icon><Search /></el-icon> 查询</el-button>
        <div class="spacer" />
        <el-button v-if="can('create')" type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon> 新增{{ config.title }}
        </el-button>
        <el-button v-if="config.exportable && can('export')" @click="doExport">
          <el-icon><Download /></el-icon> 导出
        </el-button>
        <el-upload v-if="config.importable && can('import')" :show-file-list="false"
                   accept=".xlsx,.xls" :http-request="doImport" style="display:inline-block;margin-left:10px">
          <el-button><el-icon><Upload /></el-icon> 导入</el-button>
        </el-upload>
      </div>

      <el-table :data="rows" v-loading="loading" border stripe size="default">
        <el-table-column type="index" label="#" width="55" />
        <el-table-column v-for="col in config.columns" :key="col.prop" :prop="col.prop"
                         :label="col.label" :width="col.width" min-width="110" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag v-if="col.type === 'status'" :type="row[col.prop] === 1 ? 'success' : 'info'" size="small">
              {{ row[col.prop] === 1 ? '正常' : '停用' }}
            </el-tag>
            <span v-else-if="col.type === 'money'">{{ fmtMoney(row[col.prop]) }}</span>
            <span v-else-if="col.type === 'date'">{{ fmtDate(row[col.prop]) }}</span>
            <span v-else>{{ row[col.prop] ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="can('update')" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="can('delete')" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="display:flex;justify-content:flex-end;margin-top:14px">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                       :total="total" :page-sizes="[10, 20, 50, 100]"
                       layout="total, sizes, prev, pager, next, jumper" @change="load" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="`${editing ? '编辑' : '新增'}${config.title}`" width="560px"
               destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item v-for="field in config.fields" :key="field.prop" :label="field.label" :prop="field.prop">
          <el-input-number v-if="field.type === 'number'" v-model="form[field.prop]" :min="0"
                           controls-position="right" style="width:100%" />
          <el-select v-else-if="field.type === 'select'" v-model="form[field.prop]" style="width:100%" clearable filterable>
            <el-option v-for="opt in fieldOptions(field)" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
          <el-input v-else-if="field.type === 'textarea'" v-model="form[field.prop]" type="textarea" :rows="3" />
          <el-input v-else v-model="form[field.prop]" :placeholder="field.hint || `请输入${field.label}`" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resultVisible" title="导入结果" width="560px">
      <el-alert :title="`共 ${importResult.total} 行，成功 ${importResult.successCount} 行，失败 ${importResult.failCount} 行`"
                :type="importResult.failCount ? 'warning' : 'success'" :closable="false" />
      <el-table v-if="importResult.errors.length" :data="importResult.errors" size="small" max-height="260" class="mt-12">
        <el-table-column prop="rowIndex" label="行号" width="90" />
        <el-table-column prop="message" label="原因" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type UploadRequestOptions } from 'element-plus'
import { baseApi } from '@/api/business'
import { useUserStore } from '@/stores/user'
import { fmtDate, fmtMoney } from '@/utils/format'
import type { PageQuery } from '@/api/request'
import type { ImportResult } from '@/api/types'
import type { CrudConfig, CrudField, RemoteResource } from './crud'

const props = defineProps<{ config: CrudConfig }>()
const user = useUserStore()
const can = (action: string) => user.hasPerm(`${props.config.perm}:${action}`)

const rows = ref<Record<string, any>[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const resultVisible = ref(false)
const editing = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<Record<string, any>>({})
const query = reactive<PageQuery>({ pageNum: 1, pageSize: 20, keyword: '' })
const importResult = reactive<ImportResult>({ total: 0, successCount: 0, failCount: 0, errors: [] })

const remoteOptions = reactive<Record<string, { label: string; value: string | number }[]>>({})
const LABEL_KEY: Record<RemoteResource, string> = {
  categories: 'categoryName', products: 'productName', warehouses: 'warehouseName',
  suppliers: 'supplierName', customers: 'customerName'
}
function fieldOptions(field: CrudField) {
  return field.remote ? (remoteOptions[field.remote] || []) : (field.options || [])
}

const rules = computed(() => {
  const r: Record<string, any> = {}
  props.config.fields.forEach((f) => {
    if (f.required) r[f.prop] = [{ required: true, message: `请填写${f.label}`, trigger: 'blur' }]
  })
  return r
})

async function load() {
  loading.value = true
  try {
    const data = await baseApi.list(props.config.resource, query)
    rows.value = data.records || []
    total.value = Number(data.total || 0)
  } finally {
    loading.value = false
  }
}
function reload() { query.pageNum = 1; load() }

function openCreate() {
  editing.value = false
  Object.keys(form).forEach((k) => delete form[k])
  props.config.fields.forEach((f) => { form[f.prop] = f.type === 'number' ? 0 : '' })
  form.status = 1
  dialogVisible.value = true
}
function openEdit(row: Record<string, any>) {
  editing.value = true
  Object.keys(form).forEach((k) => delete form[k])
  Object.assign(form, row)
  dialogVisible.value = true
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editing.value) await baseApi.update(props.config.resource, String(form.id), { ...form })
    else await baseApi.create(props.config.resource, { ...form })
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch (e) {
    if ((e as Error).message !== 'invalid') ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function remove(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除「${row.productName || row.categoryName || row.warehouseName || row.supplierName || row.customerName || row.id}」？`, '提示', { type: 'warning' })
  await baseApi.remove(props.config.resource, String(row.id))
  ElMessage.success('删除成功')
  load()
}

async function doExport() {
  await baseApi.export(props.config.resource, query, `${props.config.title}列表`)
}
async function doImport(options: UploadRequestOptions) {
  const result = await baseApi.import(props.config.resource, options.file as File)
  Object.assign(importResult, result)
  resultVisible.value = true
  load()
}

async function loadRemote() {
  const resources = Array.from(new Set(props.config.fields.map((f) => f.remote).filter(Boolean))) as RemoteResource[]
  await Promise.all(resources.map(async (r) => {
    try {
      const list = await baseApi.all(r)
      remoteOptions[r] = list.map((row) => ({
        label: String(row[LABEL_KEY[r]] ?? row.id),
        value: String(row.id)
      }))
    } catch { remoteOptions[r] = [] }
  }))
}

onMounted(() => { load(); loadRemote() })
</script>

<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-toolbar">
        <el-input v-model="query.keyword" placeholder="账号 / 姓名 / 手机号" clearable style="width:220px" @keyup.enter="reload" />
        <el-select v-model="query.status" placeholder="状态" clearable style="width:120px">
          <el-option label="正常" :value="1" /><el-option label="停用" :value="0" />
        </el-select>
        <el-button type="primary" @click="reload"><el-icon><Search /></el-icon> 查询</el-button>
        <div class="spacer" />
        <el-button v-if="can('create')" type="primary" @click="openCreate"><el-icon><Plus /></el-icon> 新增用户</el-button>
        <el-button v-if="can('export')" @click="systemApi.exportUsers(query)"><el-icon><Download /></el-icon> 导出</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" border stripe>
        <el-table-column type="index" label="#" width="55" />
        <el-table-column prop="username" label="账号" width="140" />
        <el-table-column prop="nickname" label="姓名" width="130" />
        <el-table-column prop="deptName" label="部门" width="140" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="170" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }"><el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="最后登录" width="175"><template #default="{ row }">{{ fmtDate(row.lastLoginTime) }}</template></el-table-column>
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <el-button v-if="can('update')" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="can('update')" link type="warning" @click="resetPwd(row)">重置密码</el-button>
            <el-button v-if="can('delete')" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total"
                       :page-sizes="[10,20,50,100]" layout="total, sizes, prev, pager, next, jumper" @change="load" />
      </div>
    </div>

    <el-dialog v-model="visible" :title="editing ? '编辑用户' : '新增用户'" width="620px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="账号" prop="username"><el-input v-model="form.username" :disabled="editing" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="姓名" prop="nickname"><el-input v-model="form.nickname" /></el-form-item></el-col>
          <el-col v-if="!editing" :span="12"><el-form-item label="初始密码" prop="password"><el-input v-model="form.password" type="password" show-password /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="所属部门">
            <el-select v-model="form.deptId" clearable filterable class="full-width">
              <el-option v-for="o in depts" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态">
            <el-radio-group v-model="form.status"><el-radio :value="1">正常</el-radio><el-radio :value="0">停用</el-radio></el-radio-group>
          </el-form-item></el-col>
          <el-col :span="24"><el-form-item label="角色">
            <el-select v-model="form.roleIds" multiple filterable class="full-width">
              <el-option v-for="o in roles" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { systemApi } from '@/api/system'
import { useUserStore } from '@/stores/user'
import { useOptions, statusTag, statusText } from '@/composables/useOptions'
import { fmtDate } from '@/utils/format'
import type { PageQuery } from '@/api/request'

const user = useUserStore()
const can = (a: string) => user.hasPerm(`system:user:${a}`)
const { roles, depts, loadRoles, loadDepts } = useOptions()
const rows = ref<Record<string, any>[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const editing = ref(false)
const formRef = ref<FormInstance>()
const query = reactive<PageQuery>({ pageNum: 1, pageSize: 20, keyword: '' })
const form = reactive<Record<string, any>>({})
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const data = await systemApi.users(query)
    rows.value = data.records || []
    total.value = Number(data.total || 0)
  } finally { loading.value = false }
}
function reload() { query.pageNum = 1; load() }

function resetForm() {
  Object.keys(form).forEach((k) => delete form[k])
  Object.assign(form, { status: 1, sex: 0, roleIds: [] })
}
function openCreate() { editing.value = false; resetForm(); visible.value = true }
async function openEdit(row: Record<string, any>) {
  editing.value = true
  resetForm()
  try { Object.assign(form, await systemApi.user(String(row.id))) } catch { Object.assign(form, row) }
  form.password = undefined
  visible.value = true
}
async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload = { ...form }
    if (editing.value) { delete payload.password; await systemApi.updateUser(String(form.id), payload) }
    else await systemApi.createUser(payload)
    ElMessage.success('保存成功'); visible.value = false; load()
  } finally { saving.value = false }
}
async function resetPwd(row: Record<string, any>) {
  const { value } = await ElMessageBox.prompt(`为「${row.username}」设置新密码`, '重置密码', {
    inputPattern: /^.{6,32}$/, inputErrorMessage: '密码长度 6-32 位'
  })
  await systemApi.resetPassword(String(row.id), value)
  ElMessage.success('密码已重置')
}
async function remove(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除用户「${row.username}」？`, '提示', { type: 'warning' })
  await systemApi.deleteUser(String(row.id))
  ElMessage.success('删除成功'); load()
}

onMounted(() => { load(); loadRoles(); loadDepts() })
</script>

<style scoped>
.pager { display: flex; justify-content: flex-end; margin-top: 14px; }
</style>

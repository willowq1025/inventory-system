<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-toolbar">
        <el-input v-model="query.keyword" placeholder="角色名称 / 标识" clearable style="width:220px" @keyup.enter="reload" />
        <el-button type="primary" @click="reload"><el-icon><Search /></el-icon> 查询</el-button>
        <div class="spacer" />
        <el-button v-if="can('create')" type="primary" @click="openCreate"><el-icon><Plus /></el-icon> 新增角色</el-button>
      </div>
      <el-alert type="info" :closable="false" class="mb-12"
                title="数据权限：全部数据 / 自定义部门 / 本部门 / 仅本人。授权后权限缓存会在事务提交后自动失效。" />

      <el-table :data="rows" v-loading="loading" border stripe>
        <el-table-column type="index" label="#" width="55" />
        <el-table-column prop="roleName" label="角色名称" width="160" />
        <el-table-column prop="roleKey" label="权限标识" width="160" />
        <el-table-column label="数据权限" width="140">
          <template #default="{ row }"><el-tag size="small" :type="scopeType(row.dataScope)">{{ scopeText(row.dataScope) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }"><el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="can('grant')" link type="primary" @click="openGrant(row)">分配权限</el-button>
            <el-button v-if="can('update')" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="can('delete')" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total"
                       layout="total, prev, pager, next" @change="load" />
      </div>
    </div>

    <el-dialog v-model="visible" :title="editing ? '编辑角色' : '新增角色'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="角色名称" prop="roleName"><el-input v-model="form.roleName" /></el-form-item>
        <el-form-item label="权限标识" prop="roleKey"><el-input v-model="form.roleKey" placeholder="如 purchase:manager" /></el-form-item>
        <el-form-item label="数据权限">
          <el-select v-model="form.dataScope" class="full-width">
            <el-option v-for="o in scopes" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="form.status"><el-radio :value="1">正常</el-radio><el-radio :value="0">停用</el-radio></el-radio-group></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="grantVisible" :title="`分配权限 - ${current?.roleName || ''}`" width="620px" destroy-on-close>
      <el-tabs v-model="grantTab">
        <el-tab-pane label="菜单 / 接口权限" name="menu">
          <el-tree ref="treeRef" :data="menuTree" node-key="id" show-checkbox default-expand-all
                   :props="{ label: 'menuName', children: 'children' }" style="max-height:380px;overflow:auto" />
        </el-tab-pane>
        <el-tab-pane label="自定义数据权限部门" name="dept">
          <el-alert type="info" :closable="false" class="mb-12" title="仅当数据权限为「自定义部门」时生效" />
          <el-tree ref="deptTreeRef" :data="deptTree" node-key="id" show-checkbox default-expand-all
                   :props="{ label: 'deptName', children: 'children' }" style="max-height:340px;overflow:auto" />
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="grantVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitGrant">保存授权</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { systemApi } from '@/api/system'
import { useUserStore } from '@/stores/user'
import { statusTag, statusText } from '@/composables/useOptions'
import { buildTree } from '@/utils/format'
import type { PageQuery } from '@/api/request'
import type { TreeNode } from '@/utils/format'

const user = useUserStore()
const can = (a: string) => user.hasPerm(`system:role:${a}`)
const scopes = [
  { label: '全部数据', value: 1 }, { label: '自定义部门', value: 2 },
  { label: '本部门数据', value: 3 }, { label: '仅本人数据', value: 4 }
]
const scopeText = (v: unknown) => scopes.find((s) => s.value === Number(v))?.label || '未知'
const scopeType = (v: unknown) => (Number(v) === 1 ? 'danger' : Number(v) === 4 ? 'info' : 'warning')

const rows = ref<Record<string, any>[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const grantVisible = ref(false)
const grantTab = ref('menu')
const editing = ref(false)
const current = ref<Record<string, any> | null>(null)
const formRef = ref<FormInstance>()
type TreeInstance = {
  setCheckedKeys: (keys: string[], leafOnly?: boolean) => void
  getCheckedKeys: (leafOnly?: boolean) => (string | number)[]
  getHalfCheckedKeys: () => (string | number)[]
}
const treeRef = ref<TreeInstance | null>(null)
const deptTreeRef = ref<TreeInstance | null>(null)
const menuTree = ref<TreeNode[]>([])
const deptTree = ref<TreeNode[]>([])
const query = reactive<PageQuery>({ pageNum: 1, pageSize: 20, keyword: '' })
const form = reactive<Record<string, any>>({})
const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入权限标识', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const data = await systemApi.roles(query)
    rows.value = data.records || []
    total.value = Number(data.total || 0)
  } finally { loading.value = false }
}
function reload() { query.pageNum = 1; load() }
function resetForm() {
  Object.keys(form).forEach((k) => delete form[k])
  Object.assign(form, { dataScope: 4, sort: 0, status: 1 })
}
function openCreate() { editing.value = false; resetForm(); visible.value = true }
function openEdit(row: Record<string, any>) {
  editing.value = true; resetForm(); Object.assign(form, row); visible.value = true
}
async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editing.value) await systemApi.updateRole(String(form.id), { ...form })
    else await systemApi.createRole({ ...form })
    ElMessage.success('保存成功'); visible.value = false; load()
  } finally { saving.value = false }
}
async function remove(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除角色「${row.roleName}」？`, '提示', { type: 'warning' })
  await systemApi.deleteRole(String(row.id))
  ElMessage.success('删除成功'); load()
}
async function openGrant(row: Record<string, any>) {
  current.value = row
  grantVisible.value = true
  grantTab.value = 'menu'
  const [menus, depts, detail] = await Promise.all([
    systemApi.menus(), systemApi.depts(), systemApi.role(String(row.id))
  ])
  menuTree.value = menus as unknown as TreeNode[]
  deptTree.value = buildTree(depts as unknown as TreeNode[])
  setTimeout(() => {
    treeRef.value?.setCheckedKeys(((detail.menuIds as string[]) || []).map(String), false)
    deptTreeRef.value?.setCheckedKeys(((detail.deptIds as string[]) || []).map(String), false)
  }, 0)
}
async function submitGrant() {
  if (!current.value) return
  saving.value = true
  try {
    const menuIds = [
      ...(treeRef.value?.getCheckedKeys(false) || []),
      ...(treeRef.value?.getHalfCheckedKeys() || [])
    ].map(String)
    const deptIds = (deptTreeRef.value?.getCheckedKeys(false) || []).map(String)
    await systemApi.grant(String(current.value.id), menuIds, deptIds)
    ElMessage.success('授权成功，权限缓存已刷新'); grantVisible.value = false
  } finally { saving.value = false }
}

onMounted(load)
</script>

<style scoped>
.pager { display: flex; justify-content: flex-end; margin-top: 14px; }
</style>

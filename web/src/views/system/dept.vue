<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-toolbar">
        <b>组织架构</b>
        <div class="spacer" />
        <el-button v-if="can('create')" type="primary" @click="openCreate(0)"><el-icon><Plus /></el-icon> 新增根部门</el-button>
      </div>
      <el-table :data="tree" v-loading="loading" row-key="id" border default-expand-all
                :tree-props="{ children: 'children' }">
        <el-table-column prop="deptName" label="部门名称" min-width="200" />
        <el-table-column prop="deptCode" label="部门编码" width="160" />
        <el-table-column prop="leader" label="负责人" width="120" />
        <el-table-column prop="phone" label="电话" width="150" />
        <el-table-column prop="sort" label="排序" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }"><el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="can('create')" link type="primary" @click="openCreate(row.id)">新增子部门</el-button>
            <el-button v-if="can('update')" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="can('delete')" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" :title="editing ? '编辑部门' : '新增部门'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级部门">
          <el-select v-model="form.parentId" clearable filterable class="full-width">
            <el-option label="顶级部门" value="0" />
            <el-option v-for="o in flatOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName"><el-input v-model="form.deptName" /></el-form-item>
        <el-form-item label="部门编码" prop="deptCode"><el-input v-model="form.deptCode" /></el-form-item>
        <el-form-item label="负责人"><el-input v-model="form.leader" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="form.status"><el-radio :value="1">正常</el-radio><el-radio :value="0">停用</el-radio></el-radio-group></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { systemApi } from '@/api/system'
import { useUserStore } from '@/stores/user'
import { statusTag, statusText } from '@/composables/useOptions'
import { buildTree } from '@/utils/format'
import type { TreeNode } from '@/utils/format'

interface DeptNode extends TreeNode { deptName: string }
const user = useUserStore()
const can = (a: string) => user.hasPerm(`system:dept:${a}`)
const tree = ref<DeptNode[]>([])
const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const editing = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<Record<string, any>>({})
const rules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  deptCode: [{ required: true, message: '请输入部门编码', trigger: 'blur' }]
}
const flatOptions = computed(() => {
  const out: { label: string; value: string }[] = []
  const walk = (nodes: DeptNode[], depth = 0) => {
    for (const n of nodes) {
      out.push({ label: `${'　'.repeat(depth)}${n.deptName}`, value: String(n.id) })
      if (n.children?.length) walk(n.children as DeptNode[], depth + 1)
    }
  }
  walk(tree.value)
  return out
})
async function load() {
  loading.value = true
  try { tree.value = buildTree((await systemApi.depts()) as unknown as DeptNode[]) as DeptNode[] }
  finally { loading.value = false }
}
function resetForm() {
  Object.keys(form).forEach((k) => delete form[k])
  Object.assign(form, { parentId: '0', sort: 0, status: 1 })
}
function openCreate(parentId: string | number) { editing.value = false; resetForm(); form.parentId = String(parentId || 0); visible.value = true }
function openEdit(row: Record<string, any>) { editing.value = true; resetForm(); Object.assign(form, row); form.parentId = String(row.parentId ?? 0); visible.value = true }
async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload = { ...form, parentId: form.parentId === '0' || form.parentId === '' ? 0 : form.parentId }
    if (editing.value) await systemApi.updateDept(String(form.id), payload)
    else await systemApi.createDept(payload)
    ElMessage.success('保存成功'); visible.value = false; load()
  } finally { saving.value = false }
}
async function remove(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除部门「${row.deptName}」？`, '提示', { type: 'warning' })
  await systemApi.deleteDept(String(row.id))
  ElMessage.success('删除成功'); load()
}
onMounted(load)
</script>

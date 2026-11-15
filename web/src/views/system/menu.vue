<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-toolbar">
        <b>菜单与接口权限</b>
        <span class="sub">menu_type：M 目录 / C 菜单 / F 按钮（接口权限）</span>
        <div class="spacer" />
        <el-button v-if="can('create')" type="primary" @click="openCreate(0)"><el-icon><Plus /></el-icon> 新增根节点</el-button>
      </div>
      <el-table :data="tree" v-loading="loading" row-key="id" border default-expand-all
                :tree-props="{ children: 'children' }">
        <el-table-column prop="menuName" label="名称" min-width="190" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }"><el-tag size="small" :type="row.menuType === 'F' ? 'warning' : row.menuType === 'M' ? 'success' : 'primary'">{{ typeText(row.menuType) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" min-width="150" show-overflow-tooltip />
        <el-table-column prop="permission" label="权限标识" min-width="170" show-overflow-tooltip />
        <el-table-column prop="apiPath" label="接口路径" min-width="190" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="可见" width="80">
          <template #default="{ row }"><el-tag size="small" :type="row.visible === 1 ? 'success' : 'info'">{{ row.visible === 1 ? '是' : '否' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="can('create')" link type="primary" @click="openCreate(row.id)">新增子项</el-button>
            <el-button v-if="can('update')" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="can('delete')" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" :title="editing ? '编辑菜单/权限' : '新增菜单/权限'" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="上级节点">
            <el-select v-model="form.parentId" clearable filterable class="full-width" placeholder="顶级节点填 0">
              <el-option label="顶级节点" value="0" />
              <el-option v-for="o in flatOptions" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="类型">
            <el-select v-model="form.menuType" class="full-width">
              <el-option label="目录" value="M" /><el-option label="菜单" value="C" /><el-option label="按钮/接口" value="F" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="名称" prop="menuName"><el-input v-model="form.menuName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="路由名称"><el-input v-model="form.routeName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="路由路径"><el-input v-model="form.path" placeholder="如 system/user" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="组件路径"><el-input v-model="form.component" placeholder="如 system/user" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="图标"><el-input v-model="form.icon" placeholder="如 Setting" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="权限标识"><el-input v-model="form.permission" placeholder="如 system:user:list" /></el-form-item></el-col>
          <el-col :span="16"><el-form-item label="接口路径"><el-input v-model="form.apiPath" placeholder="如 /api/system/users/**" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="请求方法">
            <el-select v-model="form.apiMethod" class="full-width" clearable>
              <el-option v-for="m in ['GET','POST','PUT','DELETE','*']" :key="m" :label="m" :value="m" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="8"><el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="可见"><el-switch v-model="form.visible" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { systemApi } from '@/api/system'
import { useUserStore } from '@/stores/user'

interface MenuNode { id: string; parentId: string; menuName: string; children?: MenuNode[]; [k: string]: unknown }
const user = useUserStore()
const can = (a: string) => user.hasPerm(`system:menu:${a}`)
const typeText = (t: string) => ({ M: '目录', C: '菜单', F: '权限' } as Record<string, string>)[t] || t

const tree = ref<MenuNode[]>([])
const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const editing = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<Record<string, any>>({})
const rules = { menuName: [{ required: true, message: '请输入名称', trigger: 'blur' }] }

const flatOptions = computed(() => {
  const out: { label: string; value: string }[] = []
  const walk = (nodes: MenuNode[], depth = 0) => {
    for (const n of nodes) {
      out.push({ label: `${'　'.repeat(depth)}${n.menuName}`, value: String(n.id) })
      if (n.children?.length) walk(n.children, depth + 1)
    }
  }
  walk(tree.value)
  return out
})

async function load() {
  loading.value = true
  try { tree.value = (await systemApi.menus()) as unknown as MenuNode[] } finally { loading.value = false }
}
function resetForm() {
  Object.keys(form).forEach((k) => delete form[k])
  Object.assign(form, { parentId: '0', menuType: 'C', sort: 0, visible: 1, status: 1, apiMethod: '*' })
}
function openCreate(parentId: string | number) { editing.value = false; resetForm(); form.parentId = String(parentId || 0); visible.value = true }
function openEdit(row: Record<string, any>) { editing.value = true; resetForm(); Object.assign(form, row); form.parentId = String(row.parentId ?? 0); visible.value = true }
async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload = { ...form, parentId: form.parentId === '0' || form.parentId === '' ? 0 : form.parentId }
    if (editing.value) await systemApi.updateMenu(String(form.id), payload)
    else await systemApi.createMenu(payload)
    ElMessage.success('保存成功，权限缓存已刷新'); visible.value = false; load()
  } finally { saving.value = false }
}
async function remove(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除「${row.menuName}」及其子节点？`, '提示', { type: 'warning' })
  await systemApi.deleteMenu(String(row.id))
  ElMessage.success('删除成功'); load()
}
onMounted(load)
</script>

<style scoped>
.sub { color: #9ca3af; font-size: 12px; margin-left: 12px; }
</style>

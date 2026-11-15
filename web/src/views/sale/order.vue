<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-toolbar">
        <el-input v-model="query.keyword" placeholder="订单号" clearable style="width:200px" @keyup.enter="reload" />
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width:140px">
          <el-option v-for="(v, k) in statusMap" :key="k" :label="v" :value="Number(k)" />
        </el-select>
        <el-date-picker v-model="range" type="daterange" value-format="YYYY-MM-DD HH:mm:ss"
                        start-placeholder="开始" end-placeholder="结束" style="width:340px" />
        <el-button type="primary" @click="reload"><el-icon><Search /></el-icon> 查询</el-button>
        <div class="spacer" />
        <el-button v-if="can('create')" type="primary" @click="openCreate"><el-icon><Plus /></el-icon> 新建销售订单</el-button>
        <el-button v-if="canExport" @click="orderApi.export(TYPE, query)"><el-icon><Download /></el-icon> 导出</el-button>
      </div>
      <el-table :data="rows" v-loading="loading" border stripe>
        <el-table-column prop="orderNo" label="订单号" width="200" show-overflow-tooltip />
        <el-table-column prop="partnerName" label="客户" width="160" />
        <el-table-column prop="warehouseName" label="仓库" width="150" />
        <el-table-column label="订单金额" width="140"><template #default="{ row }">{{ fmtMoney(row.totalAmount) }}</template></el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }"><el-tag size="small" :type="statusType(row.status)">{{ statusMap[row.status] || '未知' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="expectDate" label="期望日期" width="130" />
        <el-table-column label="创建时间" width="175"><template #default="{ row }">{{ fmtDate(row.createTime) }}</template></el-table-column>
        <el-table-column label="操作" width="270" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">明细</el-button>
            <el-button v-if="row.status === 1 && can('audit')" link type="warning" @click="audit(row, true)">通过</el-button>
            <el-button v-if="row.status === 1 && can('audit')" link type="danger" @click="audit(row, false)">驳回</el-button>
            <el-button v-if="row.status === 2 && canExec" link type="success" @click="execute(row)">出库</el-button>
            <el-button v-if="row.status <= 2 && can('audit')" link type="info" @click="cancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total"
                       :page-sizes="[10,20,50,100]" layout="total, sizes, prev, pager, next" @change="load" />
      </div>
    </div>

    <el-dialog v-model="visible" title="新建销售订单" width="780px" destroy-on-close>
      <el-form :model="form" label-width="100px" inline>
        <el-form-item label="客户">
          <el-select v-model="form.customerId" filterable style="width:210px">
            <el-option v-for="o in partners" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="入库仓库">
          <el-select v-model="form.warehouseId" filterable style="width:210px">
            <el-option v-for="o in warehouses" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="期望日期"><el-date-picker v-model="form.expectDate" type="date" value-format="YYYY-MM-DD" style="width:210px" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" style="width:210px" /></el-form-item>
      </el-form>
      <div class="page-toolbar mt-12">
        <b>订单明细</b><div class="spacer" />
        <el-button size="small" @click="addItem"><el-icon><Plus /></el-icon> 添加商品</el-button>
      </div>
      <el-table :data="form.items" border size="small">
        <el-table-column label="商品" min-width="220">
          <template #default="{ row }">
            <el-select v-model="row.productId" filterable class="full-width" @change="onProductChange(row)">
              <el-option v-for="o in products" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="150"><template #default="{ row }"><el-input-number v-model="row.quantity" :min="1" size="small" /></template></el-table-column>
        <el-table-column label="单价" width="160"><template #default="{ row }"><el-input-number v-model="row.unitPrice" :min="0" :precision="2" size="small" /></template></el-table-column>
        <el-table-column label="小计" width="130"><template #default="{ row }">{{ fmtMoney((row.quantity || 0) * (row.unitPrice || 0)) }}</template></el-table-column>
        <el-table-column label="操作" width="80"><template #default="{ $index }"><el-button link type="danger" @click="form.items.splice($index, 1)">移除</el-button></template></el-table-column>
      </el-table>
      <div class="total-line">合计：<b>{{ fmtMoney(totalAmount) }}</b></div>
      <template #footer><el-button @click="visible = false">取消</el-button><el-button type="primary" :loading="saving" @click="submit">提交订单</el-button></template>
    </el-dialog>

    <el-drawer v-model="drawer" title="订单明细" size="620px">
      <el-descriptions :column="2" border size="small" class="mb-12">
        <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusMap[detail.status] || '-' }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ detail.partnerName }}</el-descriptions-item>
        <el-descriptions-item label="仓库">{{ detail.warehouseName }}</el-descriptions-item>
        <el-descriptions-item label="总金额">{{ fmtMoney(detail.totalAmount) }}</el-descriptions-item>
        <el-descriptions-item label="期望日期">{{ detail.expectDate || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="detail.items || []" border size="small">
        <el-table-column label="商品" min-width="180"><template #default="{ row }">{{ productName(row.productId) }}</template></el-table-column>
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column label="单价" width="130"><template #default="{ row }">{{ fmtMoney(row.unitPrice) }}</template></el-table-column>
        <el-table-column label="小计" width="140"><template #default="{ row }">{{ fmtMoney((row.quantity || 0) * (row.unitPrice || 0)) }}</template></el-table-column>
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi, baseApi } from '@/api/business'
import { useUserStore } from '@/stores/user'
import { useOptions } from '@/composables/useOptions'
import { fmtDate, fmtMoney } from '@/utils/format'
import type { PageQuery } from '@/api/request'
import type { Option } from '@/composables/useOptions'
import type { RemoteResource } from '@/components/crud'

const TYPE: 'purchase' | 'sale' = 'sale'
const PARTNER_RESOURCE: RemoteResource = 'customers'
const user = useUserStore()
const can = (a: string) => user.hasPerm(`sale:order:${a}`)
const canExec = user.hasPerm('sale:order:stock-out')
const canExport = user.hasPerm('sale:order:export')
const { products, warehouses, load: loadOptions } = useOptions()
const partners = ref<Option[]>([])
const rows = ref<Record<string, any>[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const drawer = ref(false)
const detail = reactive<Record<string, any>>({})
const range = ref<[string, string] | null>(null)
const query = reactive<PageQuery>({ pageNum: 1, pageSize: 20, keyword: '' })
const form = reactive<Record<string, any>>({ items: [] })
const priceMap = ref<Record<string, number>>({})

const statusMap: Record<number, string> = { 1: '待审核', 2: '已审核', 3: '已出库', 4: '已取消', 5: '已驳回' }
const statusType = (s: number) => ({ 1: 'warning', 2: 'primary', 3: 'success', 4: 'info', 5: 'danger' } as Record<number, string>)[s] || 'info'
const productName = (id: unknown) => products.value.find((o) => o.value === String(id))?.label || String(id ?? '-')
const totalAmount = computed(() => (form.items as Record<string, any>[]).reduce((s, i) => s + (Number(i.quantity) || 0) * (Number(i.unitPrice) || 0), 0))

watch(range, (v) => { query.startTime = v?.[0]; query.endTime = v?.[1] })

async function load() {
  loading.value = true
  try {
    const data = await orderApi.list(TYPE, query)
    rows.value = data.records || []
    total.value = Number(data.total || 0)
  } finally { loading.value = false }
}
function reload() { query.pageNum = 1; load() }
function addItem() { form.items.push({ productId: '', quantity: 1, unitPrice: 0 }) }
function onProductChange(row: Record<string, any>) {
  const price = priceMap.value[String(row.productId)]
  if (price !== undefined) row.unitPrice = price
}
function openCreate() {
  Object.assign(form, { customerId: '', warehouseId: '', expectDate: dayjs().format('YYYY-MM-DD'), remark: '', items: [] })
  addItem(); visible.value = true
}
async function submit() {
  if (!form.customerId || !form.warehouseId || !form.items.some((i: Record<string, any>) => i.productId)) {
    ElMessage.warning('请选择客户、仓库并至少填写一条明细'); return
  }
  saving.value = true
  try {
    await orderApi.create(TYPE, { ...form })
    ElMessage.success('订单已提交，等待审核'); visible.value = false; load()
  } finally { saving.value = false }
}
async function showDetail(row: Record<string, any>) {
  Object.assign(detail, await orderApi.detail(TYPE, String(row.id)))
  drawer.value = true
}
async function audit(row: Record<string, any>, pass: boolean) {
  await ElMessageBox.confirm(`确认${pass ? '通过' : '驳回'}订单「${row.orderNo}」？`, '提示', { type: 'warning' })
  await orderApi.audit(TYPE, String(row.id), pass)
  ElMessage.success('审核完成'); load()
}
async function execute(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认执行「${row.orderNo}」出库？该操作幂等，重复点击不会重复出入库。`, '提示', { type: 'warning' })
  await orderApi.execute(TYPE, String(row.id))
  ElMessage.success('出库完成'); load()
}
async function cancel(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认取消订单「${row.orderNo}」？`, '提示', { type: 'warning' })
  await orderApi.cancel(TYPE, String(row.id))
  ElMessage.success('订单已取消'); load()
}

onMounted(async () => {
  await loadOptions(['products', 'warehouses', PARTNER_RESOURCE])
  const bucket: Record<string, typeof partners> = { customers: partners }
  const partnerRows = await baseApi.all(PARTNER_RESOURCE).catch(() => [])
  bucket[PARTNER_RESOURCE].value = partnerRows.map((r) => ({ label: String(r.customerName ?? r.id), value: String(r.id) }))
  const allProducts = await baseApi.all('products').catch(() => [])
  const key = 'salePrice'
  priceMap.value = Object.fromEntries(allProducts.map((r) => [String(r.id), Number(r[key] || 0)]))
  load()
})
</script>

<style scoped>
.pager { display: flex; justify-content: flex-end; margin-top: 14px; }
.total-line { text-align: right; margin-top: 12px; font-size: 15px; }
.total-line b { color: #dc2626; font-size: 18px; }
</style>

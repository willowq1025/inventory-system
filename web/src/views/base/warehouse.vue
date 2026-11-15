<template>
  <CrudPage :config="config" />
</template>

<script setup lang="ts">
import CrudPage from '@/components/CrudPage.vue'
import type { CrudConfig } from '@/components/crud'

const config: CrudConfig = {
  title: '仓库',
  resource: 'warehouses',
  perm: 'base:warehouse',
  exportable: true,
  columns: [
    { prop: 'warehouseCode', label: '仓库编码', width: 150 },
    { prop: 'warehouseName', label: '仓库名称' },
    { prop: 'address', label: '地址' },
    { prop: 'phone', label: '电话', width: 140 },
    { prop: 'status', label: '状态', width: 90, type: 'status' }
  ],
  fields: [
    { prop: 'warehouseCode', label: '仓库编码', required: true },
    { prop: 'warehouseName', label: '仓库名称', required: true },
    { prop: 'address', label: '地址' },
    { prop: 'phone', label: '电话' },
    { prop: 'managerId', label: '负责人ID', hint: '填写系统用户 ID（可留空）' },
    { prop: 'status', label: '状态', type: 'select', options: [{ label: '正常', value: 1 }, { label: '停用', value: 0 }] },
    { prop: 'remark', label: '备注', type: 'textarea' }
  ]
}
</script>

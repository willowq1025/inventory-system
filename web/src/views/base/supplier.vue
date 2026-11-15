<template>
  <CrudPage :config="config" />
</template>

<script setup lang="ts">
import CrudPage from '@/components/CrudPage.vue'
import type { CrudConfig } from '@/components/crud'

const config: CrudConfig = {
  title: '供应商',
  resource: 'suppliers',
  perm: 'base:supplier',
  exportable: true,
  columns: [
    { prop: 'supplierCode', label: '编码', width: 140 },
    { prop: 'supplierName', label: '供应商名称' },
    { prop: 'contact', label: '联系人', width: 120 },
    { prop: 'phone', label: '电话', width: 140 },
    { prop: 'email', label: '邮箱', width: 180 },
    { prop: 'status', label: '状态', width: 90, type: 'status' }
  ],
  fields: [
    { prop: 'supplierCode', label: '供应商编码', required: true },
    { prop: 'supplierName', label: '供应商名称', required: true },
    { prop: 'contact', label: '联系人' },
    { prop: 'phone', label: '电话' },
    { prop: 'email', label: '邮箱' },
    { prop: 'address', label: '地址' },
    { prop: 'status', label: '状态', type: 'select', options: [{ label: '正常', value: 1 }, { label: '停用', value: 0 }] },
    { prop: 'remark', label: '备注', type: 'textarea' }
  ]
}
</script>

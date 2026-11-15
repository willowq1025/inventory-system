<template>
  <CrudPage :config="config" />
</template>

<script setup lang="ts">
import CrudPage from '@/components/CrudPage.vue'
import type { CrudConfig } from '@/components/crud'

const config: CrudConfig = {
  title: '商品分类',
  resource: 'categories',
  perm: 'base:category',
  exportable: true,
  columns: [
    { prop: 'categoryName', label: '分类名称' },
    { prop: 'categoryCode', label: '分类编码' },
    { prop: 'parentId', label: '上级ID', width: 180 },
    { prop: 'sort', label: '排序', width: 90, type: 'number' },
    { prop: 'status', label: '状态', width: 90, type: 'status' },
    { prop: 'createTime', label: '创建时间', width: 180, type: 'date' }
  ],
  fields: [
    { prop: 'categoryName', label: '分类名称', required: true },
    { prop: 'categoryCode', label: '分类编码', required: true },
    { prop: 'parentId', label: '上级分类', type: 'select', remote: 'categories', hint: '顶级分类留空' },
    { prop: 'sort', label: '排序', type: 'number' },
    { prop: 'status', label: '状态', type: 'select', options: [{ label: '正常', value: 1 }, { label: '停用', value: 0 }] },
    { prop: 'remark', label: '备注', type: 'textarea' }
  ]
}
</script>

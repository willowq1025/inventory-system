<template>
  <CrudPage :config="config" />
</template>

<script setup lang="ts">
import CrudPage from '@/components/CrudPage.vue'
import type { CrudConfig } from '@/components/crud'

const config: CrudConfig = {
  title: '商品',
  resource: 'products',
  perm: 'base:product',
  exportable: true,
  importable: true,
  columns: [
    { prop: 'productCode', label: 'SKU', width: 150 },
    { prop: 'productName', label: '商品名称' },
    { prop: 'spec', label: '规格', width: 150 },
    { prop: 'unit', label: '单位', width: 80 },
    { prop: 'brand', label: '品牌', width: 120 },
    { prop: 'purchasePrice', label: '采购价', width: 120, type: 'money' },
    { prop: 'salePrice', label: '销售价', width: 120, type: 'money' },
    { prop: 'safetyStock', label: '安全库存', width: 110, type: 'number' },
    { prop: 'status', label: '状态', width: 90, type: 'status' }
  ],
  fields: [
    { prop: 'productCode', label: 'SKU 编码', required: true },
    { prop: 'productName', label: '商品名称', required: true },
    { prop: 'categoryId', label: '所属分类', type: 'select', remote: 'categories' },
    { prop: 'spec', label: '规格' },
    { prop: 'unit', label: '单位' },
    { prop: 'brand', label: '品牌' },
    { prop: 'barcode', label: '条码' },
    { prop: 'purchasePrice', label: '采购价', type: 'number' },
    { prop: 'salePrice', label: '销售价', type: 'number' },
    { prop: 'safetyStock', label: '安全库存', type: 'number' },
    { prop: 'status', label: '状态', type: 'select', options: [{ label: '正常', value: 1 }, { label: '停用', value: 0 }] },
    { prop: 'remark', label: '备注', type: 'textarea' }
  ]
}
</script>

<template>
  <div class="login-page">
    <div class="login-box">
      <div class="brand">
        <el-icon :size="34"><Box /></el-icon>
        <h1>进销存管理平台</h1>
        <p>Enterprise Inventory Management System</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="onSubmit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" :prefix-icon="User" clearable />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock"
                    show-password clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="full-width" :loading="loading" @click="onSubmit">登 录</el-button>
        </el-form-item>
      </el-form>
      <div class="tip">
        默认管理员：<b>admin</b> / <b>Admin@123</b><br />
        <span>登录即代表同意系统使用规范，所有操作将被审计留痕</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const user = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'Admin@123' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await user.login(form)
    ElMessage.success('登录成功')
    router.push((route.query.redirect as string) || '/dashboard')
  } catch {
    ElMessage.error('账号或密码错误')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 50%, #38bdf8 100%);
}
.login-box {
  width: 410px; background: #fff; border-radius: 14px; padding: 38px 36px 26px;
  box-shadow: 0 20px 60px rgba(0,0,0,.22);
}
.brand { text-align: center; margin-bottom: 28px; color: #2563eb; }
.brand h1 { font-size: 22px; margin: 10px 0 4px; color: #111827; }
.brand p { font-size: 12px; color: #9ca3af; margin: 0; }
.tip { margin-top: 18px; padding-top: 14px; border-top: 1px solid #f3f4f6; color: #6b7280; font-size: 12px; line-height: 1.8; }
.tip b { color: #2563eb; }
.tip span { color: #9ca3af; }
</style>

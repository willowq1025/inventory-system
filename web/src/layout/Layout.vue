<template>
  <el-container class="layout">
    <el-aside :width="collapsed ? '64px' : '220px'" class="aside">
      <div class="logo" @click="router.push('/dashboard')">
        <el-icon :size="22"><Box /></el-icon>
        <span v-if="!collapsed">进销存平台</span>
      </div>
      <el-scrollbar>
        <el-menu
          :default-active="route.path"
          :collapse="collapsed"
          :collapse-transition="false"
          background-color="#1f2937"
          text-color="#cbd5e1"
          active-text-color="#fff"
          router
          unique-opened
        >
          <template v-for="menu in sideMenus" :key="menu.path">
            <el-sub-menu v-if="menu.children && menu.children.length" :index="menu.path">
              <template #title>
                <el-icon><component :is="iconOf(menu.icon)" /></el-icon>
                <span>{{ menu.title }}</span>
              </template>
              <el-menu-item v-for="child in menu.children" :key="child.path" :index="child.path">
                <el-icon><component :is="iconOf(child.icon)" /></el-icon>
                <span>{{ child.title }}</span>
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="menu.path">
              <el-icon><component :is="iconOf(menu.icon)" /></el-icon>
              <span>{{ menu.title }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container>
      <el-header class="header">
        <el-icon class="collapse-btn" :size="20" @click="collapsed = !collapsed">
          <component :is="collapsed ? 'Expand' : 'Fold'" />
        </el-icon>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item>首页</el-breadcrumb-item>
          <el-breadcrumb-item>{{ route.meta.title || '页面' }}</el-breadcrumb-item>
        </el-breadcrumb>
        <div class="spacer" />
        <el-badge :value="unread" :hidden="unread === 0" class="msg-badge">
          <el-button text circle @click="router.push('/message/mine')">
            <el-icon :size="19"><Bell /></el-icon>
          </el-button>
        </el-badge>
        <el-dropdown @command="onCommand">
          <span class="user-info">
            <el-avatar :size="28" style="background:#2563eb">{{ (user.user?.nickname || 'U').slice(0, 1) }}</el-avatar>
            <span>{{ user.user?.nickname || '未登录' }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">{{ user.user?.username }}</el-dropdown-item>
              <el-dropdown-item command="doc" divided>接口文档</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main class="main">
        <router-view v-slot="{ Component }">
          <keep-alive :max="8"><component :is="Component" :key="route.path" /></keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { flattenMenus } from '@/utils/format'
import { systemApi } from '@/api/system'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const collapsed = ref(false)
const unread = ref(0)

interface SideItem { path: string; title: string; icon?: string; children?: SideItem[] }
const HOME: SideItem = { path: '/dashboard', title: '首页概览', icon: 'House' }
const sideMenus = computed<SideItem[]>(() => {
  const menus = user.menus as unknown as Parameters<typeof flattenMenus>[0]
  // 后端菜单未加载时退化为静态首页（不影响可用性）
  if (!menus || !menus.length) return [HOME]
  const dynamic = flattenMenus(menus) as unknown as SideItem[]
  // 首页概览为固定入口，不在 sys_menu 中登记
  return dynamic.some((m) => m.path === HOME.path) ? dynamic : [HOME, ...dynamic]
})

const iconOf = (name?: string) => name || 'Menu'

async function loadUnread() {
  try { unread.value = Number(await systemApi.unreadCount()) } catch { unread.value = 0 }
}

async function onCommand(command: string) {
  if (command === 'logout') {
    await ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' })
    await user.logout()
    router.push('/login')
  } else if (command === 'doc') {
    window.open('/doc.html', '_blank')
  }
}

onMounted(loadUnread)
</script>

<style scoped>
.layout { height: 100vh; }
.aside { background: #1f2937; transition: width .2s; overflow: hidden; }
.logo {
  height: 60px; display: flex; align-items: center; justify-content: center; gap: 8px;
  color: #fff; font-weight: 700; font-size: 16px; cursor: pointer; border-bottom: 1px solid #374151;
}
.aside :deep(.el-menu) { border-right: none; }
.aside :deep(.el-menu-item.is-active) { background: #2563eb !important; }
.header {
  height: 60px; background: #fff; display: flex; align-items: center; gap: 14px;
  border-bottom: 1px solid #e5e7eb; padding: 0 18px;
}
.collapse-btn { cursor: pointer; }
.breadcrumb { margin-left: 6px; }
.spacer { flex: 1; }
.msg-badge { margin-right: 8px; }
.user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; outline: none; }
.main { background: #f5f7fa; padding: 0; overflow: auto; }
</style>

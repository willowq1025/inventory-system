import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'
import type { LoginCmd, LoginVO, RouteVO } from '@/api/types'

interface UserState {
  token: string
  user: LoginVO['user'] | null
  permissions: string[]
  roles: string[]
  menus: RouteVO[]
  loaded: boolean
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: localStorage.getItem('inv_token') || '',
    user: JSON.parse(localStorage.getItem('inv_user') || 'null'),
    permissions: [],
    roles: [],
    menus: [],
    loaded: false
  }),
  getters: {
    isLogged: (s) => !!s.token,
    hasPerm: (s) => (perm?: string) => {
      if (!perm) return true
      return s.roles.includes('admin') || s.permissions.includes('*:*:*') || s.permissions.includes(perm)
    }
  },
  actions: {
    applyLogin(data: LoginVO) {
      this.token = data.token.accessToken
      this.user = data.user
      this.permissions = data.permissions || []
      this.roles = data.roles || []
      this.menus = data.menus || []
      this.loaded = true
      localStorage.setItem('inv_token', this.token)
      localStorage.setItem('inv_user', JSON.stringify(this.user))
    },
    async login(cmd: LoginCmd) {
      const data = await authApi.login(cmd)
      this.applyLogin(data)
      return data
    },
    async loadInfo() {
      const data = await authApi.info()
      this.user = data.user
      this.permissions = data.permissions || []
      this.roles = data.roles || []
      this.menus = data.menus || []
      this.loaded = true
    },
    async logout() {
      try { await authApi.logout() } catch { /* 忽略 */ }
      this.reset()
    },
    reset() {
      this.token = ''
      this.user = null
      this.permissions = []
      this.roles = []
      this.menus = []
      this.loaded = false
      localStorage.removeItem('inv_token')
      localStorage.removeItem('inv_user')
    }
  }
})

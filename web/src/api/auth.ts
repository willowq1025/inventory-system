import { get, post } from './request'
import type { LoginCmd, LoginVO } from './types'

export const authApi = {
  login: (data: LoginCmd) => post<LoginVO>('/auth/login', data),
  info: () => get<LoginVO>('/auth/info'),
  logout: () => post<void>('/auth/logout'),
  captcha: () => get<Record<string, unknown>>('/auth/captcha')
}

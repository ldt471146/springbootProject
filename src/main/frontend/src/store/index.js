import { defineStore } from 'pinia'
import { clearAuth, getToken, getUserCache, setToken, setUserCache } from '@/utils/auth'
import { fetchMe } from '@/api/auth'
import { fetchPendingUserCount } from '@/api/user'

export const useAppStore = defineStore('app', {
  state: () => ({
    token: getToken(),
    user: getUserCache(),
    adminPendingUsersCount: 0
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    role: (state) => state.user?.role || '',
    displayName: (state) => state.user?.realName || state.user?.username || '未登录',
    adminPendingUsersBadge: (state) => {
      if (!state.adminPendingUsersCount) {
        return ''
      }
      return state.adminPendingUsersCount > 99 ? '99+' : String(state.adminPendingUsersCount)
    }
  },
  actions: {
    setAuth(payload) {
      this.token = payload.token
      this.user = payload.user
      setToken(payload.token)
      setUserCache(payload.user)
    },
    setUser(user) {
      this.user = user
      setUserCache(user)
    },
    setAdminPendingUsersCount(count) {
      this.adminPendingUsersCount = Number(count || 0)
    },
    async refreshAdminPendingUsersCount() {
      if (!this.token || this.role !== 'ADMIN') {
        this.setAdminPendingUsersCount(0)
        return 0
      }
      const count = await fetchPendingUserCount()
      this.setAdminPendingUsersCount(count)
      return count
    },
    logout() {
      this.token = ''
      this.user = null
      this.adminPendingUsersCount = 0
      clearAuth()
    },
    async ensureMe() {
      if (!this.token) {
        return null
      }
      if (this.user) {
        return this.user
      }
      const user = await fetchMe()
      this.setUser(user)
      return user
    }
  }
})

import { defineStore } from 'pinia'
import { clearAuth, getToken, getUserCache, setToken, setUserCache } from '@/utils/auth'
import { fetchMe } from '@/api/auth'
import { fetchReminderSummary } from '@/api/reminder'

export const useAppStore = defineStore('app', {
  state: () => ({
    token: getToken(),
    user: getUserCache(),
    reminders: {
      adminPendingUsers: 0,
      adminPendingGrades: 0,
      teacherPendingApplications: 0,
      teacherPendingJournals: 0,
      teacherPendingReports: 0
    }
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    role: (state) => state.user?.role || '',
    displayName: (state) => state.user?.realName || state.user?.username || '未登录',
    adminPendingUsersBadge: (state) => formatBadge(state.reminders.adminPendingUsers),
    adminPendingGradesBadge: (state) => formatBadge(state.reminders.adminPendingGrades),
    teacherPendingApplicationsBadge: (state) => formatBadge(state.reminders.teacherPendingApplications),
    teacherPendingJournalsBadge: (state) => formatBadge(state.reminders.teacherPendingJournals),
    teacherPendingReportsBadge: (state) => formatBadge(state.reminders.teacherPendingReports)
  },
  actions: {
    setAuth(payload, rememberMe = true) {
      this.token = payload.token
      this.user = payload.user
      setToken(payload.token, rememberMe)
      setUserCache(payload.user, rememberMe)
    },
    setUser(user) {
      this.user = user
      setUserCache(user)
    },
    setReminders(summary = {}) {
      this.reminders = {
        adminPendingUsers: Number(summary.adminPendingUsers || 0),
        adminPendingGrades: Number(summary.adminPendingGrades || 0),
        teacherPendingApplications: Number(summary.teacherPendingApplications || 0),
        teacherPendingJournals: Number(summary.teacherPendingJournals || 0),
        teacherPendingReports: Number(summary.teacherPendingReports || 0)
      }
    },
    resetReminders() {
      this.setReminders()
    },
    async refreshReminders() {
      if (!this.token) {
        this.resetReminders()
        return this.reminders
      }
      const summary = await fetchReminderSummary()
      this.setReminders(summary)
      return this.reminders
    },
    logout() {
      this.token = ''
      this.user = null
      this.resetReminders()
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

function formatBadge(count) {
  if (!count) {
    return ''
  }
  return count > 99 ? '99+' : String(count)
}

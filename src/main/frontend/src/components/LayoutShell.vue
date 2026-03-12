<template>
  <div class="layout-shell">
    <SidebarNav
      :user="store.user"
      :open="sidebarOpen"
      :reminder-badges="{
        adminPendingUsers: store.adminPendingUsersBadge,
        adminPendingGrades: store.adminPendingGradesBadge,
        teacherPendingApplications: store.teacherPendingApplicationsBadge,
        teacherPendingJournals: store.teacherPendingJournalsBadge,
        teacherPendingReports: store.teacherPendingReportsBadge
      }"
      @logout="handleLogout"
      @navigate="sidebarOpen = false"
    />
    <main class="layout-main">
      <div class="mobile-topbar">
        <el-button @click="sidebarOpen = !sidebarOpen">菜单</el-button>
        <div class="soft-badge">{{ store.displayName }}</div>
      </div>
      <HeaderBar
        :user="store.user"
        :reminders="store.reminders"
        @toggle-sidebar="sidebarOpen = !sidebarOpen"
      />
      <div class="main-content">
        <RouterView v-slot="{ Component, route }">
          <Transition name="page-switch" mode="out-in">
            <component :is="Component" :key="route.path" />
          </Transition>
        </RouterView>
      </div>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAppStore } from '@/store'
import SidebarNav from './SidebarNav.vue'
import HeaderBar from './HeaderBar.vue'

const store = useAppStore()
const router = useRouter()
const sidebarOpen = ref(false)

async function syncReminders() {
  await store.refreshReminders()
}

onMounted(syncReminders)

watch(() => store.role, syncReminders)

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确认退出当前账号？', '退出登录', {
      type: 'warning'
    })
    store.logout()
    router.push('/login')
  } catch (error) {
    return
  }
}
</script>

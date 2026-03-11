<template>
  <div class="layout-shell">
    <SidebarNav
      :user="store.user"
      :open="sidebarOpen"
      :admin-pending-users-badge="store.adminPendingUsersBadge"
      @logout="handleLogout"
      @navigate="sidebarOpen = false"
    />
    <main class="layout-main">
      <div class="mobile-topbar">
        <el-button @click="sidebarOpen = !sidebarOpen">菜单</el-button>
        <div class="soft-badge">{{ store.displayName }}</div>
      </div>
      <HeaderBar :user="store.user" @toggle-sidebar="sidebarOpen = !sidebarOpen" />
      <div class="main-content">
        <RouterView />
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

async function syncAdminBadges() {
  if (store.role !== 'ADMIN') {
    store.setAdminPendingUsersCount(0)
    return
  }
  await store.refreshAdminPendingUsersCount()
}

onMounted(syncAdminBadges)

watch(() => store.role, syncAdminBadges)

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

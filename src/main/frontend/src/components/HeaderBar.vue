<template>
  <header class="main-topbar">
    <div>
      <div class="soft-badge">{{ currentTime }}</div>
      <h1 class="page-title">{{ route.meta.title || '工作台' }}</h1>
      <p class="page-subtitle">保持固定导航，压缩操作路径，让高频任务稳定可达。</p>
    </div>
    <div class="toolbar-group">
      <div class="topbar-shortcuts">
        <el-badge
          v-for="item in shortcutActions"
          :key="item.label"
          :value="item.badge"
          :hidden="!item.badge"
        >
          <el-button
            :class="['topbar-shortcut', { 'is-active': route.path === item.path }]"
            :type="route.path === item.path ? 'primary' : 'default'"
            :plain="route.path !== item.path"
            @click="router.push(item.path)"
          >
            <span class="topbar-shortcut-label">{{ item.label }}</span>
            <span v-if="route.path === item.path" class="topbar-shortcut-state">当前</span>
          </el-button>
        </el-badge>
      </div>
      <el-tag effect="plain" size="large">{{ user?.realName || user?.username }}</el-tag>
      <el-button @click="$emit('toggle-sidebar')">菜单</el-button>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const props = defineProps({
  user: {
    type: Object,
    default: null
  },
  reminders: {
    type: Object,
    default: () => ({})
  }
})

defineEmits(['toggle-sidebar'])

const route = useRoute()
const router = useRouter()

const currentTime = computed(() => new Date().toLocaleString('zh-CN', { hour12: false }))

const shortcutActions = computed(() => {
  const role = props.user?.role
  if (role === 'ADMIN') {
    return [
      {
        label: '用户审批',
        path: '/admin/users',
        badge: badgeValue(props.reminders.adminPendingUsers)
      },
      {
        label: '成绩终审',
        path: '/admin/grades',
        badge: badgeValue(props.reminders.adminPendingGrades)
      },
      {
        label: '导出中心',
        path: '/admin/export',
        badge: ''
      }
    ]
  }
  if (role === 'TEACHER') {
    return [
      {
        label: '申请审批',
        path: '/teacher/applications',
        badge: badgeValue(props.reminders.teacherPendingApplications)
      },
      {
        label: '日志评阅',
        path: '/teacher/journals',
        badge: badgeValue(props.reminders.teacherPendingJournals)
      },
      {
        label: '报告评阅',
        path: '/teacher/reports',
        badge: badgeValue(props.reminders.teacherPendingReports)
      }
    ]
  }
  if (role === 'STUDENT') {
    return [
      { label: '项目报名', path: '/student/projects', badge: '' },
      { label: '我的申请', path: '/student/applications', badge: '' },
      { label: '实习日志', path: '/student/journals', badge: '' }
    ]
  }
  return []
})

function badgeValue(count) {
  if (!count) {
    return ''
  }
  return count > 99 ? '99+' : String(count)
}
</script>

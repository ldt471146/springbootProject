<template>
  <aside :class="['layout-sidebar', { open }]">
    <div class="brand-card">
      <div class="brand-mark">IM</div>
      <div class="brand-title">实习教学系统</div>
      <p class="brand-desc">固定侧边栏后台框架，围绕学生申请、教师评阅与管理员终审设计。</p>
    </div>

    <div class="nav-card">
      <template v-for="group in navGroups" :key="group.label">
        <div class="nav-group-label">{{ group.label }}</div>
        <RouterLink
          v-for="item in group.items"
          :key="item.path"
          :to="item.path"
          class="nav-link"
          :class="{ active: route.path === item.path }"
          @click="$emit('navigate')"
        >
          <span>{{ item.label }}</span>
          <span v-if="item.badge" class="nav-badge">{{ item.badge }}</span>
        </RouterLink>
      </template>
    </div>

    <div class="profile-card">
      <div class="soft-badge">{{ roleLabel }}</div>
      <div class="profile-name">{{ user?.realName || user?.username }}</div>
      <p class="profile-meta">{{ user?.college || '教学管理平台' }}</p>
      <el-button class="full-width" type="primary" plain @click="$emit('logout')">退出登录</el-button>
    </div>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const props = defineProps({
  user: {
    type: Object,
    default: null
  },
  open: {
    type: Boolean,
    default: false
  },
  adminPendingUsersBadge: {
    type: String,
    default: ''
  }
})

defineEmits(['logout', 'navigate'])

const route = useRoute()

const navGroups = computed(() => {
  const menuByRole = {
    STUDENT: [
      {
        label: '工作区',
        items: [
          { path: '/dashboard', label: '总览' },
          { path: '/student/projects', label: '项目报名' },
          { path: '/student/applications', label: '我的申请' },
          { path: '/student/journals', label: '实习日志' },
          { path: '/student/reports', label: '实习报告' },
          { path: '/student/grades', label: '我的成绩' }
        ]
      },
      {
        label: '账号',
        items: [
          { path: '/profile', label: '个人资料' },
          { path: '/password', label: '修改密码' }
        ]
      }
    ],
    TEACHER: [
      {
        label: '教学管理',
        items: [
          { path: '/dashboard', label: '总览' },
          { path: '/teacher/projects', label: '项目管理' },
          { path: '/teacher/applications', label: '申请审批' },
          { path: '/teacher/journals', label: '日志评阅' },
          { path: '/teacher/reports', label: '报告评阅' },
          { path: '/teacher/students', label: '学生进度' }
        ]
      },
      {
        label: '账号',
        items: [
          { path: '/profile', label: '个人资料' },
          { path: '/password', label: '修改密码' }
        ]
      }
    ],
    ADMIN: [
      {
        label: '系统控制',
        items: [
          { path: '/dashboard', label: '总览' },
          { path: '/admin/users', label: '用户审批', badge: props.adminPendingUsersBadge },
          { path: '/admin/grades', label: '成绩终审' },
          { path: '/admin/export', label: '导出中心' }
        ]
      },
      {
        label: '账号',
        items: [
          { path: '/profile', label: '个人资料' },
          { path: '/password', label: '修改密码' }
        ]
      }
    ]
  }
  return menuByRole[props.user?.role] || []
})
const roleLabel = computed(() => {
  const map = {
    STUDENT: '学生端',
    TEACHER: '教师端',
    ADMIN: '管理端'
  }
  return map[props.user?.role] || '未登录'
})
</script>

<style scoped>
.full-width {
  width: 100%;
  margin-top: 14px;
}

.nav-badge {
  min-width: 22px;
  height: 22px;
  padding: 0 7px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #dc2626;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  line-height: 1;
  box-shadow: 0 8px 20px rgba(220, 38, 38, 0.28);
}
</style>

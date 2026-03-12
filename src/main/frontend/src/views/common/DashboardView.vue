<template>
  <div v-loading="loading" class="content-stack">
    <section class="dashboard-hero">
      <div>
        <div class="soft-badge">{{ hero.badge }}</div>
        <h2 class="dashboard-hero-title">{{ hero.title }}</h2>
        <p class="dashboard-hero-desc">{{ hero.description }}</p>
      </div>
      <div class="dashboard-quick-grid">
        <button
          v-for="item in quickActions"
          :key="item.label"
          type="button"
          class="dashboard-quick-card"
          @click="router.push(item.path)"
        >
          <span class="dashboard-quick-label">{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <p>{{ item.caption }}</p>
        </button>
      </div>
    </section>

    <section class="stats-grid">
      <article v-for="card in statCards" :key="card.label" class="stat-tile stat-tile-emphasis">
        <div class="stat-label">{{ card.label }}</div>
        <div class="stat-value">{{ card.value }}</div>
        <div class="stat-footnote">{{ card.note }}</div>
      </article>
    </section>

    <div class="page-grid">
      <PanelCard title="当前待办" subtitle="优先处理这些任务" style="grid-column: span 7">
        <div v-if="todoItems.length" class="dashboard-list">
          <button
            v-for="item in todoItems"
            :key="item.label"
            type="button"
            class="dashboard-list-item"
            @click="router.push(item.path)"
          >
            <div class="dashboard-list-main">
              <span class="dashboard-list-label">{{ item.label }}</span>
              <span class="dashboard-list-meta">{{ item.description }}</span>
            </div>
            <span :class="['dashboard-list-count', { danger: item.count > 0 }]">{{ item.display }}</span>
          </button>
        </div>
        <div v-else class="dashboard-empty">当前没有待处理事项，可以继续浏览其他功能模块。</div>
      </PanelCard>

      <PanelCard title="本周建议" subtitle="按当前角色推进业务" style="grid-column: span 5">
        <div class="dashboard-guide-grid">
          <article v-for="item in guidance" :key="item.title" class="dashboard-guide-item">
            <span class="dashboard-guide-step">{{ item.step }}</span>
            <h3>{{ item.title }}</h3>
            <p>{{ item.description }}</p>
          </article>
        </div>
      </PanelCard>

      <PanelCard title="个人信息" subtitle="当前登录账号概览" style="grid-column: span 6">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户名">{{ store.user?.username || '--' }}</el-descriptions-item>
          <el-descriptions-item label="角色">{{ roleLabel }}</el-descriptions-item>
          <el-descriptions-item label="账号状态">{{ statusLabel }}</el-descriptions-item>
          <el-descriptions-item label="学院">{{ store.user?.college || '--' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ store.user?.email || '--' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ store.user?.phone || '--' }}</el-descriptions-item>
        </el-descriptions>
      </PanelCard>

      <PanelCard title="系统提醒" subtitle="和当前工作区强相关的信息" style="grid-column: span 6">
        <div class="dashboard-inline-meta">
          <div class="dashboard-inline-item">
            <span>固定导航</span>
            <strong>左侧菜单常驻，适合高频业务切换</strong>
          </div>
          <div class="dashboard-inline-item">
            <span>审批链路</span>
            <strong>申请、日志、报告、成绩都已接入角色化待办</strong>
          </div>
          <div class="dashboard-inline-item">
            <span>账号治理</span>
            <strong>学生与教师注册后需管理员审批通过才能启用</strong>
          </div>
        </div>
      </PanelCard>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { fetchDashboardSummary } from '@/api/dashboard'
import { useAppStore } from '@/store'
import PanelCard from '@/components/PanelCard.vue'

const router = useRouter()
const store = useAppStore()

const loading = ref(false)
const summary = ref({})

const roleLabel = computed(() => {
  const map = {
    STUDENT: '学生',
    TEACHER: '教师',
    ADMIN: '管理员'
  }
  return map[store.user?.role] || '--'
})

const statusLabel = computed(() => {
  const map = {
    PENDING: '待审批',
    ACTIVE: '已启用',
    DISABLED: '已停用'
  }
  return map[store.user?.status] || store.user?.status || '--'
})

const hero = computed(() => {
  const role = store.user?.role
  if (role === 'ADMIN') {
    return {
      badge: '管理工作台',
      title: '把账号审批、成绩终审和导出控制放到同一条管理视图里。',
      description: '优先处理待审批用户与待终审成绩，再回到导出中心统一输出结果。'
    }
  }
  if (role === 'TEACHER') {
    return {
      badge: '教师工作台',
      title: '围绕项目、申请、日志与报告形成连续评阅路径。',
      description: '先看待审批申请，再处理日志和报告，最后回到学生进度确认整体推进情况。'
    }
  }
  return {
    badge: '学生工作台',
    title: '从项目报名到成绩确认，所有实习过程都在一个入口里完成。',
    description: '先确认申请状态，再持续填写日志和提交报告，最后回到成绩页查看终审结果。'
  }
})

const quickActions = computed(() => {
  const role = store.user?.role
  const data = summary.value || {}
  if (role === 'ADMIN') {
    return [
      {
        label: '待审批用户',
        value: formatCount(data.adminPendingUsers),
        caption: '先处理新注册账号的启用状态。',
        path: '/admin/users'
      },
      {
        label: '待终审成绩',
        value: formatCount(data.adminPendingGrades),
        caption: '教师提交成绩后在这里完成最终确认。',
        path: '/admin/grades'
      },
      {
        label: '开放项目',
        value: formatCount(data.adminOpenProjects),
        caption: '当前系统内仍可报名的项目数量。',
        path: '/admin/export'
      }
    ]
  }
  if (role === 'TEACHER') {
    return [
      {
        label: '我的项目',
        value: formatCount(data.teacherProjects),
        caption: '查看和维护当前负责的项目。',
        path: '/teacher/projects'
      },
      {
        label: '待审批申请',
        value: formatCount(data.teacherPendingApplications),
        caption: '优先处理学生提交的报名申请。',
        path: '/teacher/applications'
      },
      {
        label: '在带学生',
        value: formatCount(data.teacherActiveStudents),
        caption: '已通过申请并进入过程管理的学生数。',
        path: '/teacher/students'
      }
    ]
  }
  return [
    {
      label: '开放项目',
      value: formatCount(data.studentOpenProjects),
      caption: '当前仍可报名的项目数量。',
      path: '/student/projects'
    },
    {
      label: '我的申请',
      value: formatCount(data.studentApplications),
      caption: '查看全部申请记录和审核结果。',
      path: '/student/applications'
    },
    {
      label: '已通过申请',
      value: formatCount(data.studentApprovedApplications),
      caption: '这些申请可以继续填写日志和提交报告。',
      path: '/student/applications'
    }
  ]
})

const statCards = computed(() => {
  const role = store.user?.role
  const data = summary.value || {}
  if (role === 'ADMIN') {
    return [
      {
        label: '待审批用户',
        value: formatCount(data.adminPendingUsers),
        note: '新注册账号等待管理员处理'
      },
      {
        label: '待终审成绩',
        value: formatCount(data.adminPendingGrades),
        note: '教师成绩单等待最终确认'
      },
      {
        label: '已启用教师',
        value: formatCount(data.adminActiveTeachers),
        note: '当前可进入教师工作台的账号'
      },
      {
        label: '已启用学生',
        value: formatCount(data.adminActiveStudents),
        note: '当前可进入学生工作台的账号'
      }
    ]
  }
  if (role === 'TEACHER') {
    return [
      {
        label: '我的项目',
        value: formatCount(data.teacherProjects),
        note: '当前负责维护的项目总数'
      },
      {
        label: '待审批申请',
        value: formatCount(data.teacherPendingApplications),
        note: '学生报名后等待教师审核'
      },
      {
        label: '待评阅日志',
        value: formatCount(data.teacherPendingJournals),
        note: '已提交但尚未评分的日志'
      },
      {
        label: '待评阅报告',
        value: formatCount(data.teacherPendingReports),
        note: '已提交但尚未完成评语的报告'
      }
    ]
  }
  return [
    {
      label: '开放项目',
      value: formatCount(data.studentOpenProjects),
      note: '当前仍可报名的项目总数'
    },
    {
      label: '我的申请',
      value: formatCount(data.studentApplications),
      note: '已提交的报名申请记录'
    },
    {
      label: '实习日志',
      value: formatCount(data.studentJournals),
      note: '已经保存的日志条目'
    },
    {
      label: '已确认成绩',
      value: formatCount(data.studentConfirmedGrades),
      note: '管理员完成终审后的成绩记录'
    }
  ]
})

const todoItems = computed(() => {
  const role = store.user?.role
  const data = summary.value || {}
  if (role === 'ADMIN') {
    return [
      {
        label: '处理用户审批',
        description: '尽快处理新注册的教师和学生账号。',
        count: Number(data.adminPendingUsers || 0),
        display: formatCount(data.adminPendingUsers),
        path: '/admin/users'
      },
      {
        label: '完成成绩终审',
        description: '教师已提交的成绩记录等待终审。',
        count: Number(data.adminPendingGrades || 0),
        display: formatCount(data.adminPendingGrades),
        path: '/admin/grades'
      }
    ].filter((item) => item.count > 0)
  }
  if (role === 'TEACHER') {
    return [
      {
        label: '审批学生申请',
        description: '先处理待审核的报名申请。',
        count: Number(data.teacherPendingApplications || 0),
        display: formatCount(data.teacherPendingApplications),
        path: '/teacher/applications'
      },
      {
        label: '评阅实习日志',
        description: '给已提交日志补评分和评语。',
        count: Number(data.teacherPendingJournals || 0),
        display: formatCount(data.teacherPendingJournals),
        path: '/teacher/journals'
      },
      {
        label: '评阅实习报告',
        description: '继续处理学生提交的最终报告。',
        count: Number(data.teacherPendingReports || 0),
        display: formatCount(data.teacherPendingReports),
        path: '/teacher/reports'
      }
    ].filter((item) => item.count > 0)
  }
  return [
    {
      label: '继续项目报名',
      description: '先筛选开放项目并提交申请。',
      count: Number(data.studentOpenProjects || 0),
      display: formatCount(data.studentOpenProjects),
      path: '/student/projects'
    },
    {
      label: '完善日志记录',
      description: '持续补充实习周志，避免后续集中补写。',
      count: Number(data.studentJournals || 0),
      display: formatCount(data.studentJournals),
      path: '/student/journals'
    },
    {
      label: '检查报告状态',
      description: '确认是否已经提交报告并等待教师评阅。',
      count: Number(data.studentReports || 0),
      display: formatCount(data.studentReports),
      path: '/student/reports'
    }
  ]
})

const guidance = computed(() => {
  const role = store.user?.role
  if (role === 'ADMIN') {
    return [
      {
        step: '01',
        title: '先清理审批积压',
        description: '优先处理用户审批和成绩终审，保证主流程不会卡在最后一环。'
      },
      {
        step: '02',
        title: '保持账号状态干净',
        description: '停用异常账号，确保启用状态与实际角色一致。'
      },
      {
        step: '03',
        title: '再做集中导出',
        description: '在审批完成后进入导出中心，避免导出结果和系统状态不一致。'
      }
    ]
  }
  if (role === 'TEACHER') {
    return [
      {
        step: '01',
        title: '维护项目入口',
        description: '先保证项目信息清晰，学生申请才不会集中产生无效沟通。'
      },
      {
        step: '02',
        title: '及时处理待办',
        description: '申请、日志、报告的反馈越及时，学生后续补充成本越低。'
      },
      {
        step: '03',
        title: '关注整体进度',
        description: '回到学生进度页看整体推进情况，避免遗漏未完成学生。'
      }
    ]
  }
  return [
    {
      step: '01',
      title: '先确认申请状态',
      description: '只有申请通过后，后续日志、报告和成绩才会进入正式流程。'
    },
    {
      step: '02',
      title: '持续补日志',
      description: '按周维护日志，后面写报告时会更容易回溯全过程。'
    },
    {
      step: '03',
      title: '按节点提交报告',
      description: '报告提交后可以回到工作台持续跟进教师评阅和成绩终审状态。'
    }
  ]
})

onMounted(loadDashboard)

watch(() => store.role, loadDashboard)

async function loadDashboard() {
  if (!store.token) {
    return
  }
  loading.value = true
  try {
    const [dashboard] = await Promise.all([
      fetchDashboardSummary(),
      store.refreshReminders()
    ])
    summary.value = dashboard || {}
  } finally {
    loading.value = false
  }
}

function formatCount(value) {
  const count = Number(value || 0)
  return count > 99 ? '99+' : String(count)
}
</script>

import { createRouter, createWebHashHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAppStore } from '@/store'
import LayoutShell from '@/components/LayoutShell.vue'
import LoginView from '@/views/login/LoginView.vue'
import RegisterView from '@/views/register/RegisterView.vue'
import DashboardView from '@/views/common/DashboardView.vue'
import ProfileEdit from '@/views/common/ProfileEdit.vue'
import PasswordReset from '@/views/common/PasswordReset.vue'
import ProjectList from '@/views/student/ProjectList.vue'
import MyApplications from '@/views/student/MyApplications.vue'
import JournalEditor from '@/views/student/JournalEditor.vue'
import ReportUpload from '@/views/student/ReportUpload.vue'
import MyGrades from '@/views/student/MyGrades.vue'
import ProjectManage from '@/views/teacher/ProjectManage.vue'
import ApplicationReview from '@/views/teacher/ApplicationReview.vue'
import JournalReview from '@/views/teacher/JournalReview.vue'
import ReportReview from '@/views/teacher/ReportReview.vue'
import StudentProgress from '@/views/teacher/StudentProgress.vue'
import UserApproval from '@/views/admin/UserApproval.vue'
import GradeApproval from '@/views/admin/GradeApproval.vue'
import ExportCenter from '@/views/admin/ExportCenter.vue'

const routes = [
  {
    path: '/login',
    component: LoginView,
    meta: { public: true, title: '登录' }
  },
  {
    path: '/register',
    component: RegisterView,
    meta: { public: true, title: '注册' }
  },
  {
    path: '/',
    component: LayoutShell,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: DashboardView, meta: { title: '工作台', roles: ['STUDENT', 'TEACHER', 'ADMIN'] } },
      { path: 'profile', component: ProfileEdit, meta: { title: '个人资料', roles: ['STUDENT', 'TEACHER', 'ADMIN'] } },
      { path: 'password', component: PasswordReset, meta: { title: '修改密码', roles: ['STUDENT', 'TEACHER', 'ADMIN'] } },
      { path: 'student/projects', component: ProjectList, meta: { title: '项目报名', roles: ['STUDENT'] } },
      { path: 'student/applications', component: MyApplications, meta: { title: '我的申请', roles: ['STUDENT'] } },
      { path: 'student/journals', component: JournalEditor, meta: { title: '实习日志', roles: ['STUDENT'] } },
      { path: 'student/reports', component: ReportUpload, meta: { title: '实习报告', roles: ['STUDENT'] } },
      { path: 'student/grades', component: MyGrades, meta: { title: '我的成绩', roles: ['STUDENT'] } },
      { path: 'teacher/projects', component: ProjectManage, meta: { title: '项目管理', roles: ['TEACHER'] } },
      { path: 'teacher/applications', component: ApplicationReview, meta: { title: '申请审批', roles: ['TEACHER'] } },
      { path: 'teacher/journals', component: JournalReview, meta: { title: '日志评阅', roles: ['TEACHER'] } },
      { path: 'teacher/reports', component: ReportReview, meta: { title: '报告评阅', roles: ['TEACHER'] } },
      { path: 'teacher/students', component: StudentProgress, meta: { title: '学生进度', roles: ['TEACHER'] } },
      { path: 'admin/users', component: UserApproval, meta: { title: '用户审批', roles: ['ADMIN'] } },
      { path: 'admin/grades', component: GradeApproval, meta: { title: '成绩终审', roles: ['ADMIN'] } },
      { path: 'admin/export', component: ExportCenter, meta: { title: '导出中心', roles: ['ADMIN'] } }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach(async (to) => {
  const appStore = useAppStore()
  document.title = `${to.meta.title || '工作台'} - 学生实习与实践教学管理系统`

  if (to.meta.public) {
    if (appStore.isAuthenticated) {
      return '/dashboard'
    }
    return true
  }

  if (!appStore.isAuthenticated) {
    return '/login'
  }

  await appStore.ensureMe()
  const roles = to.meta.roles || []
  if (roles.length && !roles.includes(appStore.role)) {
    ElMessage.warning('当前账号无权访问该页面')
    return '/dashboard'
  }
  return true
})

export default router

<template>
  <div class="auth-shell auth-shell-login">
    <section class="auth-brand auth-brand-login">
      <div class="auth-brand-top">
        <div>
          <span class="auth-brand-badge">OPERATIONS CONSOLE</span>
          <p class="auth-kicker">Internship Workflow Center</p>
          <h1>让审批、评阅与终审始终落在同一条业务视图里。</h1>
          <p class="auth-brand-lead">统一入口覆盖项目发布、申请审批、过程记录、报告评阅与成绩终审，适合面向学生、教师和管理员的长期迭代后台。</p>
        </div>
      </div>
      <div class="auth-metric-row">
        <article v-for="item in highlights" :key="item.label" class="auth-metric-card">
          <span class="auth-metric-label">{{ item.label }}</span>
          <strong class="auth-metric-value">{{ item.value }}</strong>
          <p>{{ item.description }}</p>
        </article>
      </div>
      <div class="auth-process-card">
        <div class="auth-process-head">
          <div>
            <span class="auth-process-label">业务主线</span>
            <h3>登录后直达角色工作台</h3>
          </div>
          <span class="auth-process-state">Always on</span>
        </div>
        <div class="auth-process-list">
          <article v-for="step in workflow" :key="step.step" class="auth-process-item">
            <span class="auth-process-step">{{ step.step }}</span>
            <h4>{{ step.title }}</h4>
            <p>{{ step.description }}</p>
          </article>
        </div>
      </div>
    </section>

    <section class="auth-panel-wrap">
      <div class="auth-panel auth-panel-login">
        <p class="auth-panel-kicker">身份认证</p>
        <h2>欢迎回来</h2>
        <p>输入账号密码，进入对应角色的工作台与待办列表。</p>
        <el-form :model="form" label-position="top" class="auth-form" @submit.prevent>
          <el-form-item label="用户名">
            <el-input v-model="form.username" placeholder="请输入用户名" autocomplete="username" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input
              v-model="form.password"
              type="password"
              show-password
              placeholder="请输入密码"
              autocomplete="current-password"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-button class="auth-primary-btn" type="primary" :loading="loading" @click="handleLogin">登录</el-button>
          <el-button class="auth-secondary-btn" @click="router.push('/register')">去注册</el-button>
        </el-form>
        <div class="auth-panel-note">
          <span class="auth-note-dot" />
          <span>学生和教师新账号提交后需要管理员审批，审批通过后方可登录。</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/api/auth'
import { useAppStore } from '@/store'

const router = useRouter()
const store = useAppStore()
const loading = ref(false)
const form = reactive({
  username: '',
  password: ''
})

const highlights = [
  {
    label: '角色入口',
    value: '3',
    description: '学生、教师、管理员共用一套认证入口，权限边界清晰。'
  },
  {
    label: '核心流程',
    value: '5',
    description: '申请、日志、报告、评分、终审在同一后台链路中协同。'
  },
  {
    label: '导航方式',
    value: '1',
    description: '登录后保持固定导航和内容优先的工作区布局。'
  }
]

const workflow = [
  {
    step: '01',
    title: '进入角色工作台',
    description: '按身份加载对应导航、提醒和快捷操作。'
  },
  {
    step: '02',
    title: '处理待办事项',
    description: '围绕审批、评阅和终审形成连续处理路径。'
  },
  {
    step: '03',
    title: '沉淀过程结果',
    description: '日志、报告和成绩在同一业务链条内归档留痕。'
  }
]

async function handleLogin() {
  loading.value = true
  try {
    const data = await login(form)
    store.setAuth(data)
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

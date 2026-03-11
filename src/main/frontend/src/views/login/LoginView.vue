<template>
  <div class="auth-shell">
    <section class="auth-brand">
      <div>
        <span class="auth-brand-badge">INTERNSHIP MANAGEMENT</span>
        <h1>学生实习与实践教学管理系统</h1>
        <p>围绕“项目发布、申请审批、过程记录、报告评阅、成绩终审”构建一条闭环链路，服务学生、教师与管理员三个角色。</p>
      </div>
      <p>固定侧边栏风格强调导航常驻和内容优先，适合你现在这个后台系统的长期迭代。</p>
    </section>

    <section class="auth-panel-wrap">
      <div class="auth-panel">
        <h2>欢迎回来</h2>
        <p>输入账号密码进入后台。</p>
        <el-form :model="form" label-position="top" @submit.prevent>
          <el-form-item label="用户名">
            <el-input v-model="form.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
          </el-form-item>
          <el-button type="primary" style="width: 100%" :loading="loading" @click="handleLogin">登录</el-button>
          <el-button style="width: 100%; margin-top: 12px" @click="router.push('/register')">去注册</el-button>
        </el-form>
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

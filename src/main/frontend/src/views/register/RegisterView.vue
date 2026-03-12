<template>
  <div class="auth-shell">
    <section class="auth-brand">
      <div class="auth-brand-top">
        <div>
          <span class="auth-brand-badge">ACCOUNT REGISTRATION</span>
          <p class="auth-kicker">New Account Onboarding</p>
          <h1>先创建账号，再进入审批流。</h1>
          <p class="auth-brand-lead">学生和教师账号提交后会进入管理员审批队列，通过后自动启用对应角色的后台入口权限。</p>
        </div>
      </div>
      <div class="auth-inline-tips">
        <div class="auth-inline-tip">学生建议完整填写学号、学院、专业和年级信息，便于后续项目匹配与成绩归档。</div>
        <div class="auth-inline-tip">教师账号审批通过后即可进入项目发布、申请审批、日志评阅和报告评阅流程。</div>
        <div class="auth-inline-tip">注册成功后返回登录页，待管理员审核通过后再完成身份认证。</div>
      </div>
    </section>

    <section class="auth-panel-wrap">
      <div class="auth-panel">
        <p class="auth-panel-kicker">账号创建</p>
        <h2>创建账号</h2>
        <p>填写基础信息，提交后等待管理员审批。</p>
        <el-form :model="form" label-position="top" class="auth-form" @submit.prevent>
          <div class="form-grid">
            <el-form-item label="用户名">
              <el-input v-model="form.username" placeholder="用户名" />
            </el-form-item>
            <el-form-item label="真实姓名">
              <el-input v-model="form.realName" placeholder="真实姓名" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="form.password" type="password" show-password />
            </el-form-item>
            <el-form-item label="角色">
              <el-select v-model="form.role" style="width: 100%">
                <el-option label="学生" value="STUDENT" />
                <el-option label="教师" value="TEACHER" />
              </el-select>
            </el-form-item>
            <el-form-item label="学号/工号">
              <el-input v-model="form.studentNo" placeholder="学生填学号，教师可留空" />
            </el-form-item>
            <el-form-item label="学院">
              <el-input v-model="form.college" />
            </el-form-item>
            <el-form-item label="专业">
              <el-input v-model="form.major" />
            </el-form-item>
            <el-form-item label="年级">
              <el-input v-model="form.grade" />
            </el-form-item>
            <el-form-item class="full" label="邮箱">
              <el-input v-model="form.email" />
            </el-form-item>
          </div>
          <el-button class="auth-primary-btn" type="primary" :loading="loading" @click="handleRegister">提交注册</el-button>
          <el-button class="auth-secondary-btn" @click="router.push('/login')">返回登录</el-button>
        </el-form>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()
const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
  realName: '',
  role: 'STUDENT',
  studentNo: '',
  college: '',
  major: '',
  grade: '',
  className: '',
  phone: '',
  email: ''
})

async function handleRegister() {
  loading.value = true
  try {
    await register(form)
    ElMessage.success('注册成功，请等待管理员审批')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

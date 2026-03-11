<template>
  <div class="auth-shell">
    <section class="auth-brand">
      <div>
        <span class="auth-brand-badge">ACCOUNT REGISTRATION</span>
        <h1>先注册，再进入审批流</h1>
        <p>学生和教师账号注册后默认进入待审批状态，管理员在后台审核通过后才能登录系统。</p>
      </div>
      <p>建议先创建一个学生账号和一个教师账号，再使用默认管理员 `admin / 123456` 完成审批。</p>
    </section>

    <section class="auth-panel-wrap">
      <div class="auth-panel">
        <h2>创建账号</h2>
        <p>填写基础信息，提交后等待管理员审批。</p>
        <el-form :model="form" label-position="top" @submit.prevent>
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
          <el-button type="primary" style="width: 100%" :loading="loading" @click="handleRegister">提交注册</el-button>
          <el-button style="width: 100%; margin-top: 12px" @click="router.push('/login')">返回登录</el-button>
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

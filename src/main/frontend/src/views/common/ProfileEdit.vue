<template>
  <PanelCard title="个人资料" subtitle="修改后即时写回当前账号信息">
    <el-form :model="form" label-position="top">
      <div class="form-grid">
        <el-form-item label="真实姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
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
        <el-form-item label="班级">
          <el-input v-model="form.className" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="头像 URL">
          <el-input v-model="form.avatar" />
        </el-form-item>
      </div>
      <el-button type="primary" :loading="loading" @click="handleSave">保存资料</el-button>
    </el-form>
  </PanelCard>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useAppStore } from '@/store'
import { updateProfile } from '@/api/user'
import PanelCard from '@/components/PanelCard.vue'

const store = useAppStore()
const loading = ref(false)
const form = reactive({
  realName: '',
  college: '',
  major: '',
  grade: '',
  className: '',
  phone: '',
  email: '',
  avatar: ''
})

watch(
  () => store.user,
  (user) => {
    Object.assign(form, {
      realName: user?.realName || '',
      college: user?.college || '',
      major: user?.major || '',
      grade: user?.grade || '',
      className: user?.className || '',
      phone: user?.phone || '',
      email: user?.email || '',
      avatar: user?.avatar || ''
    })
  },
  { immediate: true }
)

async function handleSave() {
  loading.value = true
  try {
    const user = await updateProfile(form)
    store.setUser(user)
    ElMessage.success('资料已更新')
  } finally {
    loading.value = false
  }
}
</script>

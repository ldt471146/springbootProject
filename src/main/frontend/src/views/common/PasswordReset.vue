<template>
  <PanelCard title="修改密码" subtitle="当前页面只处理当前登录账号密码">
    <el-form :model="form" label-position="top" style="max-width: 520px">
      <el-form-item label="原密码">
        <el-input v-model="form.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码">
        <el-input v-model="form.newPassword" type="password" show-password />
      </el-form-item>
      <el-button type="primary" :loading="loading" @click="handleSubmit">更新密码</el-button>
    </el-form>
  </PanelCard>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { updatePassword } from '@/api/user'
import PanelCard from '@/components/PanelCard.vue'

const loading = ref(false)
const form = reactive({
  oldPassword: '',
  newPassword: ''
})

async function handleSubmit() {
  loading.value = true
  try {
    await updatePassword(form)
    ElMessage.success('密码修改成功')
    form.oldPassword = ''
    form.newPassword = ''
  } finally {
    loading.value = false
  }
}
</script>

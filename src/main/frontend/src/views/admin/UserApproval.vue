<template>
  <div class="content-stack">
    <PanelCard title="待审批用户" subtitle="新注册学生和教师账号会先进入这里">
      <el-table :data="pendingRecords" border>
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="role" label="角色" width="120" />
        <el-table-column prop="college" label="学院" min-width="160" />
        <el-table-column prop="createTime" label="注册时间" width="170" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="success" link @click="handleApprove(row.id, 'APPROVED')">通过</el-button>
            <el-button type="danger" link @click="handleApprove(row.id, 'REJECTED')">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </PanelCard>

    <PanelCard title="全部用户" subtitle="支持启停账号与管理员重置密码">
      <el-table :data="allRecords" border>
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="role" label="角色" width="120" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="college" label="学院" min-width="160" />
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button type="primary" link @click="toggleStatus(row)">
              {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
            </el-button>
            <el-button type="warning" link @click="handleResetPassword(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>
    </PanelCard>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PanelCard from '@/components/PanelCard.vue'
import { useAppStore } from '@/store'
import { approveUser, listPendingUsers, listUsers, resetPassword, updateUserStatus } from '@/api/user'

const store = useAppStore()
const pendingRecords = ref([])
const allRecords = ref([])

async function loadData() {
  const [pendingData, allData] = await Promise.all([
    listPendingUsers({ page: 1, size: 50 }),
    listUsers({ page: 1, size: 100 })
  ])
  pendingRecords.value = pendingData.records
  allRecords.value = allData.records
  store.setAdminPendingUsersCount(pendingData.total)
}

async function handleApprove(id, status) {
  await approveUser(id, { status, comment: '' })
  ElMessage.success('审批已完成')
  await loadData()
}

async function toggleStatus(row) {
  const status = row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  await updateUserStatus(row.id, { status })
  ElMessage.success('用户状态已更新')
  await loadData()
}

async function handleResetPassword(row) {
  const result = await ElMessageBox.prompt(`为 ${row.realName} 设置新密码`, '重置密码', {
    inputValue: '123456',
    confirmButtonText: '确认',
    cancelButtonText: '取消'
  })
  await resetPassword(row.id, { newPassword: result.value })
  ElMessage.success('密码已重置')
}

onMounted(loadData)
</script>

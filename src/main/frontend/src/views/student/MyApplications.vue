<template>
  <PanelCard title="我的申请" subtitle="查看申请状态并处理待审批记录">
    <el-table :data="records" border>
      <el-table-column prop="projectTitle" label="项目" min-width="180" />
      <el-table-column prop="status" label="审批状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phase" label="实习阶段" width="120" />
      <el-table-column prop="reviewComment" label="审批意见" min-width="180" />
      <el-table-column prop="createTime" label="申请时间" width="170" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING'" type="danger" link @click="handleWithdraw(row.id)">撤回</el-button>
        </template>
      </el-table-column>
    </el-table>
  </PanelCard>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import PanelCard from '@/components/PanelCard.vue'
import { listMyApplications, withdrawApplication } from '@/api/application'

const records = ref([])

function statusType(status) {
  return status === 'APPROVED' ? 'success' : status === 'REJECTED' ? 'danger' : 'warning'
}

async function loadData() {
  const data = await listMyApplications({ page: 1, size: 50 })
  records.value = data.records
}

async function handleWithdraw(id) {
  await ElMessageBox.confirm('确认撤回这条申请？', '撤回申请', { type: 'warning' })
  await withdrawApplication(id)
  ElMessage.success('申请已撤回')
  loadData()
}

onMounted(loadData)
</script>

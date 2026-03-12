<template>
  <div class="content-stack">
    <PanelCard title="申请审批" subtitle="按项目维度审批学生报名">
      <div class="toolbar">
        <div class="toolbar-group">
          <el-select v-model="selectedProjectId" placeholder="选择项目" style="width: 280px" @change="loadApplications">
            <el-option v-for="item in projects" :key="item.id" :label="item.title" :value="item.id" />
          </el-select>
          <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 160px" @change="loadApplications">
            <el-option label="待审批" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </div>
      </div>

      <el-table :data="records" border>
        <el-table-column prop="studentName" label="学生" width="120" />
        <el-table-column prop="studentNo" label="学号" width="130" />
        <el-table-column prop="applyReason" label="申请理由" min-width="220" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="reviewComment" label="审批意见" min-width="180" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" type="success" link @click="openReview(row, 'APPROVED')">通过</el-button>
            <el-button v-if="row.status === 'PENDING'" type="danger" link @click="openReview(row, 'REJECTED')">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </PanelCard>

    <el-dialog v-model="dialogVisible" title="审批申请" width="520px">
      <el-form :model="reviewForm" label-position="top">
        <el-form-item label="审批结论">
          <el-input :model-value="reviewForm.status" disabled />
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="reviewForm.comment" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import PanelCard from '@/components/PanelCard.vue'
import { useAppStore } from '@/store'
import { listMyProjects } from '@/api/project'
import { listProjectApplications, reviewApplication } from '@/api/application'

const store = useAppStore()
const projects = ref([])
const records = ref([])
const selectedProjectId = ref('')
const statusFilter = ref('')
const dialogVisible = ref(false)
const currentId = ref(null)
const reviewForm = reactive({
  status: 'APPROVED',
  comment: ''
})

async function loadProjects() {
  const data = await listMyProjects({ page: 1, size: 50 })
  projects.value = data.records
  if (!selectedProjectId.value && projects.value.length) {
    selectedProjectId.value = projects.value[0].id
  }
  loadApplications()
}

async function loadApplications() {
  if (!selectedProjectId.value) {
    records.value = []
    return
  }
  const data = await listProjectApplications(selectedProjectId.value, {
    page: 1,
    size: 50,
    status: statusFilter.value
  })
  records.value = data.records
}

function openReview(row, status) {
  currentId.value = row.id
  reviewForm.status = status
  reviewForm.comment = ''
  dialogVisible.value = true
}

async function submitReview() {
  await reviewApplication(currentId.value, reviewForm)
  ElMessage.success('审批已提交')
  dialogVisible.value = false
  await Promise.all([loadApplications(), store.refreshReminders()])
}

onMounted(async () => {
  await Promise.all([loadProjects(), store.refreshReminders()])
})
</script>

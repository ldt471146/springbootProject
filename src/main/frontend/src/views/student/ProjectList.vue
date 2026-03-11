<template>
  <div class="content-stack">
    <PanelCard title="可报名项目" subtitle="根据关键词、学院、年级筛选教师已发布项目">
      <div class="toolbar">
        <div class="toolbar-group">
          <el-input v-model="filters.keyword" placeholder="搜索项目/单位" clearable style="width: 220px" />
          <el-input v-model="filters.college" placeholder="学院" clearable style="width: 180px" />
          <el-input v-model="filters.grade" placeholder="年级" clearable style="width: 160px" />
          <el-button type="primary" @click="loadProjects">查询</el-button>
        </div>
      </div>

      <el-table :data="records" border>
        <el-table-column prop="title" label="项目标题" min-width="180" />
        <el-table-column prop="teacherName" label="指导教师" width="120" />
        <el-table-column prop="company" label="实习单位" min-width="160" />
        <el-table-column prop="semester" label="学期" width="130" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openApply(row)">申请</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 16px; display: flex; justify-content: flex-end">
        <el-pagination
          background
          layout="prev, pager, next"
          :current-page="pagination.page"
          :page-size="pagination.size"
          :total="pagination.total"
          @current-change="handlePageChange"
        />
      </div>
    </PanelCard>

    <el-dialog v-model="dialogVisible" title="提交项目申请" width="520px">
      <el-form :model="applyForm" label-position="top">
        <el-form-item label="项目标题">
          <el-input :model-value="currentProject?.title" disabled />
        </el-form-item>
        <el-form-item label="申请理由">
          <el-input v-model="applyForm.applyReason" type="textarea" :rows="5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import PanelCard from '@/components/PanelCard.vue'
import { listProjects } from '@/api/project'
import { createApplication } from '@/api/application'

const records = ref([])
const dialogVisible = ref(false)
const submitting = ref(false)
const currentProject = ref(null)
const filters = reactive({
  keyword: '',
  college: '',
  grade: ''
})
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})
const applyForm = reactive({
  applyReason: ''
})

async function loadProjects() {
  const data = await listProjects({
    page: pagination.page,
    size: pagination.size,
    ...filters
  })
  records.value = data.records
  pagination.total = data.total
}

function handlePageChange(page) {
  pagination.page = page
  loadProjects()
}

function openApply(project) {
  currentProject.value = project
  applyForm.applyReason = ''
  dialogVisible.value = true
}

async function submitApply() {
  submitting.value = true
  try {
    await createApplication({
      projectId: currentProject.value.id,
      applyReason: applyForm.applyReason
    })
    ElMessage.success('申请已提交')
    dialogVisible.value = false
  } finally {
    submitting.value = false
  }
}

onMounted(loadProjects)
</script>

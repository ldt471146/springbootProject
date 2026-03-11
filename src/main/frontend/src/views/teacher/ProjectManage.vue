<template>
  <div class="content-stack">
    <PanelCard title="项目管理" subtitle="创建项目并导出参与学生名单">
      <template #extra>
        <el-button type="primary" @click="openDialog()">新建项目</el-button>
      </template>

      <el-table :data="records" border>
        <el-table-column prop="title" label="项目" min-width="180" />
        <el-table-column prop="company" label="单位" min-width="140" />
        <el-table-column prop="semester" label="学期" width="130" />
        <el-table-column prop="participantCount" label="已通过人数" width="110" />
        <el-table-column prop="status" label="状态" width="110" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button type="warning" link @click="handleClose(row.id)">关闭</el-button>
            <el-button type="success" link @click="handleExport(row.id, false)">导出 Excel</el-button>
            <el-button type="success" link @click="handleExport(row.id, true)">导出 PDF</el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </PanelCard>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑项目' : '新建项目'" width="760px">
      <el-form :model="form" label-position="top">
        <div class="form-grid">
          <el-form-item label="项目标题" class="full">
            <el-input v-model="form.title" />
          </el-form-item>
          <el-form-item label="实习单位">
            <el-input v-model="form.company" />
          </el-form-item>
          <el-form-item label="实习地点">
            <el-input v-model="form.location" />
          </el-form-item>
          <el-form-item label="学期">
            <el-input v-model="form.semester" />
          </el-form-item>
          <el-form-item label="学分类型">
            <el-input v-model="form.creditType" />
          </el-form-item>
          <el-form-item label="最大人数">
            <el-input-number v-model="form.maxStudents" :min="0" style="width: 100%" />
          </el-form-item>
          <el-form-item label="限定学院">
            <el-input v-model="form.collegeLimit" />
          </el-form-item>
          <el-form-item label="限定年级">
            <el-input v-model="form.gradeLimit" />
          </el-form-item>
          <el-form-item label="开始日期">
            <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
          <el-form-item label="结束日期">
            <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
          <el-form-item label="报名截止">
            <el-date-picker v-model="form.enrollDeadline" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
          <el-form-item label="项目状态">
            <el-select v-model="form.status" style="width: 100%">
              <el-option label="开放" value="OPEN" />
              <el-option label="关闭" value="CLOSED" />
              <el-option label="归档" value="ARCHIVED" />
            </el-select>
          </el-form-item>
          <el-form-item label="项目描述" class="full">
            <el-input v-model="form.description" type="textarea" :rows="5" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存项目</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PanelCard from '@/components/PanelCard.vue'
import { archiveProject, closeProject, createProject, deleteProject, listMyProjects, updateProject } from '@/api/project'
import { exportProjectParticipants, exportProjectParticipantsPdf } from '@/api/export'
import { saveBlobResponse } from '@/utils/download'

const records = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const saving = ref(false)
const form = reactive({
  title: '',
  description: '',
  company: '',
  location: '',
  maxStudents: 0,
  collegeLimit: '',
  gradeLimit: '',
  startDate: '',
  endDate: '',
  enrollDeadline: '',
  semester: '',
  creditType: '',
  status: 'OPEN'
})

async function loadData() {
  const data = await listMyProjects({ page: 1, size: 50 })
  records.value = data.records
}

function openDialog(row) {
  editingId.value = row?.id || null
  Object.assign(form, {
    title: row?.title || '',
    description: row?.description || '',
    company: row?.company || '',
    location: row?.location || '',
    maxStudents: row?.maxStudents || 0,
    collegeLimit: row?.collegeLimit || '',
    gradeLimit: row?.gradeLimit || '',
    startDate: row?.startDate || '',
    endDate: row?.endDate || '',
    enrollDeadline: row?.enrollDeadline || '',
    semester: row?.semester || '',
    creditType: row?.creditType || '',
    status: row?.status || 'OPEN'
  })
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    if (editingId.value) {
      await updateProject(editingId.value, form)
    } else {
      await createProject(form)
    }
    ElMessage.success('项目已保存')
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

async function handleClose(id) {
  await closeProject(id)
  ElMessage.success('项目已关闭')
  loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确认删除这个项目？', '删除项目', { type: 'warning' })
  await deleteProject(id)
  ElMessage.success('项目已删除')
  loadData()
}

async function handleExport(id, pdf) {
  const response = pdf ? await exportProjectParticipantsPdf(id) : await exportProjectParticipants(id)
  saveBlobResponse(response, pdf ? `participants-${id}.pdf` : `participants-${id}.xlsx`)
}

onMounted(loadData)
</script>

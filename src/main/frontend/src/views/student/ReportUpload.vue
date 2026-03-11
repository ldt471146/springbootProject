<template>
  <div class="content-stack">
    <PanelCard title="实习报告" subtitle="先上传文件，再绑定到报告记录">
      <template #extra>
        <el-button type="primary" @click="openDialog()">新建报告</el-button>
      </template>

      <el-table :data="records" border>
        <el-table-column prop="projectTitle" label="项目" min-width="160" />
        <el-table-column prop="title" label="报告标题" min-width="180" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="teacherScore" label="分数" width="100" />
        <el-table-column prop="aiFlag" label="AI 标记" width="140" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-link :href="`/api/report/file/${row.id}`" target="_blank" type="primary">查看文件</el-link>
          </template>
        </el-table-column>
      </el-table>
    </PanelCard>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑报告' : '新建报告'" width="720px">
      <el-form :model="form" label-position="top">
        <div class="form-grid">
          <el-form-item label="所属申请">
            <el-select v-model="form.applicationId" style="width: 100%">
              <el-option v-for="item in approvedApplications" :key="item.id" :label="item.projectTitle" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="学期">
            <el-input v-model="form.semester" />
          </el-form-item>
          <el-form-item label="报告标题" class="full">
            <el-input v-model="form.title" />
          </el-form-item>
          <el-form-item label="学分类型">
            <el-input v-model="form.creditType" />
          </el-form-item>
          <el-form-item label="已上传文件">
            <el-input v-model="form.fileUrl" disabled />
          </el-form-item>
          <el-form-item label="摘要" class="full">
            <el-input v-model="form.summary" type="textarea" :rows="5" />
          </el-form-item>
          <el-form-item label="上传文件" class="full">
            <input type="file" @change="handleFileChange" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存报告</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import PanelCard from '@/components/PanelCard.vue'
import { listMyApplications } from '@/api/application'
import { createReport, listMyReports, updateReport, uploadReportFile } from '@/api/report'

const records = ref([])
const applications = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)
const selectedFile = ref(null)
const form = reactive({
  applicationId: '',
  title: '',
  summary: '',
  fileUrl: '',
  semester: '',
  creditType: ''
})

const approvedApplications = computed(() => applications.value.filter((item) => item.status === 'APPROVED'))

async function loadData() {
  const [reportData, applicationData] = await Promise.all([
    listMyReports({ page: 1, size: 50 }),
    listMyApplications({ page: 1, size: 50 })
  ])
  records.value = reportData.records
  applications.value = applicationData.records
}

function openDialog(row) {
  editingId.value = row?.id || null
  selectedFile.value = null
  Object.assign(form, {
    applicationId: row?.applicationId || '',
    title: row?.title || '',
    summary: row?.summary || '',
    fileUrl: row?.fileUrl || '',
    semester: row?.semester || '',
    creditType: row?.creditType || ''
  })
  dialogVisible.value = true
}

function handleFileChange(event) {
  selectedFile.value = event.target.files?.[0] || null
}

async function ensureFileUploaded() {
  if (!selectedFile.value) {
    return
  }
  const uploaded = await uploadReportFile(selectedFile.value)
  form.fileUrl = uploaded.fileUrl
}

async function handleSave() {
  saving.value = true
  try {
    await ensureFileUploaded()
    if (editingId.value) {
      await updateReport(editingId.value, form)
    } else {
      await createReport(form)
    }
    ElMessage.success('报告已保存')
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

onMounted(loadData)
</script>

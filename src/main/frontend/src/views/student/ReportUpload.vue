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
        <el-table-column label="附加材料" width="110">
          <template #default="{ row }">
            {{ row.attachments?.length || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button type="primary" link @click="handleOpenReportFile(row.id)">查看文件</el-button>
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
          <el-form-item label="附加材料" class="full">
            <input type="file" multiple @change="handleAttachmentChange" />
            <div class="hint-text" style="margin-top: 8px">
              可补充证明、照片、单位评价扫描件等材料。保存报告后会自动绑定到当前记录。
            </div>
          </el-form-item>
        </div>
      </el-form>
      <div v-if="attachmentRecords.length" style="margin-top: 12px">
        <div class="hint-text" style="margin-bottom: 10px">已绑定材料</div>
        <el-table :data="attachmentRecords" border size="small">
          <el-table-column prop="originalName" label="文件名" min-width="220" />
          <el-table-column prop="contentType" label="类型" min-width="140" />
          <el-table-column prop="fileSize" label="大小" width="120">
            <template #default="{ row }">
              {{ formatFileSize(row.fileSize) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button type="primary" link @click="handleOpenAttachment(row.id)">查看</el-button>
              <el-button type="danger" link @click="handleDeleteAttachment(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存报告</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="previewVisible"
      :title="previewState.fileName || '文件预览'"
      width="min(1120px, 92vw)"
      top="4vh"
      destroy-on-close
      @closed="resetPreview"
    >
      <FilePreview
        :mode="previewState.mode"
        :src="previewState.src"
        :html-content="previewState.htmlContent"
        :file-name="previewState.fileName"
        :message="previewState.message"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import FilePreview from '@/components/FilePreview.vue'
import PanelCard from '@/components/PanelCard.vue'
import { createObjectUrlFromResponse } from '@/utils/download'
import { listMyApplications } from '@/api/application'
import {
  createReport,
  deleteReportAttachment,
  fetchReport,
  fetchReportAttachmentFile,
  fetchReportAttachmentPreview,
  fetchReportFile,
  fetchReportFilePreview,
  listMyReports,
  updateReport,
  uploadReportAttachment,
  uploadReportFile
} from '@/api/report'

const records = ref([])
const applications = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)
const selectedFile = ref(null)
const selectedAttachments = ref([])
const attachmentRecords = ref([])
const previewVisible = ref(false)
const previewState = reactive({
  mode: '',
  src: '',
  htmlContent: '',
  fileName: '',
  message: ''
})
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

async function openDialog(row) {
  editingId.value = row?.id || null
  selectedFile.value = null
  selectedAttachments.value = []
  attachmentRecords.value = []
  const detail = row?.id ? await fetchReport(row.id) : null
  Object.assign(form, {
    applicationId: detail?.applicationId || row?.applicationId || '',
    title: detail?.title || row?.title || '',
    summary: detail?.summary || row?.summary || '',
    fileUrl: detail?.fileUrl || row?.fileUrl || '',
    semester: detail?.semester || row?.semester || '',
    creditType: detail?.creditType || row?.creditType || ''
  })
  attachmentRecords.value = detail?.attachments || []
  dialogVisible.value = true
}

function handleFileChange(event) {
  selectedFile.value = event.target.files?.[0] || null
}

function handleAttachmentChange(event) {
  selectedAttachments.value = Array.from(event.target.files || [])
}

async function ensureFileUploaded() {
  if (!selectedFile.value) {
    return
  }
  const uploaded = await uploadReportFile(selectedFile.value)
  form.fileUrl = uploaded.fileUrl
}

async function uploadSelectedAttachments(reportId) {
  if (!selectedAttachments.value.length) {
    return
  }
  await Promise.all(selectedAttachments.value.map((file) => uploadReportAttachment(reportId, file)))
  selectedAttachments.value = []
}

async function handleSave() {
  saving.value = true
  try {
    await ensureFileUploaded()
    const report = editingId.value
      ? await updateReport(editingId.value, form)
      : await createReport(form)
    await uploadSelectedAttachments(report.id)
    ElMessage.success('报告已保存')
    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

async function handleDeleteAttachment(id) {
  await deleteReportAttachment(id)
  ElMessage.success('附加材料已删除')
  if (editingId.value) {
    const detail = await fetchReport(editingId.value)
    attachmentRecords.value = detail.attachments || []
  }
  await loadData()
}

function resetPreview() {
  if (previewState.src && previewState.src.startsWith('blob:')) {
    window.URL.revokeObjectURL(previewState.src)
  }
  Object.assign(previewState, {
    mode: '',
    src: '',
    htmlContent: '',
    fileName: '',
    message: ''
  })
}

async function openProtectedPreview(metaLoader, fileLoader, fallbackFilename) {
  resetPreview()
  previewVisible.value = true
  try {
    const meta = await metaLoader()
    previewState.mode = meta.mode
    previewState.fileName = meta.fileName || fallbackFilename
    previewState.htmlContent = meta.htmlContent || ''
    previewState.message = meta.message || ''
    if (meta.mode === 'pdf' || meta.mode === 'image') {
      const response = await fileLoader()
      const { url } = await createObjectUrlFromResponse(response, previewState.fileName)
      previewState.src = url
    }
  } catch (error) {
    previewVisible.value = false
    resetPreview()
    ElMessage.error(error.message || '文件打开失败')
  }
}

async function handleOpenReportFile(reportId) {
  await openProtectedPreview(
    () => fetchReportFilePreview(reportId),
    () => fetchReportFile(reportId),
    `report-${reportId}`
  )
}

async function handleOpenAttachment(attachmentId) {
  await openProtectedPreview(
    () => fetchReportAttachmentPreview(attachmentId),
    () => fetchReportAttachmentFile(attachmentId),
    `attachment-${attachmentId}`
  )
}

function formatFileSize(size) {
  const value = Number(size || 0)
  if (value >= 1024 * 1024) {
    return `${(value / (1024 * 1024)).toFixed(1)} MB`
  }
  if (value >= 1024) {
    return `${(value / 1024).toFixed(1)} KB`
  }
  return `${value} B`
}

onMounted(loadData)
</script>

<template>
  <div class="content-stack">
    <PanelCard title="报告评阅" subtitle="查看学生提交的报告并给出评语">
      <el-table :data="records" border>
        <el-table-column prop="studentName" label="学生" width="120" />
        <el-table-column prop="projectTitle" label="项目" min-width="160" />
        <el-table-column prop="title" label="报告标题" min-width="180" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="teacherScore" label="评分" width="100" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" link @click="openReview(row)">评阅</el-button>
            <el-button type="primary" link @click="handleOpenReportFile(row.id)">查看文件</el-button>
          </template>
        </el-table-column>
      </el-table>
    </PanelCard>

    <el-dialog v-model="dialogVisible" title="评阅报告" width="680px">
      <el-form :model="form" label-position="top">
        <div class="form-grid">
          <el-form-item label="评阅状态">
            <el-select v-model="form.status" style="width: 100%">
              <el-option label="通过" value="APPROVED" />
              <el-option label="驳回" value="REJECTED" />
              <el-option label="优秀" value="EXCELLENT" />
            </el-select>
          </el-form-item>
          <el-form-item label="评分">
            <el-input-number v-model="form.score" :min="0" :max="100" style="width: 100%" />
          </el-form-item>
          <el-form-item label="推荐优秀">
            <el-switch v-model="form.isExcellent" />
          </el-form-item>
          <el-form-item label="评语" class="full">
            <el-input v-model="form.comment" type="textarea" :rows="5" />
          </el-form-item>
        </div>
      </el-form>
      <div v-if="attachmentRecords.length" style="margin-top: 12px">
        <div class="hint-text" style="margin-bottom: 10px">附加材料</div>
        <el-table :data="attachmentRecords" border size="small">
          <el-table-column prop="originalName" label="文件名" min-width="220" />
          <el-table-column prop="contentType" label="类型" min-width="140" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="primary" link @click="handleOpenAttachment(row.id)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交评阅</el-button>
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
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import FilePreview from '@/components/FilePreview.vue'
import PanelCard from '@/components/PanelCard.vue'
import { useAppStore } from '@/store'
import { createObjectUrlFromResponse } from '@/utils/download'
import {
  fetchReport,
  fetchReportAttachmentFile,
  fetchReportAttachmentPreview,
  fetchReportFile,
  fetchReportFilePreview,
  listTeacherReports,
  reviewReport
} from '@/api/report'

const store = useAppStore()
const records = ref([])
const dialogVisible = ref(false)
const currentId = ref(null)
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
  status: 'APPROVED',
  score: 90,
  comment: '',
  isExcellent: false
})

async function loadData() {
  const data = await listTeacherReports({ page: 1, size: 50 })
  records.value = data.records
}

async function openReview(row) {
  const detail = await fetchReport(row.id)
  currentId.value = row.id
  form.status = detail.status === 'REJECTED' ? 'REJECTED' : 'APPROVED'
  form.score = detail.teacherScore || 90
  form.comment = detail.teacherComment || ''
  form.isExcellent = detail.isExcellent === 1
  attachmentRecords.value = detail.attachments || []
  dialogVisible.value = true
}

async function submitReview() {
  await reviewReport(currentId.value, form)
  ElMessage.success('报告评阅已保存')
  dialogVisible.value = false
  await Promise.all([loadData(), store.refreshReminders()])
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

onMounted(async () => {
  await Promise.all([loadData(), store.refreshReminders()])
})
</script>

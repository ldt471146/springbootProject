<template>
  <div class="content-stack">
    <PanelCard title="实习日志" subtitle="提交日报、周报或阶段小结">
      <template #extra>
        <el-button type="primary" @click="openDialog()">新建日志</el-button>
      </template>

      <el-table :data="records" border>
        <el-table-column prop="projectTitle" label="项目" min-width="160" />
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="journalType" label="类型" width="120" />
        <el-table-column prop="journalDate" label="日期" width="130" />
        <el-table-column label="附件" width="100">
          <template #default="{ row }">
            {{ row.attachments?.length || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="score" label="评分" width="100" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </PanelCard>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑日志' : '新建日志'" width="680px">
      <el-form :model="form" label-position="top">
        <div class="form-grid">
          <el-form-item label="所属申请">
            <el-select v-model="form.applicationId" style="width: 100%">
              <el-option v-for="item in approvedApplications" :key="item.id" :label="item.projectTitle" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="日志类型">
            <el-select v-model="form.journalType" style="width: 100%">
              <el-option label="日报" value="DAILY" />
              <el-option label="周报" value="WEEKLY" />
              <el-option label="阶段小结" value="PHASE_SUMMARY" />
            </el-select>
          </el-form-item>
          <el-form-item label="日志标题" class="full">
            <el-input v-model="form.title" />
          </el-form-item>
          <el-form-item label="日志日期">
            <el-date-picker v-model="form.journalDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
          <el-form-item label="周次">
            <el-input-number v-model="form.weekNo" :min="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="日志内容" class="full">
            <el-input v-model="form.content" type="textarea" :rows="6" />
          </el-form-item>
          <el-form-item label="补充附件" class="full">
            <input type="file" accept=".pdf,.doc,.docx" multiple @change="handleAttachmentChange" />
            <div class="hint-text" style="margin-top: 8px">
              可补充上传 PDF 或 Word 材料，保存日志后会自动绑定到当前日志。
            </div>
          </el-form-item>
        </div>
      </el-form>
      <div v-if="attachmentRecords.length" style="margin-top: 12px">
        <div class="hint-text" style="margin-bottom: 10px">已绑定附件</div>
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
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import FilePreview from '@/components/FilePreview.vue'
import PanelCard from '@/components/PanelCard.vue'
import { createObjectUrlFromResponse } from '@/utils/download'
import { listMyApplications } from '@/api/application'
import {
  createJournal,
  deleteJournal,
  deleteJournalAttachment,
  fetchJournal,
  fetchJournalAttachmentFile,
  fetchJournalAttachmentPreview,
  listMyJournals,
  updateJournal,
  uploadJournalAttachment
} from '@/api/journal'

const records = ref([])
const applications = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)
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
  content: '',
  journalType: 'DAILY',
  journalDate: '',
  weekNo: 1
})

const approvedApplications = computed(() => applications.value.filter((item) => item.status === 'APPROVED'))

async function loadData() {
  const [journalData, applicationData] = await Promise.all([
    listMyJournals({ page: 1, size: 50 }),
    listMyApplications({ page: 1, size: 50 })
  ])
  records.value = journalData.records
  applications.value = applicationData.records
}

async function openDialog(row) {
  const detail = row?.id ? await fetchJournal(row.id) : null
  editingId.value = row?.id || null
  selectedAttachments.value = []
  attachmentRecords.value = detail?.attachments || row?.attachments || []
  Object.assign(form, {
    applicationId: detail?.applicationId || row?.applicationId || '',
    title: detail?.title || row?.title || '',
    content: detail?.content || row?.content || '',
    journalType: detail?.journalType || row?.journalType || 'DAILY',
    journalDate: detail?.journalDate || row?.journalDate || '',
    weekNo: detail?.weekNo || row?.weekNo || 1
  })
  dialogVisible.value = true
}

function handleAttachmentChange(event) {
  selectedAttachments.value = Array.from(event.target.files || [])
}

async function uploadSelectedAttachments(journalId) {
  if (!selectedAttachments.value.length) {
    return
  }
  await Promise.all(selectedAttachments.value.map((file) => uploadJournalAttachment(journalId, file)))
  selectedAttachments.value = []
}

async function handleSave() {
  saving.value = true
  try {
    const journal = editingId.value
      ? await updateJournal(editingId.value, form)
      : await createJournal(form)
    await uploadSelectedAttachments(journal.id)
    ElMessage.success('日志已保存')
    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确认删除这条日志？', '删除日志', { type: 'warning' })
  await deleteJournal(id)
  ElMessage.success('日志已删除')
  await loadData()
}

async function handleDeleteAttachment(id) {
  await ElMessageBox.confirm('确认删除这个日志附件？', '删除附件', { type: 'warning' })
  await deleteJournalAttachment(id)
  ElMessage.success('日志附件已删除')
  if (editingId.value) {
    const detail = await fetchJournal(editingId.value)
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

async function handleOpenAttachment(attachmentId) {
  await openProtectedPreview(
    () => fetchJournalAttachmentPreview(attachmentId),
    () => fetchJournalAttachmentFile(attachmentId),
    `journal-attachment-${attachmentId}`
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

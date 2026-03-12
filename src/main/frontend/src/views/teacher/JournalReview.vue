<template>
  <div class="content-stack">
    <PanelCard title="日志评阅" subtitle="按学生申请查看日志并填写批注">
      <div class="toolbar">
        <div class="toolbar-group">
          <el-select v-model="selectedAppId" placeholder="选择学生申请" style="width: 320px" @change="loadJournals">
            <el-option
              v-for="item in applications"
              :key="item.id"
              :label="`${item.studentName} - ${item.projectTitle}`"
              :value="item.id"
            />
          </el-select>
        </div>
      </div>

      <el-table :data="records" border>
        <el-table-column prop="title" label="标题" min-width="140" show-overflow-tooltip />
        <el-table-column prop="journalType" label="类型" width="120" />
        <el-table-column prop="journalDate" label="日期" width="130" />
        <el-table-column prop="content" label="日志内容" min-width="180" show-overflow-tooltip />
        <el-table-column label="附件" width="100">
          <template #default="{ row }">
            {{ row.attachments?.length || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="score" label="评分" width="100" />
        <el-table-column prop="teacherComment" label="批注" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetail(row)">查看内容</el-button>
            <el-button type="primary" link @click="openReview(row)">评阅</el-button>
          </template>
        </el-table-column>
      </el-table>
    </PanelCard>

    <el-dialog v-model="dialogVisible" title="评阅日志" width="720px">
      <div class="page-card" style="margin-bottom: 16px; padding: 16px">
        <div style="display: flex; justify-content: space-between; gap: 12px; flex-wrap: wrap; margin-bottom: 10px">
          <strong>{{ currentJournal?.title || '日志详情' }}</strong>
          <span class="hint-text">{{ currentJournal?.journalDate || '' }} {{ currentJournal?.journalType || '' }}</span>
        </div>
        <div style="white-space: pre-wrap; line-height: 1.7; color: #243447">
          {{ currentJournal?.content || '暂无日志内容' }}
        </div>
      </div>
      <div v-if="attachmentRecords.length" style="margin-bottom: 16px">
        <div class="hint-text" style="margin-bottom: 10px">日志附件</div>
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
      <el-form :model="form" label-position="top">
        <el-form-item label="评分">
          <el-input-number v-model="form.score" :min="0" :max="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="教师批注">
          <el-input v-model="form.teacherComment" type="textarea" :rows="5" />
        </el-form-item>
      </el-form>
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
import { listTeacherStudents } from '@/api/application'
import {
  fetchJournal,
  fetchJournalAttachmentFile,
  fetchJournalAttachmentPreview,
  listApplicationJournals,
  reviewJournal
} from '@/api/journal'

const store = useAppStore()
const applications = ref([])
const records = ref([])
const selectedAppId = ref('')
const dialogVisible = ref(false)
const currentId = ref(null)
const currentJournal = ref(null)
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
  score: 90,
  teacherComment: ''
})

async function loadApplications() {
  const data = await listTeacherStudents({ page: 1, size: 50 })
  applications.value = data.records
  if (!selectedAppId.value && applications.value.length) {
    selectedAppId.value = applications.value[0].id
  }
  loadJournals()
}

async function loadJournals() {
  if (!selectedAppId.value) {
    records.value = []
    return
  }
  const data = await listApplicationJournals(selectedAppId.value, { page: 1, size: 50 })
  records.value = data.records
}

function openReview(row) {
  openDetail(row)
}

async function openDetail(row) {
  const detail = await fetchJournal(row.id)
  currentJournal.value = detail
  currentId.value = detail.id
  attachmentRecords.value = detail.attachments || []
  form.score = detail.score || 90
  form.teacherComment = detail.teacherComment || ''
  dialogVisible.value = true
}

async function submitReview() {
  await reviewJournal(currentId.value, form)
  ElMessage.success('日志评阅已保存')
  dialogVisible.value = false
  await Promise.all([loadJournals(), store.refreshReminders()])
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

onMounted(async () => {
  await Promise.all([loadApplications(), store.refreshReminders()])
})
</script>

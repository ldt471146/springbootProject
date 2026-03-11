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
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PanelCard from '@/components/PanelCard.vue'
import { listMyApplications } from '@/api/application'
import { createJournal, deleteJournal, listMyJournals, updateJournal } from '@/api/journal'

const records = ref([])
const applications = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)
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

function openDialog(row) {
  editingId.value = row?.id || null
  Object.assign(form, {
    applicationId: row?.applicationId || '',
    title: row?.title || '',
    content: row?.content || '',
    journalType: row?.journalType || 'DAILY',
    journalDate: row?.journalDate || '',
    weekNo: row?.weekNo || 1
  })
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    if (editingId.value) {
      await updateJournal(editingId.value, form)
    } else {
      await createJournal(form)
    }
    ElMessage.success('日志已保存')
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确认删除这条日志？', '删除日志', { type: 'warning' })
  await deleteJournal(id)
  ElMessage.success('日志已删除')
  loadData()
}

onMounted(loadData)
</script>

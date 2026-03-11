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
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="journalType" label="类型" width="120" />
        <el-table-column prop="journalDate" label="日期" width="130" />
        <el-table-column prop="score" label="评分" width="100" />
        <el-table-column prop="teacherComment" label="批注" min-width="200" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" link @click="openReview(row)">评阅</el-button>
          </template>
        </el-table-column>
      </el-table>
    </PanelCard>

    <el-dialog v-model="dialogVisible" title="评阅日志" width="560px">
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
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import PanelCard from '@/components/PanelCard.vue'
import { listTeacherStudents } from '@/api/application'
import { listApplicationJournals, reviewJournal } from '@/api/journal'

const applications = ref([])
const records = ref([])
const selectedAppId = ref('')
const dialogVisible = ref(false)
const currentId = ref(null)
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
  currentId.value = row.id
  form.score = row.score || 90
  form.teacherComment = row.teacherComment || ''
  dialogVisible.value = true
}

async function submitReview() {
  await reviewJournal(currentId.value, form)
  ElMessage.success('日志评阅已保存')
  dialogVisible.value = false
  loadJournals()
}

onMounted(loadApplications)
</script>

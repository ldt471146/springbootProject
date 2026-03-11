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
            <el-link :href="`/api/report/file/${row.id}`" target="_blank" type="primary">查看文件</el-link>
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
import { listTeacherReports, reviewReport } from '@/api/report'

const records = ref([])
const dialogVisible = ref(false)
const currentId = ref(null)
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

function openReview(row) {
  currentId.value = row.id
  form.status = row.status === 'REJECTED' ? 'REJECTED' : 'APPROVED'
  form.score = row.teacherScore || 90
  form.comment = row.teacherComment || ''
  form.isExcellent = row.isExcellent === 1
  dialogVisible.value = true
}

async function submitReview() {
  await reviewReport(currentId.value, form)
  ElMessage.success('报告评阅已保存')
  dialogVisible.value = false
  loadData()
}

onMounted(loadData)
</script>

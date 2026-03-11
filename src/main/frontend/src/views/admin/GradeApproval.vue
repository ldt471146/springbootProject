<template>
  <PanelCard title="成绩终审" subtitle="管理员确认教师评分结果">
    <el-table :data="records" border>
      <el-table-column prop="studentName" label="学生" width="120" />
      <el-table-column prop="projectTitle" label="项目" min-width="160" />
      <el-table-column prop="teacherName" label="指导教师" width="120" />
      <el-table-column prop="finalScore" label="总评" width="100" />
      <el-table-column prop="adminStatus" label="状态" width="120" />
      <el-table-column prop="teacherComment" label="教师评语" min-width="180" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button type="success" link @click="openDialog(row, 'CONFIRMED')">确认</el-button>
          <el-button type="danger" link @click="openDialog(row, 'RETURNED')">退回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="成绩终审" width="520px">
      <el-form :model="form" label-position="top">
        <el-form-item label="终审状态">
          <el-input :model-value="form.adminStatus" disabled />
        </el-form-item>
        <el-form-item label="管理员意见">
          <el-input v-model="form.adminComment" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitConfirm">提交</el-button>
      </template>
    </el-dialog>
  </PanelCard>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import PanelCard from '@/components/PanelCard.vue'
import { confirmGrade, listAdminGrades } from '@/api/grade'

const records = ref([])
const dialogVisible = ref(false)
const currentId = ref(null)
const form = reactive({
  adminStatus: 'CONFIRMED',
  adminComment: ''
})

async function loadData() {
  const data = await listAdminGrades({ page: 1, size: 100 })
  records.value = data.records
}

function openDialog(row, status) {
  currentId.value = row.id
  form.adminStatus = status
  form.adminComment = row.adminComment || ''
  dialogVisible.value = true
}

async function submitConfirm() {
  await confirmGrade(currentId.value, form)
  ElMessage.success('终审已保存')
  dialogVisible.value = false
  loadData()
}

onMounted(loadData)
</script>

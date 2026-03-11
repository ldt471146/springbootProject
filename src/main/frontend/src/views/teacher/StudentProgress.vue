<template>
  <div class="content-stack">
    <PanelCard title="学生进度" subtitle="更新实习阶段，并录入最终成绩">
      <el-table :data="records" border>
        <el-table-column prop="studentName" label="学生" width="120" />
        <el-table-column prop="projectTitle" label="项目" min-width="180" />
        <el-table-column prop="phase" label="当前阶段" width="130" />
        <el-table-column prop="status" label="申请状态" width="120" />
        <el-table-column label="阶段更新" width="220">
          <template #default="{ row }">
            <el-select v-model="row.phase" style="width: 140px" @change="updatePhase(row)">
              <el-option label="已录取" value="ENROLLED" />
              <el-option label="进行中" value="IN_PROGRESS" />
              <el-option label="已完成" value="COMPLETED" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="成绩" width="180">
          <template #default="{ row }">
            <span v-if="gradeMap[row.id]">总评 {{ gradeMap[row.id].finalScore }}</span>
            <el-button type="primary" link @click="openGradeDialog(row)">
              {{ gradeMap[row.id] ? '修改成绩' : '录入成绩' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </PanelCard>

    <el-dialog v-model="dialogVisible" title="录入成绩" width="580px">
      <el-form :model="gradeForm" label-position="top">
        <div class="form-grid">
          <el-form-item label="日志成绩">
            <el-input-number v-model="gradeForm.journalScore" :min="0" :max="100" style="width: 100%" />
          </el-form-item>
          <el-form-item label="报告成绩">
            <el-input-number v-model="gradeForm.reportScore" :min="0" :max="100" style="width: 100%" />
          </el-form-item>
          <el-form-item label="教师评语" class="full">
            <el-input v-model="gradeForm.teacherComment" type="textarea" :rows="4" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitGrade">保存成绩</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import PanelCard from '@/components/PanelCard.vue'
import { listTeacherStudents, updateApplicationPhase } from '@/api/application'
import { createGrade, listTeacherGrades, updateGrade } from '@/api/grade'

const records = ref([])
const dialogVisible = ref(false)
const currentRow = ref(null)
const gradeMap = reactive({})
const gradeForm = reactive({
  journalScore: 90,
  reportScore: 90,
  teacherComment: ''
})

async function loadData() {
  const [studentData, gradeData] = await Promise.all([
    listTeacherStudents({ page: 1, size: 100 }),
    listTeacherGrades({ page: 1, size: 100 })
  ])
  records.value = studentData.records
  Object.keys(gradeMap).forEach((key) => delete gradeMap[key])
  gradeData.records.forEach((item) => {
    gradeMap[item.applicationId] = item
  })
}

async function updatePhase(row) {
  await updateApplicationPhase(row.id, { phase: row.phase })
  ElMessage.success('阶段已更新')
}

function openGradeDialog(row) {
  currentRow.value = row
  const grade = gradeMap[row.id]
  gradeForm.journalScore = grade?.journalScore || 90
  gradeForm.reportScore = grade?.reportScore || 90
  gradeForm.teacherComment = grade?.teacherComment || ''
  dialogVisible.value = true
}

async function submitGrade() {
  const grade = gradeMap[currentRow.value.id]
  if (grade) {
    await updateGrade(grade.id, {
      applicationId: currentRow.value.id,
      ...gradeForm
    })
  } else {
    await createGrade({
      applicationId: currentRow.value.id,
      ...gradeForm
    })
  }
  ElMessage.success('成绩已保存')
  dialogVisible.value = false
  loadData()
}

onMounted(loadData)
</script>

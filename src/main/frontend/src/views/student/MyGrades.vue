<template>
  <PanelCard title="我的成绩" subtitle="查看教师评分和管理员终审结果">
    <el-table :data="records" border>
      <el-table-column prop="projectTitle" label="项目" min-width="180" />
      <el-table-column prop="teacherName" label="指导教师" width="120" />
      <el-table-column prop="journalScore" label="日志成绩" width="110" />
      <el-table-column prop="reportScore" label="报告成绩" width="110" />
      <el-table-column prop="finalScore" label="总评" width="100" />
      <el-table-column prop="adminStatus" label="终审状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.adminStatus === 'CONFIRMED' ? 'success' : row.adminStatus === 'RETURNED' ? 'danger' : 'warning'">
            {{ row.adminStatus }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="teacherComment" label="教师评语" min-width="200" />
      <el-table-column prop="adminComment" label="管理员意见" min-width="200" />
    </el-table>
  </PanelCard>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import PanelCard from '@/components/PanelCard.vue'
import { listMyGrades } from '@/api/grade'

const records = ref([])

async function loadData() {
  const data = await listMyGrades({ page: 1, size: 50 })
  records.value = data.records
}

onMounted(loadData)
</script>

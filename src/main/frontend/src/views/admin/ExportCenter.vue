<template>
  <PanelCard title="导出中心" subtitle="管理员可按学期和学分类型导出成绩单">
    <div class="toolbar">
      <div class="toolbar-group">
        <el-input v-model="filters.semester" placeholder="学期，例如 2025-2026-1" style="width: 220px" />
        <el-input v-model="filters.creditType" placeholder="学分类型" style="width: 180px" />
      </div>
    </div>

    <div class="page-grid">
      <div class="content-panel" style="grid-column: span 6">
        <div class="panel-heading">
          <div>
            <h3 class="page-title" style="font-size: 20px">成绩单 Excel</h3>
            <p class="page-subtitle">适合进一步筛选、统计和打印</p>
          </div>
        </div>
        <el-button type="primary" @click="downloadExcel">导出 Excel</el-button>
      </div>

      <div class="content-panel" style="grid-column: span 6">
        <div class="panel-heading">
          <div>
            <h3 class="page-title" style="font-size: 20px">成绩单 PDF</h3>
            <p class="page-subtitle">适合固定格式归档和直接分发</p>
          </div>
        </div>
        <el-button type="primary" plain @click="downloadPdf">导出 PDF</el-button>
      </div>
    </div>
  </PanelCard>
</template>

<script setup>
import { reactive } from 'vue'
import PanelCard from '@/components/PanelCard.vue'
import { exportGrades, exportGradesPdf } from '@/api/export'
import { saveBlobResponse } from '@/utils/download'

const filters = reactive({
  semester: '',
  creditType: ''
})

async function downloadExcel() {
  const response = await exportGrades(filters)
  saveBlobResponse(response, 'grades.xlsx')
}

async function downloadPdf() {
  const response = await exportGradesPdf(filters)
  saveBlobResponse(response, 'grades.pdf')
}
</script>

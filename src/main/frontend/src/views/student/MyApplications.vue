<template>
  <div class="content-stack">
    <PanelCard title="申请记录" subtitle="查看审核结果、指导教师信息，并打开进度时间线">
      <el-table :data="records" border>
        <el-table-column prop="projectTitle" label="项目" min-width="180" />
        <el-table-column prop="teacherName" label="指导教师" width="120" />
        <el-table-column prop="status" label="审批状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phase" label="实习阶段" width="120">
          <template #default="{ row }">
            {{ phaseLabel(row.phase) }}
          </template>
        </el-table-column>
        <el-table-column prop="reviewComment" label="审批意见" min-width="180" />
        <el-table-column prop="createTime" label="申请时间" width="170" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" link @click="openTimeline(row)">时间线</el-button>
            <el-button v-if="row.status === 'PENDING'" type="danger" link @click="handleWithdraw(row.id)">撤回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </PanelCard>

    <div class="page-grid">
      <PanelCard title="当前参与项目" subtitle="已通过审批且仍在进行中的项目" style="grid-column: span 6">
        <div v-if="currentProjects.length" class="dashboard-list">
          <div v-for="item in currentProjects" :key="item.id" class="dashboard-list-item">
            <div class="dashboard-list-main">
              <span class="dashboard-list-label">{{ item.projectTitle }}</span>
              <span class="dashboard-list-meta">
                指导教师：{{ item.teacherName || '--' }} · 阶段：{{ phaseLabel(item.phase) }}
              </span>
            </div>
            <span class="dashboard-list-count">{{ phaseLabel(item.phase) }}</span>
          </div>
        </div>
        <div v-else class="dashboard-empty">当前没有进行中的实习项目。</div>
      </PanelCard>

      <PanelCard title="历史参与项目" subtitle="已完成或已归档的项目记录" style="grid-column: span 6">
        <div v-if="historyProjects.length" class="dashboard-list">
          <div v-for="item in historyProjects" :key="item.id" class="dashboard-list-item">
            <div class="dashboard-list-main">
              <span class="dashboard-list-label">{{ item.projectTitle }}</span>
              <span class="dashboard-list-meta">
                指导教师：{{ item.teacherName || '--' }} · 状态：{{ statusLabel(item.status) }}
              </span>
            </div>
            <span class="dashboard-list-count">{{ historyStatus(item) }}</span>
          </div>
        </div>
        <div v-else class="dashboard-empty">历史项目会在完成或归档后展示在这里。</div>
      </PanelCard>
    </div>

    <el-drawer v-model="timelineVisible" title="实习进度时间线" size="640px">
      <div class="content-stack">
        <div class="page-card" style="padding: 20px">
          <div class="page-card-header">
            <div>
              <h3 class="page-title" style="font-size: 20px">{{ currentApplication?.projectTitle || '--' }}</h3>
              <p class="page-subtitle">
                指导教师：{{ currentApplication?.teacherName || '--' }}，当前阶段：{{ phaseLabel(currentApplication?.phase) }}
              </p>
            </div>
          </div>
          <el-timeline>
            <el-timeline-item
              v-for="item in timelineItems"
              :key="`${item.time}-${item.title}`"
              :timestamp="item.time"
              :type="item.type"
            >
              <div style="font-weight: 700; margin-bottom: 6px">{{ item.title }}</div>
              <div class="hint-text">{{ item.description }}</div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import PanelCard from '@/components/PanelCard.vue'
import { listMyApplications, withdrawApplication } from '@/api/application'
import { listJournalTimeline } from '@/api/journal'
import { listMyReports } from '@/api/report'

const records = ref([])
const reports = ref([])
const timelineVisible = ref(false)
const currentApplication = ref(null)
const timelineItems = ref([])

const currentProjects = computed(() =>
  records.value.filter((item) => item.status === 'APPROVED' && !isHistoryProject(item))
)

const historyProjects = computed(() =>
  records.value.filter((item) => item.status === 'APPROVED' && isHistoryProject(item))
)

function statusType(status) {
  return status === 'APPROVED' ? 'success' : status === 'REJECTED' ? 'danger' : 'warning'
}

function statusLabel(status) {
  const map = {
    PENDING: '待审批',
    APPROVED: '已通过',
    REJECTED: '已驳回'
  }
  return map[status] || status || '--'
}

function phaseLabel(phase) {
  const map = {
    ENROLLED: '已录取',
    IN_PROGRESS: '进行中',
    COMPLETED: '已结束'
  }
  return map[phase] || phase || '--'
}

function historyStatus(item) {
  if (item.projectStatus === 'ARCHIVED') {
    return '已归档'
  }
  if (item.phase === 'COMPLETED') {
    return '已结束'
  }
  return phaseLabel(item.phase)
}

function isHistoryProject(item) {
  return item.projectStatus === 'ARCHIVED' || item.phase === 'COMPLETED'
}

async function loadData() {
  const [applicationData, reportData] = await Promise.all([
    listMyApplications({ page: 1, size: 50 }),
    listMyReports({ page: 1, size: 50 })
  ])
  records.value = applicationData.records
  reports.value = reportData.records
}

async function handleWithdraw(id) {
  await ElMessageBox.confirm('确认撤回这条申请？', '撤回申请', { type: 'warning' })
  await withdrawApplication(id)
  ElMessage.success('申请已撤回')
  await loadData()
}

async function openTimeline(row) {
  currentApplication.value = row
  const journals = await listJournalTimeline(row.id)
  const report = reports.value.find((item) => item.applicationId === row.id)
  timelineItems.value = buildTimeline(row, journals, report)
  timelineVisible.value = true
}

function buildTimeline(application, journals, report) {
  const items = [
    {
      time: application.createTime || '--',
      title: '提交报名申请',
      description: application.applyReason || '已提交项目申请，等待教师审核。',
      type: 'primary'
    }
  ]

  if (application.reviewedAt) {
    items.push({
      time: application.reviewedAt,
      title: `教师审核 ${statusLabel(application.status)}`,
      description: application.reviewComment || '教师已完成申请审核。',
      type: application.status === 'APPROVED' ? 'success' : 'danger'
    })
  }

  items.push({
    time: application.reviewedAt || application.createTime || '--',
    title: '当前实习阶段',
    description: `当前处于 ${phaseLabel(application.phase)}。`,
    type: isHistoryProject(application) ? 'info' : 'warning'
  })

  journals.forEach((item) => {
    items.push({
      time: item.journalDate || item.createTime || '--',
      title: `${item.journalType} ${item.title}`,
      description: item.teacherComment
        ? `已提交日志，教师批注：${item.teacherComment}`
        : '已提交日志，等待教师评阅。',
      type: item.score == null ? 'warning' : 'success'
    })
  })

  if (report) {
    items.push({
      time: report.createTime || '--',
      title: `提交实习报告 ${report.title}`,
      description: report.teacherComment
        ? `报告状态：${report.status}，教师评语：${report.teacherComment}`
        : `报告状态：${report.status}。`,
      type: report.status === 'REJECTED' ? 'danger' : 'success'
    })
  }

  return items.sort((left, right) => String(left.time).localeCompare(String(right.time)))
}

onMounted(loadData)
</script>

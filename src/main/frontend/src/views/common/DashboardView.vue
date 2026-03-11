<template>
  <div class="content-stack">
    <section class="stats-grid">
      <article class="stat-tile">
        <div class="stat-label">当前角色</div>
        <div class="stat-value">{{ roleLabel }}</div>
      </article>
      <article class="stat-tile">
        <div class="stat-label">真实姓名</div>
        <div class="stat-value">{{ store.user?.realName || '--' }}</div>
      </article>
      <article class="stat-tile">
        <div class="stat-label">学院信息</div>
        <div class="stat-value">{{ store.user?.college || '--' }}</div>
      </article>
      <article class="stat-tile">
        <div class="stat-label">账号状态</div>
        <div class="stat-value">{{ store.user?.status || '--' }}</div>
      </article>
    </section>

    <PanelCard title="操作指引" subtitle="根据不同角色进入相应工作区">
      <el-timeline>
        <el-timeline-item timestamp="学生">
          浏览项目，提交申请，填写日志，上传报告，查看成绩。
        </el-timeline-item>
        <el-timeline-item timestamp="教师">
          发布项目，审批申请，评阅日志与报告，提交成绩。
        </el-timeline-item>
        <el-timeline-item timestamp="管理员">
          审批账号，终审成绩，导出名单和成绩单。
        </el-timeline-item>
      </el-timeline>
    </PanelCard>

    <div class="page-grid">
      <PanelCard title="个人信息" subtitle="当前登录账号概览" style="grid-column: span 6">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户名">{{ store.user?.username }}</el-descriptions-item>
          <el-descriptions-item label="角色">{{ store.user?.role }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ store.user?.email || '--' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ store.user?.phone || '--' }}</el-descriptions-item>
        </el-descriptions>
      </PanelCard>

      <PanelCard title="系统提示" subtitle="第一轮初始化建议" style="grid-column: span 6">
        <ul class="hint-text" style="line-height: 1.9; padding-left: 18px">
          <li>管理员默认账号：`admin`，默认密码：`123456`。</li>
          <li>请先用管理员账号审批教师和学生账号。</li>
          <li>教师创建项目后，学生端才会显示可申请项目。</li>
          <li>报告上传建议先走“上传文件”再保存业务单据。</li>
        </ul>
      </PanelCard>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAppStore } from '@/store'
import PanelCard from '@/components/PanelCard.vue'

const store = useAppStore()

const roleLabel = computed(() => {
  const map = {
    STUDENT: '学生',
    TEACHER: '教师',
    ADMIN: '管理员'
  }
  return map[store.user?.role] || '--'
})
</script>

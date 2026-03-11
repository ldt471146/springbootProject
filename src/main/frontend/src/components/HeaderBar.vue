<template>
  <header class="main-topbar">
    <div>
      <div class="soft-badge">{{ currentTime }}</div>
      <h1 class="page-title">{{ route.meta.title || '工作台' }}</h1>
      <p class="page-subtitle">保持固定导航，压缩操作路径，让高频任务稳定可达。</p>
    </div>
    <div class="toolbar-group">
      <el-tag effect="plain" size="large">{{ user?.realName || user?.username }}</el-tag>
      <el-button @click="$emit('toggle-sidebar')">菜单</el-button>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const props = defineProps({
  user: {
    type: Object,
    default: null
  }
})

defineEmits(['toggle-sidebar'])

const route = useRoute()

const currentTime = computed(() => new Date().toLocaleString('zh-CN', { hour12: false }))
</script>

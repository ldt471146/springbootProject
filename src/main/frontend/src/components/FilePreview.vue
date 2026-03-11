<template>
  <div class="page-card" style="padding: 16px; min-height: 420px">
    <iframe
      v-if="src"
      :src="src"
      title="file-preview"
      style="width: 100%; min-height: 388px; border: 0; border-radius: 12px; background: #f8fafc"
    />
    <div v-else class="hint-text">暂无可预览文件</div>
  </div>
</template>

<script setup>
defineProps({
  src: {
    type: String,
    default: ''
  }
})
</script>

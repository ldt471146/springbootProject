<template>
  <div class="page-card file-preview-shell">
    <iframe
      v-if="mode === 'pdf' && src"
      :src="src"
      title="file-preview"
      class="file-preview-frame"
    />
    <div v-else-if="mode === 'image' && src" class="file-preview-image-wrap">
      <img :src="src" :alt="fileName || 'image-preview'" class="file-preview-image">
    </div>
    <div v-else-if="mode === 'docx' && htmlContent" class="file-preview-docx" v-html="htmlContent" />
    <div v-else class="hint-text">{{ message || '暂无可预览文件' }}</div>
  </div>
</template>

<script setup>
defineProps({
  mode: {
    type: String,
    default: ''
  },
  src: {
    type: String,
    default: ''
  },
  htmlContent: {
    type: String,
    default: ''
  },
  fileName: {
    type: String,
    default: ''
  },
  message: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.file-preview-shell {
  padding: 16px;
  min-height: 420px;
}

.file-preview-frame {
  width: 100%;
  min-height: 72vh;
  border: 0;
  border-radius: 12px;
  background: #f8fafc;
}

.file-preview-image-wrap {
  display: flex;
  justify-content: center;
  padding: 12px;
  background: #f8fafc;
  border-radius: 12px;
}

.file-preview-image {
  max-width: 100%;
  max-height: 72vh;
  border-radius: 12px;
  box-shadow: var(--shadow-card);
}

.file-preview-docx {
  min-height: 72vh;
  overflow: auto;
  padding: 22px;
  border-radius: 12px;
  background: linear-gradient(180deg, #f8fafc, #f1f5f9);
}

.file-preview-docx :deep(.docx-preview-doc) {
  max-width: 860px;
  margin: 0 auto;
  padding: 36px 42px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 18px;
  box-shadow: var(--shadow-card);
  color: #111827;
  line-height: 1.9;
}

.file-preview-docx :deep(.docx-preview-head) {
  margin-bottom: 24px;
  padding-bottom: 18px;
  border-bottom: 1px solid #e5e7eb;
}

.file-preview-docx :deep(.docx-preview-head h1) {
  margin: 0 0 6px;
  font-size: 28px;
  line-height: 1.2;
}

.file-preview-docx :deep(.docx-preview-head p) {
  margin: 0;
  color: var(--muted);
  font-size: 13px;
}

.file-preview-docx :deep(h2) {
  margin: 28px 0 12px;
  font-size: 20px;
  line-height: 1.35;
}

.file-preview-docx :deep(p) {
  margin: 0 0 12px;
  white-space: pre-wrap;
}

.file-preview-docx :deep(.docx-preview-table) {
  width: 100%;
  margin: 18px 0;
  border-collapse: collapse;
}

.file-preview-docx :deep(.docx-preview-table td) {
  padding: 10px 12px;
  border: 1px solid #d4d4d8;
  vertical-align: top;
}

.file-preview-docx :deep(.docx-preview-empty) {
  color: var(--muted);
}
</style>

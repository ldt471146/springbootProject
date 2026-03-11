<template>
  <section class="content-panel">
    <div v-if="title || subtitle || $slots.extra" class="panel-heading">
      <div>
        <h3 class="page-title" style="font-size: 20px">{{ title }}</h3>
        <p v-if="subtitle" class="page-subtitle">{{ subtitle }}</p>
      </div>
      <slot name="extra" />
    </div>
    <slot />
  </section>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: ''
  },
  subtitle: {
    type: String,
    default: ''
  }
})
</script>

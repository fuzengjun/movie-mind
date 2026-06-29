<template>
  <div class="glass-panel overflow-hidden">
    <div class="h-56 bg-slate-200"></div>
    <div class="space-y-2 p-4">
      <h3 class="text-lg font-semibold">{{ movie.title }}</h3>
      <p class="line-clamp-3 text-sm text-slate-600">
        {{ movie.overview || '影片简介待补充。' }}
      </p>
    </div>
  </div>
</template>

<script setup>
defineProps({
  movie: {
    type: Object,
    default: () => ({})
  }
})
</script>

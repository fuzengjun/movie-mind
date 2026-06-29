<template>
  <section class="space-y-4">
    <!-- 标题与副标题 -->
    <div v-if="title || eyebrow || description" class="flex flex-col md:flex-row md:items-end md:justify-between gap-2 px-1">
      <div>
        <!-- 小分类标签 -->
        <p v-if="eyebrow" class="section-kicker text-[10px] font-bold tracking-[0.2em] text-[var(--text-secondary)] uppercase">
          {{ eyebrow }}
        </p>
        <!-- 主标题 -->
        <h2 v-if="title" class="mt-1 text-xl font-bold tracking-tight md:text-2xl text-[var(--text-primary)]">
          {{ title }}
        </h2>
      </div>
      <!-- 描述文字 -->
      <p v-if="description" class="hidden max-w-md text-left md:text-right text-xs font-medium text-[var(--text-muted)] md:block">
        {{ description }}
      </p>
    </div>
    
    <!-- 内容滚动滑轨 -->
    <div class="content-rail">
      <MovieCard v-for="movie in movies" :key="movie.id || movie.title" :movie="movie" />
    </div>
  </section>
</template>

<script setup>
import MovieCard from './MovieCard.vue'

defineProps({
  title: {
    type: String,
    default: ''
  },
  eyebrow: {
    type: String,
    default: ''
  },
  description: {
    type: String,
    default: ''
  },
  movies: {
    type: Array,
    default: () => []
  }
})
</script>

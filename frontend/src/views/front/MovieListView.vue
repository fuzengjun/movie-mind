<template>
  <section class="space-y-6">
    <div class="glass-panel p-6">
      <h1 class="text-2xl font-semibold">影视列表</h1>
      <p class="mt-2 text-slate-600">预留搜索、筛选、分页区域，后续直接接 `/api/movies`。</p>
    </div>
    <MovieList :movies="mockMovies" />
  </section>
</template>

<script setup>
import MovieList from '@/components/MovieList.vue'

const mockMovies = [
  { id: 11, title: '电影列表占位 1', overview: '支持按分类、评分、年份筛选。' },
  { id: 12, title: '电影列表占位 2', overview: '支持分页查询与搜索。' }
]
</script>

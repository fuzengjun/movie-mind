<template>
  <section class="space-y-8">
    <div class="glass-panel overflow-hidden p-8">
      <p class="mb-3 text-sm uppercase tracking-[0.3em] text-brand-700">Smart Movie Information</p>
      <h1 class="mb-4 max-w-3xl text-4xl font-bold text-brand-900">
        智能影视信息管理系统前台首页骨架
      </h1>
      <p class="max-w-2xl text-slate-600">
        这里预留了热门影视、排行榜、推荐区和搜索入口，方便后续按 PRD 继续实现真实数据联动。
      </p>
    </div>
    <div>
      <h2 class="mb-4 text-2xl font-semibold">热门影视</h2>
      <MovieList :movies="mockMovies" />
    </div>
  </section>
</template>

<script setup>
import MovieList from '@/components/MovieList.vue'

const mockMovies = [
  { id: 1, title: '示例电影 A', overview: '热门影视模块占位数据。' },
  { id: 2, title: '示例电影 B', overview: '后续可接 Redis 热门缓存接口。' },
  { id: 3, title: '示例电影 C', overview: '前台推荐区可按 PRD 补齐。' },
  { id: 4, title: '示例电影 D', overview: '当前用于搭建整体页面结构。' }
]
</script>

<template>
  <section class="space-y-8">
    <!-- 头部信息面板 -->
    <div class="surface-card flex flex-col gap-6 p-6 md:p-10 md:flex-row md:items-end md:justify-between">
      <div class="space-y-2">
        <p class="section-kicker text-[10px] tracking-widest uppercase">Movie Library</p>
        <h1 class="text-3xl font-extrabold tracking-tight text-[var(--text-primary)] md:text-4xl">探索影视库</h1>
        <p class="max-w-xl text-sm font-medium text-[var(--text-secondary)]">按分类与地区自由过滤，发掘更多优质电影。像逛货架一样轻松自然。</p>
      </div>
      
      <!-- 分类筛选药丸 -->
      <div class="flex flex-wrap gap-2.5">
        <button 
          class="pill-button-premium transition duration-200" 
          :class="{ 'is-active': activeCategory === '全部' }" 
          @click="activeCategory = '全部'"
        >
          全部分类
        </button>
        <button 
          v-for="category in categories" 
          :key="category" 
          class="pill-button-premium transition duration-200" 
          :class="{ 'is-active': activeCategory === category }" 
          @click="activeCategory = category"
        >
          {{ category }}
        </button>
      </div>
    </div>

    <!-- 地区筛选药丸 -->
    <div class="flex flex-wrap gap-2.5 px-1 pb-2">
      <button 
        class="pill-button-premium transition duration-200" 
        :class="{ 'is-active': activeRegion === '全部' }" 
        @click="activeRegion = '全部'"
      >
        全部地区
      </button>
      <button 
        v-for="region in regions" 
        :key="region" 
        class="pill-button-premium transition duration-200" 
        :class="{ 'is-active': activeRegion === region }" 
        @click="activeRegion = region"
      >
        {{ region }}
      </button>
    </div>

    <!-- 电影网格展示 -->
    <div class="pt-2">
      <MovieList :movies="filteredMovies" v-if="filteredMovies.length > 0" />
      <!-- 空状态 -->
      <div v-else class="flex min-h-[300px] flex-col items-center justify-center rounded-2xl border border-dashed border-[var(--border-soft)] text-[var(--text-muted)]">
        <p class="text-sm">没有找到符合条件的影片</p>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getMovieList } from '@/api/movie'
import MovieList from '@/components/MovieList.vue'
import { mockMovies } from '@/utils/mockData'

const movies = ref([])
const activeCategory = ref('全部')
const activeRegion = ref('全部')

const categories = computed(() => [...new Set(movies.value.flatMap((movie) => movie.categories || []))].slice(0, 8))
const regions = computed(() => [...new Set(movies.value.map((movie) => movie.region).filter(Boolean))].slice(0, 8))

const filteredMovies = computed(() => movies.value.filter((movie) => {
  const categoryMatched = activeCategory.value === '全部' || (movie.categories || []).includes(activeCategory.value)
  const regionMatched = activeRegion.value === '全部' || movie.region === activeRegion.value
  return categoryMatched && regionMatched
}))

onMounted(async () => {
  try {
    const response = await getMovieList()
    movies.value = response.data && response.data.length > 0 ? response.data : mockMovies
  } catch (error) {
    console.warn('API error, falling back to mock movies:', error)
    movies.value = mockMovies
  }
})
</script>

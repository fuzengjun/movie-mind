<template>
  <section class="space-y-8 pt-20">
    <div class="surface-card flex flex-col gap-5 p-6 md:flex-row md:items-end md:justify-between">
      <div>
        <p class="section-kicker text-xs">Movie Library</p>
        <h1 class="mt-3 text-3xl font-semibold tracking-tight md:text-5xl">像流媒体首页一样浏览片库</h1>
        <p class="mt-3 max-w-2xl text-[var(--text-secondary)]">按分类与地区切换视角，让片库不再像静态表格，而是像正在运营的内容货架。</p>
      </div>
      <div class="flex flex-wrap gap-3">
        <button class="pill-button" :class="{ 'is-active': activeCategory === '全部' }" @click="activeCategory = '全部'">全部分类</button>
        <button v-for="category in categories" :key="category" class="pill-button" :class="{ 'is-active': activeCategory === category }" @click="activeCategory = category">
          {{ category }}
        </button>
      </div>
    </div>

    <div class="flex flex-wrap gap-3">
      <button class="pill-button" :class="{ 'is-active': activeRegion === '全部' }" @click="activeRegion = '全部'">全部地区</button>
      <button v-for="region in regions" :key="region" class="pill-button" :class="{ 'is-active': activeRegion === region }" @click="activeRegion = region">
        {{ region }}
      </button>
    </div>

    <MovieList :movies="filteredMovies" />
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getMovieList } from '@/api/movie'
import MovieList from '@/components/MovieList.vue'

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
  const response = await getMovieList()
  movies.value = response.data || []
})
</script>

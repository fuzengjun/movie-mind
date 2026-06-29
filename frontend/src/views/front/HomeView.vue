<template>
  <section class="space-y-10">
    <div class="hero-backdrop surface-card" :style="heroStyle">
      <div class="hero-content flex min-h-[440px] flex-col justify-end p-6 md:p-10">
        <p class="section-kicker text-xs">Now Streaming</p>
        <h1 class="mt-4 max-w-3xl text-4xl font-semibold tracking-tight md:text-6xl">
          {{ featuredMovie?.title || 'Movie Mind' }}
        </h1>
        <p class="mt-4 max-w-2xl text-base text-[var(--text-secondary)] md:text-lg">
          {{ featuredMovie?.overview || 'A cinema-inspired browsing experience built around your catalog, recommendations, and management workflows.' }}
        </p>
        <div class="mt-6 flex flex-wrap gap-3">
          <RouterLink v-if="featuredMovie" :to="`/movies/${featuredMovie.id}`" class="rounded-full bg-[var(--text-primary)] px-5 py-3 font-medium text-[var(--bg-primary)] no-underline">
            查看详情
          </RouterLink>
          <RouterLink to="/admin" class="rounded-full border border-[var(--border-soft)] bg-[var(--surface-secondary)] px-5 py-3 font-medium no-underline">
            进入后台
          </RouterLink>
        </div>
      </div>
    </div>

    <ContentRail title="热门内容" eyebrow="Featured Row" description="基于当前片库热度与浏览表现展示重点内容。" :movies="movies.slice(0, 8)" />
    <ContentRail title="高评分推荐" eyebrow="Top Rated" description="评分、收藏与观看行为共同形成这条精选内容带。" :movies="ratedMovies" />
    <ContentRail title="最近更新" eyebrow="Library Pulse" description="让首页像流媒体入口一样，持续把新近内容推到最前面。" :movies="recentMovies" />
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getMovieList } from '@/api/movie'
import ContentRail from '@/components/ContentRail.vue'

const movies = ref([])

const featuredMovie = computed(() => movies.value[0] || null)
const ratedMovies = computed(() => [...movies.value].sort((a, b) => Number(b.averageRating || 0) - Number(a.averageRating || 0)).slice(0, 8))
const recentMovies = computed(() => [...movies.value].sort((a, b) => String(b.releaseDate || '').localeCompare(String(a.releaseDate || ''))).slice(0, 8))
const heroStyle = computed(() => {
  const image = featuredMovie.value?.backdropUrl || featuredMovie.value?.posterUrl
  return image
    ? { background: `linear-gradient(110deg, rgba(0,0,0,0.18), rgba(0,0,0,0.05)), url(${image}) center/cover` }
    : {}
})

onMounted(async () => {
  const response = await getMovieList()
  movies.value = response.data || []
})
</script>

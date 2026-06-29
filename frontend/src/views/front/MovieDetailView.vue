<template>
  <section v-if="movie" class="space-y-8">
    <div class="hero-backdrop surface-card" :style="heroStyle">
      <div class="hero-content flex min-h-[460px] flex-col justify-end p-6 md:p-10">
        <p class="section-kicker text-xs">Feature Detail</p>
        <h1 class="mt-4 max-w-3xl text-4xl font-semibold tracking-tight md:text-6xl">{{ movie.title }}</h1>
        <p class="mt-4 max-w-2xl text-base text-[var(--text-secondary)] md:text-lg">{{ movie.overview }}</p>
        <div class="mt-6 flex flex-wrap gap-3">
          <span class="detail-chip">评分 {{ movie.averageRating || '0.0' }}</span>
          <span class="detail-chip" v-if="movie.releaseDate">{{ String(movie.releaseDate).slice(0, 4) }}</span>
          <span class="detail-chip" v-if="movie.runtime">{{ movie.runtime }} min</span>
          <span class="detail-chip" v-if="movie.region">{{ movie.region }}</span>
        </div>
      </div>
    </div>

    <div class="grid gap-6 lg:grid-cols-[1.35fr_0.65fr]">
      <article class="surface-card p-6 md:p-8">
        <p class="section-kicker text-xs">Overview</p>
        <h2 class="mt-3 text-2xl font-semibold">内容信息</h2>
        <div class="mt-6 space-y-4 text-[var(--text-secondary)]">
          <p>{{ movie.overview || '暂无简介。' }}</p>
          <div class="flex flex-wrap gap-3">
            <span v-for="category in movie.categories || []" :key="category" class="detail-chip">{{ category }}</span>
          </div>
          <div class="grid gap-4 pt-2 md:grid-cols-2">
            <div class="surface-card-soft p-4">
              <p class="text-xs font-semibold tracking-wider text-[var(--text-muted)] uppercase">TMDB 评分</p>
              <p class="mt-4 text-3xl font-extrabold text-[var(--text-primary)]">{{ movie.tmdbRating || 'N/A' }}</p>
            </div>
            <div class="surface-card-soft p-4">
              <p class="text-xs font-semibold tracking-wider text-[var(--text-muted)] uppercase">收藏 / 浏览</p>
              <p class="mt-4 text-3xl font-extrabold text-[var(--text-primary)]">{{ movie.favoriteCount || 0 }} / {{ movie.viewCount || 0 }}</p>
            </div>
          </div>
        </div>
      </article>

      <aside class="space-y-4">
        <div class="surface-card p-6">
          <p class="section-kicker text-xs">Metadata</p>
          <div class="mt-4 space-y-4 text-sm text-[var(--text-secondary)]">
            <div>
              <p class="text-[var(--text-muted)]">原标题</p>
              <p class="mt-1 text-base text-[var(--text-primary)]">{{ movie.originalTitle || movie.title }}</p>
            </div>
            <div>
              <p class="text-[var(--text-muted)]">语言</p>
              <p class="mt-1 text-base text-[var(--text-primary)]">{{ movie.language || '未提供' }}</p>
            </div>
            <div>
              <p class="text-[var(--text-muted)]">地区</p>
              <p class="mt-1 text-base text-[var(--text-primary)]">{{ movie.region || '未提供' }}</p>
            </div>
          </div>
        </div>
      </aside>
    </div>

    <ContentRail title="继续探索" eyebrow="Up Next" description="从同一片库中继续挑选相邻气质的内容。" :movies="relatedMovies" />
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getMovieDetail, getMovieList } from '@/api/movie'
import ContentRail from '@/components/ContentRail.vue'

const route = useRoute()
const movie = ref(null)
const relatedSource = ref([])

const heroStyle = computed(() => {
  const image = movie.value?.backdropUrl || movie.value?.posterUrl
  return image
    ? { background: `linear-gradient(110deg, rgba(0,0,0,0.16), rgba(0,0,0,0.04)), url(${image}) center/cover` }
    : {}
})
const relatedMovies = computed(() => relatedSource.value.filter((item) => item.id !== movie.value?.id).slice(0, 8))

async function loadMovie() {
  const [detailResponse, listResponse] = await Promise.all([
    getMovieDetail(route.params.id),
    getMovieList()
  ])
  movie.value = detailResponse.data
  relatedSource.value = listResponse.data || []
}

watch(() => route.params.id, () => loadMovie())
onMounted(() => loadMovie())
</script>

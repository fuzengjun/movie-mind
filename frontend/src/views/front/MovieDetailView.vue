<template>
  <section v-if="movie" class="space-y-10">
    <!-- 详情巨幕背景 -->
    <div class="cinema-hero" :style="heroStyle">
      <div class="absolute inset-0 z-0 bg-black/15"></div>
      
      <div class="hero-content relative z-10 flex min-h-[460px] flex-col justify-end p-6 md:p-12 lg:p-16">
        <!-- 小分类标签 -->
        <p class="section-kicker text-[10px] font-bold tracking-[0.25em] text-white/70 uppercase">
          Feature Detail
        </p>
        
        <!-- 电影大标题 -->
        <h1 class="mt-3 max-w-3xl text-3xl font-extrabold tracking-tight text-white md:text-5xl lg:text-6xl leading-[1.1]">
          {{ movie.title }}
        </h1>
        
        <!-- 核心元数据小胶囊 -->
        <div class="mt-6 flex flex-wrap gap-2.5">
          <span class="rounded bg-white/15 backdrop-blur-md border border-white/10 px-3.5 py-1.5 text-xs font-bold text-white">
            ★ {{ movie.averageRating ? Number(movie.averageRating).toFixed(1) : '0.0' }}
          </span>
          <span class="rounded bg-white/10 backdrop-blur-md border border-white/5 px-3.5 py-1.5 text-xs font-semibold text-white/90" v-if="movie.releaseDate">
            {{ String(movie.releaseDate).slice(0, 4) }}
          </span>
          <span class="rounded bg-white/10 backdrop-blur-md border border-white/5 px-3.5 py-1.5 text-xs font-semibold text-white/90" v-if="movie.runtime">
            {{ movie.runtime }} 分钟
          </span>
          <span class="rounded bg-white/10 backdrop-blur-md border border-white/5 px-3.5 py-1.5 text-xs font-semibold text-white/90" v-if="movie.region">
            {{ movie.region }}
          </span>
        </div>
      </div>
    </div>

    <!-- 详情网格 -->
    <div class="grid gap-8 lg:grid-cols-[1.4fr_0.6fr]">
      <!-- 左侧内容简介 -->
      <article class="surface-card p-6 md:p-10 space-y-6">
        <div>
          <p class="section-kicker">Overview</p>
          <h2 class="mt-2 text-2xl font-bold tracking-tight text-[var(--text-primary)]">内容信息</h2>
        </div>
        
        <p class="text-sm md:text-base leading-relaxed text-[var(--text-secondary)]">
          {{ movie.overview || '暂无简介。' }}
        </p>
        
        <!-- 分类标签 -->
        <div class="flex flex-wrap gap-2 pt-2">
          <span v-for="category in movie.categories || []" :key="category" class="detail-chip">
            {{ category }}
          </span>
        </div>
        
        <!-- 数据小方块 -->
        <div class="grid gap-5 pt-4 sm:grid-cols-2">
          <div class="surface-card-soft p-5 flex flex-col justify-between">
            <p class="text-xs font-semibold tracking-wider text-[var(--text-muted)] uppercase">TMDB 评分</p>
            <p class="mt-4 text-3xl font-extrabold text-[var(--text-primary)]">{{ movie.tmdbRating || 'N/A' }}</p>
          </div>
          <div class="surface-card-soft p-5 flex flex-col justify-between">
            <p class="text-xs font-semibold tracking-wider text-[var(--text-muted)] uppercase">收藏 / 浏览数</p>
            <p class="mt-4 text-3xl font-extrabold text-[var(--text-primary)]">
              {{ movie.favoriteCount || 0 }} <span class="text-sm font-medium text-[var(--text-secondary)]">/</span> {{ movie.viewCount || 0 }}
            </p>
          </div>
        </div>
      </article>

      <!-- 右侧元数据侧栏 -->
      <aside class="space-y-6">
        <div class="surface-card p-6 md:p-8 space-y-6">
          <div>
            <p class="section-kicker">Metadata</p>
            <h3 class="mt-2 text-lg font-bold text-[var(--text-primary)]">详细信息</h3>
          </div>
          
          <div class="space-y-5 text-sm text-[var(--text-secondary)]">
            <div class="border-b border-[var(--border-soft)] pb-4">
              <p class="text-xs font-semibold text-[var(--text-muted)]">原标题</p>
              <p class="mt-1.5 text-base font-semibold text-[var(--text-primary)]">{{ movie.originalTitle || movie.title }}</p>
            </div>
            
            <div class="border-b border-[var(--border-soft)] pb-4">
              <p class="text-xs font-semibold text-[var(--text-muted)]">语言</p>
              <p class="mt-1.5 text-base font-medium text-[var(--text-primary)]">{{ movie.language || '未提供' }}</p>
            </div>
            
            <div class="pb-2">
              <p class="text-xs font-semibold text-[var(--text-muted)]">上映地区</p>
              <p class="mt-1.5 text-base font-medium text-[var(--text-primary)]">{{ movie.region || '未提供' }}</p>
            </div>
          </div>
        </div>
      </aside>
    </div>

    <!-- 关联推荐 -->
    <ContentRail 
      title="继续探索" 
      eyebrow="Up Next" 
      description="从同一片库中继续挑选相似气质的内容。" 
      :movies="relatedMovies" 
    />
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getMovieDetail, getMovieList } from '@/api/movie'
import ContentRail from '@/components/ContentRail.vue'
import { mockMovies } from '@/utils/mockData'

const route = useRoute()
const movie = ref(null)
const relatedSource = ref([])

const heroStyle = computed(() => {
  const image = movie.value?.backdropUrl || movie.value?.posterUrl
  return image
    ? { backgroundImage: `linear-gradient(to top, rgba(0,0,0,0.85) 0%, rgba(0,0,0,0.4) 40%, rgba(0,0,0,0.15) 100%), url(${image})` }
    : {}
})

const relatedMovies = computed(() => relatedSource.value.filter((item) => item.id !== movie.value?.id).slice(0, 8))

async function loadMovie() {
  try {
    const [detailResponse, listResponse] = await Promise.all([
      getMovieDetail(route.params.id),
      getMovieList()
    ])
    movie.value = detailResponse.data
    relatedSource.value = listResponse.data && listResponse.data.length > 0 ? listResponse.data : mockMovies
  } catch (error) {
    console.warn('API error, falling back to mock movie details:', error)
    const currentId = Number(route.params.id)
    const matched = mockMovies.find((m) => m.id === currentId) || mockMovies[0]
    movie.value = matched
    relatedSource.value = mockMovies
  }
}

watch(() => route.params.id, () => loadMovie())
onMounted(() => loadMovie())
</script>

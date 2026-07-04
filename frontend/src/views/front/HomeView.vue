<template>
  <section class="space-y-12">
    <!-- 沉浸式巨幕 Banner（整个海报作为点击跳转入口，极致清爽无按钮） -->
    <RouterLink
        v-if="featuredMovie"
        :to="`/movies/${featuredMovie.id}`"
        class="block cinema-hero no-underline relative group"
        :style="heroStyle"
    >
      <div class="absolute inset-0 z-0 bg-black/10 transition duration-300 group-hover:bg-black/5"></div>

      <!-- pt-24 给顶栏 fixed 预留位置，使得滚动无间隙的同时，文字清晰展示 -->
      <div
          class="hero-content relative z-10 flex min-h-[560px] flex-col justify-end p-6 md:p-12 lg:p-16 pt-24 text-white text-left">


        <!-- 小分类标签 -->
        <p class="section-kicker text-[10px] font-bold tracking-[0.25em] text-white/70 uppercase">
          Now Streaming
        </p>

        <!-- 主打电影标题 -->
        <h1 class="mt-3 max-w-2xl text-3xl font-extrabold tracking-tight text-white md:text-5xl lg:text-6xl leading-[1.1]">
          {{ featuredMovie.title }}
        </h1>

        <!-- 电影简介 -->
        <p class="mt-4 max-w-xl text-sm leading-relaxed text-white/80 md:text-base">
          {{ featuredMovie.overview || '暂无影片简介。' }}
        </p>

        <!-- 核心元数据圆点连排文本 -->
        <div class="movie-meta-line">
          <span>{{ featuredMovie.releaseDate ? String(featuredMovie.releaseDate).slice(0, 4) : '未知年份' }}</span>
          <span v-if="featuredMovie.runtime" class="meta-separator">·</span>
          <span v-if="featuredMovie.runtime">{{ featuredMovie.runtime }} 分钟</span>
          <span class="meta-separator">·</span>
          <span>★ {{ featuredMovie.averageRating ? Number(featuredMovie.averageRating).toFixed(1) : '0.0' }}</span>
          <span v-if="featuredMovie.region" class="meta-separator">·</span>
          <span v-if="featuredMovie.region">{{ featuredMovie.region }}</span>
        </div>
      </div>
    </RouterLink>

    <!-- 兜底占位 -->
    <div v-else class="cinema-hero flex items-center justify-center text-[var(--text-muted)]">
      <span class="text-sm">暂无推荐内容</span>
    </div>

    <div v-if="errorMessage && !loading" class="surface-card home-state-panel">
      <p class="section-kicker">Unable to load</p>
      <h2>首页内容暂时无法加载</h2>
      <p>{{ errorMessage }}</p>
      <button class="pill-button is-active" type="button" @click="$router.go(0)">重新加载</button>
    </div>
    <div v-else-if="!loading && !movies.length" class="surface-card home-state-panel">
      <p class="section-kicker">Library Empty</p>
      <h2>片库还没有内容</h2>
      <p>管理员导入影片后，热门、高分与最新内容会在这里出现。</p>
    </div>
    <!-- 影视展示轨 (Rails) -->
    <div v-if="movies.length" class="space-y-12">
      <ContentRail
          v-if="userStore.token && personalizedMovies.length"
          title="为你推荐"
          eyebrow="Made For You"
          :description="personalizedDescription"
          :movies="personalizedMovies"
          controls
      />

      <ContentRail
          title="热门影视"
          eyebrow="Featured Row"
          description="基于当前片库热度与浏览表现展示重点内容。"
          :movies="movies.slice(0, 20)"
      />

      <ContentRail
          title="高分力荐"
          eyebrow="Top Rated"
          description="评分、收藏与观看行为共同形成这条精选内容带。"
          :movies="ratedMovies"
      />

      <ContentRail
          title="最新入库"
          eyebrow="Library Pulse"
          description="系统近期更新的影片内容，持续推荐最新上映的佳作。"
          :movies="recentMovies"
      />
    </div>
  </section>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {getMovieList} from '@/api/movie'
import {getMyRecommend} from '@/api/recommend'
import {useUserStore} from '@/stores/user'
import ContentRail from '@/components/ContentRail.vue'


const userStore = useUserStore()
const movies = ref([])
const loading = ref(true)
const errorMessage = ref('')
const personalizedMovies = ref([])

const personalizedDescription = computed(() => personalizedMovies.value[0]?.algorithm === 'USER_CF_COSINE'
    ? '根据与你观影兴趣相近的用户生成，已自动排除你接触过的影片。'
    : '你的行为数据还不多，暂时结合热门、高分与最新影片为你挑选。')

const featuredMovie = computed(() => {
  if (!movies.value || movies.value.length === 0) return null
  const sorted = [...movies.value].sort((a, b) => {
    const aVal = a.viewCount !== undefined && a.viewCount !== null ? Number(a.viewCount) : 0
    const bVal = b.viewCount !== undefined && b.viewCount !== null ? Number(b.viewCount) : 0
    if (bVal !== aVal) return bVal - aVal
    const aRating = a.averageRating !== undefined && a.averageRating !== null ? Number(a.averageRating) : 0
    const bRating = b.averageRating !== undefined && b.averageRating !== null ? Number(b.averageRating) : 0
    return bRating - aRating
  })
  return sorted[0]
})
const ratedMovies = computed(() => [...movies.value].sort((a, b) => Number(b.averageRating || 0) - Number(a.averageRating || 0)).slice(0, 20))
const recentMovies = computed(() => [...movies.value].sort((a, b) => String(b.releaseDate || '').localeCompare(String(a.releaseDate || ''))).slice(0, 20))

const heroStyle = computed(() => {
  const image = featuredMovie.value?.backdropUrl || featuredMovie.value?.posterUrl
  return image
      ? {backgroundImage: `linear-gradient(to top, rgba(0,0,0,0.85) 0%, rgba(0,0,0,0.4) 40%, rgba(0,0,0,0.15) 100%), url(${image})`}
      : {}
})

onMounted(async () => {
  try {
    const response = await getMovieList()
    movies.value = response.data || []
  } catch (error) {
    movies.value = []
    errorMessage.value = error.message || '影片内容暂时无法加载，请稍后重试。'
  } finally {
    loading.value = false
  }
  if (userStore.token) {
    try {
      const response = await getMyRecommend({limit: 20})
      personalizedMovies.value = response.data || []
    } catch (error) {
      console.warn('Personalized recommendations are temporarily unavailable:', error)
    }
  }
})
</script>

<style scoped>
.movie-meta-line {
  margin-top: 16px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.03em;
  color: rgba(255, 255, 255, 0.9);
}

.meta-separator {
  margin: 0 8px;
  opacity: 0.45;
  user-select: none;
}

.home-state-panel {
  display: grid;
  justify-items: start;
  gap: 10px;
  padding: 36px
}

.home-state-panel h2, .home-state-panel p {
  margin: 0
}

.home-state-panel p {
  color: var(--text-secondary)
}
</style>

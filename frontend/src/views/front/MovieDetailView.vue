<template>
  <section v-if="movie" class="space-y-10">
    <!-- 详情巨幕背景 -->
    <div class="cinema-hero" :style="heroStyle">
      <div class="absolute inset-0 z-0 bg-black/15"></div>
      
      <!-- pt-28 为 fixed 导航栏预留高度，保证文字内容在顶部固定导航下依然完美展示 -->
      <div class="hero-content relative z-10 flex min-h-[580px] flex-col justify-end p-6 md:p-12 lg:p-16 pt-28 text-white">
        <!-- 小分类标签 -->
        <p class="section-kicker text-[10px] font-bold tracking-[0.25em] text-white/70 uppercase border-none">
          Feature Detail
        </p>
        
        <!-- 电影大标题 -->
        <h1 class="mt-3 max-w-3xl text-3xl font-extrabold tracking-tight text-white md:text-5xl lg:text-6xl leading-[1.1]">
          {{ movie.title }}
        </h1>
        
        <!-- 底部弹性分栏排版 -->
        <div class="flex flex-col md:flex-row md:items-end md:justify-between gap-8 mt-4 w-full">
          <!-- 左侧：简介与元数据 -->
          <div class="space-y-4 max-w-2xl text-left">
            <p class="text-sm md:text-base leading-relaxed text-white/80 line-clamp-2 select-none">
              {{ movie.overview || '暂无影片简介。' }}
            </p>
            
            <div class="movie-meta-line">
              <span v-if="movie.releaseDate">{{ String(movie.releaseDate).slice(0, 4) }}</span>
              <span v-if="movie.releaseDate && movie.runtime" class="meta-separator">·</span>
              <span v-if="movie.runtime">{{ movie.runtime }} 分钟</span>
              <span v-if="movie.runtime && movie.averageRating" class="meta-separator">·</span>
              <span>★ {{ movie.averageRating ? Number(movie.averageRating).toFixed(1) : '0.0' }}</span>
              <span v-if="movie.region" class="meta-separator">·</span>
              <span v-if="movie.region">{{ movie.region }}</span>
            </div>
          </div>

          <!-- 右侧：主演列表 (Starring) -->
          <div v-if="actorsString" class="hidden md:block text-right text-xs text-white/60 font-medium max-w-[280px] leading-normal select-none pb-1">
            Starring <span class="text-white/90 font-semibold">{{ actorsString }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 中部：关于 (About) 双列磨砂玻璃面板 -->
    <div class="grid gap-6 md:grid-cols-[1.2fr_0.8fr]">
      <!-- 左列：剧情简介 与 导演团队 -->
      <article class="glass-panel-premium space-y-4">
        <div>
          <p class="section-kicker">Description</p>
          <h2 class="text-xl font-bold text-[var(--text-primary)] border-none">剧情简介</h2>
        </div>
        <p class="text-sm md:text-base leading-relaxed text-[var(--text-secondary)]">
          {{ movie.overview || '暂无详细简介。' }}
        </p>

      </article>

      <!-- 右列：类型、评分与主演卡片 -->
      <aside class="glass-panel-premium flex flex-col justify-between gap-6">
        <div class="space-y-3">
          <p class="section-kicker">Categories</p>
          <h3 class="text-lg font-bold text-[var(--text-primary)] border-none">影片分类</h3>
          <div class="flex flex-wrap gap-2 pt-1">
            <span v-for="category in movie.categories || []" :key="category" class="detail-chip">
              {{ category }}
            </span>
          </div>
        </div>

        <!-- 核心评分数据块 -->
        <div class="grid grid-cols-2 gap-4 border-t border-[var(--border-soft)] pt-4">
          <div>
            <p class="text-[10px] font-bold tracking-wider text-[var(--text-muted)] uppercase">TMDB 评分</p>
            <p class="mt-1 text-2xl font-extrabold text-[var(--text-primary)]">{{ movie.tmdbRating || 'N/A' }}</p>
          </div>
          <div>
            <p class="text-[10px] font-bold tracking-wider text-[var(--text-muted)] uppercase">人气与浏览</p>
            <p class="mt-1 text-2xl font-extrabold text-[var(--text-primary)]">{{ movie.viewCount || 0 }} 次</p>
          </div>
        </div>
      </aside>
    </div>

    <!-- 底部：Apple TV 标准三列等宽网格参数区 -->
    <div class="info-grid-three-cols">
      <!-- 第一列：基本信息 (Information) -->
      <div class="space-y-4">
        <h4 class="text-xs font-bold tracking-widest text-[var(--text-primary)] uppercase border-b border-[var(--border-soft)] pb-2">
          基本信息
        </h4>
        <div class="space-y-3 text-xs text-[var(--text-secondary)]">
          <div>
            <p class="text-[var(--text-muted)] font-medium">原标题</p>
            <p class="mt-0.5 text-sm font-semibold text-[var(--text-primary)]">{{ movie.originalTitle || movie.title }}</p>
          </div>
          <div>
            <p class="text-[var(--text-muted)] font-medium">上映地区</p>
            <p class="mt-0.5 text-sm font-semibold text-[var(--text-primary)]">{{ movie.region || '未提供' }}</p>
          </div>
          <div>
            <p class="text-[var(--text-muted)] font-medium">上映年份</p>
            <p class="mt-0.5 text-sm font-semibold text-[var(--text-primary)]">{{ movie.releaseDate ? String(movie.releaseDate).slice(0, 4) : '未知' }}</p>
          </div>
        </div>
      </div>

      <!-- 第二列：语言音频 (Languages) -->
      <div class="space-y-4">
        <h4 class="text-xs font-bold tracking-widest text-[var(--text-primary)] uppercase border-b border-[var(--border-soft)] pb-2">
          语言与字幕
        </h4>
        <div class="space-y-3 text-xs text-[var(--text-secondary)]">
          <div>
            <p class="text-[var(--text-muted)] font-medium">音频轨道 (Audio)</p>
            <p class="mt-0.5 text-sm font-semibold text-[var(--text-primary)]">
              {{ movie.language || '英语' }} (原声)
            </p>
          </div>
          <div>
            <p class="text-[var(--text-muted)] font-medium">字幕支持 (Subtitles)</p>
            <p class="mt-0.5 text-sm font-semibold text-[var(--text-primary)]">
              简体中文、繁体中文、英文、日文
            </p>
          </div>
        </div>
      </div>

      <!-- 第三列：无障碍与画质 (Accessibility) -->
      <div class="space-y-4">
        <h4 class="text-xs font-bold tracking-widest text-[var(--text-primary)] uppercase border-b border-[var(--border-soft)] pb-2">
          无障碍与画质
        </h4>
        <div class="space-y-4">
          <!-- 辅助微标 -->
          <div class="flex flex-wrap gap-2 pt-1">
            <span class="rounded bg-[var(--surface-primary)] border border-[var(--border-soft)] px-2 py-1 text-[9px] font-extrabold text-[var(--text-primary)] tracking-wide">
              CC
            </span>
            <span class="rounded bg-[var(--surface-primary)] border border-[var(--border-soft)] px-2 py-1 text-[9px] font-extrabold text-[var(--text-primary)] tracking-wide">
              AD
            </span>
            <span class="rounded bg-[var(--surface-primary)] border border-[var(--border-soft)] px-2 py-1 text-[9px] font-extrabold text-[var(--text-primary)] tracking-wide">
              4K UHD
            </span>
            <span class="rounded bg-[var(--surface-primary)] border border-[var(--border-soft)] px-2 py-1 text-[9px] font-extrabold text-[var(--text-primary)] tracking-wide">
              DOLBY VISION
            </span>
          </div>
          
          <p class="text-xs leading-relaxed text-[var(--text-muted)]">
            本片支持 Closed Captions (CC) 辅助字幕与 Audio Descriptions (AD) 视障语音双重无障碍支持，并提供 4K 超高清画质输出。
          </p>
        </div>
      </div>
    </div>

    <!-- 演职人员 -->
    <CastRail 
      v-if="castAndCrew && castAndCrew.length" 
      :movieId="movie.id"
      :cast="castAndCrew" 
    />

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
import CastRail from '@/components/CastRail.vue'
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

// Starring 主演名单拼接字符串（提取前 3 位演员）
const actorsString = computed(() => {
  if (!movie.value?.actors || movie.value.actors.length === 0) return ''
  // 兼容原本的演员数据可能是对象数组或是纯字符串列表
  if (Array.isArray(movie.value.actors)) {
    return movie.value.actors.slice(0, 3).map((a) => a.name).join(', ')
  }
  return String(movie.value.actors)
})

const relatedMovies = computed(() => relatedSource.value.filter((item) => item.id !== movie.value?.id).slice(0, 8))

// 合并导演与演员数据，构建统一的演职人员列表
const castAndCrew = computed(() => {
  const list = []
  if (movie.value) {
    // 1. 处理导演
    if (Array.isArray(movie.value.directors)) {
      movie.value.directors.forEach((d) => {
        if (typeof d === 'string') {
          list.push({
            name: d,
            originalName: '',
            profileUrl: '',
            roleName: '导演'
          })
        } else if (d && typeof d === 'object') {
          list.push({
            name: d.name || '',
            originalName: d.originalName || '',
            profileUrl: d.profileUrl || '',
            roleName: d.roleName || '导演'
          })
        }
      })
    } else if (typeof movie.value.directors === 'string' && movie.value.directors.trim() !== '') {
      const names = movie.value.directors.split(/[,/，、]/)
      names.forEach((name) => {
        const trimmed = name.trim()
        if (trimmed) {
          list.push({
            name: trimmed,
            originalName: '',
            profileUrl: '',
            roleName: '导演'
          })
        }
      })
    }

    // 2. 处理演员
    if (Array.isArray(movie.value.actors)) {
      movie.value.actors.forEach((a) => {
        if (typeof a === 'string') {
          list.push({
            name: a,
            originalName: '',
            profileUrl: '',
            roleName: '演员'
          })
        } else if (a && typeof a === 'object') {
          list.push({
            name: a.name || '',
            originalName: a.originalName || '',
            profileUrl: a.profileUrl || '',
            roleName: a.roleName || '演员'
          })
        }
      })
    } else if (typeof movie.value.actors === 'string' && movie.value.actors.trim() !== '') {
      const names = movie.value.actors.split(/[,/，、]/)
      names.forEach((name) => {
        const trimmed = name.trim()
        if (trimmed) {
          list.push({
            name: trimmed,
            originalName: '',
            profileUrl: '',
            roleName: '演员'
          })
        }
      })
    }
  }
  return list
})


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

watch(
  () => route.params.id,
  () => {
    loadMovie()
    window.scrollTo({ top: 0, behavior: 'instant' })
  }
)
onMounted(() => loadMovie())
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
</style>

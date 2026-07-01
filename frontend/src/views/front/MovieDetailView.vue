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

            <div v-if="movie.watchProviders?.length" class="movie-provider-strip">
              <span class="movie-provider-region">{{ watchProviderRegionText }}</span>
              <div class="movie-provider-list">
                <div v-for="provider in movie.watchProviders" :key="provider.providerId" class="movie-provider-chip">
                  <img v-if="provider.logoUrl" :src="provider.logoUrl" :alt="provider.name" class="movie-provider-logo" />
                  <span class="movie-provider-name">{{ provider.name }}</span>
                  <span class="movie-provider-type">{{ accessTypeText(provider.accessType) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 右侧：主演列表 (Starring) -->
          <div v-if="actorsString" class="hidden md:block text-right text-xs text-white/60 font-medium max-w-[280px] leading-normal select-none pb-1">
            Starring <span class="text-white/90 font-semibold">{{ actorsString }}</span>
          </div>
        </div>
      </div>
    </div>

    <MovieActions v-if="movie?.id" :movie-id="Number(movie.id)" />

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

    <!-- 影片资料：仅展示数据库或上游数据源能够确认的信息 -->
    <div class="info-grid-three-cols movie-info-grid">
      <h2 class="movie-info-title">基本信息</h2>
      <div class="movie-info-groups">
        <section class="movie-info-group">
          <h3>发行信息</h3>
          <div class="movie-info-list">
            <div class="movie-info-item">
              <p class="movie-info-label">上映日期</p>
              <p class="movie-info-value">{{ releaseDateText }}</p>
            </div>
            <div class="movie-info-item">
              <p class="movie-info-label">片长</p>
              <p class="movie-info-value">{{ runtimeText }}</p>
            </div>
            <div class="movie-info-item">
              <p class="movie-info-label">电影分级</p>
              <p class="movie-info-value">{{ movie.certification || '未提供' }}</p>
            </div>
            <div class="movie-info-item">
              <p class="movie-info-label">制片国家/地区</p>
              <p class="movie-info-value">{{ joinedText(movie.productionCountries, movie.region) }}</p>
            </div>
          </div>
        </section>

        <section class="movie-info-group">
          <h3>制作信息</h3>
          <div class="movie-info-list">
            <div class="movie-info-item">
              <p class="movie-info-label">原标题</p>
              <p class="movie-info-value">{{ movie.originalTitle || movie.title }}</p>
            </div>
            <div class="movie-info-item">
              <p class="movie-info-label">制作公司</p>
              <p class="movie-info-value">{{ joinedText(movie.productionCompanies) }}</p>
            </div>
            <div v-if="movie.collectionName" class="movie-info-item">
              <p class="movie-info-label">所属系列</p>
              <p class="movie-info-value">{{ movie.collectionName }}</p>
            </div>
            <div class="movie-info-item">
              <p class="movie-info-label">发行状态</p>
              <p class="movie-info-value">{{ releaseStatusText }}</p>
            </div>
            <div v-if="movie.tagline" class="movie-info-item">
              <p class="movie-info-label">宣传语</p>
              <p class="movie-info-value">{{ movie.tagline }}</p>
            </div>
          </div>
        </section>

        <section class="movie-info-group">
          <h3>语言与主题</h3>
          <div class="movie-info-list">
            <div class="movie-info-item">
              <p class="movie-info-label">原始语言</p>
              <p class="movie-info-value">{{ originalLanguageText }}</p>
            </div>
            <div class="movie-info-item">
              <p class="movie-info-label">影片语言</p>
              <p class="movie-info-value">{{ spokenLanguagesText }}</p>
            </div>
            <div class="movie-info-item">
              <p class="movie-info-label">影片分类</p>
              <p class="movie-info-value">{{ joinedText(movie.categories) }}</p>
            </div>
            <div v-if="movie.keywords?.length" class="movie-info-item">
              <p class="movie-info-label">关键词</p>
              <div class="movie-keyword-list">
                <span v-for="keyword in movie.keywords" :key="keyword">{{ keyword }}</span>
              </div>
            </div>
          </div>
        </section>
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
      :title="personalizedRelated ? '为你推荐' : '继续探索'"
      eyebrow="Up Next" 
      :description="personalizedRelated ? '结合你的历史行为生成，并排除已经接触过的影片。' : '从同一片库中继续挑选相似气质的内容。'"
      :movies="relatedMovies" 
    />
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getMovieDetail, getMovieList } from '@/api/movie'
import { getMyRecommend } from '@/api/recommend'
import { useUserStore } from '@/stores/user'
import ContentRail from '@/components/ContentRail.vue'
import CastRail from '@/components/CastRail.vue'
import MovieActions from '@/components/MovieActions.vue'
import { mockMovies } from '@/utils/mockData'

const route = useRoute()
const userStore = useUserStore()
const movie = ref(null)
const relatedSource = ref([])
const personalizedRelated = ref(false)

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

const spokenLanguagesText = computed(() => {
  const values = normalizeList(movie.value?.spokenLanguages || movie.value?.language)
  if (!values.length) return '暂无语言信息'
  return values.map((language) => localizeLanguage(language)).join('、')
})

const originalLanguageText = computed(() => {
  return movie.value?.originalLanguage ? localizeLanguage(movie.value.originalLanguage) : '未提供'
})

const releaseDateText = computed(() => {
  const value = movie.value?.releaseDate
  return value ? String(value).replaceAll('-', '.') : '未提供'
})

const runtimeText = computed(() => {
  const runtime = Number(movie.value?.runtime || 0)
  if (!runtime) return '未提供'
  const hours = Math.floor(runtime / 60)
  const minutes = runtime % 60
  return hours ? `${hours} 小时 ${minutes} 分钟` : `${minutes} 分钟`
})

const releaseStatusText = computed(() => {
  const statusMap = {
    Released: '已上映',
    'Post Production': '后期制作',
    'In Production': '制作中',
    Planned: '已计划',
    Rumored: '传闻项目',
    Canceled: '已取消'
  }
  return statusMap[movie.value?.releaseStatus] || movie.value?.releaseStatus || '未提供'
})

const watchProviderRegionText = computed(() => {
  const regionMap = { CN: '中国大陆可观看', US: '美国地区可观看' }
  return regionMap[movie.value?.watchProviderRegion] || `${movie.value?.watchProviderRegion || ''} 地区可观看`
})

function joinedText(value, fallback = '') {
  const values = normalizeList(value)
  return values.length ? values.join('、') : fallback || '未提供'
}

function accessTypeText(accessType) {
  const typeMap = {
    flatrate: '订阅',
    free: '免费',
    ads: '含广告',
    rent: '租赁',
    buy: '购买'
  }
  return typeMap[accessType] || ''
}

function normalizeList(value) {
  if (Array.isArray(value)) {
    return value.map((item) => String(item).trim()).filter(Boolean)
  }
  if (typeof value !== 'string' || !value.trim()) return []
  return value.split(/[,，、/]/).map((item) => item.trim()).filter(Boolean)
}

function localizeLanguage(language) {
  const value = String(language).trim()
  if (!/^[a-z]{2,3}(?:-[A-Z]{2})?$/i.test(value)) return value
  try {
    return new Intl.DisplayNames(['zh-CN'], { type: 'language' }).of(value) || value
  } catch {
    return value
  }
}

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
            id: d.id || null,
            personType: d.personType || 'director',
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
            id: a.id || null,
            personType: a.personType || 'actor',
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
    personalizedRelated.value = false
    if (userStore.token) {
      try {
        const recommendResponse = await getMyRecommend({ limit: 12 })
        if (recommendResponse.data?.length) {
          relatedSource.value = recommendResponse.data
          personalizedRelated.value = true
        }
      } catch (error) {
        console.warn('Personalized related movies are temporarily unavailable:', error)
      }
    }
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

.movie-provider-strip {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  margin-top: 14px;
}

.movie-provider-region {
  color: rgba(255, 255, 255, 0.62);
  font-size: 0.7rem;
  font-weight: 650;
  letter-spacing: 0.04em;
}

.movie-provider-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.movie-provider-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 38px;
  padding: 5px 10px 5px 5px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 12px;
  background: rgba(12, 12, 14, 0.48);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.18);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
}

.movie-provider-logo {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  object-fit: cover;
}

.movie-provider-name {
  max-width: 150px;
  overflow: hidden;
  color: #ffffff;
  font-size: 0.75rem;
  font-weight: 650;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.movie-provider-type {
  color: rgba(255, 255, 255, 0.55);
  font-size: 0.65rem;
}

.movie-info-grid {
  display: block;
  padding: 28px 32px 32px;
}

.movie-info-title {
  margin: 0 0 18px;
  padding-bottom: 11px;
  border-bottom: 1px solid var(--border-soft);
  color: var(--text-primary);
  font-size: 1.3rem;
  font-weight: 750;
  letter-spacing: -0.02em;
  transform: translate(-8px, -6px);
}

.movie-info-groups {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 36px;
}

.movie-info-group {
  min-width: 0;
}

.movie-info-group h3 {
  margin: 0 0 17px;
  color: var(--text-primary);
  font-size: 1rem;
  font-weight: 720;
}

.movie-info-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.movie-info-item,
.movie-info-item p {
  min-width: 0;
  margin: 0;
}

.movie-info-label {
  color: var(--text-primary);
  font-size: 0.8rem;
  font-weight: 650;
}

.movie-info-value {
  overflow-wrap: anywhere;
  margin-top: 4px !important;
  color: var(--text-muted);
  font-size: 0.88rem;
  font-weight: 500;
  line-height: 1.5;
}

.movie-keyword-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 7px;
}

.movie-keyword-list span {
  padding: 4px 8px;
  border: 1px solid var(--border-soft);
  border-radius: 999px;
  background: var(--surface-primary);
  color: var(--text-secondary);
  font-size: 0.7rem;
}

@media (max-width: 900px) {
  .movie-info-groups {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .movie-provider-name {
    max-width: 110px;
  }

  .movie-info-grid {
    padding: 24px 22px;
  }

  .movie-info-groups {
    grid-template-columns: 1fr;
    gap: 28px;
  }
}</style>

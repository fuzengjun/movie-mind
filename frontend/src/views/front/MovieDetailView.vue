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
              <div class="provider-rail-shell">
                <div ref="providerRailRef" class="movie-provider-list" @scroll.passive="updateProviderControls">
                  <div v-for="provider in movie.watchProviders" :key="provider.providerId" class="movie-provider-chip" @mouseenter="handleProviderMouseEnter" @mouseleave="handleProviderMouseLeave" @animationiteration="handleProviderAnimationIteration">
                    <img v-if="provider.logoUrl" :src="provider.logoUrl" :alt="provider.name" class="movie-provider-logo" />
                    <span class="movie-provider-name">
                      <span class="movie-provider-name-inner">
                        <span class="marquee-text">{{ provider.name }}</span>
                        <span class="marquee-spacer"></span>
                        <span class="marquee-text marquee-text-duplicate">{{ provider.name }}</span>
                        <span class="marquee-spacer marquee-spacer-duplicate"></span>
                      </span>
                    </span>
                    <span class="movie-provider-type">{{ accessTypeText(provider.accessType) }}</span>
                  </div>
                </div>
                <button v-if="canScrollLeft" class="provider-rail-control provider-rail-control-left" type="button" aria-label="向左滚动播放渠道" @click="scrollProviderRail(-1)">
                  <span>‹</span>
                </button>
                <button v-if="canScrollRight" class="provider-rail-control provider-rail-control-right" type="button" aria-label="向右滚动播放渠道" @click="scrollProviderRail(1)">
                  <span>›</span>
                </button>
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

    <MovieActions v-if="movie?.id" :movie-id="Number(movie.id)" mode="actions" />

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
    <ContentRail v-if="relatedMovies.length"
      title="相似影片"
      eyebrow="More Like This"
      description="综合影片类型、主题标签、导演与演出阵容，为你找到气质相近的作品。"
      :movies="relatedMovies"
      show-reasons
    />
    <div v-else class="surface-card related-empty-state">
      <p class="section-kicker">More Like This</p>
      <h2>暂时没有相似影片</h2>
      <p>片库内容继续丰富后，这里会出现更多相关作品。</p>
    </div>

    <!-- 用户影评与讨论区 -->
    <MovieActions v-if="movie?.id" :movie-id="Number(movie.id)" mode="comments" />
  </section>

  <div v-else-if="loading" class="detail-page-state">
    <div class="surface-card detail-state-card"><div class="state-orbit"></div><p>正在加载影片资料...</p></div>
  </div>
  <div v-else class="detail-page-state">
    <div class="surface-card detail-state-card is-error">
      <p class="section-kicker">Unable to load</p><h1>影片详情无法加载</h1><p>{{ errorMessage }}</p>
      <div class="flex gap-3"><button class="pill-button is-active" @click="loadMovie">重新加载</button><RouterLink class="pill-button no-underline" to="/movies">返回影视库</RouterLink></div>
    </div>
  </div></template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getMovieDetail, getSimilarMovies } from '@/api/movie'
import ContentRail from '@/components/ContentRail.vue'
import CastRail from '@/components/CastRail.vue'
import MovieActions from '@/components/MovieActions.vue'

const route = useRoute()
const movie = ref(null)
const loading = ref(true)
const errorMessage = ref('')
const relatedError = ref('')

// 播放渠道滚轨滚动状态控制
const providerRailRef = ref(null)
const canScrollLeft = ref(false)
const canScrollRight = ref(false)
const START_EPSILON = 12
let providerResizeObserver = null

function updateProviderControls() {
  const rail = providerRailRef.value
  if (!rail) return
  canScrollLeft.value = rail.scrollLeft > START_EPSILON
  canScrollRight.value = rail.scrollLeft + rail.clientWidth < rail.scrollWidth - 2
}

function scrollProviderRail(direction) {
  const rail = providerRailRef.value
  if (!rail) return
  rail.scrollBy({ left: direction * Math.max(rail.clientWidth * 0.82, 220), behavior: 'smooth' })
}

async function resetProviderRailPosition() {
  canScrollLeft.value = false
  await nextTick()
  const rail = providerRailRef.value
  if (!rail) return
  rail.scrollTo({ left: 0, behavior: 'auto' })
  requestAnimationFrame(() => requestAnimationFrame(updateProviderControls))
}

function freezeProviderChipSize(chip, nameEl) {
  const chipWidth = chip.getBoundingClientRect().width
  const nameWidth = nameEl.getBoundingClientRect().width
  const chipPixels = chipWidth + 'px'
  const namePixels = nameWidth + 'px'
  chip.style.width = chipPixels
  chip.style.minWidth = chipPixels
  chip.style.maxWidth = chipPixels
  chip.style.flex = '0 0 ' + chipPixels
  nameEl.style.width = namePixels
  nameEl.style.minWidth = namePixels
  nameEl.style.maxWidth = namePixels
  nameEl.style.flex = '0 0 ' + namePixels
}

function releaseProviderChipSize(chip) {
  const nameEl = chip.querySelector('.movie-provider-name')
  chip.style.removeProperty('width')
  chip.style.removeProperty('min-width')
  chip.style.removeProperty('max-width')
  chip.style.removeProperty('flex')
  nameEl?.style.removeProperty('width')
  nameEl?.style.removeProperty('min-width')
  nameEl?.style.removeProperty('max-width')
  nameEl?.style.removeProperty('flex')
}

function handleProviderMouseEnter(event) {
  const chip = event.currentTarget
  const nameEl = chip.querySelector('.movie-provider-name')
  const singleTextEl = chip.querySelector('.marquee-text')
  
  if (!nameEl || !singleTextEl) return
  
  chip.dataset.shouldStop = 'false'
  if (chip._marqueeTimeoutId) {
    clearTimeout(chip._marqueeTimeoutId)
    chip._marqueeTimeoutId = null
  }
  
  const isOverflow = singleTextEl.offsetWidth > nameEl.clientWidth
  if (isOverflow) {
    freezeProviderChipSize(chip, nameEl)
    chip.classList.add('is-overflowing')
    chip._marqueeTimeoutId = setTimeout(() => {
      chip.classList.add('is-animating')
      chip._marqueeTimeoutId = null
    }, 1000)
  }
}

function handleProviderMouseLeave(event) {
  const chip = event.currentTarget
  if (chip._marqueeTimeoutId) {
    clearTimeout(chip._marqueeTimeoutId)
    chip._marqueeTimeoutId = null
  }
  
  if (chip.classList.contains('is-animating')) {
    chip.dataset.shouldStop = 'true'
  } else {
    chip.classList.remove('is-overflowing')
    releaseProviderChipSize(chip)
  }
}

function handleProviderAnimationIteration(event) {
  const chip = event.currentTarget
  if (chip.dataset.shouldStop === 'true') {
    chip.classList.remove('is-animating')
    chip.classList.remove('is-overflowing')
    chip.dataset.shouldStop = 'false'
    releaseProviderChipSize(chip)
  }
}
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

const relatedMovies = computed(() => relatedSource.value.slice(0, 8))

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


async function loadSimilarMovies() {
  relatedError.value = ''
  try {
    const response = await getSimilarMovies(route.params.id, { limit: 8 })
    relatedSource.value = response.data || []
  } catch (error) {
    relatedSource.value = []
    relatedError.value = error.message || '请稍后重试。'
  }
}
async function loadMovie() {
  loading.value = true
  errorMessage.value = ''
  movie.value = null
  relatedSource.value = []
  try {
    const detailResponse = await getMovieDetail(route.params.id)
    movie.value = detailResponse.data
    await loadSimilarMovies()
  } catch (error) {
    errorMessage.value = error.message || '影片详情暂时无法加载，请稍后重试。'
  } finally {
    loading.value = false
  }

}

watch(
  () => route.params.id,
  () => {
    loadMovie()
  }
)

watch(() => movie.value?.watchProviders, () => {
  resetProviderRailPosition()
})

onMounted(() => {
  loadMovie()
  if (window.ResizeObserver && providerRailRef.value) {
    providerResizeObserver = new ResizeObserver(updateProviderControls)
    providerResizeObserver.observe(providerRailRef.value)
  }
})

onBeforeUnmount(() => {
  providerResizeObserver?.disconnect()
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

.movie-provider-strip {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  margin-top: 14px;
  width: 100%;
}

.movie-provider-region {
  color: rgba(255, 255, 255, 0.62);
  font-size: 0.7rem;
  font-weight: 650;
  letter-spacing: 0.04em;
}

.provider-rail-shell {
  position: relative;
  width: 100%;
  max-width: 100%;
}

.movie-provider-list {
  display: grid;
  grid-auto-flow: column;
  grid-template-rows: repeat(2, 38px);
  grid-auto-columns: max-content;
  align-items: start;
  gap: 8px;
  overflow-x: auto;
  width: 100%;
  padding-bottom: 2px;
  scrollbar-width: none; /* 隐藏 Firefox 默认滚动条 */
}
.movie-provider-list::-webkit-scrollbar {
  display: none; /* 隐藏 Chrome/Safari 默认滚动条 */
}

.provider-rail-control {
  position: absolute;
  top: 50%;
  z-index: 3;
  display: grid;
  width: 28px;
  height: 48px;
  padding: 0;
  place-items: center;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 999px;
  color: #ffffff;
  background: rgba(20, 20, 20, 0.65);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  cursor: pointer;
  opacity: 0;
  visibility: hidden;
  pointer-events: none;
  transform: translateY(-50%) scale(.94);
  transition: opacity 160ms ease, transform 160ms ease, background 160ms ease;
}

.provider-rail-shell:hover .provider-rail-control {
  opacity: 1;
  visibility: visible;
  pointer-events: auto;
  transform: translateY(-50%) scale(1);
}

.provider-rail-shell:hover .provider-rail-control:hover {
  background: rgba(20, 20, 20, 0.85);
  transform: translateY(-50%) scale(1.06);
}

.provider-rail-control span {
  font-size: 24px;
  font-weight: 300;
  line-height: 1;
  transform: translateY(-1px);
}

.provider-rail-control-left {
  left: -12px;
}
.provider-rail-control-right {
  right: -12px;
}

.movie-provider-chip {
  flex-shrink: 0;
  display: inline-flex;
  width: max-content;
  justify-self: start;
  align-items: center;
  gap: 8px;
  height: 38px;
  box-sizing: border-box;
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
  --name-width: 130px;
  width: fit-content;
  max-width: var(--name-width);
  flex: 0 1 auto;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
  color: #ffffff;
  font-size: 0.75rem;
  font-weight: 650;
}

.movie-provider-chip.is-animating .movie-provider-name {
  text-overflow: clip; /* 正在执行动画时关闭省略号，防止完成最后一轮循环时省略号闪烁 */
}

.movie-provider-name-inner {
  display: inline-flex;
  align-items: center;
  white-space: nowrap;
}

.marquee-spacer {
  display: none;
  width: 32px;
  flex-shrink: 0;
}

.marquee-text-duplicate,
.marquee-spacer-duplicate {
  display: none;
}

/* 当且仅当文字溢出时，显示复制的文字和间距，以备跑马灯无缝滚动 */
.movie-provider-chip.is-overflowing .marquee-spacer {
  display: inline-block;
}

.movie-provider-chip.is-overflowing .marquee-text-duplicate {
  display: inline-block;
}

/* 仅在文字实际超出最大宽度时，悬停 1s 后开始匀速无限跑马灯循环滚动 */
@keyframes provider-marquee {
  0% {
    transform: translateX(0);
  }
  100% {
    /* 滚动到 -50%（正好是一个完整的文字加一个间距的距离）即可实现无缝循环 */
    transform: translateX(-50%);
  }
}

.movie-provider-chip.is-animating .movie-provider-name-inner {
  animation: provider-marquee 8s linear infinite;
}

.movie-provider-type {
  margin-left: auto; /* 将“租赁/订阅”等文案推至胶囊最右侧 */
  flex-shrink: 0;
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
    --name-width: 95px;
  }

  .movie-info-grid {
    padding: 24px 22px;
  }

  .movie-info-groups {
    grid-template-columns: 1fr;
    gap: 28px;
  }
}.related-empty-state{display:grid;gap:8px;padding:30px}.related-empty-state h2,.related-empty-state p{margin:0}.related-empty-state p{color:var(--text-secondary)}.detail-page-state{display:grid;min-height:70vh;padding-top:110px;place-items:center}.detail-state-card{display:grid;min-width:min(520px,92vw);justify-items:center;gap:14px;padding:44px;text-align:center}.detail-state-card h1,.detail-state-card p{margin:0}.detail-state-card>p{color:var(--text-secondary)}.state-orbit{width:34px;height:34px;border:2px solid var(--border-soft);border-top-color:var(--accent-primary);border-radius:50%;animation:state-spin .8s linear infinite}@keyframes state-spin{to{transform:rotate(360deg)}}
</style>

<template>
  <section class="min-w-0 space-y-6">
    <div class="hero-backdrop surface-card" :style="heroStyle" v-loading="loading">
      <div class="hero-content flex min-h-[420px] flex-col justify-end p-6 md:p-10">
        <div class="flex flex-wrap items-start justify-between gap-4">
          <div>
            <p class="section-kicker text-xs">Dashboard</p>
            <h2 class="mt-3 max-w-3xl text-4xl font-semibold tracking-tight md:text-6xl">{{ heroMovie?.title || '内容与用户表现总览' }}</h2>
            <p class="mt-4 max-w-2xl text-base text-[var(--text-secondary)] md:text-lg">{{ heroMovie?.overview || '聚合用户增长、互动活跃度与片库分布，用更接近流媒体首页的方式展示后台信息。' }}</p>
          </div>
          <div class="flex flex-wrap gap-3">
            <button v-for="item in rangeOptions" :key="item" class="pill-button" :class="{ 'is-active': range === item }" @click="changeRange(item)">{{ item }} 天</button>
          </div>
        </div>
      </div>
    </div>

    <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
      <article v-for="card in summaryCards" :key="card.label" class="surface-card p-5">
        <p class="metric-value">{{ card.value }}</p>
        <p class="mt-3 metric-label">{{ card.label }}</p>
        <p class="mt-2 text-sm text-[var(--text-muted)]">{{ card.hint }}</p>
      </article>
    </div>

    <div class="grid gap-6 xl:grid-cols-[1.3fr_0.7fr]">
      <section class="surface-card p-5 md:p-6" v-loading="loading">
        <div class="mb-5 flex items-center justify-between gap-3">
          <div>
            <p class="section-kicker text-xs">Growth</p>
            <h3 class="mt-2 text-2xl font-semibold">用户增长趋势</h3>
          </div>
        </div>
        <DashboardChart :option="userGrowthOption" height="320px" />
      </section>

      <section class="surface-card p-5 md:p-6" v-loading="loading">
        <p class="section-kicker text-xs">Activity</p>
        <h3 class="mt-2 text-2xl font-semibold">日活跃互动</h3>
        <DashboardChart :option="activityOption" height="320px" />
      </section>
    </div>

    <div class="grid gap-6 lg:grid-cols-2 xl:grid-cols-4">
      <section class="surface-card p-5 md:p-6">
        <p class="section-kicker text-xs">Library</p>
        <h3 class="mt-2 text-xl font-semibold">分类分布</h3>
        <DashboardChart :option="categoryOption" height="280px" />
      </section>
      <section class="surface-card p-5 md:p-6">
        <p class="section-kicker text-xs">Scores</p>
        <h3 class="mt-2 text-xl font-semibold">评分分布</h3>
        <DashboardChart :option="ratingOption" height="280px" />
      </section>
      <section class="surface-card p-5 md:p-6">
        <p class="section-kicker text-xs">Release Years</p>
        <h3 class="mt-2 text-xl font-semibold">年份分布</h3>
        <DashboardChart :option="yearOption" height="280px" />
      </section>
      <section class="surface-card p-5 md:p-6">
        <p class="section-kicker text-xs">Regions</p>
        <h3 class="mt-2 text-xl font-semibold">地区分布</h3>
        <DashboardChart :option="regionOption" height="280px" />
      </section>
    </div>

    <div class="grid min-w-0 gap-6 xl:grid-cols-[1.05fr_0.95fr]">
      <section class="surface-card p-5 md:p-6">
        <div class="flex items-center justify-between gap-3">
          <div>
            <p class="section-kicker text-xs">Ranking</p>
            <h3 class="mt-2 text-2xl font-semibold">内容榜单</h3>
          </div>
          <div class="flex gap-2">
            <button class="pill-button" :class="{ 'is-active': rankingTab === 'favorite' }" @click="rankingTab = 'favorite'">收藏榜</button>
            <button class="pill-button" :class="{ 'is-active': rankingTab === 'view' }" @click="rankingTab = 'view'">浏览榜</button>
          </div>
        </div>
        <div class="mt-5 space-y-3">
          <article v-for="(item, index) in activeRanking" :key="`${rankingTab}-${item.id}`" class="surface-card-soft flex items-center gap-4 p-3">
            <div class="w-8 text-center text-sm font-semibold text-[var(--text-muted)]">{{ index + 1 }}</div>
            <img v-if="item.posterUrl" :src="item.posterUrl" :alt="item.title" class="h-20 w-14 rounded-2xl object-cover" />
            <div v-else class="h-20 w-14 rounded-2xl" style="background: var(--poster-fallback)"></div>
            <div class="min-w-0 flex-1">
              <h4 class="truncate text-base font-medium">{{ item.title }}</h4>
              <p class="mt-1 text-sm text-[var(--text-muted)]">评分 {{ item.averageRating || '0.0' }}</p>
            </div>
            <div class="text-right">
              <p class="text-xl font-semibold">{{ item.metricValue }}</p>
              <p class="text-xs text-[var(--text-muted)]">{{ item.metricLabel }}</p>
            </div>
          </article>
        </div>
      </section>

      <section class="surface-card min-w-0 overflow-hidden p-5 md:p-6">
        <div class="flex items-center justify-between gap-3">
          <div>
            <p class="section-kicker text-xs">Highlights</p>
            <h3 class="mt-2 text-2xl font-semibold">热门影片条带</h3>
          </div>
          <button class="pill-button" @click="fetchStatistics">刷新</button>
        </div>
        <div class="mt-5">
          <ContentRail title="" :movies="stats?.hotMovies || []" controls />
        </div>
      </section>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getAdminStatistics } from '@/api/admin'
import ContentRail from '@/components/ContentRail.vue'
import DashboardChart from '@/components/DashboardChart.vue'
import { useThemeMode } from '@/utils/theme'

const themeMode = useThemeMode()
const loading = ref(false)
const range = ref(30)
const stats = ref(null)
const rankingTab = ref('favorite')
const rangeOptions = [7, 30, 90]

const axisColor = computed(() => themeMode.value === 'dark' ? '#a8a197' : '#7b746a')
const lineColor = computed(() => themeMode.value === 'dark' ? '#ff8d4f' : '#ef7b45')
const barColor = computed(() => themeMode.value === 'dark' ? '#ffc18f' : '#c96d3f')
const panelColor = computed(() => themeMode.value === 'dark' ? 'rgba(255,255,255,0.08)' : 'rgba(0,0,0,0.06)')

const heroMovie = computed(() => stats.value?.hotMovies?.[0] || null)
const heroStyle = computed(() => {
  const image = heroMovie.value?.backdropUrl || heroMovie.value?.posterUrl
  return image
    ? { background: `linear-gradient(115deg, rgba(0,0,0,0.28), rgba(0,0,0,0.06)), url(${image}) center/cover` }
    : {}
})
const summaryCards = computed(() => {
  const summary = stats.value?.summary || {}
  return [
    { label: '总用户', value: summary.totalUsers || 0, hint: `今日新增 ${summary.todayUsers || 0}` },
    { label: '总影片', value: summary.totalMovies || 0, hint: '当前可运营内容规模' },
    { label: '总评论', value: summary.totalComments || 0, hint: `今日新增 ${summary.todayComments || 0}` },
    { label: '总收藏 / 评分', value: `${summary.totalFavorites || 0} / ${summary.totalRatings || 0}`, hint: '互动热度与口碑信号' }
  ]
})
const activeRanking = computed(() => rankingTab.value === 'favorite' ? (stats.value?.favoriteRanking || []) : (stats.value?.viewRanking || []))

function baseChart() {
  return {
    textStyle: { color: axisColor.value },
    grid: { left: 24, right: 16, top: 26, bottom: 24, containLabel: true },
    tooltip: { trigger: 'axis', backgroundColor: panelColor.value, borderWidth: 0, textStyle: { color: themeMode.value === 'dark' ? '#f5f1ea' : '#161615' } },
    xAxis: { axisLine: { lineStyle: { color: panelColor.value } }, axisLabel: { color: axisColor.value } },
    yAxis: { splitLine: { lineStyle: { color: panelColor.value } }, axisLabel: { color: axisColor.value } }
  }
}

const userGrowthOption = computed(() => ({
  ...baseChart(),
  xAxis: { ...baseChart().xAxis, type: 'category', data: (stats.value?.userGrowth || []).map((item) => item.date.slice(5)) },
  yAxis: { ...baseChart().yAxis, type: 'value' },
  series: [{ type: 'line', smooth: true, data: (stats.value?.userGrowth || []).map((item) => item.value), lineStyle: { color: lineColor.value, width: 3 }, itemStyle: { color: lineColor.value }, areaStyle: { color: 'rgba(239,123,69,0.14)' } }]
}))
const activityOption = computed(() => ({
  ...baseChart(),
  legend: { textStyle: { color: axisColor.value } },
  xAxis: { ...baseChart().xAxis, type: 'category', data: (stats.value?.dailyActivity || []).map((item) => item.date.slice(5)) },
  yAxis: { ...baseChart().yAxis, type: 'value' },
  series: [
    { name: '评论', type: 'bar', barMaxWidth: 18, data: (stats.value?.dailyActivity || []).map((item) => item.comments), itemStyle: { color: lineColor.value, borderRadius: [8, 8, 0, 0] } },
    { name: '收藏', type: 'bar', barMaxWidth: 18, data: (stats.value?.dailyActivity || []).map((item) => item.favorites), itemStyle: { color: barColor.value, borderRadius: [8, 8, 0, 0] } }
  ]
}))
const categoryOption = computed(() => pieOption(stats.value?.categoryDistribution || []))
const ratingOption = computed(() => pieOption(stats.value?.ratingDistribution || []))
const regionOption = computed(() => pieOption(stats.value?.regionDistribution || []))
const yearOption = computed(() => ({
  ...baseChart(),
  xAxis: { ...baseChart().xAxis, type: 'category', data: (stats.value?.yearDistribution || []).map((item) => item.name) },
  yAxis: { ...baseChart().yAxis, type: 'value' },
  series: [{ type: 'bar', data: (stats.value?.yearDistribution || []).map((item) => item.value), itemStyle: { color: lineColor.value, borderRadius: [10, 10, 0, 0] } }]
}))

function pieOption(source) {
  return {
    tooltip: { trigger: 'item', backgroundColor: panelColor.value, borderWidth: 0, textStyle: { color: themeMode.value === 'dark' ? '#f5f1ea' : '#161615' } },
    legend: { bottom: 0, textStyle: { color: axisColor.value } },
    series: [{ type: 'pie', radius: ['48%', '72%'], itemStyle: { borderRadius: 12, borderColor: 'transparent', borderWidth: 4 }, label: { color: axisColor.value }, data: source.map((item, index) => ({ ...item, itemStyle: { color: index % 2 === 0 ? lineColor.value : barColor.value } })) }]
  }
}

async function fetchStatistics() {
  loading.value = true
  try {
    const response = await getAdminStatistics(range.value)
    stats.value = response.data
  } finally {
    loading.value = false
  }
}

function changeRange(nextRange) {
  range.value = nextRange
  fetchStatistics()
}

onMounted(() => fetchStatistics())
</script>

<style scoped>
.hero-content {
  mix-blend-mode: normal !important;
}

.hero-content .pill-button {
  background: rgba(255, 255, 255, 0.14) !important;
  border: 1px solid rgba(255, 255, 255, 0.28) !important;
  color: rgba(255, 255, 255, 0.9) !important;
  text-shadow: 0 1px 2px rgba(0,0,0,0.2);
}

.hero-content .pill-button:hover {
  background: rgba(255, 255, 255, 0.25) !important;
  border-color: rgba(255, 255, 255, 0.45) !important;
  color: #ffffff !important;
}

.hero-content .pill-button.is-active {
  background: #ffffff !important;
  color: #1d1d1f !important;
  border-color: #ffffff !important;
  font-weight: 600 !important;
}
</style>

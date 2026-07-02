<template>
  <section class="space-y-6 pt-20">
    <div class="surface-card p-6">
      <p class="section-kicker">Movie Library</p>
      <h1 class="mt-3 text-3xl font-semibold md:text-5xl">影视库</h1>
      <div class="filters">
        <div class="filter-row search-row">
          <el-input v-model="query.keyword" clearable placeholder="搜索影片标题" class="filter-input" @keyup.enter="search" />
          <button class="pill-button is-active" @click="search">查询</button>
          <button class="pill-button" @click="reset">重置</button>
        </div>

        <div class="filter-row category-row">
          <span class="filter-label">类型</span>
          <div class="category-area">
            <div ref="categoryViewport" class="category-viewport" :class="{ 'is-expanded': categoryExpanded }" :style="categoryViewportStyle">
              <div ref="categoryGroup" class="pill-group">
                <button
                  v-for="category in options.categories"
                  :key="category"
                  class="pill-button"
                  :class="{ 'is-active': query.categories.includes(category) }"
                  @click="toggleCategory(category)"
                >
                  {{ category }}
                </button>
              </div>
            </div>
            <button
              v-if="categoryOverflow"
              class="category-toggle"
              type="button"
              :aria-expanded="categoryExpanded"
              @click="categoryExpanded = !categoryExpanded"
            >
              {{ categoryExpanded ? '收起' : '展开' }}
              <span class="toggle-arrow" :class="{ 'is-expanded': categoryExpanded }">⌄</span>
            </button>
          </div>
        </div>

        <div class="filter-toolbar">
          <div class="filter-field">
            <span class="filter-label">地区</span>
            <el-select v-model="query.region" clearable filterable placeholder="全部地区" @change="applyFilter">
              <el-option v-for="region in options.regions" :key="region" :label="region" :value="region" />
            </el-select>
          </div>
          <div class="filter-field">
            <span class="filter-label">年份</span>
            <el-select v-model="query.year" clearable filterable placeholder="全部年份" @change="applyFilter">
              <el-option v-for="year in options.years" :key="year" :label="String(year)" :value="year" />
            </el-select>
          </div>
          <div class="filter-field">
            <span class="filter-label">排序</span>
            <el-select v-model="query.sort" placeholder="排序方式" @change="applyFilter">
              <el-option v-for="item in sortOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </div>
        </div>
      </div>
    </div>

    <div v-loading="loading">
      <MovieList :movies="movies" />
      <el-empty v-if="!loading && !movies.length" description="没有符合条件的影片" />
    </div>
    <div class="pager">
      <el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total" layout="prev, pager, next, jumper, total" @current-change="goPage" />
    </div>
  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { getMovieFilters, getMoviePage } from '@/api/movie'
import MovieList from '@/components/MovieList.vue'

const movies = ref([])
const total = ref(0)
const loading = ref(false)
const options = reactive({ categories: [], regions: [], years: [] })
const query = reactive({
  keyword: '',
  categories: [],
  region: '',
  year: '',
  sort: 'hot',
  pageNum: 1,
  pageSize: 20
})

const categoryViewport = ref(null)
const categoryGroup = ref(null)
const categoryExpanded = ref(false)
const categoryOverflow = ref(false)
const collapsedCategoryHeight = ref(0)
let categoryResizeObserver

const sortOptions = [
  { value: 'hot', label: '热门优先' },
  { value: 'rating-desc', label: '评分从高到低' },
  { value: 'rating-asc', label: '评分从低到高' },
  { value: 'favorite-desc', label: '收藏最多' },
  { value: 'release-desc', label: '上映时间从新到旧' },
  { value: 'release-asc', label: '上映时间从旧到新' }
]

const categoryViewportStyle = computed(() => {
  if (categoryExpanded.value || !categoryOverflow.value) return undefined
  return { maxHeight: collapsedCategoryHeight.value + 'px' }
})

function measureCategoryRows() {
  const buttons = [...(categoryGroup.value?.children || [])]
  if (!buttons.length) {
    categoryOverflow.value = false
    return
  }

  const firstRowTop = buttons[0].offsetTop
  const firstRowButtons = buttons.filter((button) => button.offsetTop === firstRowTop)
  const firstRowBottom = Math.max(...firstRowButtons.map((button) => button.offsetTop + button.offsetHeight))
  collapsedCategoryHeight.value = firstRowBottom - firstRowTop
  categoryOverflow.value = firstRowButtons.length < buttons.length
  if (!categoryOverflow.value) categoryExpanded.value = false
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

async function load() {
  const params = {
    keyword: query.keyword || undefined,
    sort: query.sort,
    pageNum: query.pageNum,
    pageSize: query.pageSize
  }
  if (query.categories.length) params.categories = query.categories
  if (query.region) params.regions = [query.region]
  if (query.year !== undefined && query.year !== null && query.year !== '') params.years = [query.year]

  loading.value = true
  try {
    const response = await getMoviePage(params)
    movies.value = response.data?.records || []
    total.value = Number(response.data?.total || 0)
  } finally {
    loading.value = false
  }
}

function applyFilter() {
  query.pageNum = 1
  load()
  scrollToTop()
}

function toggleCategory(category) {
  const index = query.categories.indexOf(category)
  if (index === -1) query.categories.push(category)
  else query.categories.splice(index, 1)
  applyFilter()
}


function goPage(page) {
  query.pageNum = page
  load()
  scrollToTop()
}

function search() {
  applyFilter()
}

function reset() {
  query.keyword = ''
  query.categories.splice(0)
  query.region = ''
  query.year = ''
  query.sort = 'hot'
  categoryExpanded.value = false
  applyFilter()
}

onMounted(async () => {
  const response = await getMovieFilters()
  Object.assign(options, response.data || {})
  await nextTick()

  categoryResizeObserver = new ResizeObserver(measureCategoryRows)
  if (categoryViewport.value) categoryResizeObserver.observe(categoryViewport.value)
  measureCategoryRows()
  await load()
})

onBeforeUnmount(() => categoryResizeObserver?.disconnect())
</script>

<style scoped>
.filters { margin-top: 24px; display: flex; flex-direction: column; gap: 16px; }
.filter-row { display: flex; align-items: flex-start; gap: 12px; }
.search-row { align-items: center; flex-wrap: wrap; }
.filter-input { flex: 1; min-width: 200px; max-width: 360px; }
.filter-label { flex: 0 0 36px; padding-top: 8px; font-size: 0.82rem; font-weight: 650; color: var(--text-secondary); white-space: nowrap; }
.category-area { min-width: 0; flex: 1; }
.category-viewport { overflow: hidden; transition: max-height 0.25s ease; }
.category-viewport.is-expanded { max-height: 500px; }
.pill-group { display: flex; flex-wrap: wrap; gap: 8px; }
.category-toggle {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 10px;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--text-secondary);
  font-size: 0.82rem;
  cursor: pointer;
  transition: color 0.2s ease;
}
.category-toggle:hover { color: var(--text-primary); }
.toggle-arrow { display: inline-block; line-height: 1; transition: transform 0.2s ease; }
.toggle-arrow.is-expanded { transform: rotate(180deg); }
.filter-toolbar { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 20px 32px; }
.filter-field { display: flex; align-items: flex-start; gap: 12px; min-width: 0; }
.filter-field .el-select { flex: 1; min-width: 0; }
.pager { display: flex; justify-content: center; margin-top: 24px; }

@media (max-width: 600px) {
  .filter-input { width: 100%; max-width: 100%; }
  .category-row { align-items: flex-start; }
  .filter-toolbar { grid-template-columns: 1fr; gap: 12px; }
  .filter-field { width: 100%; }
}
</style>
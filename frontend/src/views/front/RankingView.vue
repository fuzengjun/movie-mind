<template>
  <section class="ranking-page space-y-6 pt-20">
    <div class="surface-card ranking-head">
      <div>
        <p class="section-kicker">Rankings</p>
        <h1>影片排行榜</h1>
        <p class="ranking-description">从评分、收藏、浏览与上映时间等维度，发现片库中值得关注的作品。</p>
      </div>
      <div class="ranking-tabs" role="tablist" aria-label="排行榜类型">
        <button v-for="item in types" :key="item.value" class="pill-button" :class="{'is-active':type===item.value}"
                @click="select(item.value)">{{ item.label }}
        </button>
      </div>
    </div>

    <div v-loading="loading" class="ranking-results">
      <div v-if="errorMessage && !loading" class="surface-card state-panel">
        <p class="section-kicker">Unable to load</p>
        <h2>排行榜暂时无法加载</h2>
        <p>{{ errorMessage }}</p>
        <button class="pill-button is-active" @click="load">重新加载</button>
      </div>
      <template v-else>
        <MovieList :movies="movies"/>
        <el-empty v-if="!loading&&!movies.length" description="当前榜单暂无影片"/>
      </template>
    </div>

    <div class="pager-container" v-if="!errorMessage && total > pageSize">
      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        prev-text="上一页"
        next-text="下一页"
        @current-change="goPage"
      />
      <div class="pager-info">
        共 {{ Math.ceil(total / pageSize) }} 页 / {{ total }} 个，跳至
        <input
          type="number"
          v-model.number="jumperPage"
          min="1"
          :max="Math.ceil(total / pageSize)"
          class="pager-jumper-input"
          @keyup.enter="handleJumper"
        />
        页
      </div>
    </div>
  </section>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {getRankings} from '@/api/movie'
import MovieList from '@/components/MovieList.vue'

const type = ref('rating'), movies = ref([]), loading = ref(false), errorMessage = ref(''), total = ref(0),
    pageNum = ref(1)
const pageSize = 20
const types = [{value: 'rating', label: '高分榜'}, {value: 'favorite', label: '收藏榜'}, {
  value: 'view',
  label: '浏览榜'
}, {value: 'latest', label: '最新榜'}, {value: 'user-rating', label: '用户评分榜'}]

async function load() {
  loading.value = true;
  errorMessage.value = ''
  try {
    const response = await getRankings({type: type.value, pageNum: pageNum.value, pageSize});
    movies.value = response.data?.records || [];
    total.value = Number(response.data?.total || 0)
  } catch (error) {
    movies.value = [];
    total.value = 0;
    errorMessage.value = error.message || '请检查网络连接或稍后重试。'
  } finally {
    loading.value = false
  }
}

function select(value) {
  type.value = value;
  pageNum.value = 1;
  load()
}

function goPage(page) {
  pageNum.value = page;
  load();
  window.scrollTo({top: 0, behavior: 'smooth'})
}

const jumperPage = ref('')
function handleJumper() {
  const p = parseInt(jumperPage.value, 10)
  const maxPage = Math.ceil(total.value / pageSize)
  if (p >= 1 && p <= maxPage) {
    goPage(p)
    jumperPage.value = ''
  }
}

onMounted(load)
</script>

<style scoped>
.ranking-head {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 32px
}

.ranking-head h1 {
  margin: 8px 0 10px;
  font-size: clamp(2rem, 5vw, 3.5rem)
}

.ranking-description {
  max-width: 620px;
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.7
}

.ranking-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 10px
}

.ranking-results {
  min-height: 280px
}

.pager-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-top: 24px;
  flex-wrap: wrap;
}

.pager-info {
  font-size: 0.88rem;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.pager-jumper-input {
  width: 54px;
  height: 30px;
  border: 1px solid var(--border-soft);
  border-radius: 6px;
  background-color: var(--surface-secondary);
  color: var(--text-primary);
  text-align: center;
  font-size: 0.88rem;
  outline: none;
  transition: border-color 0.2s, background-color 0.2s;
  box-sizing: border-box;
  -moz-appearance: textfield;
  appearance: textfield;
}

.pager-jumper-input::-webkit-outer-spin-button,
.pager-jumper-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  appearance: none;
  margin: 0;
}

.pager-jumper-input:focus {
  border-color: var(--text-primary);
  background-color: var(--bg-secondary);
}

:deep(.el-pagination) {
  --el-pagination-button-bg-color: var(--surface-secondary) !important;
  --el-pagination-hover-color: var(--text-primary) !important;
  --el-pagination-button-color: var(--text-secondary) !important;
  --el-pagination-button-disabled-color: var(--text-muted) !important;
}

:deep(.el-pagination .btn-next), :deep(.el-pagination .btn-prev) {
  padding: 0 12px !important;
  border-radius: 6px !important;
  border: 1px solid var(--border-soft) !important;
  background-color: var(--surface-secondary) !important;
  color: var(--text-secondary) !important;
  font-size: 13px !important;
  height: 32px !important;
  line-height: 30px !important;
  transition: all 0.2s !important;
}

:deep(.el-pagination .btn-next:hover), :deep(.el-pagination .btn-prev:hover) {
  color: var(--text-primary) !important;
  border-color: var(--text-primary) !important;
}

:deep(.el-pagination .el-pager li) {
  border-radius: 6px !important;
  border: 1px solid var(--border-soft) !important;
  background-color: var(--surface-secondary) !important;
  color: var(--text-secondary) !important;
  font-size: 14px !important;
  min-width: 32px !important;
  height: 32px !important;
  line-height: 30px !important;
  margin: 0 2px !important;
  font-weight: 500 !important;
  transition: all 0.2s !important;
}

:deep(.el-pagination .el-pager li:hover) {
  color: var(--text-primary) !important;
  border-color: var(--text-primary) !important;
}

:deep(.el-pagination .el-pager li.is-active) {
  background-color: var(--text-primary) !important;
  border-color: var(--text-primary) !important;
  color: var(--bg-primary) !important;
}

.state-panel {
  display: grid;
  justify-items: start;
  gap: 10px;
  padding: 36px
}

.state-panel h2, .state-panel p {
  margin: 0
}

.state-panel p {
  color: var(--text-secondary)
}
</style>

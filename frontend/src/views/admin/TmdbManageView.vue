<template>
  <AdminPage title="TMDB 数据管理" kicker="External Source" hint="从 TMDB 批量同步热门影片、类型、演职员与海报资料。">
    <div class="surface-card tmdb-search-panel">
      <div><h3>按名称搜索并导入</h3><p>输入影片名称，从 TMDB 候选结果中选择目标影片导入本地片库。</p></div>
      <div class="tmdb-search-bar"><el-input v-model="keyword" clearable placeholder="例如：星际穿越" @keyup.enter="runSearch"/><button class="pill-button is-active" :disabled="searchLoading" @click="runSearch">搜索 TMDB</button></div>
      <div v-if="searchResults.length" class="tmdb-results">
        <article v-for="movie in searchResults" :key="movie.tmdbId" class="tmdb-result">
          <img v-if="movie.posterUrl" :src="movie.posterUrl" :alt="movie.title"/>
          <div><h4>{{ movie.title }}</h4><p>{{ movie.originalTitle }} · {{ movie.releaseDate || '上映日期未知' }} · {{ Number(movie.rating||0).toFixed(1) }} 分</p><small>{{ movie.overview || '暂无简介' }}</small></div>
          <button class="pill-button" :class="{'is-active':!movie.imported}" :disabled="movie.imported||importingId===movie.tmdbId" @click="importOne(movie)">{{ movie.imported ? '已导入' : importingId===movie.tmdbId ? '导入中…' : '选择导入' }}</button>
        </article>
      </div>
      <el-empty v-else-if="searched&&!searchLoading" description="没有找到匹配影片"/>
    </div>
    <div class="tmdb-ops-grid">
      <!-- 添加新影片 -->
      <div class="surface-card admin-import-panel">
        <div>
          <h3>添加新影片</h3>
          <p>从 TMDB 热门榜或高分榜导入本地尚未收录的影片，自动跳过已存在影片并继续翻页补足。</p>
        </div>
        <el-form-item label="影片来源榜单">
          <el-radio-group v-model="addSource">
            <el-radio-button value="popular">热门榜</el-radio-button>
            <el-radio-button value="top-rated">高分榜</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-slider v-model="addLimit" :min="1" :max="100" show-input />
        <button class="pill-button is-active" :disabled="addLoading" @click="runAdd">
          {{ addButtonText }}
        </button>
        <div v-if="addResult" class="admin-import-result">
          <div><b>{{ addResult.imported }}</b><span>新增入库</span></div>
          <div><b>{{ addResult.skipped }}</b><span>跳过 / 失败</span></div>
          <div><b>{{ addResult.requested }}</b><span>请求数量</span></div>
        </div>
        <el-alert
          v-if="addResult?.errors?.length"
          title="部分影片未能添加"
          type="warning"
          :closable="false"
          show-icon
        >
          <template #default>
            <p v-for="message in addResult.errors" :key="message" class="import-error-line">{{ message }}</p>
          </template>
        </el-alert>
      </div>

      <!-- 刷新已有影片 -->
      <div class="surface-card admin-import-panel">
        <div>
          <h3>刷新已有影片</h3>
          <p>更新本地已有影片的最新 TMDB 数据，优先刷新最久未更新的影片。</p>
        </div>
        <el-slider v-model="refreshLimit" :min="1" :max="100" show-input />
        <button class="pill-button is-active" :disabled="refreshLoading" @click="runRefresh">
          {{ refreshLoading ? `正在刷新影片数据…` : `刷新 ${refreshLimit} 部影片` }}
        </button>
        <div v-if="refreshResult" class="admin-import-result">
          <div><b>{{ refreshResult.updated }}</b><span>已更新</span></div>
          <div><b>{{ refreshResult.skipped }}</b><span>跳过 / 失败</span></div>
          <div><b>{{ refreshResult.requested }}</b><span>请求数量</span></div>
        </div>
        <el-alert
          v-if="refreshResult?.errors?.length"
          title="部分影片刷新失败"
          type="warning"
          :closable="false"
          show-icon
        >
          <template #default>
            <p v-for="message in refreshResult.errors" :key="message" class="import-error-line">{{ message }}</p>
          </template>
        </el-alert>
      </div>
    </div>
  </AdminPage>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { addNewMovies, refreshExistingMovies } from '@/api/adminManagement'
import { searchTmdbMovies, importTmdbMovie } from '@/api/tmdb'
import AdminPage from '@/components/admin/AdminPage.vue'

const keyword = ref('')
const searchLoading = ref(false)
const searched = ref(false)
const searchResults = ref([])
const importingId = ref(null)

async function runSearch() {
  if (!keyword.value.trim()) return ElMessage.warning('请输入影片名称')
  searchLoading.value = true
  try { searchResults.value = (await searchTmdbMovies(keyword.value.trim())).data || []; searched.value = true }
  finally { searchLoading.value = false }
}
async function importOne(movie) {
  importingId.value = movie.tmdbId
  try { await importTmdbMovie(movie.tmdbId); movie.imported = true; ElMessage.success('影片已导入本地片库') }
  finally { importingId.value = null }
}
const addSource = ref('popular')
const addSourceLabel = computed(() => addSource.value === 'top-rated' ? '高分榜' : '热门榜')
const addButtonText = computed(() => addLoading.value ? '正在从' + addSourceLabel.value + '添加…' : '从' + addSourceLabel.value + '添加 ' + addLimit.value + ' 部')
const addLimit = ref(20)
const addLoading = ref(false)
const addResult = ref(null)

const refreshLimit = ref(20)
const refreshLoading = ref(false)
const refreshResult = ref(null)

async function runAdd() {
  addLoading.value = true
  addResult.value = null
  try {
    addResult.value = (await addNewMovies(addLimit.value, addSource.value)).data
    if (addResult.value.imported > 0) {
      ElMessage.success(`成功添加 ${addResult.value.imported} 部新影片`)
    } else {
      ElMessage.info('未找到可添加的新影片，本地已包含该榜单中的影片')
    }
  } catch {
    // 请求层统一展示错误，避免同一次失败出现两条提示。
  } finally {
    addLoading.value = false
  }
}

async function runRefresh() {
  refreshLoading.value = true
  refreshResult.value = null
  try {
    refreshResult.value = (await refreshExistingMovies(refreshLimit.value)).data
    if (refreshResult.value.updated > 0) {
      ElMessage.success(`成功刷新 ${refreshResult.value.updated} 部影片的数据`)
    } else {
      ElMessage.warning('没有需要刷新的影片')
    }
  } catch {
    // 请求层统一展示错误，避免同一次失败出现两条提示。
  } finally {
    refreshLoading.value = false
  }
}
</script>

<style scoped>
.tmdb-search-panel { margin-bottom: 24px; padding: 24px; }
.tmdb-search-bar { display: grid; grid-template-columns: 1fr auto; gap: 12px; margin: 18px 0; }
.tmdb-results { display: grid; gap: 12px; }
.tmdb-result { display: grid; grid-template-columns: 64px 1fr auto; gap: 14px; align-items: center; padding: 12px; border: 1px solid var(--border-soft); border-radius: 16px; }
.tmdb-result img { width: 64px; height: 92px; object-fit: cover; border-radius: 10px; }
.tmdb-result h4,.tmdb-result p { margin: 0 0 5px; }
.tmdb-result small { color: var(--text-muted); display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.tmdb-ops-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}
.import-error-line { margin: 4px 0; line-height: 1.5; }
@media (max-width: 960px) {
  .tmdb-search-panel { margin-bottom: 24px; padding: 24px; }
.tmdb-search-bar { display: grid; grid-template-columns: 1fr auto; gap: 12px; margin: 18px 0; }
.tmdb-results { display: grid; gap: 12px; }
.tmdb-result { display: grid; grid-template-columns: 64px 1fr auto; gap: 14px; align-items: center; padding: 12px; border: 1px solid var(--border-soft); border-radius: 16px; }
.tmdb-result img { width: 64px; height: 92px; object-fit: cover; border-radius: 10px; }
.tmdb-result h4,.tmdb-result p { margin: 0 0 5px; }
.tmdb-result small { color: var(--text-muted); display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.tmdb-ops-grid {
    grid-template-columns: 1fr;
  }
}
</style>
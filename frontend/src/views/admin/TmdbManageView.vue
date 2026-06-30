<template>
  <AdminPage title="TMDB 数据管理" kicker="External Source" hint="从 TMDB 批量同步热门影片、类型、演职员与海报资料。">
    <div class="tmdb-ops-grid">
      <!-- 添加新影片 -->
      <div class="surface-card admin-import-panel">
        <div>
          <h3>添加新影片</h3>
          <p>从 TMDB 热门榜导入本地尚未收录的影片，自动跳过已存在的影片并继续翻页补足。</p>
        </div>
        <el-slider v-model="addLimit" :min="1" :max="100" show-input />
        <button class="pill-button is-active" :disabled="addLoading" @click="runAdd">
          {{ addLoading ? `正在添加新影片…` : `添加 ${addLimit} 部新影片` }}
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
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { addNewMovies, refreshExistingMovies } from '@/api/adminManagement'
import AdminPage from '@/components/admin/AdminPage.vue'

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
    addResult.value = (await addNewMovies(addLimit.value)).data
    if (addResult.value.imported > 0) {
      ElMessage.success(`成功添加 ${addResult.value.imported} 部新影片`)
    } else {
      ElMessage.info('未找到可添加的新影片，本地已包含热门榜上的影片')
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
.tmdb-ops-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}
.import-error-line { margin: 4px 0; line-height: 1.5; }
@media (max-width: 960px) {
  .tmdb-ops-grid {
    grid-template-columns: 1fr;
  }
}
</style>
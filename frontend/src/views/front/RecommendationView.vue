<template>
  <section class="recommend-page space-y-8">
    <header class="surface-card recommend-hero">
      <div>
        <p class="section-kicker">Personalized Cinema</p>
        <h1>{{ userStore.profile?.nickname || '你的' }}专属片单</h1>
        <p>{{ summary }}</p>
      </div>
      <button class="pill-button is-active" :disabled="loading" @click="loadRecommendations">
        {{ loading ? '正在计算…' : '重新生成' }}
      </button>
    </header>

    <div class="recommend-stats">
      <div><b>{{ recommendations.length }}</b><span>推荐影片</span></div>
      <div><b>{{ algorithmLabel }}</b><span>当前策略</span></div>
      <div><b>30 min</b><span>推荐缓存</span></div>
    </div>

    <div class="surface-card recommend-content" v-loading="loading">
      <ContentRail
        v-if="recommendations.length"
        title="可能合你口味"
        eyebrow="Because You Watched"
        :description="recommendationDescription"
        :movies="recommendations"
        controls
      />
      <el-empty v-else-if="!loading" description="片库中暂时没有可推荐的影片" />
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getMyRecommend } from '@/api/recommend'
import ContentRail from '@/components/ContentRail.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const recommendations = ref([])

const collaborative = computed(() => recommendations.value.some((item) => item.algorithm === 'USER_CF_COSINE'))
const algorithmLabel = computed(() => collaborative.value ? '协同过滤' : '冷启动')
const summary = computed(() => collaborative.value
  ? '我们找到了与你兴趣相近的观众，并从他们喜欢的影片中整理出这份片单。'
  : '你的行为数据还不多，当前片单综合热门程度、评分与上映时间生成。')

const recommendationDescription = computed(() => collaborative.value
  ? '根据与你观影兴趣相近的用户生成，已自动排除你接触过的影片。'
  : '你的行为数据还不多，暂时结合热门、高分与最新影片为你挑选。')

async function loadRecommendations() {
  loading.value = true
  try {
    const response = await getMyRecommend({ limit: 30 })
    recommendations.value = response.data || []
  } catch {
    recommendations.value = []
  } finally {
    loading.value = false
  }
}

onMounted(loadRecommendations)
</script>

<style scoped>
.recommend-page { padding-top: 6.5rem; }
.recommend-hero { padding: clamp(1.5rem, 5vw, 3rem); display: flex; align-items: flex-end; justify-content: space-between; gap: 2rem; }
.recommend-hero h1 { margin: .65rem 0 .8rem; font-size: clamp(2.4rem, 6vw, 5rem); letter-spacing: -.05em; line-height: 1; }
.recommend-hero p:last-child { max-width: 46rem; margin: 0; color: var(--text-secondary); line-height: 1.7; }
.recommend-stats { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1rem; }
.recommend-stats div { padding: 1.35rem; border: 1px solid var(--border-soft); border-radius: var(--radius-lg); background: var(--surface-secondary); }
.recommend-stats b, .recommend-stats span { display: block; }
.recommend-stats b { font-size: 1.35rem; }
.recommend-stats span { margin-top: .35rem; color: var(--text-muted); font-size: .75rem; }
.recommend-content { min-height: 360px; padding: clamp(1.25rem, 4vw, 2rem); overflow: hidden; }
@media (max-width: 700px) { .recommend-hero { align-items: stretch; flex-direction: column; } .recommend-stats { grid-template-columns: 1fr; } }
</style>
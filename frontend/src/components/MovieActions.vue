<template>
  <section
    v-if="movieId"
    :class="[
      mode === 'actions' ? 'actions-only-bar' : 'glass-panel-premium',
      mode === 'comments' ? 'comments-only-card' : ''
    ]"
  >
    <!-- 动作行：收藏、想看、看过、评分 -->
    <div v-if="mode === 'actions' || mode === 'both'" class="action-row">
      <button class="pill-button" :class="{'is-active': state.favorite}" @click="toggle('favorite')">
        {{ state.favorite ? '已收藏' : '收藏' }}
      </button>
      <button class="pill-button" :class="{'is-active': state.watchlist}" @click="toggle('watchlist')">
        {{ state.watchlist ? '已想看' : '想看' }}
      </button>
      <button class="pill-button" :class="{'is-active': state.watched}" @click="toggle('watched')">
        {{ state.watched ? '已看过' : '看过' }}
      </button>
      <div class="rating">
        <span>我的评分</span>
        <el-rate 
          v-model="rating" 
          :max="5" 
          allow-half 
          :clearable="false" 
          @change="saveRating" 
        />
      </div>
    </div>

    <!-- 评论撰写区域 -->
    <div v-if="mode === 'comments' || mode === 'both'" class="comment-compose">
      <p v-if="mode === 'comments'" class="section-kicker">Review</p>
      <h2 v-if="mode === 'comments'" class="text-xl font-bold text-[var(--text-primary)] border-none mb-3">发表影评</h2>
      <el-input 
        v-model="content" 
        maxlength="500" 
        show-word-limit 
        type="textarea" 
        :rows="3" 
        placeholder="写下你对这部影片的看法" 
      />
      <button class="pill-button is-active mt-2" @click="submit">发表评论</button>
    </div>

    <!-- 评论列表区域 -->
    <div v-if="mode === 'comments' || mode === 'both'" class="comments">
      <h3 v-if="mode === 'both'">用户评论</h3>
      <h3 v-else class="text-lg font-semibold text-[var(--text-primary)] mb-4">
        全部讨论 ({{ comments.length }})
      </h3>
      
      <div v-for="item in comments" :key="item.id" class="comment">
        <div>
          <b>{{ item.nickname }}</b>
          <small>{{ format(item.createTime) }}</small>
        </div>
        <p>{{ item.content }}</p>
        <el-button v-if="item.userId === store.profile?.id" link type="danger" @click="remove(item)">
          删除
        </el-button>
      </div>
      <el-empty v-if="!comments.length" description="还没有评论" />
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  getInteraction,
  recordView,
  setFavorite,
  setWatchlist,
  setWatched,
  setRating,
  getComments,
  addComment,
  deleteOwnComment
} from '@/api/interaction'

const props = defineProps({
  movieId: {
    type: Number,
    required: true
  },
  mode: {
    type: String,
    default: 'both' // 'both' | 'actions' | 'comments'
  }
})

const store = useUserStore()
const router = useRouter()
const state = ref({ favorite: false, watchlist: false, watched: false })
const rating = ref(0)
const content = ref('')
const comments = ref([])

const login = () => {
  if (store.token) return true
  router.push({
    path: '/login',
    query: { redirect: router.currentRoute.value.fullPath }
  })
  return false
}

async function load() {
  if (props.mode === 'comments' || props.mode === 'both') {
    const c = await getComments(props.movieId)
    comments.value = c.data?.records || []
  }
  if (store.token && (props.mode === 'actions' || props.mode === 'both')) {
    const r = await getInteraction(props.movieId)
    state.value = r.data || {}
    rating.value = Number(state.value.rating || 0) / 2 // 10分制转为5星半星制
    await recordView(props.movieId)
  }
}

async function toggle(k) {
  if (!login()) return
  const on = !state.value[k]
  if (k === 'favorite') await setFavorite(props.movieId, on)
  if (k === 'watchlist') await setWatchlist(props.movieId, on)
  if (k === 'watched') await setWatched(props.movieId, on)
  state.value[k] = on
  if (k === 'watched' && on) state.value.watchlist = false
  ElMessage.success(on ? (k === 'watched' ? '已标记为看过' : '操作成功') : '已取消')
}

async function saveRating(v) {
  if (!login()) return
  const backendScore = v * 2 // 5星半星制转回10分制存入数据库
  await setRating(props.movieId, backendScore)
  ElMessage.success('评分已保存')
}

async function submit() {
  if (!login() || !content.value.trim()) return
  await addComment(props.movieId, content.value)
  content.value = ''
  await load()
  ElMessage.success('评论已发布')
}

async function remove(i) {
  await deleteOwnComment(i.id)
  await load()
}

const format = (v) => (v ? new Date(v).toLocaleString('zh-CN') : '')

watch(() => props.movieId, load)
onMounted(load)
</script>

<style scoped>
.interaction-card {
  padding: 24px;
}

.actions-only-bar {
  display: flex;
  align-items: center;
  padding: 12px 0;
  background: transparent;
  box-shadow: none;
  border: none;
  width: 100%;
}

.comments-only-card {
  margin-top: 16px;
  width: 100%;
}

.action-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  width: 100%;
}

.rating {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: auto;
}

.comment-compose {
  display: grid;
  gap: 12px;
  margin-top: 22px;
}

.comments-only-card .comment-compose {
  margin-top: 0;
}

.comment-compose button {
  justify-self: end;
}

.comments {
  margin-top: 24px;
}

.comment {
  position: relative;
  padding: 16px 0;
  border-top: 1px solid var(--border-soft);
}

.comment div {
  display: flex;
  gap: 10px;
  align-items: center;
}

.comment small {
  color: var(--text-muted);
}

.comment p {
  margin: 8px 70px 0 0;
  color: var(--text-secondary);
}

.comment .el-button {
  position: absolute;
  right: 0;
  top: 12px;
}
</style>
<template>
  <div class="ai-assistant" :class="{ 'is-open': isOpen }">
    <button class="ai-fab" @click="toggle" :title="isOpen ? '关闭助手' : 'AI 影片助手'">
      <svg v-if="!isOpen" class="ai-fab-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        <path d="M8 9h8" stroke-linecap="round"/><path d="M8 13h5" stroke-linecap="round"/>
      </svg>
      <svg v-else class="ai-fab-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
        <line x1="18" y1="6" x2="6" y2="18" stroke-linecap="round"/><line x1="6" y1="6" x2="18" y2="18" stroke-linecap="round"/>
      </svg>
    </button>

    <Transition name="chat-panel">
      <div v-if="isOpen" class="ai-panel">
        <div class="ai-panel-header">
          <div class="flex items-center gap-3">
            <span class="ai-panel-title">影片精灵</span>
            <span class="ai-badge">AI</span>
            <span v-if="userStore.profile" class="ai-personalized">已个性化</span>
          </div>
          <button class="ai-panel-close" @click="isOpen = false" title="关闭">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
              <line x1="18" y1="6" x2="6" y2="18" stroke-linecap="round"/><line x1="6" y1="6" x2="18" y2="18" stroke-linecap="round"/>
            </svg>
          </button>
        </div>

        <div class="ai-messages" ref="msgContainer">
          <div v-if="messages.length === 0" class="ai-welcome">
            <p>你好！我是影片精灵，你的专属影视助手。</p>
            <p v-if="userStore.profile" class="ai-welcome-hint">我已了解你的观影偏好，可以为你提供个性化推荐。</p>
            <p class="ai-welcome-hint">试试问我：</p>
            <ul>
              <li @click="quickAsk('推荐几部类似《肖申克的救赎》的电影')">推荐几部类似《肖申克的救赎》的电影</li>
              <li @click="quickAsk('《盗梦空间》的结局到底是什么意思？')">《盗梦空间》的结局到底是什么意思？</li>
              <li @click="quickAsk('有什么好看的科幻电影推荐？')">有什么好看的科幻电影推荐？</li>
            </ul>
          </div>
          <template v-for="(msg, i) in messages" :key="i">
            <div class="ai-msg" :class="msg.role">
              <div class="ai-msg-avatar">{{ msg.role === 'user' ? '👤' : '🎬' }}</div>
              <div class="ai-msg-bubble">
                <div class="ai-msg-text">{{ msg.content }}</div>
                <div v-if="msg.links && msg.links.length" class="ai-msg-links">
                  <div class="ai-links-title">站内相关影片：</div>
                  <router-link
                    v-for="link in msg.links"
                    :key="link.id"
                    :to="`/movies/${link.id}`"
                    class="ai-movie-link"
                    @click="isOpen = false"
                  >
                    {{ link.title }}
                  </router-link>
                </div>
              </div>
            </div>
          </template>
          <div v-if="loading" class="ai-msg assistant">
            <div class="ai-msg-avatar">🎬</div>
            <div class="ai-msg-bubble ai-typing">
              <span class="ai-dot"></span><span class="ai-dot"></span><span class="ai-dot"></span>
            </div>
          </div>
        </div>

        <div class="ai-input-area">
          <input
            v-model="input"
            class="ai-input"
            placeholder="输入你的问题..."
            @keyup.enter="send"
            :disabled="loading"
          />
          <button class="ai-send-btn" @click="send" :disabled="loading || !input.trim()">
            <svg viewBox="0 0 24 24" fill="currentColor" width="18" height="18">
              <path d="M2.01 21 23 12 2.01 3 2 10l15 2-15 2z"/>
            </svg>
          </button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { sendChatMessage } from '@/api/assistant'
import { getMyRecords } from '@/api/interaction'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const isOpen = ref(false)
const input = ref('')
const loading = ref(false)
const messages = ref([])
const msgContainer = ref(null)
const userContext = ref(null)
let contextFetched = false

async function fetchUserContext() {
  if (!userStore.token) {
    userContext.value = null
    contextFetched = false
    return
  }
  try {
    const [favRes, watchedRes, watchlistRes, ratingsRes] = await Promise.all([
      getMyRecords('favorites', { pageNum: 1, pageSize: 20 }),
      getMyRecords('watched', { pageNum: 1, pageSize: 20 }),
      getMyRecords('watchlist', { pageNum: 1, pageSize: 20 }),
      getMyRecords('ratings', { pageNum: 1, pageSize: 20 })
    ])
    userContext.value = {
      username: userStore.profile?.nickname || userStore.profile?.username || '',
      favorites: (favRes.data?.records || []).map(r => ({ id: r.movieId, title: r.title })),
      watched: (watchedRes.data?.records || []).map(r => ({ id: r.movieId, title: r.title })),
      watchlist: (watchlistRes.data?.records || []).map(r => ({ id: r.movieId, title: r.title })),
      ratings: (ratingsRes.data?.records || []).map(r => ({ id: r.movieId, title: r.title, score: r.score }))
    }
    contextFetched = true
  } catch {
    userContext.value = null
  }
}

watch(() => userStore.token, (token) => {
  if (token) fetchUserContext()
  else { userContext.value = null; contextFetched = false }
}, { immediate: true })

function toggle() {
  isOpen.value = !isOpen.value
}

function scrollToBottom() {
  nextTick(() => {
    const el = msgContainer.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

async function send() {
  const text = input.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text })
  input.value = ''
  scrollToBottom()

  loading.value = true
  try {
    const payload = messages.value
      .filter(m => m.role === 'user' || m.role === 'assistant')
      .map(m => ({ role: m.role, content: m.content }))

    const reqData = { messages: payload }
    if (userContext.value) reqData.userContext = userContext.value

    const res = await sendChatMessage(reqData)
    messages.value.push({
      role: 'assistant',
      content: res.data.content,
      links: res.data.links || []
    })
  } catch {
    ElMessage.error('AI 助手暂时不可用')
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

function quickAsk(text) {
  input.value = text
  send()
}

watch(isOpen, (val) => {
  if (val) {
    if (!contextFetched && userStore.token) fetchUserContext()
    scrollToBottom()
  }
})
</script>

<style scoped>
.ai-assistant {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}

.ai-fab {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  border: 1px solid var(--border-soft);
  background: var(--surface-strong);
  color: var(--text-primary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  transition: transform 0.25s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.25s ease;
}

.ai-fab:hover {
  transform: scale(1.08);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.18);
}

.ai-fab-icon {
  width: 24px;
  height: 24px;
}

.ai-panel {
  width: 380px;
  height: 520px;
  border-radius: var(--radius-xl);
  border: 1px solid var(--border-soft);
  background: var(--surface-strong);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.ai-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  border-bottom: 1px solid var(--border-soft);
  flex-shrink: 0;
}

.ai-panel-title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-primary);
}

.ai-badge {
  font-size: 0.65rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  padding: 2px 8px;
  border-radius: 99px;
  background: var(--accent-primary);
  color: var(--bg-primary);
}

.ai-personalized {
  font-size: 0.7rem;
  color: var(--text-muted);
  background: var(--surface-secondary);
  padding: 2px 10px;
  border-radius: 99px;
  border: 1px solid var(--border-soft);
}

.ai-panel-close {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  display: flex;
  transition: color 0.2s ease;
}

.ai-panel-close:hover {
  color: var(--text-primary);
}

.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  scrollbar-width: thin;
  scrollbar-color: var(--border-soft) transparent;
}

.ai-welcome {
  color: var(--text-secondary);
  font-size: 0.9rem;
  line-height: 1.6;
  padding: 8px 0;
}

.ai-welcome-hint {
  margin-top: 10px;
  font-size: 0.8rem;
  color: var(--text-muted);
}

.ai-welcome ul {
  margin: 8px 0 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ai-welcome li {
  padding: 8px 12px;
  border-radius: 10px;
  background: var(--bg-primary);
  border: 1px solid var(--border-soft);
  cursor: pointer;
  font-size: 0.85rem;
  color: var(--text-primary);
  transition: background 0.2s ease;
}

.ai-welcome li:hover {
  background: var(--surface-secondary);
}

.ai-msg {
  display: flex;
  gap: 10px;
  max-width: 100%;
}

.ai-msg.user {
  flex-direction: row-reverse;
}

.ai-msg-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
  background: var(--surface-secondary);
}

.ai-msg-bubble {
  max-width: calc(100% - 50px);
  padding: 10px 14px;
  border-radius: 16px;
  font-size: 0.88rem;
  line-height: 1.55;
  word-break: break-word;
}

.ai-msg-text {
  white-space: pre-wrap;
}

.ai-msg.user .ai-msg-bubble {
  background: var(--accent-primary);
  color: var(--bg-primary);
  border-bottom-right-radius: 6px;
}

.ai-msg.assistant .ai-msg-bubble {
  background: var(--bg-primary);
  color: var(--text-primary);
  border: 1px solid var(--border-soft);
  border-bottom-left-radius: 6px;
}

.ai-msg-links {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid var(--border-soft);
}

.ai-links-title {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.ai-movie-link {
  display: block;
  padding: 6px 10px;
  margin-bottom: 4px;
  border-radius: 8px;
  background: var(--surface-secondary);
  color: var(--text-primary);
  text-decoration: none;
  font-size: 0.85rem;
  font-weight: 500;
  transition: background 0.2s ease;
}

.ai-movie-link:hover {
  background: var(--accent-primary);
  color: var(--bg-primary);
}

.ai-typing {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 14px 18px;
}

.ai-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--text-muted);
  animation: ai-bounce 1.4s infinite ease-in-out both;
}

.ai-dot:nth-child(1) { animation-delay: -0.32s; }
.ai-dot:nth-child(2) { animation-delay: -0.16s; }
.ai-dot:nth-child(3) { animation-delay: 0s; }

@keyframes ai-bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

.ai-input-area {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid var(--border-soft);
  flex-shrink: 0;
}

.ai-input {
  flex: 1;
  border: 1px solid var(--border-soft);
  border-radius: 99px;
  padding: 10px 16px;
  font-size: 0.88rem;
  background: var(--bg-primary);
  color: var(--text-primary);
  outline: none;
  transition: border-color 0.2s ease;
}

.ai-input:focus {
  border-color: var(--text-muted);
}

.ai-input::placeholder {
  color: var(--text-muted);
}

.ai-send-btn {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  border: none;
  background: var(--accent-primary);
  color: var(--bg-primary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.ai-send-btn:hover:not(:disabled) {
  opacity: 0.85;
  transform: scale(1.05);
}

.ai-send-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.chat-panel-enter-active {
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.chat-panel-leave-active {
  transition: all 0.2s ease-in;
}

.chat-panel-enter-from,
.chat-panel-leave-to {
  opacity: 0;
  transform: translateY(16px) scale(0.95);
}

@media (max-width: 480px) {
  .ai-assistant {
    right: 12px;
    bottom: 12px;
  }

  .ai-panel {
    width: calc(100vw - 24px);
    height: calc(100vh - 80px);
    border-radius: var(--radius-lg);
  }
}
</style>

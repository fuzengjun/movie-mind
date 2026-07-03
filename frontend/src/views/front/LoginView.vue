<template>
  <div class="auth-page-container">
    <!-- Topbar -->
    <header class="auth-topbar">
      <RouterLink to="/" class="auth-logo">
        <span class="logo-text">Movie Mind</span>
      </RouterLink>
      <div class="auth-topbar-right">
        <button class="theme-toggle" @click="cycleTheme" :title="themeTitle">
          <!-- 浅色模式图标：太阳 -->
          <svg v-if="currentSetting === 'light'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
            <circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/>
          </svg>
          <!-- 深色模式图标：月亮 -->
          <svg v-else-if="currentSetting === 'dark'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
            <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
          </svg>
          <!-- 跟随系统图标：显示器 -->
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
            <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/>
          </svg>
        </button>
        <RouterLink to="/" class="back-home-link">返回首页</RouterLink>
      </div>
    </header>

    <!-- Main Content -->
    <div class="auth-grid">
      <!-- Left Form Column -->
      <div class="auth-form-column">
        <div class="auth-form-inner">
          <h1 class="auth-title">欢迎回到<br>你的影音世界</h1>
          <p class="auth-subtitle">
            登录后继续收藏、评分与探索影片。管理员账号会自动进入内容控制台。
          </p>

          <!-- Card Container -->
          <div class="auth-card">
            <!-- Top Alert Badge -->
            <div class="auth-badge-banner">
              <span class="badge-tag">Cinema</span>
              <span class="badge-text">欢迎探索 Movie Mind</span>
            </div>

            <div class="auth-card-body">
              <h2 class="form-title">登录账号</h2>
              
              <el-form label-position="top" @submit.prevent="handleLogin" class="custom-form">
                <el-form-item label="用户名">
                  <el-input 
                    v-model="form.username" 
                    autocomplete="username" 
                    maxlength="64" 
                    placeholder="请输入用户名" 
                    @keyup.enter="handleLogin" 
                  />
                </el-form-item>
                <el-form-item label="密码">
                  <el-input 
                    v-model="form.password" 
                    autocomplete="current-password" 
                    placeholder="请输入密码" 
                    show-password 
                    type="password" 
                    @keyup.enter="handleLogin" 
                  />
                </el-form-item>
                <button class="auth-submit-btn" type="submit" :disabled="submitting">
                  {{ submitting ? '正在登录…' : '使用密码登录' }}
                </button>
              </el-form>
            </div>
          </div>

          <!-- Bottom Switch Link -->
          <div class="auth-switch-prompt">
            首次来到这里？ <RouterLink to="/register" class="switch-link">免费注册账号 →</RouterLink>
          </div>
        </div>
      </div>

      <!-- Right Visual Column -->
      <div class="auth-visual-column">
        <div class="visual-wrapper">
          <img src="@/assets/auth-art.png" alt="Movie Mind Cinematic Art" class="visual-img" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { useThemeSetting, toggleTheme } from '@/utils/theme'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const submitting = ref(false)
const form = reactive({ username: String(route.query.username || ''), password: '' })

const currentSetting = computed(() => useThemeSetting().value)
const titleMap = { light: '当前：浅色模式，点击切换', dark: '当前：深色模式，点击切换', system: '当前：跟随系统，点击切换' }
const themeTitle = computed(() => titleMap[currentSetting.value] || '')

function cycleTheme() {
  toggleTheme()
}

async function handleLogin() {
  form.username = form.username.trim()
  if (!form.username || !form.password) {
    ElMessage.warning('请完整填写用户名和密码')
    return
  }
  submitting.value = true
  try {
    const response = await login(form)
    const user = response.data?.user || null
    userStore.setToken(response.data?.token || '')
    userStore.setProfile(user)
    ElMessage.success(`欢迎回来，${user?.nickname || form.username}`)

    const requested = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    if (user?.role === 'ADMIN') {
      await router.push(requested || '/admin')
    } else {
      await router.push(requested && !requested.startsWith('/admin') ? requested : '/')
    }
  } catch {
    // 请求层已统一展示错误信息
  } finally {
    submitting.value = false
  }
}
</script>

<style>
.auth-page-container {
  --auth-bg: #faf8f5;
  --auth-text: #191919;
  --auth-text-secondary: #66635c;
  --auth-card-bg: #ffffff;
  --auth-card-border: #e6e1d6;
  --auth-card-shadow: 0 12px 40px rgba(25, 25, 25, 0.03);
  --auth-btn-bg: #191919;
  --auth-btn-text: #ffffff;
  --auth-btn-hover: #333333;
  --auth-input-bg: #ffffff;
  --auth-input-border: #d9d4c7;
  --auth-input-focus: #191919;
  --auth-badge-bg: #eadeca;
  --auth-badge-text: #664d03;
  --auth-logo-color: #d97706;
}

html[data-theme='dark'] .auth-page-container {
  --auth-bg: #161514;
  --auth-text: #f3efe6;
  --auth-text-secondary: #a19d95;
  --auth-card-bg: #22201e;
  --auth-card-border: #2e2c2a;
  --auth-card-shadow: 0 12px 40px rgba(0, 0, 0, 0.3);
  --auth-btn-bg: #f3efe6;
  --auth-btn-text: #161514;
  --auth-btn-hover: #e6e1d6;
  --auth-input-bg: #2c2a27;
  --auth-input-border: #474440;
  --auth-input-focus: #f3efe6;
  --auth-badge-bg: #3d372e;
  --auth-badge-text: #e9c46a;
  --auth-logo-color: #f59e0b;
}
</style>

<style scoped>
.auth-page-container {
  min-height: 100vh;
  background-color: var(--auth-bg);
  color: var(--auth-text);
  display: flex;
  flex-direction: column;
  transition: background-color 300ms ease, color 300ms ease;
}

.auth-topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 3rem;
  width: 100%;
}

.auth-topbar-right {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.theme-toggle {
  background: none;
  border: none;
  color: var(--auth-text-secondary);
  cursor: pointer;
  padding: 6px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s ease, background-color 0.2s ease;
}

.theme-toggle:hover {
  color: var(--auth-text);
  background-color: var(--auth-card-border);
}

.auth-logo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.25rem;
  font-weight: 700;
  text-decoration: none;
  color: var(--auth-text);
  letter-spacing: -0.01em;
}

.logo-icon {
  color: var(--auth-logo-color);
  font-size: 1.5rem;
  line-height: 1;
}

.logo-text {
  font-family: -apple-system, BlinkMacSystemFont, sans-serif;
}

.back-home-link {
  font-size: 0.875rem;
  text-decoration: none;
  color: var(--auth-text-secondary);
  font-weight: 500;
  transition: color 0.2s;
  line-height: 1;
}

.back-home-link:hover {
  color: var(--auth-text);
}

.auth-grid {
  flex: 1;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 4rem;
  padding: 0 4rem 3rem;
  align-items: center;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

.auth-form-column {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
}

.auth-form-inner {
  max-width: 440px;
  width: 100%;
}

.auth-title {
  font-family: Georgia, Cambria, "Times New Roman", Times, serif;
  font-size: clamp(2.2rem, 3.5vw, 3rem);
  font-weight: 500;
  line-height: 1.15;
  letter-spacing: -0.02em;
  margin: 0 0 1rem;
  color: var(--auth-text);
}

.auth-subtitle {
  font-size: 0.9375rem;
  line-height: 1.6;
  color: var(--auth-text-secondary);
  margin: 0 0 2.2rem;
}

.auth-card {
  background-color: var(--auth-card-bg);
  border: 1px solid var(--auth-card-border);
  border-radius: 24px;
  box-shadow: var(--auth-card-shadow);
  overflow: hidden;
  margin-bottom: 1.5rem;
  transition: border-color 300ms ease, background-color 300ms ease, box-shadow 300ms ease;
}

.auth-badge-banner {
  background-color: var(--auth-badge-bg);
  color: var(--auth-badge-text);
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1.5rem;
  font-size: 0.8125rem;
  font-weight: 500;
}

.badge-tag {
  background: rgba(255, 255, 255, 0.25);
  padding: 0.15rem 0.5rem;
  border-radius: 4px;
  font-weight: 700;
  font-size: 0.72rem;
  letter-spacing: 0.05em;
}

:global(html[data-theme='dark']) .badge-tag {
  background: rgba(0, 0, 0, 0.2);
}

.auth-card-body {
  padding: 2.2rem 2.2rem 2.5rem;
}

.form-title {
  font-size: 1.125rem;
  font-weight: 600;
  margin: 0 0 1.5rem;
  color: var(--auth-text);
}

.auth-submit-btn {
  width: 100%;
  margin-top: 1.25rem;
  padding: 0.9rem;
  border: none;
  border-radius: 99px;
  background-color: var(--auth-btn-bg);
  color: var(--auth-btn-text);
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s, transform 0.1s;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 0.9375rem;
}

.auth-submit-btn:hover:not(:disabled) {
  background-color: var(--auth-btn-hover);
}

.auth-submit-btn:active:not(:disabled) {
  transform: scale(0.985);
}

.auth-submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.auth-switch-prompt {
  text-align: center;
  font-size: 0.875rem;
  color: var(--auth-text-secondary);
  margin-top: 1.8rem;
}

.switch-link {
  color: var(--auth-text);
  font-weight: 600;
  text-decoration: none;
  margin-left: 0.25rem;
  transition: opacity 0.2s;
}

.switch-link:hover {
  opacity: 0.8;
}

.auth-visual-column {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.visual-wrapper {
  width: 100%;
  height: calc(100vh - 120px);
  max-height: 720px;
  min-height: 480px;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: var(--auth-card-shadow);
  border: 1px solid var(--auth-card-border);
  transition: border-color 300ms ease;
}

.visual-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.16, 1, 0.3, 1);
  display: block;
}



/* Custom Element Plus styles override */
:deep(.el-form-item) {
  margin-bottom: 1.25rem;
}

:deep(.el-form-item__label) {
  color: var(--auth-text-secondary) !important;
  font-weight: 600;
  font-size: 0.8125rem;
  padding-bottom: 0.4rem !important;
  letter-spacing: 0.02em;
}

:deep(.el-input__wrapper) {
  background-color: var(--auth-input-bg) !important;
  border: 1px solid var(--auth-input-border) !important;
  box-shadow: none !important;
  border-radius: 12px !important;
  padding: 0.65rem 1rem !important;
  transition: border-color 0.2s, box-shadow 0.2s, background-color 300ms ease !important;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: var(--auth-input-focus) !important;
  box-shadow: 0 0 0 1px var(--auth-input-focus) !important;
}

:deep(.el-input__inner) {
  color: var(--auth-text) !important;
  font-size: 0.9375rem;
}

@media (max-width: 1024px) {
  .auth-grid {
    grid-template-columns: 1fr;
    gap: 2rem;
    padding: 0 2rem 2rem;
  }
  .auth-visual-column {
    display: none;
  }
  .auth-topbar {
    padding: 1.5rem 2rem;
  }
}

@media (max-width: 480px) {
  .auth-grid {
    padding: 0 1.25rem 2rem;
  }
  .auth-topbar {
    padding: 1.25rem 1.25rem;
  }
  .auth-title {
    font-size: 2.2rem;
  }
  .auth-card-body {
    padding: 1.5rem;
  }
}
</style>
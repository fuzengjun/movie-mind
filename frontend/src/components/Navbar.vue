<template>
  <header class="fixed inset-x-0 top-0 z-30 glass-navbar py-4 text-[var(--text-primary)]">
    <div class="page-frame flex items-center justify-between">
      <!-- 左侧 logo -->
      <RouterLink class="text-lg font-bold tracking-tight no-underline md:text-xl" to="/">
        Movie Mind
      </RouterLink>
      
      <!-- 中间菜单居中对齐 -->
      <nav class="absolute left-1/2 hidden -translate-x-1/2 items-center gap-8 text-[13px] font-medium tracking-wide md:flex">
        <RouterLink class="nav-link transition duration-200" to="/">首页</RouterLink>
        <RouterLink class="nav-link transition duration-200" to="/movies">影视库</RouterLink>
        <RouterLink class="nav-link transition duration-200" to="/rankings">排行榜</RouterLink>
        <RouterLink v-if="userStore.token" class="nav-link transition duration-200" to="/recommendations">为你推荐</RouterLink>
        <RouterLink v-if="isAdmin" class="nav-link transition duration-200" to="/admin">后台</RouterLink>
      </nav>
      
      <!-- 右侧登录按钮 -->
      <div class="hidden items-center gap-3 md:flex">
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
        <template v-if="userStore.token">
          <RouterLink class="rounded-full bg-[var(--text-primary)] px-5 py-2 text-xs font-semibold text-[var(--bg-primary)] no-underline transition duration-200 hover:opacity-90" to="/profile">
            {{ userStore.profile?.nickname || '个人中心' }}
          </RouterLink>
          <button class="nav-link logout-button" type="button" @click="handleLogout">退出登录</button>
        </template>
        <template v-else>
          <RouterLink class="nav-link text-xs no-underline" to="/register">注册</RouterLink>
          <RouterLink class="rounded-full bg-[var(--text-primary)] px-5 py-2 text-xs font-semibold text-[var(--bg-primary)] no-underline transition duration-200 hover:opacity-90" to="/login">登录</RouterLink>
        </template>
      </div>
      
      <button class="pill-button-premium md:hidden" @click="drawerVisible = true">导航</button>
    </div>

    <!-- 抽屉菜单 -->
    <el-drawer v-model="drawerVisible" :with-header="false" size="82%">
      <div class="space-y-4 px-2 py-8">
        <RouterLink class="block rounded-xl px-4 py-3 nav-link" to="/" @click="drawerVisible = false">首页</RouterLink>
        <RouterLink class="block rounded-xl px-4 py-3 nav-link" to="/movies" @click="drawerVisible = false">影视库</RouterLink>
        <RouterLink class="block rounded-xl px-4 py-3 nav-link" to="/rankings" @click="drawerVisible = false">排行榜</RouterLink>
        <RouterLink v-if="userStore.token" class="block rounded-xl px-4 py-3 nav-link" to="/recommendations" @click="drawerVisible = false">为你推荐</RouterLink>
        <RouterLink v-if="userStore.token" class="block rounded-xl px-4 py-3 nav-link" to="/profile" @click="drawerVisible = false">个人中心</RouterLink>
        <RouterLink v-if="isAdmin" class="block rounded-xl px-4 py-3 nav-link" to="/admin" @click="drawerVisible = false">后台</RouterLink>
        <div class="mobile-theme-row">
          <span class="text-sm text-[var(--text-secondary)]">{{ themeLabel }}</span>
          <div class="mobile-theme-btns">
            <button
              v-for="opt in themeOptions"
              :key="opt.value"
              class="mobile-theme-opt"
              :class="{ active: currentSetting === opt.value }"
              @click="setTheme(opt.value)"
              :title="opt.label"
            >
              <!-- 太阳 -->
              <svg v-if="opt.value === 'light'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                <circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/>
              </svg>
              <!-- 月亮 -->
              <svg v-else-if="opt.value === 'dark'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
              </svg>
              <!-- 显示器 -->
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/>
              </svg>
            </button>
          </div>
        </div>
        <button v-if="userStore.token" class="mobile-logout" type="button" @click="handleLogout">退出登录</button>
        <RouterLink v-if="!userStore.token" class="block rounded-xl px-4 py-3 nav-link" to="/register" @click="drawerVisible = false">注册</RouterLink>
        <RouterLink v-if="!userStore.token" class="mt-4 block rounded-full bg-[var(--text-primary)] px-4 py-3 text-center text-sm font-semibold text-[var(--bg-primary)] no-underline" to="/login" @click="drawerVisible = false">
          登录
        </RouterLink>
      </div>
    </el-drawer>
  </header>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useThemeMode, useThemeSetting, toggleTheme, setTheme } from '@/utils/theme'

const router = useRouter()
const userStore = useUserStore()
const isAdmin = computed(() => userStore.profile?.role === 'ADMIN')
const isDark = computed(() => useThemeMode().value === 'dark')
const currentSetting = computed(() => useThemeSetting().value)
const drawerVisible = ref(false)

const themeOptions = [
  { value: 'light', label: '浅色' },
  { value: 'dark', label: '深色' },
  { value: 'system', label: '跟随系统' }
]

const settingLabels = { light: '浅色模式', dark: '深色模式', system: '跟随系统' }
const themeLabel = computed(() => settingLabels[currentSetting.value] || '跟随系统')

const titleMap = { light: '当前：浅色模式，点击切换', dark: '当前：深色模式，点击切换', system: '当前：跟随系统，点击切换' }
const themeTitle = computed(() => titleMap[currentSetting.value] || '')

function cycleTheme() {
  toggleTheme()
}

async function handleLogout() {
  userStore.logout()
  drawerVisible.value = false
  ElMessage.success('已退出登录')
  await router.push('/')
}
</script>

<style scoped>
.logout-button {
  padding: .5rem .2rem;
  border: 0;
  background: transparent;
  cursor: pointer;
  font-size: .75rem;
}
.mobile-logout {
  width: 100%;
  margin-top: 1rem;
  padding: .8rem 1rem;
  border: 1px solid var(--border-soft);
  border-radius: 999px;
  color: var(--text-primary);
  background: var(--surface-secondary);
  font-weight: 650;
  cursor: pointer;
}
.theme-toggle {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 6px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s ease, background 0.2s ease;
}
.theme-toggle:hover {
  color: var(--text-primary);
  background: var(--surface-secondary);
}
.mobile-theme-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  margin-top: 12px;
  border-radius: 12px;
  background: var(--surface-secondary);
  border: 1px solid var(--border-soft);
}
.mobile-theme-btns {
  display: flex;
  gap: 4px;
  background: var(--bg-primary);
  border-radius: 8px;
  padding: 3px;
  border: 1px solid var(--border-soft);
}
.mobile-theme-opt {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 28px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s ease;
}
.mobile-theme-opt.active {
  background: var(--surface-strong);
  color: var(--text-primary);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}
.mobile-theme-opt:hover:not(.active) {
  color: var(--text-secondary);
}
</style>
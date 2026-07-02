import { readonly, ref } from 'vue'

const STORAGE_KEY = 'movie-mind-theme'

/** 用户选择的模式：'light' | 'dark' | 'system' */
const themeSetting = ref('system')
/** 实际生效的外观：'light' | 'dark' */
const themeMode = ref('light')

let mediaQuery = null
let mediaListener = null

function applyTheme(isDark) {
  themeMode.value = isDark ? 'dark' : 'light'
  document.documentElement.dataset.theme = themeMode.value
}

function getSystemDark() {
  return window.matchMedia('(prefers-color-scheme: dark)').matches
}

/** 根据当前 setting 计算并应用实际主题 */
function syncTheme() {
  const setting = themeSetting.value
  if (setting === 'dark') {
    applyTheme(true)
  } else if (setting === 'light') {
    applyTheme(false)
  } else {
    applyTheme(getSystemDark())
  }
}

/** 绑定或解绑系统偏好监听器 */
function updateSystemListener() {
  if (!mediaQuery) return

  // 先移除旧监听
  if (mediaListener) {
    mediaQuery.removeEventListener('change', mediaListener)
    mediaListener = null
  }

  // 仅在「跟随系统」模式下监听
  if (themeSetting.value === 'system') {
    mediaListener = (e) => applyTheme(e.matches)
    mediaQuery.addEventListener('change', mediaListener)
  }
}

/**
 * 初始化主题系统。在 app 启动时调用一次。
 */
export function setupThemeSync() {
  if (typeof window === 'undefined') return

  mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')

  // 从 localStorage 读取用户设置
  const stored = localStorage.getItem(STORAGE_KEY)
  if (stored === 'dark' || stored === 'light' || stored === 'system') {
    themeSetting.value = stored
  } else {
    themeSetting.value = 'system'
  }

  syncTheme()
  updateSystemListener()
}

/**
 * 在 light → dark → system 之间循环切换。
 */
export function toggleTheme() {
  const order = ['light', 'dark', 'system']
  const idx = order.indexOf(themeSetting.value)
  const next = order[(idx + 1) % order.length]
  setTheme(next)
}

/**
 * 直接设置主题模式。
 * @param {'light' | 'dark' | 'system'} mode
 */
export function setTheme(mode) {
  themeSetting.value = mode
  localStorage.setItem(STORAGE_KEY, mode)
  syncTheme()
  updateSystemListener()
}

/**
 * 获取实际生效的外观（'light' | 'dark'）。
 */
export function useThemeMode() {
  return readonly(themeMode)
}

/**
 * 获取用户选择的模式设置（'light' | 'dark' | 'system'）。
 */
export function useThemeSetting() {
  return readonly(themeSetting)
}

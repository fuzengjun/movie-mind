import { readonly, ref } from 'vue'

const STORAGE_KEY = 'movie-mind-theme'
const themeMode = ref('light')

function applyTheme(isDark) {
  themeMode.value = isDark ? 'dark' : 'light'
  document.documentElement.dataset.theme = themeMode.value
}

function resolveTheme() {
  const stored = localStorage.getItem(STORAGE_KEY)
  if (stored === 'dark' || stored === 'light') return stored
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

export function setupThemeSync() {
  if (typeof window === 'undefined') return
  applyTheme(resolveTheme() === 'dark')
}

export function toggleTheme() {
  const next = themeMode.value === 'dark' ? 'light' : 'dark'
  localStorage.setItem(STORAGE_KEY, next)
  applyTheme(next === 'dark')
}

export function useThemeMode() {
  return readonly(themeMode)
}

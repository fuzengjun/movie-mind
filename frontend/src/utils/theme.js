import { readonly, ref } from 'vue'

const themeMode = ref('light')
let mediaQuery

function applyTheme(isDark) {
  themeMode.value = isDark ? 'dark' : 'light'
  document.documentElement.dataset.theme = themeMode.value
}

export function setupThemeSync() {
  if (typeof window === 'undefined') {
    return
  }

  mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  applyTheme(mediaQuery.matches)

  const listener = (event) => applyTheme(event.matches)
  if (mediaQuery.addEventListener) {
    mediaQuery.addEventListener('change', listener)
  } else {
    mediaQuery.addListener(listener)
  }
}

export function useThemeMode() {
  return readonly(themeMode)
}

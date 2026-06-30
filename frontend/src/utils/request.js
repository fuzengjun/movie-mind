import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

let authDialogOpen = false
let authNoticeShown = false
let rejectedToken = ''

request.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) {
    if (authNoticeShown && userStore.token !== rejectedToken) {
      authNoticeShown = false
      rejectedToken = ''
    }
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

function showLoginExpired(message) {
  const userStore = useUserStore()
  if (authNoticeShown || authDialogOpen) return

  rejectedToken = userStore.token
  authNoticeShown = true
  userStore.logout()
  authDialogOpen = true

  const currentPath = `${window.location.pathname}${window.location.search}${window.location.hash}`
  const redirect = currentPath.startsWith('/login') ? '/' : currentPath
  ElMessageBox.confirm(
    message || '登录状态已失效，请重新登录后继续。',
    '需要重新登录',
    {
      confirmButtonText: '前往登录',
      cancelButtonText: '暂时取消',
      type: 'warning',
      customClass: 'auth-session-dialog',
      closeOnClickModal: false,
      closeOnPressEscape: true,
      distinguishCancelAndClose: true
    }
  )
    .then(() => {
      window.location.assign(`/login?redirect=${encodeURIComponent(redirect)}`)
    })
    .catch(() => {})
    .finally(() => {
      authDialogOpen = false
    })
}

function handleFailure(status, message) {
  if (status === 401) {
    showLoginExpired(message)
  } else if (status === 403) {
    ElMessage.warning(message || '当前账号没有访问权限')
  } else {
    ElMessage.error(message || '请求失败，请稍后重试')
  }
}

request.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (payload?.code !== 200) {
      handleFailure(Number(payload?.code), payload?.message)
      return Promise.reject(new Error(payload?.message || 'Request failed'))
    }
    return payload
  },
  (error) => {
    if (error.code === 'ECONNABORTED') {
      ElMessage.error(error.config?.timeoutMessage || '请求超时，请稍后重试')
    } else {
      handleFailure(error.response?.status, error.response?.data?.message)
    }
    return Promise.reject(error)
  }
)

export default request
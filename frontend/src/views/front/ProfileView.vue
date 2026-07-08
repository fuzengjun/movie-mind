<template>
  <section class="max-w-6xl mx-auto px-4 py-8 pt-24 min-h-[80vh]">
    <!-- Title Header Banner -->
    <div class="mb-8 pb-2 border-b border-soft">
      <p class="section-kicker text-xs font-semibold tracking-wider text-[var(--text-secondary)] uppercase">Account Settings</p>
      <h1 class="text-3xl font-bold tracking-tight text-[var(--text-primary)] mt-1">个人中心</h1>
      <p class="text-sm text-[var(--text-muted)] mt-1">管理个人资料，查看并跟踪您的影视收藏与互动轨迹。</p>
    </div>

    <!-- Main Content Layout Grid -->
    <div class="profile-layout">
      <!-- Left Column: Sidebar Cards -->
      <aside class="sidebar-wrapper space-y-6">
        <!-- User Info Summary Card -->
        <div class="surface-card p-6 flex flex-col items-center text-center">
          <div class="profile-avatar-outer">
            <div class="profile-avatar-inner">
              {{ store.profile?.nickname?.[0] || 'U' }}
            </div>
          </div>
          <h2 class="mt-4 text-xl font-bold tracking-tight text-[var(--text-primary)]">
            {{ store.profile?.nickname || '未知用户' }}
          </h2>
          <p class="text-xs text-[var(--text-secondary)] mt-1">@{{ store.profile?.username }}</p>
          <div class="mt-4 inline-flex items-center justify-center px-4 py-1.5 text-xs font-semibold rounded-full bg-accent-glow border border-accent-soft text-accent whitespace-nowrap">
            {{ store.profile?.role === 'ADMIN' ? '系统管理员' : '普通用户' }}
          </div>
        </div>

        <!-- Sidebar Navigation Menu -->
        <div class="surface-card overflow-hidden p-2">
          <nav class="sidebar-menu">
            <button 
              v-for="item in menuItems" 
              :key="item.name" 
              class="menu-item" 
              :class="{ 'is-active': tab === item.name }" 
              @click="changeSection(item.name)"
            >
              <el-icon class="menu-icon"><component :is="item.icon" /></el-icon>
              <span class="menu-label">{{ item.label }}</span>
              <el-icon class="menu-arrow"><ArrowRight /></el-icon>
            </button>
          </nav>
        </div>
      </aside>

      <!-- Right Column: Settings Content Cards -->
      <main class="content-panel">
        <!-- Tab Panel: Profile Editing -->
        <div v-if="tab === 'profile'" class="surface-card p-6 md:p-8 space-y-6">
          <div class="panel-header border-b border-soft pb-4">
            <h2 class="text-2xl font-bold tracking-tight text-[var(--text-primary)]">个人资料</h2>
            <p class="text-sm text-[var(--text-secondary)] mt-1">查看和管理您的账号基本信息。</p>
          </div>
          <el-form label-position="top" class="profile-form" @submit.prevent="saveProfile">
            <el-form-item label="用户名 (不可修改)">
              <el-input :model-value="form.username" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="form.nickname" placeholder="请输入您的昵称" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="请输入您的电子邮箱" />
            </el-form-item>
            <button class="pill-button is-active mt-4" type="submit">保存资料</button>
          </el-form>
        </div>

        <!-- Tab Panel: Security Password Editing -->
        <div v-else-if="tab === 'password'" class="surface-card p-6 md:p-8 space-y-6">
          <div class="panel-header border-b border-soft pb-4">
            <h2 class="text-2xl font-bold tracking-tight text-[var(--text-primary)]">安全与密码</h2>
            <p class="text-sm text-[var(--text-secondary)] mt-1">建议定期修改密码以确保您的账号安全。</p>
          </div>
          <el-form label-position="top" class="profile-form" @submit.prevent="changePassword">
            <el-form-item label="原密码">
              <el-input v-model="password.oldPassword" type="password" show-password placeholder="请输入原密码" />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="password.newPassword" type="password" show-password placeholder="请输入新密码，最少 6 位" />
            </el-form-item>
            <el-form-item label="确认新密码">
              <el-input v-model="password.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
            <button class="pill-button is-active mt-4" type="submit">修改密码</button>
          </el-form>
        </div>

        <!-- Tab Panel: Movie Record Grids -->
        <div v-else class="surface-card p-6 md:p-8 space-y-6">
          <div class="panel-header border-b border-soft pb-4 flex flex-wrap items-center justify-between gap-4">
            <div>
              <h2 class="text-2xl font-bold tracking-tight text-[var(--text-primary)]">{{ currentTabLabel }}</h2>
              <p class="text-sm text-[var(--text-secondary)] mt-1">管理和查看您的{{ currentTabLabel }}历史记录。</p>
            </div>
            <div class="record-tools">
              <span class="detail-chip" v-if="records.length">共 {{ totalRecords }} 个记录</span>
            </div>
          </div>

          <div v-loading="loadingRecords" class="min-h-[200px] flex flex-col justify-center">
            <div v-if="records.length">
              <div class="record-grid">
                <RouterLink 
                  v-for="r in records" 
                  :key="r.id" 
                  :to="`/movies/${r.movieId}`" 
                  class="record-card-new"
                >
                  <div class="record-poster-wrapper">
                    <img v-if="r.posterUrl" :src="r.posterUrl" :alt="r.title" class="record-poster" />
                    <div v-else class="record-poster-fallback">
                      <span>{{ r.title?.[0] }}</span>
                    </div>
                    <!-- Rating Badge Overlay -->
                    <div class="record-badge" v-if="r.score">
                      <el-icon class="text-yellow-400 mr-xs"><StarFilled /></el-icon>
                      <span>{{ r.score }} 分</span>
                    </div>
                  </div>
                  <div class="record-info">
                    <h3 class="record-title" :title="r.title">{{ r.title }}</h3>
                    <p class="record-time">{{ format(r.actionTime) }}</p>
                  </div>
                </RouterLink>
              </div>
              <div class="pager-container">
                <el-pagination
                  v-model:current-page="currentPage"
                  :page-size="pageSize"
                  :total="totalRecords"
                  layout="prev, pager, next"
                  prev-text="上一页"
                  next-text="下一页"
                  @current-change="handlePageChange"
                />
                <div class="pager-info" v-if="totalRecords > pageSize">
                  共 {{ Math.ceil(totalRecords / pageSize) }} 页 / {{ totalRecords }} 个，跳至
                  <input
                    type="number"
                    v-model.number="jumperPage"
                    min="1"
                    :max="Math.ceil(totalRecords / pageSize)"
                    class="pager-jumper-input"
                    @keyup.enter="handleJumper"
                  />
                  页
                </div>
              </div>
            </div>
            <div v-else-if="recordError" class="record-error"><p>{{ recordError }}</p><button class="pill-button" @click="load(tab)">重试</button></div>
            <el-empty v-else :description="'暂无' + currentTabLabel + '记录'" />
          </div>
        </div>
      </main>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getProfile, updateProfile, changePassword as updatePassword } from '@/api/user'
import { getMyRecords } from '@/api/interaction'
import { useUserStore } from '@/stores/user'
import { 
  User, 
  Lock, 
  Star, 
  StarFilled, 
  Calendar, 
  CircleCheck, 
  Clock, 
  ArrowRight 
} from '@element-plus/icons-vue'

const store = useUserStore()
const router = useRouter()
const tab = ref('profile')
const records = ref([])
const loadingRecords = ref(false)
const recordError = ref('')
const currentPage = ref(1)
const totalRecords = ref(0)
const pageSize = ref(18)

const form = reactive({
  username: '',
  nickname: '',
  email: ''
})

const password = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const menuItems = [
  { name: 'profile', label: '个人资料', icon: User },
  { name: 'password', label: '安全与密码', icon: Lock },
  { name: 'favorites', label: '我的收藏', icon: Star },
  { name: 'ratings', label: '评分记录', icon: StarFilled },
  { name: 'watchlist', label: '想看清单', icon: Calendar },
  { name: 'watched', label: '看过记录', icon: CircleCheck },
  { name: 'views', label: '浏览历史', icon: Clock }
]

const currentTabLabel = computed(() => {
  const item = menuItems.find(i => i.name === tab.value)
  return item ? item.label : ''
})

async function init() {
  try {
    const r = await getProfile()
    Object.assign(form, r.data)
    store.setProfile({ ...store.profile, ...r.data })
  } catch (error) {
    // 拦截器中已统一处理错误弹出
  }
}

async function load(name) {
  if (!name || name === 'profile' || name === 'password') return
  loadingRecords.value = true
  recordError.value = ''
  try {
    const params = { 
      pageNum: currentPage.value,
      pageSize: pageSize.value 
    }
    const r = await getMyRecords(name, params)
    records.value = r.data?.records || []
    totalRecords.value = r.data?.total || 0
  } catch (error) {
    records.value = []
    totalRecords.value = 0
    recordError.value = error.message || '记录暂时无法加载。'
  } finally {
    loadingRecords.value = false
  }
}

async function changeSection(name) {
  tab.value = name
  currentPage.value = 1
  totalRecords.value = 0
  await load(name)
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

async function saveProfile() {
  form.nickname = (form.nickname || '').trim()
  form.email = (form.email || '').trim()

  if (!form.nickname) {
    ElMessage.warning('昵称不能为空')
    return
  }
  if (form.email && !emailPattern.test(form.email)) {
    ElMessage.warning('请输入正确的邮箱地址')
    return
  }

  try {
    const r = await updateProfile(form)
    store.setProfile({ ...store.profile, ...r.data })
    ElMessage.success('资料已保存')
  } catch (error) {
    // 拦截器已展示异常信息
  }
}

async function changePassword() {
  if (!password.oldPassword) {
    ElMessage.warning('请输入原密码')
    return
  }
  if (!password.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  if (password.newPassword.length < 6) {
    ElMessage.warning('新密码长度不能少于 6 位')
    return
  }
  if (password.newPassword === password.oldPassword) {
    ElMessage.warning('新密码不能与原密码相同')
    return
  }
  if (password.newPassword !== password.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }

  try {
    await updatePassword(password)
    store.logout()
    ElMessage.success('密码已修改，请重新登录')
    router.push('/login')
  } catch (error) {
    // 拦截器已展示异常信息，清空密码框以便重新输入
    password.oldPassword = ''
    password.newPassword = ''
    password.confirmPassword = ''
  }
}

function format(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  const pad = number => String(number).padStart(2, '0')
  return date.getFullYear() + '-' +
    pad(date.getMonth() + 1) + '-' +
    pad(date.getDate()) + ' ' +
    pad(date.getHours()) + ':' +
    pad(date.getMinutes()) + ':' +
    pad(date.getSeconds())
}

async function handlePageChange(val) {
  currentPage.value = val
  await load(tab.value)
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const jumperPage = ref('')
function handleJumper() {
  const p = parseInt(jumperPage.value, 10)
  const maxPage = Math.ceil(totalRecords.value / pageSize.value)
  if (p >= 1 && p <= maxPage) {
    handlePageChange(p)
    jumperPage.value = ''
  }
}

onMounted(async () => {
  await init()
})
</script>

<style scoped>
.profile-layout {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
}
@media (min-width: 1024px) {
  .profile-layout {
    grid-template-columns: 280px 1fr;
  }
}

.profile-form {
  max-width: 480px;
}

/* Avatar styling */
.profile-avatar-outer {
  padding: 4px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ef7b45, #ffc18f);
  box-shadow: 0 8px 24px rgba(239, 123, 69, 0.2);
}
.profile-avatar-inner {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: var(--bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  font-weight: 700;
  color: var(--accent-primary);
  border: 4px solid var(--surface-primary);
}

/* Role badge styling */
.bg-accent-glow {
  background: color-mix(in srgb, var(--accent-primary) 6%, transparent);
}
.border-accent-soft {
  border-color: color-mix(in srgb, var(--accent-primary) 12%, transparent);
}
.text-accent {
  color: var(--accent-primary);
}

/* Sidebar navigation menu */
.sidebar-menu {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.menu-item {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 12px 16px;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  border-radius: var(--radius-lg);
  cursor: pointer;
  font-size: 0.95rem;
  font-weight: 500;
  transition: all 0.2s ease;
}
.menu-item:hover {
  background: var(--surface-secondary);
  color: var(--text-primary);
}
.menu-item.is-active {
  background: color-mix(in srgb, var(--accent-primary) 8%, transparent);
  color: var(--accent-primary);
  font-weight: 600;
}
.menu-icon {
  margin-right: 12px;
  font-size: 1.15rem;
}
.menu-arrow {
  margin-left: auto;
  font-size: 0.85rem;
  opacity: 0.4;
  transition: transform 0.2s ease;
}
.menu-item:hover .menu-arrow {
  opacity: 0.8;
  transform: translateX(2px);
}
.menu-item.is-active .menu-arrow {
  opacity: 0.9;
  color: var(--accent-primary);
}

/* Panel border divider */
.border-soft {
  border-color: var(--border-soft);
}

/* Movie record grid and cards */
.record-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 18px;
}

.record-card-new {
  display: flex;
  flex-direction: column;
  background: var(--surface-secondary);
  border: 1px solid var(--border-soft);
  border-radius: var(--radius-lg);
  overflow: hidden;
  color: var(--text-primary);
  text-decoration: none;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}
.record-card-new:hover {
  transform: translateY(-4px);
  background: var(--surface-primary);
  border-color: color-mix(in srgb, var(--accent-primary) 20%, transparent);
  box-shadow: var(--shadow-md);
}

.record-poster-wrapper {
  position: relative;
  width: 100%;
  aspect-ratio: 2 / 3;
  background: var(--poster-fallback);
  overflow: hidden;
}

.record-poster {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}
.record-card-new:hover .record-poster {
  transform: scale(1.04);
}

.record-poster-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.6rem;
  color: var(--text-muted);
}

.record-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 8px;
  background: rgba(0, 0, 0, 0.65);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  color: #fff;
  font-size: 0.75rem;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.record-info {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.record-title {
  margin: 0;
  font-size: 0.95rem;
  font-weight: 600;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--text-primary);
}

.record-time {
  margin: 0;
  font-size: 0.75rem;
  color: var(--text-muted);
}

.text-yellow-400 {
  color: #fbbf24;
}
.mr-xs {
  margin-right: 2px;
}
.record-tools{display:flex;align-items:center;gap:12px;flex-wrap:wrap}.record-tools .el-select{width:220px}.record-error{display:grid;justify-items:center;gap:12px;color:var(--text-secondary)}
.pager-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-top: 24px;
  flex-wrap: wrap;
}

.pager-info {
  font-size: 0.88rem;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.pager-jumper-input {
  width: 54px;
  height: 30px;
  border: 1px solid var(--border-soft);
  border-radius: 6px;
  background-color: var(--surface-secondary);
  color: var(--text-primary);
  text-align: center;
  font-size: 0.88rem;
  outline: none;
  transition: border-color 0.2s, background-color 0.2s;
  box-sizing: border-box;
  -moz-appearance: textfield;
  appearance: textfield;
}

.pager-jumper-input::-webkit-outer-spin-button,
.pager-jumper-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  appearance: none;
  margin: 0;
}

.pager-jumper-input:focus {
  border-color: var(--text-primary);
  background-color: var(--bg-secondary);
}

:deep(.el-pagination) {
  --el-pagination-button-bg-color: var(--surface-secondary) !important;
  --el-pagination-hover-color: var(--text-primary) !important;
  --el-pagination-button-color: var(--text-secondary) !important;
  --el-pagination-button-disabled-color: var(--text-muted) !important;
}

:deep(.el-pagination .btn-next), :deep(.el-pagination .btn-prev) {
  padding: 0 12px !important;
  border-radius: 6px !important;
  border: 1px solid var(--border-soft) !important;
  background-color: var(--surface-secondary) !important;
  color: var(--text-secondary) !important;
  font-size: 13px !important;
  height: 32px !important;
  line-height: 30px !important;
  transition: all 0.2s !important;
}

:deep(.el-pagination .btn-next:hover), :deep(.el-pagination .btn-prev:hover) {
  color: var(--text-primary) !important;
  border-color: var(--text-primary) !important;
}

:deep(.el-pagination .el-pager li) {
  border-radius: 6px !important;
  border: 1px solid var(--border-soft) !important;
  background-color: var(--surface-secondary) !important;
  color: var(--text-secondary) !important;
  font-size: 14px !important;
  min-width: 32px !important;
  height: 32px !important;
  line-height: 30px !important;
  margin: 0 2px !important;
  font-weight: 500 !important;
  transition: all 0.2s !important;
}

:deep(.el-pagination .el-pager li:hover) {
  color: var(--text-primary) !important;
  border-color: var(--text-primary) !important;
}

:deep(.el-pagination .el-pager li.is-active) {
  background-color: var(--text-primary) !important;
  border-color: var(--text-primary) !important;
  color: var(--bg-primary) !important;
}
</style>

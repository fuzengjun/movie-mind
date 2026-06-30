<template>
  <section class="auth-page">
    <div class="auth-intro">
      <p class="section-kicker">Sign In</p>
      <h1>欢迎回到<br>你的影音世界</h1>
      <p>登录后继续收藏、评分与探索影片。管理员账号会自动进入内容控制台。</p>
      <div class="auth-note">
        <span>还没有账号？</span>
        <RouterLink to="/register">创建账号 →</RouterLink>
      </div>
    </div>

    <div class="surface-card auth-card">
      <div class="auth-card-head">
        <p class="section-kicker">Account</p>
        <h2>登录 Movie Mind</h2>
        <p>使用用户名和密码继续</p>
      </div>
      <el-form label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="用户名">
          <el-input v-model="form.username" autocomplete="username" maxlength="64" placeholder="请输入用户名" @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" autocomplete="current-password" placeholder="请输入密码" show-password type="password" @keyup.enter="handleLogin" />
        </el-form-item>
        <button class="auth-submit" type="submit" :disabled="submitting">
          {{ submitting ? '正在登录…' : '登录' }}
        </button>
      </el-form>
      <p class="auth-switch">首次来到这里？<RouterLink to="/register">免费注册</RouterLink></p>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const submitting = ref(false)
const form = reactive({ username: String(route.query.username || ''), password: '' })

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

<style scoped>
.auth-page { min-height: 660px; padding-top: 7rem; display: grid; grid-template-columns: minmax(0, 1.08fr) minmax(360px, .92fr); align-items: center; gap: clamp(3rem, 8vw, 8rem); }
.auth-intro h1 { margin: .8rem 0 1.25rem; font-size: clamp(2.8rem, 6vw, 5.6rem); line-height: 1.02; letter-spacing: -.055em; }
.auth-intro > p { max-width: 34rem; color: var(--text-secondary); line-height: 1.8; }
.auth-note { margin-top: 2rem; display: flex; gap: .65rem; color: var(--text-muted); font-size: .9rem; }
.auth-note a, .auth-switch a { color: var(--text-primary); font-weight: 700; text-decoration: none; }
.auth-card { padding: clamp(1.5rem, 4vw, 2.5rem); }
.auth-card-head { margin-bottom: 1.8rem; }
.auth-card-head h2 { margin: .45rem 0; font-size: 1.8rem; }
.auth-card-head > p:last-child, .auth-switch { color: var(--text-muted); font-size: .85rem; }
.auth-submit { width: 100%; margin-top: .5rem; padding: .9rem 1.5rem; border: 0; border-radius: 999px; color: var(--bg-primary); background: var(--text-primary); font-weight: 700; cursor: pointer; transition: opacity .2s ease, transform .2s ease; }
.auth-submit:hover { opacity: .88; }
.auth-submit:active { transform: scale(.985); }
.auth-submit:disabled { cursor: wait; opacity: .55; }
.auth-switch { margin: 1.25rem 0 0; text-align: center; }
.auth-switch a { margin-left: .35rem; }
:deep(.el-form-item__label) { padding-bottom: .4rem; color: var(--text-secondary); font-size: .72rem; font-weight: 700; letter-spacing: .08em; }
:deep(.el-input__wrapper) { padding: .55rem 1rem; border: 1px solid var(--border-soft); border-radius: 14px; background: var(--surface-secondary); box-shadow: none; }
:deep(.el-input__wrapper.is-focus) { border-color: var(--text-primary); box-shadow: none; }
@media (max-width: 900px) { .auth-page { grid-template-columns: 1fr; gap: 2.5rem; padding-top: 6rem; } .auth-intro h1 { font-size: clamp(2.7rem, 12vw, 4.4rem); } }
</style>
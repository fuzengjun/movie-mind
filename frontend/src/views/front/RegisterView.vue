<template>
  <section class="register-page">
    <div class="register-intro">
      <p class="section-kicker">Join Movie Mind</p>
      <h1>建立属于你的<br>观影坐标</h1>
      <p>收藏想看的电影，记录每一次评分，让 Movie Mind 慢慢理解你的偏好。</p>
      <ul>
        <li><span>01</span>建立个人片单与观看记录</li>
        <li><span>02</span>参与评分和影片讨论</li>
        <li><span>03</span>获得更贴近兴趣的推荐</li>
      </ul>
    </div>

    <div class="surface-card register-card">
      <div class="register-card-head">
        <div>
          <p class="section-kicker">Create Account</p>
          <h2>注册账号</h2>
        </div>
        <RouterLink to="/login">已有账号</RouterLink>
      </div>

      <el-form label-position="top" @submit.prevent="handleRegister">
        <div class="form-grid">
          <el-form-item label="用户名" required>
            <el-input v-model="form.username" autocomplete="username" maxlength="64" placeholder="用于登录" />
          </el-form-item>
          <el-form-item label="昵称" required>
            <el-input v-model="form.nickname" autocomplete="nickname" maxlength="64" placeholder="展示给其他用户" />
          </el-form-item>
        </div>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" autocomplete="email" maxlength="128" placeholder="name@example.com（选填）" />
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="密码" required>
            <el-input v-model="form.password" autocomplete="new-password" maxlength="64" placeholder="至少 6 位" show-password type="password" />
          </el-form-item>
          <el-form-item label="确认密码" required>
            <el-input v-model="form.confirmPassword" autocomplete="new-password" maxlength="64" placeholder="再次输入密码" show-password type="password" @keyup.enter="handleRegister" />
          </el-form-item>
        </div>
        <p class="form-hint">注册即表示你同意以友善、真实的方式参与 Movie Mind 社区。</p>
        <button class="register-submit" type="submit" :disabled="submitting">
          {{ submitting ? '正在创建账号…' : '创建账号' }}
        </button>
      </el-form>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()
const submitting = ref(false)
const form = reactive({ username: '', nickname: '', email: '', password: '', confirmPassword: '' })
const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

async function handleRegister() {
  form.username = form.username.trim()
  form.nickname = form.nickname.trim()
  form.email = form.email.trim()

  if (!form.username || !form.nickname || !form.password || !form.confirmPassword) {
    ElMessage.warning('请填写所有必填项')
    return
  }
  if (form.password.length < 6) {
    ElMessage.warning('密码长度不能少于 6 位')
    return
  }
  if (form.password !== form.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  if (form.email && !emailPattern.test(form.email)) {
    ElMessage.warning('请输入正确的邮箱地址')
    return
  }

  submitting.value = true
  try {
    await register(form)
    ElMessage.success('注册成功，请登录')
    await router.push({ path: '/login', query: { username: form.username } })
  } catch {
    // 请求层已统一展示错误信息
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.register-page { min-height: 700px; padding-top: 6.5rem; display: grid; grid-template-columns: minmax(0, .86fr) minmax(480px, 1.14fr); align-items: center; gap: clamp(3rem, 7vw, 7rem); }
.register-intro h1 { margin: .8rem 0 1.25rem; font-size: clamp(2.8rem, 5.5vw, 5rem); line-height: 1.04; letter-spacing: -.055em; }
.register-intro > p { max-width: 32rem; color: var(--text-secondary); line-height: 1.8; }
.register-intro ul { margin: 2rem 0 0; padding: 0; display: grid; gap: .85rem; list-style: none; color: var(--text-secondary); font-size: .9rem; }
.register-intro li { display: flex; align-items: center; gap: .8rem; }
.register-intro li span { display: grid; width: 2rem; height: 2rem; place-items: center; border: 1px solid var(--border-soft); border-radius: 50%; color: var(--text-muted); font-size: .65rem; }
.register-card { padding: clamp(1.5rem, 4vw, 2.6rem); }
.register-card-head { margin-bottom: 1.7rem; display: flex; align-items: center; justify-content: space-between; gap: 1rem; }
.register-card-head h2 { margin: .45rem 0 0; font-size: 1.9rem; }
.register-card-head a { color: var(--text-secondary); font-size: .82rem; font-weight: 700; text-decoration: none; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 1rem; }
.form-hint { margin: 0 0 1.2rem; color: var(--text-muted); font-size: .75rem; line-height: 1.6; }
.register-submit { width: 100%; padding: .9rem 1.5rem; border: 0; border-radius: 999px; color: var(--bg-primary); background: var(--text-primary); font-weight: 700; cursor: pointer; transition: opacity .2s ease, transform .2s ease; }
.register-submit:hover { opacity: .88; }
.register-submit:active { transform: scale(.985); }
.register-submit:disabled { cursor: wait; opacity: .55; }
:deep(.el-form-item__label) { padding-bottom: .4rem; color: var(--text-secondary); font-size: .72rem; font-weight: 700; letter-spacing: .08em; }
:deep(.el-input__wrapper) { padding: .52rem 1rem; border: 1px solid var(--border-soft); border-radius: 14px; background: var(--surface-secondary); box-shadow: none; }
:deep(.el-input__wrapper.is-focus) { border-color: var(--text-primary); box-shadow: none; }
@media (max-width: 920px) { .register-page { grid-template-columns: 1fr; gap: 2.5rem; padding-top: 6rem; } }
@media (max-width: 560px) { .form-grid { grid-template-columns: 1fr; } .register-card-head { align-items: flex-start; } }
</style>
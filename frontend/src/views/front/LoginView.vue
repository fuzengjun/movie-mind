<template>
  <section class="grid gap-12 lg:grid-cols-[1.1fr_0.9fr] lg:items-center min-h-[480px] pt-24 md:pt-28">
    <!-- 左侧标题与描述 -->
    <div class="space-y-6 text-left">
      <p class="section-kicker text-[10px] tracking-widest uppercase">Sign In</p>
      <h1 class="text-4xl font-extrabold tracking-tight text-[var(--text-primary)] md:text-5xl lg:text-6xl leading-[1.15]">
        进入智能影音视界
      </h1>
      <p class="max-w-md text-sm leading-relaxed text-[var(--text-secondary)] md:text-base">
        登录页保持流媒体影院风格的极简轻量感，摒弃繁杂的大表单设计，让每一次交互都自然、高效且沉浸。
      </p>
    </div>

    <!-- 右侧磨砂玻璃登录表单 -->
    <div class="surface-card p-6 md:p-10">
      <el-form label-position="top" @submit.prevent="handleLogin" class="space-y-4">
        <el-form-item label="用户名" class="el-form-item-premium">
          <el-input 
            v-model="form.username" 
            placeholder="请输入您的用户名" 
            class="premium-input"
          />
        </el-form-item>
        
        <el-form-item label="密码" class="el-form-item-premium">
          <el-input 
            v-model="form.password" 
            placeholder="请输入密码" 
            show-password 
            type="password" 
            class="premium-input"
          />
        </el-form-item>
        
        <!-- 按钮排布 -->
        <div class="mt-8 flex flex-col sm:flex-row gap-3 pt-2">
          <button 
            type="submit" 
            :disabled="submitting"
            @click.prevent="handleLogin" 
            class="flex-1 rounded-full bg-[var(--text-primary)] px-6 py-3 text-sm font-semibold text-[var(--bg-primary)] transition duration-200 hover:opacity-90 active:scale-[0.98]"
          >
            {{ submitting ? '正在验证...' : '登录' }}
          </button>
          
          <button 
            type="button"
            @click="fillDemo" 
            class="flex-1 rounded-full bg-[var(--surface-secondary)] border border-[var(--border-soft)] px-6 py-3 text-sm font-semibold text-[var(--text-primary)] transition duration-200 hover:bg-[var(--border-soft)] active:scale-[0.98]"
          >
            使用示例账户
          </button>
        </div>
      </el-form>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const submitting = ref(false)
const form = reactive({
  username: '',
  password: ''
})

function fillDemo() {
  form.username = 'admin'
  form.password = 'MovieMind123!'
}

async function handleLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请完整填写用户名和密码')
    return
  }
  submitting.value = true
  try {
    const response = await login(form)
    userStore.setToken(response.data?.token || 'demo-token')
    ElMessage.success('已进入管理员视角')
    router.push('/admin')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '登录失败，请检查账号密码')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
/* 深度定制表单标签字号和颜色，符合流媒体精细化要求 */
:deep(.el-form-item__label) {
  font-size: 11px !important;
  font-weight: 700 !important;
  text-transform: uppercase !important;
  letter-spacing: 0.1em !important;
  color: var(--text-secondary) !important;
  padding-bottom: 6px !important;
  margin-bottom: 0 !important;
}

:deep(.el-input__wrapper) {
  padding: 8px 16px !important;
  background-color: var(--surface-secondary) !important;
  box-shadow: none !important;
  border: 1px solid var(--border-soft) !important;
  border-radius: 12px !important;
  transition: all 0.2s ease !important;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: var(--text-primary) !important;
  background-color: var(--bg-secondary) !important;
}

:deep(.el-input__inner) {
  font-size: 14px !important;
  color: var(--text-primary) !important;
}
</style>

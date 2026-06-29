<template>
  <section class="grid gap-8 lg:grid-cols-[1.1fr_0.9fr] lg:items-center">
    <div class="space-y-5">
      <p class="section-kicker text-xs">Sign In</p>
      <h1 class="text-4xl font-semibold tracking-tight md:text-6xl">以影院入口的方式进入系统</h1>
      <p class="max-w-xl text-base text-[var(--text-secondary)] md:text-lg">登录页保持 Apple TV 风格的轻量感，不堆砌大表单盒子，让操作集中且沉浸。</p>
    </div>

    <div class="surface-card p-6 md:p-8">
      <el-form label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" placeholder="请输入密码" show-password type="password" />
        </el-form-item>
        <div class="mt-6 flex gap-3">
          <el-button type="primary" size="large" :loading="submitting" @click="handleLogin">登录</el-button>
          <el-button size="large" @click="fillDemo">使用示例账户</el-button>
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
  submitting.value = true
  try {
    const response = await login(form)
    userStore.setToken(response.data?.token || 'demo-token')
    ElMessage.success('已进入管理员视角')
    router.push('/admin')
  } finally {
    submitting.value = false
  }
}
</script>

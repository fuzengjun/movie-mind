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
        <RouterLink v-if="userStore.token" class="block rounded-xl px-4 py-3 nav-link" to="/recommendations" @click="drawerVisible = false">为你推荐</RouterLink>
        <RouterLink v-if="userStore.token" class="block rounded-xl px-4 py-3 nav-link" to="/profile" @click="drawerVisible = false">个人中心</RouterLink>
        <RouterLink v-if="isAdmin" class="block rounded-xl px-4 py-3 nav-link" to="/admin" @click="drawerVisible = false">后台</RouterLink>
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

const router = useRouter()
const userStore = useUserStore()
const isAdmin = computed(() => userStore.profile?.role === 'ADMIN')
const drawerVisible = ref(false)

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
</style>
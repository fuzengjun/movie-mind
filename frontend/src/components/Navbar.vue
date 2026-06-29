<template>
  <header class="absolute inset-x-0 top-0 z-30 glass-navbar">
    <!-- 顶部极细蓝色渐变微光 -->
    <div class="glow-line"></div>
    
    <div class="page-frame relative flex items-center justify-between py-4">
      <!-- 左侧 logo -->
      <RouterLink class="text-lg font-bold tracking-tight no-underline md:text-xl" to="/">
        Movie Mind
      </RouterLink>
      
      <!-- 中间菜单居中对齐 -->
      <nav class="absolute left-1/2 hidden -translate-x-1/2 items-center gap-8 text-[13px] font-medium tracking-wide md:flex">
        <RouterLink class="nav-link transition duration-200" to="/">首页</RouterLink>
        <RouterLink class="nav-link transition duration-200" to="/movies">影视库</RouterLink>
        <RouterLink class="nav-link transition duration-200" to="/admin">后台</RouterLink>
      </nav>
      
      <!-- 右侧登录按钮 -->
      <div class="hidden items-center md:flex">
        <RouterLink class="rounded-full bg-[var(--text-primary)] px-5 py-2 text-xs font-semibold text-[var(--bg-primary)] no-underline transition duration-200 hover:opacity-90" to="/login">
          登录
        </RouterLink>
      </div>
      
      <button class="pill-button-premium md:hidden" @click="drawerVisible = true">导航</button>
    </div>

    <!-- 抽屉菜单 -->
    <el-drawer v-model="drawerVisible" :with-header="false" size="82%">
      <div class="space-y-4 px-2 py-8">
        <RouterLink class="block rounded-xl px-4 py-3 nav-link" to="/" @click="drawerVisible = false">首页</RouterLink>
        <RouterLink class="block rounded-xl px-4 py-3 nav-link" to="/movies" @click="drawerVisible = false">影视库</RouterLink>
        <RouterLink class="block rounded-xl px-4 py-3 nav-link" to="/admin" @click="drawerVisible = false">后台</RouterLink>
        <RouterLink class="mt-4 block rounded-full bg-[var(--text-primary)] px-4 py-3 text-center text-sm font-semibold text-[var(--bg-primary)] no-underline" to="/login" @click="drawerVisible = false">
          登录
        </RouterLink>
      </div>
    </el-drawer>
  </header>
</template>

<script setup>
import { ref } from 'vue'

const drawerVisible = ref(false)
</script>

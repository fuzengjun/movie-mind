<template>
  <header class="sticky top-0 z-30 border-b border-[var(--border-soft)] bg-[var(--surface-primary)] backdrop-blur-2xl">
    <div class="page-frame flex items-center justify-between py-4">
      <RouterLink class="text-lg font-semibold tracking-tight no-underline md:text-xl" to="/">
        Movie Mind
      </RouterLink>
      <nav class="hidden items-center gap-5 text-sm md:flex">
        <RouterLink class="nav-link" to="/">首页</RouterLink>
        <RouterLink class="nav-link" to="/movies">影视库</RouterLink>
        <RouterLink class="nav-link" to="/admin">后台</RouterLink>
        <RouterLink class="rounded-full bg-[var(--text-primary)] px-4 py-2 text-sm font-medium text-[var(--bg-primary)] no-underline transition hover:opacity-90" to="/login">
          登录
        </RouterLink>
      </nav>
      <button class="pill-button md:hidden" @click="drawerVisible = true">导航</button>
    </div>

    <el-drawer v-model="drawerVisible" :with-header="false" size="82%">
      <div class="space-y-3 px-2 py-6">
        <RouterLink class="block rounded-2xl px-4 py-3 nav-link" to="/" @click="drawerVisible = false">首页</RouterLink>
        <RouterLink class="block rounded-2xl px-4 py-3 nav-link" to="/movies" @click="drawerVisible = false">影视库</RouterLink>
        <RouterLink class="block rounded-2xl px-4 py-3 nav-link" to="/admin" @click="drawerVisible = false">后台</RouterLink>
        <RouterLink class="mt-2 block rounded-2xl bg-[var(--text-primary)] px-4 py-3 text-center font-medium text-[var(--bg-primary)] no-underline" to="/login" @click="drawerVisible = false">
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

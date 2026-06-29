<template>
  <div class="page-shell pb-10">
    <header class="page-frame flex items-center justify-between py-5 md:py-7">
      <div>
        <p class="section-kicker text-xs">Movie Mind Admin</p>
        <h1 class="mt-2 text-2xl font-semibold tracking-tight md:text-3xl">运营与内容控制台</h1>
      </div>
      <button class="pill-button md:hidden" @click="drawerVisible = true">菜单</button>
    </header>

    <div class="page-frame grid gap-6 md:grid-cols-[240px_minmax(0,1fr)]">
      <aside class="surface-card hidden h-fit p-4 md:block">
        <nav class="space-y-2">
          <RouterLink v-for="item in navItems" :key="item.to" :to="item.to" class="block rounded-2xl px-4 py-3 nav-link" active-class="!text-[var(--text-primary)] bg-[rgba(239,123,69,0.12)]">
            <p class="font-medium">{{ item.label }}</p>
            <p class="mt-1 text-xs text-[var(--text-muted)]">{{ item.description }}</p>
          </RouterLink>
        </nav>
      </aside>

      <main class="min-w-0">
        <router-view />
      </main>
    </div>

    <el-drawer v-model="drawerVisible" direction="ltr" size="78%" :with-header="false">
      <div class="space-y-5 px-2 py-6">
        <div>
          <p class="section-kicker text-xs">Admin Menu</p>
          <h2 class="mt-2 text-2xl font-semibold">Movie Mind</h2>
        </div>
        <nav class="space-y-2">
          <RouterLink
            v-for="item in navItems"
            :key="item.to"
            :to="item.to"
            class="block rounded-2xl px-4 py-3 nav-link"
            active-class="!text-[var(--text-primary)] bg-[rgba(239,123,69,0.12)]"
            @click="drawerVisible = false"
          >
            <p class="font-medium">{{ item.label }}</p>
            <p class="mt-1 text-xs text-[var(--text-muted)]">{{ item.description }}</p>
          </RouterLink>
        </nav>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const drawerVisible = ref(false)

const navItems = [
  { to: '/admin', label: '数据概览', description: '趋势、榜单与内容热度' },
  { to: '/admin/movies', label: '影片管理', description: '重点内容与发行表现' },
  { to: '/admin/users', label: '用户管理', description: '活跃用户与行为概况' }
]
</script>

<template>
  <section class="space-y-6">
    <div class="surface-card flex flex-col gap-4 p-6 md:flex-row md:items-end md:justify-between">
      <div>
        <p class="section-kicker text-xs">Audience Ops</p>
        <h2 class="mt-3 text-3xl font-semibold tracking-tight md:text-5xl">用户管理</h2>
        <p class="mt-3 max-w-2xl text-[var(--text-secondary)]">聚合用户的收藏、评论与评分行为，让后台页面维持与前台一致的内容驱动式视觉。</p>
      </div>
      <div class="detail-chip">已展示 {{ users.length }} 位用户</div>
    </div>

    <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
      <article v-for="user in users" :key="user.id" class="surface-card p-5">
        <div class="flex items-center gap-4">
          <img v-if="user.avatar" :src="user.avatar" :alt="user.nickname" class="h-14 w-14 rounded-full object-cover" />
          <div v-else class="flex h-14 w-14 items-center justify-center rounded-full bg-[rgba(239,123,69,0.18)] text-lg font-semibold">{{ user.nickname?.slice(0, 1) || 'U' }}</div>
          <div>
            <h3 class="text-lg font-semibold">{{ user.nickname }}</h3>
            <p class="text-sm text-[var(--text-muted)]">@{{ user.username }} · {{ user.role }}</p>
          </div>
        </div>
        <div class="mt-5 grid grid-cols-3 gap-3">
          <div class="surface-card-soft p-3">
            <p class="text-xs text-[var(--text-muted)]">收藏</p>
            <p class="mt-2 text-xl font-semibold">{{ user.favoriteCount }}</p>
          </div>
          <div class="surface-card-soft p-3">
            <p class="text-xs text-[var(--text-muted)]">评论</p>
            <p class="mt-2 text-xl font-semibold">{{ user.commentCount }}</p>
          </div>
          <div class="surface-card-soft p-3">
            <p class="text-xs text-[var(--text-muted)]">评分</p>
            <p class="mt-2 text-xl font-semibold">{{ user.ratingCount }}</p>
          </div>
        </div>
        <div class="mt-5 flex items-center justify-between text-sm text-[var(--text-secondary)]">
          <span>{{ user.email || '未提供邮箱' }}</span>
          <span>{{ user.status === 1 ? '正常' : '禁用' }}</span>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getAdminUsers } from '@/api/admin'

const users = ref([])

onMounted(async () => {
  const response = await getAdminUsers()
  users.value = response.data || []
})
</script>

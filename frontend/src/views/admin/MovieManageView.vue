<template>
  <section class="space-y-6">
    <div class="surface-card flex flex-col gap-4 p-6 md:flex-row md:items-end md:justify-between">
      <div>
        <p class="section-kicker text-xs">Content Ops</p>
        <h2 class="mt-3 text-3xl font-semibold tracking-tight md:text-5xl">影片管理</h2>
        <p class="mt-3 max-w-2xl text-[var(--text-secondary)]">当前用真实接口拉取片库数据，展示重点影片的地区、评分、收藏、浏览以及导演演员信息。</p>
      </div>
      <div class="detail-chip">最近 {{ movies.length }} 部内容</div>
    </div>

    <div class="table-shell surface-card overflow-hidden p-3 md:p-4">
      <el-table :data="movies" stripe>
        <el-table-column label="影片" min-width="260">
          <template #default="{ row }">
            <div class="flex items-center gap-4">
              <img v-if="row.posterUrl" :src="row.posterUrl" :alt="row.title" class="h-20 w-14 rounded-2xl object-cover" />
              <div v-else class="h-20 w-14 rounded-2xl" style="background: var(--poster-fallback)"></div>
              <div>
                <p class="font-medium">{{ row.title }}</p>
                <p class="mt-1 text-sm text-[var(--text-muted)]">{{ (row.categories || []).join(' · ') || '未分类' }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="导演" min-width="180">
          <template #default="{ row }">{{ (row.directors || []).join(' / ') || '未导入' }}</template>
        </el-table-column>
        <el-table-column label="主要演员" min-width="220">
          <template #default="{ row }">{{ (row.actors || []).slice(0, 3).join(' / ') || '未导入' }}</template>
        </el-table-column>
        <el-table-column prop="region" label="地区" width="150" />
        <el-table-column label="上映" width="120">
          <template #default="{ row }">{{ row.releaseDate ? String(row.releaseDate).slice(0, 10) : '未提供' }}</template>
        </el-table-column>
        <el-table-column prop="averageRating" label="评分" width="100" />
        <el-table-column prop="favoriteCount" label="收藏" width="100" />
        <el-table-column prop="viewCount" label="浏览" width="100" />
      </el-table>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getAdminMovies } from '@/api/admin'

const movies = ref([])

onMounted(async () => {
  const response = await getAdminMovies()
  movies.value = response.data || []
})
</script>

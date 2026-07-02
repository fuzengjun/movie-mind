<template>
  <RouterLink :to="`/movies/${movie.id}`" class="group block no-underline">
    <!-- 外层卡片容器应用高级流媒体卡片类 -->
    <div class="poster-card-premium relative overflow-hidden">
      <!-- 比例容器 -->
      <div class="aspect-[2/3] overflow-hidden bg-[var(--bg-secondary)]">
        <img 
          v-if="movie.posterUrl" 
          :src="movie.posterUrl" 
          :alt="movie.title" 
          class="poster-image" 
        />
        <!-- 缺省海报占位 -->
        <div v-else class="flex h-full w-full items-center justify-center bg-[var(--border-soft)] text-[var(--text-muted)]">
          <span class="text-xs">No Poster</span>
        </div>
      </div>
      
      <!-- 磨砂渐变遮罩，只在悬停时或暗色背景中提供微弱阴影保障，此处使用全天候柔和遮罩 -->
      <div class="absolute inset-0 bg-gradient-to-t from-black/20 via-transparent to-transparent opacity-0 transition-opacity duration-300 group-hover:opacity-100"></div>
    </div>
    
    <!-- 信息排布在海报下方，显得极为干净和高级 -->
    <div class="mt-2.5 px-0.5 text-left">
      <!-- 标题 -->
      <h3 class="line-clamp-1 text-sm font-semibold tracking-wide text-[var(--text-primary)] transition duration-200 group-hover:text-[var(--accent-primary)]">
        {{ movie.title }}
      </h3>
      
      <!-- 底部元数据与评分一行排列 -->
      <div class="mt-1 flex items-center justify-between text-xs text-[var(--text-secondary)] font-medium">
        <span>{{ movie.releaseDate ? String(movie.releaseDate).slice(0, 4) : '未知年份' }}</span>
        
        <div class="flex items-center gap-2">
          <!-- 评分小胶囊 -->
          <span class="rounded bg-[var(--surface-secondary)] border border-[var(--border-soft)] px-1.5 py-0.5 text-[10px] font-bold text-[var(--text-primary)]">
            ★ {{ movie.averageRating ? Number(movie.averageRating).toFixed(1) : '0.0' }}
          </span>
        </div>
      </div>
      <p v-if="showReason && movie.reason" class="recommend-reason">{{ movie.reason }}</p>
    </div>
  </RouterLink>
</template>

<script setup>
defineProps({
  showReason: {
    type: Boolean,
    default: false
  },
  movie: {
    type: Object,
    default: () => ({})
  }
})
</script>

<style scoped>
.recommend-reason {
  min-height: 2.5rem;
  margin: .45rem 0 0;
  color: var(--text-muted);
  font-size: .7rem;
  line-height: 1.45;
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-clamp: 2;
}
</style>

<template>
  <section class="space-y-3">
    <!-- 标题区，带类似 Apple TV 的大箭头，点击跳转到演职人员详情页面 -->
    <RouterLink 
      :to="`/movies/${movieId}/cast`"
      class="title-link flex items-center gap-3 px-1 mb-2 select-none group no-underline"
    >
      <h2 class="text-base md:text-lg font-bold text-[var(--text-primary)] tracking-tight">演职人员</h2>
      <!-- 偏大的淡灰色向右箭头 -->
      <span 
        class="text-lg md:text-xl font-light text-[var(--text-muted)] opacity-60 transition-transform group-hover:translate-x-1 select-none"
      >
        &gt;
      </span>
    </RouterLink>

    <!-- 演职人员横向滚动滑轨 -->
    <div class="cast-rail">
      <div v-for="person in cast" :key="person.name + '-' + person.roleName" class="cast-card">
        <!-- 圆形头像 -->
        <div class="avatar-wrapper">
          <img 
            v-if="person.profileUrl" 
            :src="person.profileUrl" 
            :alt="person.name" 
            class="avatar-img"
            loading="lazy" 
          />
          <!-- 默认占位头像 -->
          <div v-else class="avatar-placeholder">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-8 h-8 opacity-60">
              <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 6a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0ZM4.501 20.118a7.5 7.5 0 0 1 14.998 0A17.933 17.933 0 0 1 12 21.75c-2.676 0-5.216-.584-7.499-1.632Z" />
            </svg>
          </div>
        </div>
        <!-- 名字 -->
        <div class="cast-name" :title="person.name">
          {{ person.name }}
        </div>
        <!-- 角色或职责 -->
        <div class="cast-role" :title="person.roleName">
          {{ person.roleName }}
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  movieId: {
    type: [Number, String],
    required: true
  },
  cast: {
    type: Array,
    default: () => []
  }
})
</script>

<style scoped>
.cast-rail {
  display: flex;
  gap: 20px;
  overflow-x: auto;
  padding: 4px 4px 16px 4px;
  scrollbar-width: none; /* Firefox 隐藏滚动条 */
  -ms-overflow-style: none; /* IE/Edge 隐藏滚动条 */
}

.cast-rail::-webkit-scrollbar {
  display: none; /* Chrome/Safari/Opera 隐藏滚动条 */
}

.cast-card {
  flex: 0 0 auto;
  width: 96px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.cast-card:hover {
  transform: translateY(-2px);
}

.avatar-wrapper {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  overflow: hidden;
  margin-bottom: 6px;
  background-color: var(--surface-secondary);
  border: 1px solid var(--border-soft);
  box-shadow: var(--shadow-md);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  position: relative;
}

.cast-card:hover .avatar-wrapper {
  box-shadow: var(--shadow-lg);
  transform: scale(1.04);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  background: var(--poster-fallback);
}

.cast-name {
  width: 100%;
  font-size: 11px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.3;
  margin-top: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cast-role {
  width: 100%;
  font-size: 10px;
  color: var(--text-muted);
  line-height: 1.2;
  margin-top: 2px;
  font-weight: 400;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.title-link {
  width: fit-content;
  display: inline-flex;
}
</style>

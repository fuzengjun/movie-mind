<template>
  <section class="space-y-12">
    <!-- 沉浸式巨幕 Banner -->
    <div class="cinema-hero" :style="heroStyle">
      <div class="absolute inset-0 z-0 bg-black/10"></div>
      
      <div class="hero-content relative z-10 flex min-h-[500px] flex-col justify-end p-6 md:p-12 lg:p-16">
        <!-- 小分类标签 -->
        <p class="section-kicker text-[10px] font-bold tracking-[0.25em] text-white/70 uppercase">
          Now Streaming
        </p>
        
        <!-- 主打电影标题 -->
        <h1 class="mt-3 max-w-2xl text-3xl font-extrabold tracking-tight text-white md:text-5xl lg:text-6xl leading-[1.1]">
          {{ featuredMovie?.title || '电影之境' }}
        </h1>
        
        <!-- 电影简介 -->
        <p class="mt-4 max-w-xl text-sm leading-relaxed text-white/80 md:text-base">
          {{ featuredMovie?.overview || '探索无限视界，这里汇聚了精选影视、高分推荐以及您私人的影视管理体验。' }}
        </p>
        
        <!-- 主次药丸按钮组合 -->
        <div class="mt-8 flex flex-wrap gap-4">
          <RouterLink 
            v-if="featuredMovie" 
            :to="`/movies/${featuredMovie.id}`" 
            class="rounded-full bg-white px-7 py-3 text-sm font-semibold text-black no-underline transition duration-200 hover:bg-white/90 hover:scale-[1.02]"
          >
            查看详情
          </RouterLink>
          
          <RouterLink 
            to="/admin" 
            class="rounded-full bg-white/10 backdrop-blur-md px-7 py-3 text-sm font-semibold text-white border border-white/10 no-underline transition duration-200 hover:bg-white/20 hover:scale-[1.02]"
          >
            进入后台
          </RouterLink>
        </div>
      </div>
    </div>

    <!-- 影视展示轨 (Rails) -->
    <div class="space-y-12">
      <ContentRail 
        title="热门影视" 
        eyebrow="Featured Row" 
        description="基于当前片库热度与浏览表现展示重点内容。" 
        :movies="movies.slice(0, 8)" 
      />
      
      <ContentRail 
        title="高分力荐" 
        eyebrow="Top Rated" 
        description="评分、收藏与观看行为共同形成这条精选内容带。" 
        :movies="ratedMovies" 
      />
      
      <ContentRail 
        title="最新入库" 
        eyebrow="Library Pulse" 
        description="系统近期更新的影片内容，持续推荐最新上映的佳作。" 
        :movies="recentMovies" 
      />
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getMovieList } from '@/api/movie'
import ContentRail from '@/components/ContentRail.vue'
import { mockMovies } from '@/utils/mockData'

const movies = ref([])

const featuredMovie = computed(() => movies.value[0] || null)
const ratedMovies = computed(() => [...movies.value].sort((a, b) => Number(b.averageRating || 0) - Number(a.averageRating || 0)).slice(0, 8))
const recentMovies = computed(() => [...movies.value].sort((a, b) => String(b.releaseDate || '').localeCompare(String(a.releaseDate || ''))).slice(0, 8))

const heroStyle = computed(() => {
  const image = featuredMovie.value?.backdropUrl || featuredMovie.value?.posterUrl
  return image
    ? { backgroundImage: `linear-gradient(to top, rgba(0,0,0,0.85) 0%, rgba(0,0,0,0.4) 40%, rgba(0,0,0,0.15) 100%), url(${image})` }
    : {}
})

onMounted(async () => {
  try {
    const response = await getMovieList()
    movies.value = response.data && response.data.length > 0 ? response.data : mockMovies
  } catch (error) {
    console.warn('API error, falling back to mock movies:', error)
    movies.value = mockMovies
  }
})
</script>

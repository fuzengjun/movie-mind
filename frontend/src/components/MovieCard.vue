<template>
  <RouterLink :to="`/movies/${movie.id}`" class="group block no-underline">
    <article class="poster-shell h-full transition duration-300 group-hover:-translate-y-1 group-hover:scale-[1.01]">
      <div class="aspect-[3/4] overflow-hidden">
        <img v-if="movie.posterUrl" :src="movie.posterUrl" :alt="movie.title" class="poster-image transition duration-500 group-hover:scale-105" />
        <div v-else class="h-full w-full" />
      </div>
      <div class="poster-overlay"></div>
      <div class="absolute inset-x-0 bottom-0 z-10 space-y-3 p-4 text-white">
        <div class="flex flex-wrap gap-2 text-[11px] uppercase tracking-[0.2em] text-white/70">
          <span v-for="tag in visibleTags" :key="tag">{{ tag }}</span>
        </div>
        <div>
          <h3 class="text-lg font-semibold leading-tight">{{ movie.title }}</h3>
          <p class="mt-1 text-sm text-white/72">{{ metaLine }}</p>
        </div>
        <div class="flex items-center justify-between text-sm text-white/76">
          <span>{{ ratingLabel }}</span>
          <span>{{ viewLabel }}</span>
        </div>
      </div>
    </article>
  </RouterLink>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  movie: {
    type: Object,
    default: () => ({})
  }
})

const visibleTags = computed(() => (props.movie.categories || []).slice(0, 3))
const metaLine = computed(() => {
  const bits = []
  if (props.movie.releaseDate) {
    bits.push(String(props.movie.releaseDate).slice(0, 4))
  }
  if (props.movie.region) {
    bits.push(props.movie.region)
  }
  if (props.movie.runtime) {
    bits.push(`${props.movie.runtime} min`)
  }
  return bits.join(' · ') || 'Featured title'
})
const ratingLabel = computed(() => `评分 ${props.movie.averageRating || '0.0'}`)
const viewLabel = computed(() => `${props.movie.viewCount || 0} 浏览`)
</script>

<template>
  <section class="space-y-4">
    <div v-if="title || eyebrow || description" class="flex items-end justify-between gap-4">
      <div>
        <p v-if="eyebrow" class="section-kicker text-xs">{{ eyebrow }}</p>
        <h2 v-if="title" class="mt-2 text-2xl font-semibold tracking-tight">{{ title }}</h2>
      </div>
      <p v-if="description" class="hidden max-w-xl text-right text-sm text-[var(--text-muted)] md:block">{{ description }}</p>
    </div>
    <div class="content-rail">
      <MovieCard v-for="movie in movies" :key="movie.id || movie.title" :movie="movie" />
    </div>
  </section>
</template>

<script setup>
import MovieCard from './MovieCard.vue'

defineProps({
  title: {
    type: String,
    default: ''
  },
  eyebrow: {
    type: String,
    default: ''
  },
  description: {
    type: String,
    default: ''
  },
  movies: {
    type: Array,
    default: () => []
  }
})
</script>

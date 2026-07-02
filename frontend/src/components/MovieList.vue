<template>
  <div class="grid gap-5 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5">
    <MovieCard v-for="movie in movies" :key="movie.id || movie.title" :movie="movie" />
  </div>
</template>

<script setup>
import MovieCard from './MovieCard.vue'

defineProps({
  movies: {
    type: Array,
    default: () => []
  }
})
</script>

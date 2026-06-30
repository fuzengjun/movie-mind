<template>
  <section class="content-rail-section space-y-4">
    <div v-if="title || eyebrow || description" class="flex flex-col md:flex-row md:items-end md:justify-between gap-2 px-1">
      <div>
        <p v-if="eyebrow" class="section-kicker text-[10px] font-bold tracking-[0.2em] text-[var(--text-secondary)] uppercase">{{ eyebrow }}</p>
        <h2 v-if="title" class="mt-1 text-xl font-bold tracking-tight md:text-2xl text-[var(--text-primary)]">{{ title }}</h2>
      </div>
      <p v-if="description" class="hidden max-w-md text-left md:text-right text-xs font-medium text-[var(--text-muted)] md:block">{{ description }}</p>
    </div>

    <div class="rail-shell">
      <div ref="railRef" class="content-rail" @scroll.passive="updateControls">
        <MovieCard v-for="movie in movies" :key="movie.id || movie.title" :movie="movie" />
      </div>
      <button v-if="controls && canScrollLeft" class="rail-control rail-control-left" type="button" aria-label="向左浏览影片" @click="scrollRail(-1)">
        <span aria-hidden="true">‹</span>
      </button>
      <button v-if="controls && canScrollRight" class="rail-control rail-control-right" type="button" aria-label="向右浏览影片" @click="scrollRail(1)">
        <span aria-hidden="true">›</span>
      </button>
    </div>
  </section>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import MovieCard from './MovieCard.vue'

const props = defineProps({
  title: { type: String, default: '' },
  eyebrow: { type: String, default: '' },
  description: { type: String, default: '' },
  movies: { type: Array, default: () => [] },
  controls: { type: Boolean, default: true }
})

const railRef = ref(null)
const canScrollLeft = ref(false)
const canScrollRight = ref(false)
let resizeObserver

function updateControls() {
  const rail = railRef.value
  if (!rail || !props.controls) return
  canScrollLeft.value = rail.scrollLeft > 2
  canScrollRight.value = rail.scrollLeft + rail.clientWidth < rail.scrollWidth - 2
}

function scrollRail(direction) {
  const rail = railRef.value
  if (!rail) return
  rail.scrollBy({ left: direction * Math.max(rail.clientWidth * 0.82, 220), behavior: 'smooth' })
}

async function refreshControls() {
  await nextTick()
  requestAnimationFrame(() => requestAnimationFrame(updateControls))
}

watch(() => props.movies.length, refreshControls)

onMounted(() => {
  refreshControls()
  if (window.ResizeObserver) {
    resizeObserver = new ResizeObserver(updateControls)
    resizeObserver.observe(railRef.value)
  }
})

onBeforeUnmount(() => resizeObserver?.disconnect())
</script>

<style scoped>
.content-rail-section,
.rail-shell {
  width: 100%;
  min-width: 0;
  max-width: 100%;
}
.rail-shell { position: relative; overflow: hidden; }
.content-rail {
  width: 100%;
  min-width: 0;
  max-width: 100%;
  grid-auto-columns: clamp(160px, 42%, 210px);
  overscroll-behavior-inline: contain;
}
.rail-control {
  position: absolute;
  top: 42%;
  z-index: 3;
  display: grid;
  width: 40px;
  height: 68px;
  padding: 0;
  place-items: center;
  border: 1px solid var(--border-soft);
  border-radius: 999px;
  color: var(--text-primary);
  background: color-mix(in srgb, var(--surface-primary) 88%, transparent);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.16);
  backdrop-filter: blur(18px);
  cursor: pointer;
  transform: translateY(-50%);
  transition: transform 160ms ease, background 160ms ease;
}
.rail-control:hover { background: var(--surface-primary); transform: translateY(-50%) scale(1.06); }
.rail-control:focus-visible { outline: 2px solid var(--accent-primary); outline-offset: 3px; }
.rail-control span { font-size: 38px; font-weight: 300; line-height: 1; transform: translateY(-2px); }
.rail-control-left { left: 8px; }
.rail-control-right { right: 8px; }
@media (max-width: 768px) { .rail-control { width: 36px; height: 58px; } }
</style>

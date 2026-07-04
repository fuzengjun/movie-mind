<template>
  <section class="person-works-page">
    <button type="button" class="works-back-button" @click="goBack">
      <span aria-hidden="true">&lt;</span>
      <span>返回</span>
    </button>

    <div v-if="loading" class="works-state">正在加载作品...</div>

    <div v-else-if="error" class="works-state">
      <p>{{ error }}</p>
      <button class="pill-button-premium" type="button" @click="loadPerson">重试</button>
    </div>

    <template v-else-if="person">
      <header class="works-header">
        <p class="works-person-name">{{ person.name }}</p>
        <h1>{{ pageTitle }}</h1>
        <p>共收录 {{ movies.length }} 部作品</p>
      </header>

      <div v-if="movies.length" class="works-grid">
        <RouterLink
            v-for="movie in movies"
            :key="movie.id"
            :to="`/movies/${movie.id}`"
            class="works-movie-link"
        >
          <article class="works-movie-card">
            <div class="works-poster poster-shell">
              <img
                  v-if="movie.posterUrl"
                  :src="movie.posterUrl"
                  :alt="movie.title"
                  class="poster-image"
                  loading="lazy"
              />
              <div v-else class="works-poster-fallback">{{ movie.title?.slice(0, 1) || '?' }}</div>
            </div>
            <h2>{{ movie.title }}</h2>
            <p>
              <span>{{ releaseYear(movie.releaseDate) }}</span>
              <span v-if="movie.averageRating"> · ★ {{ Number(movie.averageRating).toFixed(1) }}</span>
            </p>
            <p v-if="movie.roleName" class="works-role">{{ movie.roleName }}</p>
          </article>
        </RouterLink>
      </div>

      <div v-else class="works-state">暂未收录该分类下的作品。</div>
    </template>
  </section>
</template>

<script setup>
import {computed, onMounted, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {getPersonDetail} from '@/api/person'

const route = useRoute()
const router = useRouter()
const person = ref(null)
const loading = ref(false)
const error = ref('')

const currentSection = computed(() => {
  return person.value?.sections?.find((section) => section.key === route.params.sectionKey) || null
})

const movies = computed(() => currentSection.value?.movies || [])
const pageTitle = computed(() => route.params.sectionKey === 'directing' ? '导演作品' : '参演作品')

function releaseYear(date) {
  return date ? String(date).slice(0, 4) : '未知年份'
}

function goBack() {
  if (window.history.state?.back) {
    router.back()
    return
  }
  router.push({
    name: 'person-detail',
    params: {type: route.params.type, id: route.params.id}
  })
}

async function loadPerson() {
  loading.value = true
  error.value = ''
  try {
    const response = await getPersonDetail(route.params.type, route.params.id)
    person.value = response.data
  } catch (err) {
    person.value = null
    error.value = err?.message || '作品信息加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

watch(
    () => [route.params.type, route.params.id, route.params.sectionKey],
    () => loadPerson()
)

onMounted(() => loadPerson())
</script>

<style scoped>
.person-works-page {
  padding-top: 52px;
  padding-bottom: 56px;
}

.works-back-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--text-primary);
  cursor: pointer;
  font: inherit;
  font-size: 14px;
  font-weight: 600;
}

.works-header {
  margin: 42px 0 30px;
}

.works-person-name {
  margin: 0 0 6px;
  color: var(--text-secondary);
  font-size: 0.9rem;
  font-weight: 650;
}

.works-header h1 {
  margin: 0;
  color: var(--text-primary);
  font-size: clamp(2.25rem, 5vw, 4.25rem);
  line-height: 1;
  letter-spacing: -0.055em;
}

.works-header > p:last-child {
  margin: 14px 0 0;
  color: var(--text-secondary);
}

.works-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 30px 20px;
}

.works-movie-link {
  min-width: 0;
  color: inherit;
  text-decoration: none;
}

.works-movie-card {
  min-width: 0;
}

.works-poster {
  width: 100%;
  aspect-ratio: 2 / 3;
}

.works-poster-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--poster-fallback);
  color: var(--text-muted);
  font-size: 2.5rem;
  font-weight: 700;
}

.works-movie-card h2 {
  overflow: hidden;
  margin: 12px 2px 0;
  color: var(--text-primary);
  font-size: 0.95rem;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.works-movie-card p {
  margin: 5px 2px 0;
  color: var(--text-secondary);
  font-size: 0.8rem;
}

.works-role {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.works-state {
  min-height: 280px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 14px;
  margin-top: 36px;
  border: 1px solid var(--border-soft);
  border-radius: 28px;
  background: var(--surface-primary);
  color: var(--text-secondary);
}

@media (min-width: 1100px) {
  .works-grid {
    grid-template-columns: repeat(6, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .person-works-page {
    padding-top: 44px;
  }

  .works-header {
    margin-top: 32px;
  }

  .works-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 24px 14px;
  }
}
</style>
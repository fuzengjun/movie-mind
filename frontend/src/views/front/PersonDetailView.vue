<template>
  <section class="person-detail-page">
    <div class="person-back-row">
      <button type="button" class="person-back-link" @click="goBack">
        <span class="person-back-icon">&lt;</span>
        <span>返回</span>
      </button>
    </div>

    <div v-if="loading" class="person-state-card">
      正在加载人物信息...
    </div>

    <div v-else-if="error" class="person-state-card">
      <p>{{ error }}</p>
      <button class="pill-button-premium mt-4" type="button" @click="loadPerson">重试</button>
    </div>

    <template v-else-if="person">
      <section class="person-hero" :style="heroStyle">
        <div class="person-hero-overlay"></div>
        <div class="person-hero-content">
          <div class="person-avatar-shell">
            <img v-if="person.profileUrl" :src="person.profileUrl" :alt="person.name" class="person-avatar" />
            <div v-else class="person-avatar-fallback">{{ initials }}</div>
          </div>

          <div class="person-copy">
            <p class="section-kicker person-kicker">{{ person.typeLabel }}</p>
            <h1 class="person-name">{{ person.name }}</h1>
            <p v-if="person.originalName && person.originalName !== person.name" class="person-original-name">
              {{ person.originalName }}
            </p>
            <p class="person-biography">
              {{ person.biography || '暂无人物简介。' }}
            </p>

            <div class="person-meta-grid">
              <div class="person-meta-item">
                <span class="person-meta-label">收录作品</span>
                <span class="person-meta-value">{{ person.movieCount || 0 }}</span>
              </div>
              <div class="person-meta-item">
                <span class="person-meta-label">性别</span>
                <span class="person-meta-value">{{ person.gender || '未提供' }}</span>
              </div>
              <div class="person-meta-item">
                <span class="person-meta-label">生日</span>
                <span class="person-meta-value">{{ birthdayText }}</span>
              </div>
              <div class="person-meta-item">
                <span class="person-meta-label">地区</span>
                <span class="person-meta-value">{{ person.nationality || '未提供' }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="person-sections">
        <div v-for="section in person.sections || []" :key="section.key" class="person-section">
          <RouterLink
            :to="{
              name: 'person-works',
              params: {
                type: person.type || route.params.type,
                id: person.id,
                sectionKey: section.key
              }
            }"
            class="person-section-head"
          >
            <h2 class="person-section-title">{{ sectionTitle(section) }}</h2>
            <span class="person-section-arrow">&gt;</span>
          </RouterLink>

          <div v-if="section.movies && section.movies.length" class="content-rail">
            <RouterLink
              v-for="movie in section.movies"
              :key="section.key + '-' + movie.id"
              :to="`/movies/${movie.id}`"
              class="person-movie-link"
            >
              <article class="person-movie-card">
                <div class="person-movie-poster poster-shell">
                  <img v-if="movie.posterUrl" :src="movie.posterUrl" :alt="movie.title" class="poster-image" loading="lazy" />
                  <div v-else class="person-movie-fallback">{{ movie.title?.slice(0, 1) || '?' }}</div>
                </div>
                <div class="person-movie-copy">
                  <h3 class="person-movie-title">{{ movie.title }}</h3>
                  <p class="person-movie-meta">
                    <span>{{ releaseYear(movie.releaseDate) }}</span>
                    <span v-if="movie.averageRating" class="person-movie-dot">·</span>
                    <span v-if="movie.averageRating">★ {{ Number(movie.averageRating).toFixed(1) }}</span>
                  </p>
                  <p v-if="movie.roleName" class="person-movie-role">{{ movie.roleName }}</p>
                </div>
              </article>
            </RouterLink>
          </div>
        </div>

        <div v-if="!person.sections || person.sections.length === 0" class="person-state-card">
          暂未收录该人物的相关作品。
        </div>
      </section>
    </template>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPersonDetail } from '@/api/person'

const route = useRoute()
const router = useRouter()
const person = ref(null)
const loading = ref(false)
const error = ref('')

const heroStyle = computed(() => {
  const image = person.value?.backdropUrl
  if (!image) {
    return {}
  }
  return {
    backgroundImage: `radial-gradient(circle at 35% 30%, rgba(244, 196, 180, 0.42), transparent 42%), linear-gradient(120deg, rgba(240, 236, 235, 0.96), rgba(247, 222, 214, 0.92) 48%, rgba(238, 237, 240, 0.96)), url(${image})`
  }
})

const initials = computed(() => {
  const value = person.value?.name || ''
  if (!value) {
    return '?'
  }
  return value.slice(0, 1).toUpperCase()
})

const birthdayText = computed(() => person.value?.birthday || '未提供')

function releaseYear(date) {
  return date ? String(date).slice(0, 4) : '未知年份'
}

function sectionTitle(section) {
  return section?.key === 'directing' ? '导演作品' : '参演作品'
}

function goBack() {
  if (window.history.state?.back) {
    router.back()
    return
  }
  router.push('/')
}

async function loadPerson() {
  loading.value = true
  error.value = ''
  try {
    const response = await getPersonDetail(route.params.type, route.params.id)
    person.value = response.data
  } catch (err) {
    person.value = null
    error.value = err?.message || '人物信息加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

watch(
  () => [route.params.type, route.params.id],
  () => {
    loadPerson()
  }
)

onMounted(() => {
  loadPerson()
})
</script>

<style scoped>
.person-detail-page {
  padding-top: 52px;
  padding-bottom: 56px;
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.person-back-row {
  display: flex;
  align-items: center;
}

.person-back-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--text-primary);
  cursor: pointer;
  font-family: inherit;
  font-size: 14px;
  font-weight: 600;
}

.person-back-icon {
  font-size: 18px;
  line-height: 1;
}

.person-state-card {
  min-height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 12px;
  padding: 32px;
  border-radius: 28px;
  background: var(--surface-primary);
  border: 1px solid var(--border-soft);
  box-shadow: var(--shadow-md);
  color: var(--text-secondary);
  text-align: center;
}

.person-hero {
  position: relative;
  overflow: hidden;
  min-height: 380px;
  border-radius: 32px;
  background-color: #ebe8ea;
  background-size: cover;
  background-position: center;
  box-shadow: var(--shadow-lg);
}

.person-hero-overlay {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 32% 26%, rgba(255, 219, 204, 0.6), transparent 30%),
    linear-gradient(90deg, rgba(236, 235, 239, 0.96) 0%, rgba(249, 219, 209, 0.92) 50%, rgba(235, 235, 239, 0.96) 100%);
}

html[data-theme='dark'] .person-hero-overlay {
  background:
    radial-gradient(circle at 32% 26%, rgba(255, 210, 188, 0.18), transparent 28%),
    linear-gradient(90deg, rgba(26, 26, 28, 0.96) 0%, rgba(58, 42, 38, 0.9) 50%, rgba(22, 22, 24, 0.96) 100%);
}

.person-hero-content {
  position: relative;
  z-index: 1;
  min-height: 380px;
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 36px;
  align-items: center;
  padding: 56px 56px 48px;
}

.person-avatar-shell {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 26px 48px rgba(0, 0, 0, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.28);
  background: rgba(255, 255, 255, 0.5);
}

.person-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.person-avatar-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 72px;
  font-weight: 700;
  color: rgba(0, 0, 0, 0.4);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.8), rgba(236, 229, 228, 0.95));
}

.person-copy {
  width: 100%;
  min-width: 0;
}

.person-kicker {
  color: rgba(0, 0, 0, 0.45);
}

html[data-theme='dark'] .person-kicker {
  color: rgba(255, 255, 255, 0.58);
}

.person-name {
  margin: 8px 0 0;
  font-size: clamp(2.8rem, 5vw, 4.5rem);
  line-height: 1.02;
  letter-spacing: -0.05em;
  color: #111111;
}

html[data-theme='dark'] .person-name {
  color: #f5f5f7;
}

.person-original-name {
  margin: 10px 0 0;
  font-size: 1rem;
  font-weight: 600;
  color: rgba(17, 17, 17, 0.66);
}

html[data-theme='dark'] .person-original-name {
  color: rgba(245, 245, 247, 0.7);
}

.person-biography {
  margin: 16px 0 0;
  max-width: 100%;
  font-size: 1.08rem;
  line-height: 1.65;
  color: rgba(17, 17, 17, 0.78);
}

html[data-theme='dark'] .person-biography {
  color: rgba(245, 245, 247, 0.82);
}

.person-meta-grid {
  margin-top: 24px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.person-meta-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 16px 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.38);
  border: 1px solid rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
}

html[data-theme='dark'] .person-meta-item {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.08);
}

.person-meta-label {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: rgba(17, 17, 17, 0.48);
}

html[data-theme='dark'] .person-meta-label {
  color: rgba(245, 245, 247, 0.52);
}

.person-meta-value {
  font-size: 1rem;
  font-weight: 700;
  color: #111111;
}

html[data-theme='dark'] .person-meta-value {
  color: #f5f5f7;
}

.person-sections {
  display: flex;
  flex-direction: column;
  gap: 36px;
}

.person-section {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.person-section-head {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  width: fit-content;
  padding-left: 2px;
  color: inherit;
  text-decoration: none;
}

.person-section-arrow {
  transition: transform 180ms ease;
}

.person-section-head:hover .person-section-arrow {
  transform: translateX(4px);
}

.person-section .content-rail {
  grid-auto-columns: 190px;
  justify-content: start;
}

.person-section-title {
  margin: 0;
  font-size: 2rem;
  line-height: 1.1;
  letter-spacing: -0.04em;
  color: var(--text-primary);
}

.person-section-arrow {
  font-size: 2rem;
  font-weight: 300;
  color: var(--text-muted);
}

.person-movie-link {
  display: block;
  width: 190px;
  text-decoration: none;
  color: inherit;
}

.person-movie-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.person-movie-poster {
  width: 100%;
  aspect-ratio: 2 / 3;
  min-height: 0;
}

.person-movie-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  font-size: 48px;
  font-weight: 700;
  background: var(--poster-fallback);
}

.person-movie-copy {
  padding: 0 2px;
}

.person-movie-title {
  margin: 0;
  font-size: 1rem;
  line-height: 1.35;
  color: var(--text-primary);
}

.person-movie-meta,
.person-movie-role {
  margin: 6px 0 0;
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.person-movie-dot {
  margin: 0 6px;
}

@media (max-width: 960px) {
  .person-hero-content {
    grid-template-columns: 1fr;
    justify-items: center;
    text-align: center;
    padding: 44px 24px 36px;
  }

  .person-copy {
    max-width: 100%;
  }

  .person-meta-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .person-detail-page {
    gap: 22px;
  }

  .person-hero {
    border-radius: 24px;
  }

  .person-avatar-shell {
    width: 148px;
    height: 148px;
  }

  .person-biography {
    font-size: 0.96rem;
  }

  .person-meta-grid {
    grid-template-columns: 1fr;
  }

  .person-section-title,
  .person-section-arrow {
    font-size: 1.6rem;
  }

  .person-movie-poster {
    min-height: 0;
  }

  .person-section .content-rail {
    grid-auto-columns: 148px;
  }

  .person-movie-link {
    width: 148px;
  }
}
</style>
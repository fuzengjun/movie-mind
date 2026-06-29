<template>
  <section class="cast-view-container space-y-4">
    <div>
      <RouterLink 
        :to="`/movies/${id}`" 
        class="back-link inline-flex items-center gap-1.5 text-sm font-semibold text-[var(--text-primary)] hover:opacity-85 no-underline select-none transition"
      >
        <span class="text-base font-bold">&lt;</span>
        <span>返回</span>
      </RouterLink>
    </div>

    <div v-if="movie" class="space-y-6">
      <div class="px-1 select-none">
        <p class="section-kicker text-[10px] font-bold tracking-[0.25em] text-[var(--text-muted)] uppercase">
          Cast & Crew
        </p>
        <h1 class="mt-1 text-2xl font-bold tracking-tight md:text-4xl text-[var(--text-primary)]">
          {{ movie.title }} 演职人员
        </h1>
      </div>

      <div v-if="castAndCrew && castAndCrew.length" class="cast-grid">
        <component
          :is="personLink(person) ? RouterLink : 'div'"
          v-for="person in castAndCrew"
          :key="person.personType + '-' + person.id + '-' + person.name + '-' + person.roleName"
          :to="personLink(person)"
          class="cast-card"
        >
          <div class="avatar-wrapper">
            <img 
              v-if="person.profileUrl" 
              :src="person.profileUrl" 
              :alt="person.name" 
              class="avatar-img"
              loading="lazy" 
            />
            <div v-else class="avatar-placeholder">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-12 h-12 opacity-60">
                <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 6a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0ZM4.501 20.118a7.5 7.5 0 0 1 14.998 0A17.933 17.933 0 0 1 12 21.75c-2.676 0-5.216-.584-7.499-1.632Z" />
              </svg>
            </div>
          </div>
          <div class="cast-name" :title="person.name">
            {{ person.name }}
          </div>
          <div class="cast-role" :title="person.roleName">
            {{ person.roleName }}
          </div>
        </component>
      </div>
      
      <div v-else class="text-center py-12 text-[var(--text-muted)] text-sm">
        暂无演职人员信息
      </div>
    </div>

    <div v-else class="text-center py-20 text-[var(--text-muted)] text-sm">
      正在加载演职人员信息...
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { getMovieDetail } from '@/api/movie'
import { mockMovies } from '@/utils/mockData'

const props = defineProps({
  id: {
    type: [Number, String],
    required: true
  }
})

const movie = ref(null)

function personLink(person) {
  if (!person || !person.id || !person.personType) {
    return ''
  }
  return `/people/${person.personType}/${person.id}`
}

const castAndCrew = computed(() => {
  const list = []
  if (movie.value) {
    if (Array.isArray(movie.value.directors)) {
      movie.value.directors.forEach((d) => {
        if (typeof d === 'string') {
          list.push({
            name: d,
            originalName: '',
            profileUrl: '',
            roleName: '导演'
          })
        } else if (d && typeof d === 'object') {
          list.push({
            id: d.id || null,
            personType: d.personType || 'director',
            name: d.name || '',
            originalName: d.originalName || '',
            profileUrl: d.profileUrl || '',
            roleName: d.roleName || '导演'
          })
        }
      })
    } else if (typeof movie.value.directors === 'string' && movie.value.directors.trim() !== '') {
      const names = movie.value.directors.split(/[,/，、]/)
      names.forEach((name) => {
        const trimmed = name.trim()
        if (trimmed) {
          list.push({
            name: trimmed,
            originalName: '',
            profileUrl: '',
            roleName: '导演'
          })
        }
      })
    }

    if (Array.isArray(movie.value.actors)) {
      movie.value.actors.forEach((a) => {
        if (typeof a === 'string') {
          list.push({
            name: a,
            originalName: '',
            profileUrl: '',
            roleName: '演员'
          })
        } else if (a && typeof a === 'object') {
          list.push({
            id: a.id || null,
            personType: a.personType || 'actor',
            name: a.name || '',
            originalName: a.originalName || '',
            profileUrl: a.profileUrl || '',
            roleName: a.roleName || '演员'
          })
        }
      })
    } else if (typeof movie.value.actors === 'string' && movie.value.actors.trim() !== '') {
      const names = movie.value.actors.split(/[,/，、]/)
      names.forEach((name) => {
        const trimmed = name.trim()
        if (trimmed) {
          list.push({
            name: trimmed,
            originalName: '',
            profileUrl: '',
            roleName: '演员'
          })
        }
      })
    }
  }
  return list
})

onMounted(async () => {
  try {
    const response = await getMovieDetail(props.id)
    movie.value = response.data
  } catch (error) {
    console.warn('API error, falling back to mock movie details in CastView:', error)
    const currentId = Number(props.id)
    movie.value = mockMovies.find((m) => m.id === currentId) || mockMovies[0]
  }
})
</script>

<style scoped>
.cast-view-container {
  padding-top: 40px;
  padding-bottom: 48px;
}

.back-link {
  transition: opacity 200ms ease, color 200ms ease;
}

.cast-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px 12px;
  padding: 4px;
}

@media (min-width: 576px) {
  .cast-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (min-width: 768px) {
  .cast-grid {
    grid-template-columns: repeat(6, 1fr);
  }
}

@media (min-width: 1024px) {
  .cast-grid {
    grid-template-columns: repeat(8, 1fr);
    gap: 32px 12px;
  }
}

.cast-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  text-decoration: none;
  transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  width: 100%;
}

.cast-card:hover {
  transform: translateY(-2px);
}

.avatar-wrapper {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  overflow: hidden;
  margin-bottom: 8px;
  background-color: var(--surface-secondary);
  border: 1px solid var(--border-soft);
  box-shadow: var(--shadow-md);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  position: relative;
}

@media (max-width: 1280px) {
  .avatar-wrapper {
    width: 120px;
    height: 120px;
  }
}
@media (max-width: 992px) {
  .avatar-wrapper {
    width: 100px;
    height: 100px;
  }
}
@media (max-width: 576px) {
  .avatar-wrapper {
    width: 80px;
    height: 80px;
  }
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
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.3;
  margin-top: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cast-role {
  width: 100%;
  font-size: 11px;
  color: var(--text-muted);
  line-height: 1.2;
  margin-top: 3px;
  font-weight: 400;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
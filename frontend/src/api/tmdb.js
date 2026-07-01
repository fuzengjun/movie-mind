import request from '@/utils/request'

export function searchTmdbMovies(query, page = 1) {
  return request({
    url: '/admin/tmdb/search',
    method: 'get',
    params: { query, page }
  })
}

export function importTmdbMovie(tmdbId) {
  return request({
    url: `/admin/tmdb/import/${tmdbId}`,
    method: 'post',
    timeout: 60000
  })
}

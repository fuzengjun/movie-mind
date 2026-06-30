import request from '@/utils/request'

export const pageAdminMovies = (params) => request({ url: '/admin/movies/page', method: 'get', params })
export const getAdminMovie = (id) => request({ url: `/admin/movies/${id}`, method: 'get' })
export const saveAdminMovie = (id, data) => request({ url: id ? `/admin/movies/${id}` : '/admin/movies', method: id ? 'put' : 'post', data })
export const setMovieStatus = (id, status) => request({ url: `/admin/movies/${id}/status`, method: 'put', data: { status } })
export const deleteAdminMovie = (id) => request({ url: `/admin/movies/${id}`, method: 'delete' })
export const batchDeleteMovies = (ids) => request({ url: '/admin/movies/batch', method: 'delete', data: ids })
export const pageAdminUsers = (params) => request({ url: '/admin/users/page', method: 'get', params })
export const getAdminUser = (id) => request({ url: `/admin/users/${id}`, method: 'get' })
export const setUserStatus = (id, status) => request({ url: `/admin/users/${id}/status`, method: 'put', data: { status } })
export const resetUserPassword = (id, password) => request({ url: `/admin/users/${id}/password`, method: 'put', data: { password } })
export const pageComments = (params) => request({ url: '/admin/comments', method: 'get', params })
export const deleteComment = (id) => request({ url: `/admin/comments/${id}`, method: 'delete' })
export const listDictionaries = (type, params) => request({ url: `/admin/${type}`, method: 'get', params })
export const saveDictionary = (type, id, data) => request({ url: id ? `/admin/${type}/${id}` : `/admin/${type}`, method: id ? 'put' : 'post', data })
export const setDictionaryStatus = (type, id, status) => request({ url: `/admin/${type}/${id}/status`, method: 'put', data: { status } })
export const deleteDictionary = (type, id) => request({ url: `/admin/${type}/${id}`, method: 'delete' })
export const pagePeople = (type, params) => request({ url: `/admin/${type}`, method: 'get', params })
export const savePerson = (type, id, data) => request({ url: id ? `/admin/${type}/${id}` : `/admin/${type}`, method: id ? 'put' : 'post', data })
export const deletePerson = (type, id) => request({ url: `/admin/${type}/${id}`, method: 'delete' })
export const pageAnnouncements = (params) => request({ url: '/admin/announcements', method: 'get', params })
export const saveAnnouncement = (id, data) => request({ url: id ? `/admin/announcements/${id}` : '/admin/announcements', method: id ? 'put' : 'post', data })
export const deleteAnnouncement = (id) => request({ url: `/admin/announcements/${id}`, method: 'delete' })
export const importPopularMovies = (limit) => request({
  url: '/admin/tmdb/import/popular',
  method: 'post',
  params: { limit },
  timeout: 60000
})
export const addNewMovies = (limit) => request({
  url: '/admin/tmdb/add',
  method: 'post',
  params: { limit },
  timeout: 120000
})
export const refreshExistingMovies = (limit) => request({
  url: '/admin/tmdb/refresh',
  method: 'post',
  params: { limit },
  timeout: 120000
})


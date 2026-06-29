import request from '@/utils/request'

export function getAdminStatistics(range = 30) {
  return request({
    url: '/admin/statistics',
    method: 'get',
    params: { range }
  })
}

export function getAdminMovies(params) {
  return request({
    url: '/admin/movies',
    method: 'get',
    params
  })
}

export function getAdminUsers(params) {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

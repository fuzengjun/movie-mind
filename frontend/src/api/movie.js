import request from '@/utils/request'

export function getMovieList(params) {
  return request({
    url: '/movies',
    method: 'get',
    params
  })
}

export function getMovieDetail(id) {
  return request({
    url: `/movies/${id}`,
    method: 'get'
  })
}

export function getHotMovies() {
  return request({
    url: '/movies/hot',
    method: 'get'
  })
}

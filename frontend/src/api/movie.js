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

export function getRankings(params) {
    return request({url: '/movies/rankings', method: 'get', params})
}

export function getSimilarMovies(id, params) {
    return request({url: `/movies/${id}/similar`, method: 'get', params})
}

export function getMoviePage(params) {
    return request({url: '/movies/page', method: 'get', params})
}

export function getMovieFilters() {
    return request({url: '/movies/filters', method: 'get'})
}

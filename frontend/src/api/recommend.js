import request from '@/utils/request'

export function getMyRecommend(params) {
  return request({
    url: '/recommend/me',
    method: 'get',
    params
  })
}
import request from '@/utils/request'

export function getMyRecommend() {
  return request({
    url: '/recommend/me',
    method: 'get'
  })
}

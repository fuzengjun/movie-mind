import request from '@/utils/request'

export function getPersonDetail(type, id) {
  return request({
    url: `/people/${type}/${id}`,
    method: 'get'
  })
}
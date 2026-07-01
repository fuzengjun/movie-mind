import request from '@/utils/request'

export function sendChatMessage(data) {
  return request({
    url: '/assistant/chat',
    method: 'post',
    data,
    timeout: 35000
  })
}

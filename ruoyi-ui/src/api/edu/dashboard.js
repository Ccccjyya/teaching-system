import request from '@/utils/request'

export function getStats() {
  return request({
    url: '/edu/dashboard/stats',
    method: 'get'
  })
}
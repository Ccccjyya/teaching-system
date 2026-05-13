import request from '@/utils/request'

export function queryCourseTable(params) {
  return request({
    url: '/edu/student/courseTable',
    method: 'get',
    params: params
  })
}

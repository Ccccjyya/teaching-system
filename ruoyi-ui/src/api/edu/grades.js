import request from '@/utils/request'

export function queryGrades(academicYear, semester) {
  return request({
    url: '/edu/student/grades',
    method: 'get',
    params: { academicYear, semester }
  })
}
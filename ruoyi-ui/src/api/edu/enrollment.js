import request from '@/utils/request'

// 查询选课列表
export function listEnrollment(query) {
  return request({
    url: '/edu/enrollment/list',
    method: 'get',
    params: query
  })
}

// 查询学生已选课程
export function myEnrollments() {
  return request({
    url: '/edu/enrollment/my',
    method: 'get'
  })
}

// 查询选课详细
export function getEnrollment(enrollmentId) {
  return request({
    url: '/edu/enrollment/' + enrollmentId,
    method: 'get'
  })
}

// 学生选课
export function studentEnroll(data) {
  return request({
    url: '/edu/enrollment/studentEnroll',
    method: 'post',
    data: data
  })
}

// 新增选课
export function addEnrollment(data) {
  return request({
    url: '/edu/enrollment',
    method: 'post',
    data: data
  })
}

// 修改选课
export function updateEnrollment(data) {
  return request({
    url: '/edu/enrollment',
    method: 'put',
    data: data
  })
}

// 学生退课
export function studentDrop(enrollmentId) {
  return request({
    url: '/edu/enrollment/studentDrop/' + enrollmentId,
    method: 'delete'
  })
}

// 删除选课
export function delEnrollment(enrollmentId) {
  return request({
    url: '/edu/enrollment/' + enrollmentId,
    method: 'delete'
  })
}

// 查询某门课的学生名单
export function getCourseStudents(offeringId, keyword) {
  return request({
    url: '/edu/enrollment/courseStudents/' + offeringId,
    method: 'get',
    params: { keyword }
  })
}

// 保存单个学生成绩
export function saveScore(data) {
  return request({
    url: '/edu/enrollment/saveScore',
    method: 'put',
    data: data
  })
}

// 批量保存学生成绩
export function saveScores(data) {
  return request({
    url: '/edu/enrollment/saveScores',
    method: 'put',
    data: data
  })
}

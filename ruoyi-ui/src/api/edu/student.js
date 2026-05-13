import request from '@/utils/request'

// 查询学生列表
export function listStudent(query) {
  return request({
    url: '/edu/student/list',
    method: 'get',
    params: query
  })
}

// 查询学生详细
export function getStudent(studentId) {
  return request({
    url: '/edu/student/' + studentId,
    method: 'get'
  })
}

// 新增学生
export function addStudent(data) {
  return request({
    url: '/edu/student',
    method: 'post',
    data: data
  })
}

// 修改学生
export function updateStudent(data) {
  return request({
    url: '/edu/student',
    method: 'put',
    data: data
  })
}

// 删除学生
export function delStudent(studentIds) {
  return request({
    url: '/edu/student/' + studentIds,
    method: 'delete'
  })
}

// 重置密码
export function resetPwd(studentId) {
  return request({
    url: '/edu/student/resetPwd/' + studentId,
    method: 'put'
  })
}

// 更改状态
export function changeStatus(studentId, status) {
  return request({
    url: '/edu/student/changeStatus/' + studentId,
    method: 'put',
    params: { status: status }
  })
}

// 获取用户信息
export function getUserInfo(studentId) {
  return request({
    url: '/edu/student/getUserInfo/' + studentId,
    method: 'get'
  })
}

// 获取学生统计数据
export function getStudentStats() {
  return request({
    url: '/edu/student/stats',
    method: 'get'
  })
}

import request from '@/utils/request'

// 查询教师列表
export function listTeacher(query) {
  return request({
    url: '/edu/teacher/list',
    method: 'get',
    params: query
  })
}

// 查询教师详细
export function getTeacher(teacherId) {
  return request({
    url: '/edu/teacher/' + teacherId,
    method: 'get'
  })
}

// 新增教师
export function addTeacher(data) {
  return request({
    url: '/edu/teacher',
    method: 'post',
    data: data
  })
}

// 修改教师
export function updateTeacher(data) {
  return request({
    url: '/edu/teacher',
    method: 'put',
    data: data
  })
}

// 删除教师
export function delTeacher(teacherIds) {
  return request({
    url: '/edu/teacher/' + teacherIds,
    method: 'delete'
  })
}

// 重置教师密码
export function resetTeacherPwd(teacherId) {
  return request({
    url: '/edu/teacher/resetPwd/' + teacherId,
    method: 'put'
  })
}

// 更改教师状态
export function changeTeacherStatus(teacherId, status) {
  return request({
    url: '/edu/teacher/changeStatus/' + teacherId,
    method: 'put',
    params: { status: status }
  })
}

// 教师首页统计数据
export function getTeacherStats() {
  return request({
    url: '/edu/teacher/stats',
    method: 'get'
  })
}

// 获取教师授课列表
export function getTeacherCourses(queryParams) {
  return request({
    url: '/edu/teacher/courses',
    method: 'get',
    params: queryParams
  })
}

// 获取教师开课申请列表
export function getTeacherApplyList(queryParams) {
  return request({
    url: '/edu/teacher/apply/list',
    method: 'get',
    params: queryParams
  })
}

// 提交开课申请
export function submitTeacherApply(data) {
  return request({
    url: '/edu/teacher/apply',
    method: 'post',
    data: data
  })
}

// 获取申请详情
export function getTeacherApplyInfo(id) {
  return request({
    url: '/edu/teacher/apply/' + id,
    method: 'get'
  })
}

// 导出开课申请
export function exportTeacherApply(queryParams) {
  return request({
    url: '/edu/teacher/apply/export',
    method: 'post',
    params: queryParams,
    responseType: 'blob'
  })
}

// 获取教师端可用学期列表
export function getTeacherSemesters(queryParams) {
  return request({
    url: '/edu/teacher/semesters',
    method: 'get',
    params: queryParams
  })
}

// 获取教师端可用院系列表
export function getTeacherDepartments(queryParams) {
  return request({
    url: '/edu/teacher/departments',
    method: 'get',
    params: queryParams
  })
}

// 获取教师端可用课程列表
export function getTeacherCourseCatalog(queryParams) {
  return request({
    url: '/edu/teacher/courses/catalog',
    method: 'get',
    params: queryParams
  })
}

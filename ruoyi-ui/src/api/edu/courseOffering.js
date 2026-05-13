import request from '@/utils/request'

// 查询开课列表（管理端用，不限制学期）
export function adminListCourseOffering(query) {
  return request({
    url: '/edu/offering/admin/list',
    method: 'get',
    params: query
  })
}

// 查询开课列表（学生选课用）
export function listCourseOffering(query) {
  return request({
    url: '/edu/offering/list',
    method: 'get',
    params: query
  })
}

// 查询开课详细
export function getCourseOffering(offeringId) {
  return request({
    url: '/edu/offering/' + offeringId,
    method: 'get'
  })
}

// 新增开课
export function addCourseOffering(data) {
  return request({
    url: '/edu/offering',
    method: 'post',
    data: data
  })
}

// 修改开课
export function updateCourseOffering(data) {
  return request({
    url: '/edu/offering',
    method: 'put',
    data: data
  })
}

// 删除开课
export function delCourseOffering(offeringId) {
  return request({
    url: '/edu/offering/' + offeringId,
    method: 'delete'
  })
}

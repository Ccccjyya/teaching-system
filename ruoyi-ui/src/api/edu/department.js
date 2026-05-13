import request from '@/utils/request'

export function listDepartment(query) {
  return request({
    url: '/edu/department/list',
    method: 'get',
    params: query
  })
}

export function getDepartment(deptId) {
  return request({
    url: '/edu/department/' + deptId,
    method: 'get'
  })
}

export function addDepartment(data) {
  return request({
    url: '/edu/department',
    method: 'post',
    data: data
  })
}

export function updateDepartment(data) {
  return request({
    url: '/edu/department',
    method: 'put',
    data: data
  })
}

export function delDepartment(deptId) {
  return request({
    url: '/edu/department/' + deptId,
    method: 'delete'
  })
}
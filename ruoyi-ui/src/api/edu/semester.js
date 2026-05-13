import request from '@/utils/request'

export function getCurrentSemester() {
  return request({
    url: '/edu/semester/current',
    method: 'get'
  })
}

export function listSemester(query) {
  return request({
    url: '/edu/semester/list',
    method: 'get',
    params: query
  })
}

export function getSemester(id) {
  return request({
    url: '/edu/semester/' + id,
    method: 'get'
  })
}

export function addSemester(data) {
  return request({
    url: '/edu/semester',
    method: 'post',
    data: data
  })
}

export function updateSemester(data) {
  return request({
    url: '/edu/semester',
    method: 'put',
    data: data
  })
}

export function delSemester(ids) {
  return request({
    url: '/edu/semester/' + ids,
    method: 'delete'
  })
}

export function switchSemester(data) {
  return request({
    url: '/edu/semester/switch',
    method: 'post',
    data: data
  })
}
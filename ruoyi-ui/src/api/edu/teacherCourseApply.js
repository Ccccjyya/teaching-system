import request from '@/utils/request'

export function listApply(query) {
  return request({
    url: '/edu/apply/list',
    method: 'get',
    params: query
  })
}

export function getApply(id) {
  return request({
    url: '/edu/apply/' + id,
    method: 'get'
  })
}

export function addApply(data) {
  return request({
    url: '/edu/apply',
    method: 'post',
    data: data
  })
}

export function updateApply(data) {
  return request({
    url: '/edu/apply',
    method: 'put',
    data: data
  })
}

export function delApply(ids) {
  return request({
    url: '/edu/apply/' + ids,
    method: 'delete'
  })
}

export function applyCommit(data) {
  return request({
    url: '/edu/apply/commit',
    method: 'post',
    data: data
  })
}

export function applyRefuse(data) {
  return request({
    url: '/edu/apply/refuse',
    method: 'post',
    data: data
  })
}
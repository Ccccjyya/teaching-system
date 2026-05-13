import request from '@/utils/request'

// 学期管理
export function getCurrentSemester() {
  return request({
    url: '/edu/global/semester/current',
    method: 'get'
  })
}

export function listSemester(query) {
  return request({
    url: '/edu/global/semester/list',
    method: 'get',
    params: query
  })
}

export function getSemester(id) {
  return request({
    url: '/edu/global/semester/' + id,
    method: 'get'
  })
}

export function addSemester(data) {
  return request({
    url: '/edu/global/semester',
    method: 'post',
    data: data
  })
}

export function updateSemester(data) {
  return request({
    url: '/edu/global/semester',
    method: 'put',
    data: data
  })
}

export function delSemester(ids) {
  return request({
    url: '/edu/global/semester/' + ids,
    method: 'delete'
  })
}

export function setCurrentSemester(id) {
  return request({
    url: '/edu/global/semester/setCurrent/' + id,
    method: 'post'
  })
}

// 全局设置
export function getGlobalSettings() {
  return request({
    url: '/edu/global/settings',
    method: 'get'
  })
}

export function setSelectOpen(open) {
  return request({
    url: '/edu/global/settings/selectOpen',
    method: 'put',
    params: { open: open }
  })
}

export function setScoreEntryOpen(open) {
  return request({
    url: '/edu/global/settings/scoreEntryOpen',
    method: 'put',
    params: { open: open }
  })
}

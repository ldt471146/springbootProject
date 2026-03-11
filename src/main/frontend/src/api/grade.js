import request from '@/utils/request'

export function createGrade(data) {
  return request({
    url: '/api/grade',
    method: 'post',
    data
  })
}

export function updateGrade(id, data) {
  return request({
    url: `/api/grade/${id}`,
    method: 'put',
    data
  })
}

export function fetchGrade(id) {
  return request({
    url: `/api/grade/${id}`,
    method: 'get'
  })
}

export function listMyGrades(params) {
  return request({
    url: '/api/grade/my',
    method: 'get',
    params
  })
}

export function listTeacherGrades(params) {
  return request({
    url: '/api/grade/teacher/list',
    method: 'get',
    params
  })
}

export function listAdminGrades(params) {
  return request({
    url: '/api/grade/admin/list',
    method: 'get',
    params
  })
}

export function confirmGrade(id, data) {
  return request({
    url: `/api/grade/${id}/confirm`,
    method: 'put',
    data
  })
}

import request from '@/utils/request'

export function createProject(data) {
  return request({
    url: '/api/project',
    method: 'post',
    data
  })
}

export function updateProject(id, data) {
  return request({
    url: `/api/project/${id}`,
    method: 'put',
    data
  })
}

export function deleteProject(id) {
  return request({
    url: `/api/project/${id}`,
    method: 'delete'
  })
}

export function fetchProject(id) {
  return request({
    url: `/api/project/${id}`,
    method: 'get'
  })
}

export function listProjects(params) {
  return request({
    url: '/api/project/list',
    method: 'get',
    params
  })
}

export function listMyProjects(params) {
  return request({
    url: '/api/project/my',
    method: 'get',
    params
  })
}

export function closeProject(id) {
  return request({
    url: `/api/project/${id}/close`,
    method: 'put'
  })
}

export function archiveProject(id) {
  return request({
    url: `/api/project/${id}/archive`,
    method: 'put'
  })
}

export function listParticipants(id, params) {
  return request({
    url: `/api/project/${id}/participants`,
    method: 'get',
    params
  })
}

import request from '@/utils/request'

export function createApplication(data) {
  return request({
    url: '/api/application',
    method: 'post',
    data
  })
}

export function listMyApplications(params) {
  return request({
    url: '/api/application/my',
    method: 'get',
    params
  })
}

export function fetchApplication(id) {
  return request({
    url: `/api/application/${id}`,
    method: 'get'
  })
}

export function withdrawApplication(id) {
  return request({
    url: `/api/application/${id}`,
    method: 'delete'
  })
}

export function listProjectApplications(projectId, params) {
  return request({
    url: `/api/application/project/${projectId}`,
    method: 'get',
    params
  })
}

export function reviewApplication(id, data) {
  return request({
    url: `/api/application/${id}/review`,
    method: 'put',
    data
  })
}

export function updateApplicationPhase(id, data) {
  return request({
    url: `/api/application/${id}/phase`,
    method: 'put',
    data
  })
}

export function listTeacherStudents(params) {
  return request({
    url: '/api/application/teacher/students',
    method: 'get',
    params
  })
}

import request from '@/utils/request'

export function exportGrades(params) {
  return request({
    url: '/api/export/grades',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function exportGradesPdf(params) {
  return request({
    url: '/api/export/grades/pdf',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function exportProjectParticipants(id) {
  return request({
    url: `/api/export/project/${id}/participants`,
    method: 'get',
    responseType: 'blob'
  })
}

export function exportProjectParticipantsPdf(id) {
  return request({
    url: `/api/export/project/${id}/participants/pdf`,
    method: 'get',
    responseType: 'blob'
  })
}

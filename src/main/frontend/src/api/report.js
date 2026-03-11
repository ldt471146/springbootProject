import request from '@/utils/request'

export function createReport(data) {
  return request({
    url: '/api/report',
    method: 'post',
    data
  })
}

export function updateReport(id, data) {
  return request({
    url: `/api/report/${id}`,
    method: 'put',
    data
  })
}

export function fetchReport(id) {
  return request({
    url: `/api/report/${id}`,
    method: 'get'
  })
}

export function listMyReports(params) {
  return request({
    url: '/api/report/my',
    method: 'get',
    params
  })
}

export function listTeacherReports(params) {
  return request({
    url: '/api/report/teacher/list',
    method: 'get',
    params
  })
}

export function reviewReport(id, data) {
  return request({
    url: `/api/report/${id}/review`,
    method: 'put',
    data
  })
}

export function uploadReportFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/api/report/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

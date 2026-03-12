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

export function uploadReportAttachment(id, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: `/api/report/${id}/attachments`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function deleteReportAttachment(attachmentId) {
  return request({
    url: `/api/report/attachments/${attachmentId}`,
    method: 'delete'
  })
}

export function fetchReportFile(id) {
  return request({
    url: `/api/report/file/${id}`,
    method: 'get',
    responseType: 'blob'
  })
}

export function fetchReportAttachmentFile(attachmentId) {
  return request({
    url: `/api/report/attachments/${attachmentId}/file`,
    method: 'get',
    responseType: 'blob'
  })
}

export function fetchReportFilePreview(id) {
  return request({
    url: `/api/report/file/${id}/preview`,
    method: 'get'
  })
}

export function fetchReportAttachmentPreview(attachmentId) {
  return request({
    url: `/api/report/attachments/${attachmentId}/preview`,
    method: 'get'
  })
}

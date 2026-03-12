import request from '@/utils/request'

export function createJournal(data) {
  return request({
    url: '/api/journal',
    method: 'post',
    data
  })
}

export function updateJournal(id, data) {
  return request({
    url: `/api/journal/${id}`,
    method: 'put',
    data
  })
}

export function deleteJournal(id) {
  return request({
    url: `/api/journal/${id}`,
    method: 'delete'
  })
}

export function fetchJournal(id) {
  return request({
    url: `/api/journal/${id}`,
    method: 'get'
  })
}

export function listMyJournals(params) {
  return request({
    url: '/api/journal/my',
    method: 'get',
    params
  })
}

export function listApplicationJournals(appId, params) {
  return request({
    url: `/api/journal/application/${appId}`,
    method: 'get',
    params
  })
}

export function listJournalTimeline(appId) {
  return request({
    url: `/api/journal/timeline/${appId}`,
    method: 'get'
  })
}

export function reviewJournal(id, data) {
  return request({
    url: `/api/journal/${id}/review`,
    method: 'put',
    data
  })
}

export function uploadJournalAttachment(id, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: `/api/journal/${id}/attachments`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function deleteJournalAttachment(attachmentId) {
  return request({
    url: `/api/journal/attachments/${attachmentId}`,
    method: 'delete'
  })
}

export function fetchJournalAttachmentFile(attachmentId) {
  return request({
    url: `/api/journal/attachments/${attachmentId}/file`,
    method: 'get',
    responseType: 'blob'
  })
}

export function fetchJournalAttachmentPreview(attachmentId) {
  return request({
    url: `/api/journal/attachments/${attachmentId}/preview`,
    method: 'get'
  })
}

import request from '@/utils/request'

export function fetchReminderSummary() {
  return request({
    url: '/api/reminder/me',
    method: 'get'
  })
}

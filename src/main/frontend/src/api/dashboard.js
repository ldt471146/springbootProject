import request from '@/utils/request'

export function fetchDashboardSummary() {
  return request({
    url: '/api/dashboard/summary',
    method: 'get'
  })
}

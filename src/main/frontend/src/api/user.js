import request from '@/utils/request'

export function updateProfile(data) {
  return request({
    url: '/api/user/profile',
    method: 'put',
    data
  })
}

export function updatePassword(data) {
  return request({
    url: '/api/user/password',
    method: 'put',
    data
  })
}

export function listPendingUsers(params) {
  return request({
    url: '/api/user/pending',
    method: 'get',
    params
  })
}

export function approveUser(id, data) {
  return request({
    url: `/api/user/${id}/approve`,
    method: 'put',
    data
  })
}

export function listUsers(params) {
  return request({
    url: '/api/user/list',
    method: 'get',
    params
  })
}

export function updateUserStatus(id, data) {
  return request({
    url: `/api/user/${id}/status`,
    method: 'put',
    data
  })
}

export function resetPassword(id, data) {
  return request({
    url: `/api/user/password/reset/${id}`,
    method: 'put',
    data
  })
}

export function deleteUser(id) {
  return request({
    url: `/api/user/${id}`,
    method: 'delete'
  })
}

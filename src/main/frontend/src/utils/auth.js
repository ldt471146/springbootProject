const TOKEN_KEY = 'internship_token'
const USER_KEY = 'internship_user'

function getStorage(remember) {
  return remember ? localStorage : sessionStorage
}

function getAuthStorage() {
  if (localStorage.getItem(TOKEN_KEY)) {
    return localStorage
  }
  if (sessionStorage.getItem(TOKEN_KEY)) {
    return sessionStorage
  }
  return localStorage
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token, remember = true) {
  clearToken()
  getStorage(remember).setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(TOKEN_KEY)
}

export function getUserCache() {
  const raw = localStorage.getItem(USER_KEY) || sessionStorage.getItem(USER_KEY)
  return raw ? JSON.parse(raw) : null
}

export function setUserCache(user, remember) {
  clearUserCache()
  const storage = typeof remember === 'boolean' ? getStorage(remember) : getAuthStorage()
  storage.setItem(USER_KEY, JSON.stringify(user))
}

export function clearUserCache() {
  localStorage.removeItem(USER_KEY)
  sessionStorage.removeItem(USER_KEY)
}

export function clearAuth() {
  clearToken()
  clearUserCache()
}

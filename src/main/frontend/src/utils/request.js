import axios from 'axios'
import { ElMessage } from 'element-plus'
import { clearAuth, getToken } from './auth'

const request = axios.create({
  baseURL: '',
  timeout: 15000
})

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    if (response.config.responseType === 'blob') {
      return response
    }
    const payload = response.data
    if (payload.code !== 200) {
      if (payload.code === 401) {
        clearAuth()
        window.location.hash = '#/login'
      }
      ElMessage.error(payload.message || '请求失败')
      return Promise.reject(new Error(payload.message || '请求失败'))
    }
    return payload.data
  },
  async (error) => {
    let message = error.message || '网络异常'
    const responseData = error.response?.data
    if (responseData instanceof Blob && responseData.type?.includes('application/json')) {
      try {
        const payload = JSON.parse(await responseData.text())
        message = payload.message || message
      } catch {
        message = error.message || '网络异常'
      }
    } else {
      message = responseData?.message || message
    }
    if (error.response?.status === 401) {
      clearAuth()
      window.location.hash = '#/login'
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request

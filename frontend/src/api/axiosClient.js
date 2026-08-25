import axios from 'axios'

const axiosClient = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

// attach JWT token to every request
axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('gu_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// on 401, clear session and redirect to login
axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('gu_token')
      localStorage.removeItem('gu_user')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default axiosClient

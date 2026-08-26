import axiosClient from './axiosClient'

export const committeeApi = {
  search: (params) => axiosClient.get('/committees', { params }).then((res) => res.data),
  getById: (id) => axiosClient.get(`/committees/${id}`).then((res) => res.data),
  create: (payload) => axiosClient.post('/committees', payload).then((res) => res.data),
  update: (id, payload) => axiosClient.put(`/committees/${id}`, payload).then((res) => res.data),
  regenerateCode: (id) => axiosClient.post(`/committees/${id}/regenerate-code`).then((res) => res.data),
  lock: (id) => axiosClient.put(`/committees/${id}/lock`).then((res) => res.data),
  unlock: (id) => axiosClient.put(`/committees/${id}/unlock`).then((res) => res.data),
}

export const developerDashboardApi = {
  getOverview: () => axiosClient.get('/developer/dashboard/overview').then((res) => res.data),
}

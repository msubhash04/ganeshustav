import axiosClient from './axiosClient'

export const donationApi = {
  getAll: () => axiosClient.get('/donations').then((res) => res.data),

  search: (filters) =>
    axiosClient.get('/donations', { params: filters }).then((res) => res.data),

  getById: (id) => axiosClient.get(`/donations/${id}`).then((res) => res.data),

  create: (payload) => axiosClient.post('/donations', payload).then((res) => res.data),

  update: (id, payload) => axiosClient.put(`/donations/${id}`, payload).then((res) => res.data),

  remove: (id) => axiosClient.delete(`/donations/${id}`),

  getTotal: () => axiosClient.get('/donations/total').then((res) => res.data),
}

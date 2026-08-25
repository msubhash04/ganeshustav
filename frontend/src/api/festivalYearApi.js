import axiosClient from './axiosClient'

export const festivalYearApi = {
  getAll: () => axiosClient.get('/festival-years').then((res) => res.data),
  getActive: () => axiosClient.get('/festival-years/active').then((res) => res.data),
  getById: (id) => axiosClient.get(`/festival-years/${id}`).then((res) => res.data),
  create: (payload) => axiosClient.post('/festival-years', payload).then((res) => res.data),
  update: (id, payload) => axiosClient.put(`/festival-years/${id}`, payload).then((res) => res.data),
  activate: (id) => axiosClient.put(`/festival-years/${id}/activate`).then((res) => res.data),
}

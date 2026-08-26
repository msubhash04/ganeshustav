import axiosClient from './axiosClient'

export const sponsorshipCategoryApi = {
  getAll: () => axiosClient.get('/sponsorship-categories').then((res) => res.data),
  getActive: () => axiosClient.get('/sponsorship-categories/active').then((res) => res.data),
  create: (payload) => axiosClient.post('/sponsorship-categories', payload).then((res) => res.data),
  update: (id, payload) => axiosClient.put(`/sponsorship-categories/${id}`, payload).then((res) => res.data),
  remove: (id) => axiosClient.delete(`/sponsorship-categories/${id}`),
}

export const generalSponsorApi = {
  getAll: () => axiosClient.get('/general-sponsors').then((res) => res.data),
  getByFestivalYear: (festivalYearId) =>
    axiosClient.get(`/general-sponsors/festival-year/${festivalYearId}`).then((res) => res.data),
  create: (payload) => axiosClient.post('/general-sponsors', payload).then((res) => res.data),
  update: (id, payload) => axiosClient.put(`/general-sponsors/${id}`, payload).then((res) => res.data),
  remove: (id) => axiosClient.delete(`/general-sponsors/${id}`),
}

export const annadanamSponsorApi = {
  getByFestivalYear: (festivalYearId) =>
    axiosClient.get(`/annadanam-sponsors/festival-year/${festivalYearId}`).then((res) => res.data),
  create: (payload) => axiosClient.post('/annadanam-sponsors', payload).then((res) => res.data),
  update: (id, payload) => axiosClient.put(`/annadanam-sponsors/${id}`, payload).then((res) => res.data),
  remove: (id) => axiosClient.delete(`/annadanam-sponsors/${id}`),
}

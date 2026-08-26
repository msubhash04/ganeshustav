import axiosClient from './axiosClient'

export const auctionApi = {
  getByFestivalYear: (festivalYearId) =>
    axiosClient.get(`/auction-items/festival-year/${festivalYearId}`).then((res) => res.data),

  getTotal: (festivalYearId) =>
    axiosClient.get(`/auction-items/festival-year/${festivalYearId}/total`).then((res) => res.data),

  create: (festivalYearId, payload) =>
    axiosClient.post(`/auction-items/festival-year/${festivalYearId}`, payload).then((res) => res.data),

  update: (id, payload) => axiosClient.put(`/auction-items/${id}`, payload).then((res) => res.data),

  remove: (id) => axiosClient.delete(`/auction-items/${id}`),
}

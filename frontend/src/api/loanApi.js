import axiosClient from './axiosClient'

export const loanApi = {
  getAll: () => axiosClient.get('/loans').then((res) => res.data),
  getById: (id) => axiosClient.get(`/loans/${id}`).then((res) => res.data),
  getOutstandingTotal: () => axiosClient.get('/loans/outstanding-total').then((res) => res.data),
  create: (payload) => axiosClient.post('/loans', payload).then((res) => res.data),
  recordRepayment: (loanId, payload) =>
    axiosClient.post(`/loans/${loanId}/repayments`, payload).then((res) => res.data),git 
}

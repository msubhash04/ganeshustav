import axiosClient from './axiosClient'

export const dashboardApi = {
  getSummary: () => axiosClient.get('/dashboard/summary').then((res) => res.data),
}

export const reportApi = {
  downloadPdf: (startDate, endDate) =>
    axiosClient.get('/reports/pdf', {
      params: { startDate, endDate },
      responseType: 'blob',
    }),

  downloadExcel: (startDate, endDate) =>
    axiosClient.get('/reports/excel', {
      params: { startDate, endDate },
      responseType: 'blob',
    }),
}

export const publicApi = {
  getTransparency: () => axiosClient.get('/public/transparency').then((res) => res.data),
}

export const memberApi = {
  getAll: () => axiosClient.get('/members').then((res) => res.data),
  create: (payload) => axiosClient.post('/members', payload).then((res) => res.data),
  deactivate: (id) => axiosClient.put(`/members/${id}/deactivate`).then((res) => res.data),
  remove: (id) => axiosClient.delete(`/members/${id}`),
}

// helper to trigger a browser download from a blob response
export function downloadBlob(blobResponse, filename) {
  const url = window.URL.createObjectURL(new Blob([blobResponse.data]))
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', filename)
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}

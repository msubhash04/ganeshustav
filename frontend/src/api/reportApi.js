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

  // Festival Archives - full read-only audit report for one festival
  // year (active or archived): financial summary, category breakdown,
  // and the complete ledger. Available to every committee role.
  getFestivalAudit: (festivalYearId) =>
    axiosClient.get(`/reports/festival-audit/${festivalYearId}`).then((res) => res.data),
}

export const publicApi = {
  getTransparency: (tenantCode) => axiosClient.get(`/public/transparency/${tenantCode}`).then((res) => res.data),
  // New, festival-year-scoped endpoints for the landing page's Public
  // Committee Viewer and Read-Only Observation Dashboard - separate
  // from getTransparency above (all-time totals), left untouched.
  observeActive: (tenantCode) => axiosClient.get(`/public/observe/${tenantCode}`).then((res) => res.data),
  getYearOptions: (tenantCode) => axiosClient.get(`/public/committees/${tenantCode}/years`).then((res) => res.data),
  getYearSummary: (tenantCode, festivalYearId) =>
    axiosClient.get(`/public/committees/${tenantCode}/years/${festivalYearId}/summary`).then((res) => res.data),
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

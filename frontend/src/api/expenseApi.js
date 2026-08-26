import axiosClient from './axiosClient'

export const expenseApi = {
  getAll: () => axiosClient.get('/expenses').then((res) => res.data),

  search: (filters) =>
    axiosClient.get('/expenses', { params: filters }).then((res) => res.data),

  getById: (id) => axiosClient.get(`/expenses/${id}`).then((res) => res.data),

  create: (expenseDto, billFile) => {
    const form = new FormData()
    form.append('expense', new Blob([JSON.stringify(expenseDto)], { type: 'application/json' }))
    if (billFile) form.append('billFile', billFile)
    return axiosClient
      .post('/expenses', form, { headers: { 'Content-Type': 'multipart/form-data' } })
      .then((res) => res.data)
  },

  update: (id, expenseDto, billFile) => {
    const form = new FormData()
    form.append('expense', new Blob([JSON.stringify(expenseDto)], { type: 'application/json' }))
    if (billFile) form.append('billFile', billFile)
    return axiosClient
      .put(`/expenses/${id}`, form, { headers: { 'Content-Type': 'multipart/form-data' } })
      .then((res) => res.data)
  },

  remove: (id) => axiosClient.delete(`/expenses/${id}`),

  getTotal: () => axiosClient.get('/expenses/total').then((res) => res.data),

  getCategorySummary: () => axiosClient.get('/expenses/category-summary').then((res) => res.data),
}

export const EXPENSE_CATEGORIES = [
  { value: 'IDOL_MURTI', label: 'Idol (Murti)' },
  { value: 'PANDAL_DECORATION', label: 'Pandal Decoration' },
  { value: 'ELECTRICITY_LIGHTING', label: 'Electricity/Lighting' },
  { value: 'SOUND_SYSTEM', label: 'Sound System' },
  { value: 'PRIEST_POOJA_MATERIALS', label: 'Priest/Pooja Materials' },
  { value: 'FOOD_PRASAD', label: 'Food & Prasad' },
  { value: 'IMMERSION_VISARJAN', label: 'Immersion (Visarjan)' },
  { value: 'SECURITY', label: 'Security' },
  { value: 'CULTURAL_PROGRAMS', label: 'Cultural Programs' },
  { value: 'MISCELLANEOUS', label: 'Miscellaneous' },
]

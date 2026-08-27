import axiosClient from './axiosClient'

// Note: public self-registration has been removed for security reasons -
// staff accounts are created by an authenticated President via the
// Committee Members page (see memberApi.create in reportApi.js) instead.
export const authApi = {
  login: (username, password) =>
    axiosClient.post('/auth/login', { username, password }).then((res) => res.data),

  changePassword: (currentPassword, newPassword) =>
    axiosClient.post('/auth/change-password', { currentPassword, newPassword }).then((res) => res.data),
}

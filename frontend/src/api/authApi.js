import axiosClient from './axiosClient'

export const authApi = {
  login: (username, password) =>
    axiosClient.post('/auth/login', { username, password }).then((res) => res.data),

  register: (payload) =>
    axiosClient.post('/auth/register', payload).then((res) => res.data),
}

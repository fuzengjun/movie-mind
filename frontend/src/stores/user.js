import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    profile: null
  }),
  actions: {
    setToken(token) {
      this.token = token
    },
    setProfile(profile) {
      this.profile = profile
    },
    logout() {
      this.token = ''
      this.profile = null
    }
  }
})

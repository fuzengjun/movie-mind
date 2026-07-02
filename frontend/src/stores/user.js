import {defineStore} from 'pinia'

const saved = () => {
    try {
        return JSON.parse(localStorage.getItem('movie-mind-user') || '{}')
    } catch {
        return {}
    }
}
export const useUserStore = defineStore('user', {
    state: () => ({
        token: saved().token || '',
        profile: saved().profile || null
    }), actions: {
        persist() {
            localStorage.setItem('movie-mind-user', JSON.stringify({token: this.token, profile: this.profile}))
        }, setToken(token) {
            this.token = token;
            this.persist()
        }, setProfile(profile) {
            this.profile = profile;
            this.persist()
        }, logout() {
            this.token = '';
            this.profile = null;
            localStorage.removeItem('movie-mind-user')
        }
    }
})

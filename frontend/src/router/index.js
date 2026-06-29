import { createRouter, createWebHistory } from 'vue-router'
import FrontLayout from '@/layout/FrontLayout.vue'
import AdminLayout from '@/layout/AdminLayout.vue'
import HomeView from '@/views/front/HomeView.vue'
import MovieListView from '@/views/front/MovieListView.vue'
import MovieDetailView from '@/views/front/MovieDetailView.vue'
import MovieCastView from '@/views/front/MovieCastView.vue'
import LoginView from '@/views/front/LoginView.vue'
import DashboardView from '@/views/admin/DashboardView.vue'
import MovieManageView from '@/views/admin/MovieManageView.vue'
import UserManageView from '@/views/admin/UserManageView.vue'

const routes = [
  {
    path: '/',
    component: FrontLayout,
    children: [
      { path: '', name: 'home', component: HomeView },
      { path: 'movies', name: 'movies', component: MovieListView },
      { path: 'movies/:id', name: 'movie-detail', component: MovieDetailView, props: true },
      { path: 'movies/:id/cast', name: 'movie-cast', component: MovieCastView, props: true },
      { path: 'login', name: 'login', component: LoginView }
    ]
  },
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      { path: '', name: 'admin-dashboard', component: DashboardView },
      { path: 'movies', name: 'admin-movies', component: MovieManageView },
      { path: 'users', name: 'admin-users', component: UserManageView }
    ]
  }
]

export default createRouter({
  history: createWebHistory(),
  routes
})

import { createRouter, createWebHistory } from 'vue-router'
import FrontLayout from '@/layout/FrontLayout.vue'
import AdminLayout from '@/layout/AdminLayout.vue'
import HomeView from '@/views/front/HomeView.vue'
import MovieListView from '@/views/front/MovieListView.vue'
import RankingView from '@/views/front/RankingView.vue'
import MovieDetailView from '@/views/front/MovieDetailView.vue'
import MovieCastView from '@/views/front/MovieCastView.vue'
import PersonDetailView from '@/views/front/PersonDetailView.vue'
import PersonWorksView from '@/views/front/PersonWorksView.vue'
import LoginView from '@/views/front/LoginView.vue'
import RegisterView from '@/views/front/RegisterView.vue'
import RecommendationView from '@/views/front/RecommendationView.vue'
import ProfileView from '@/views/front/ProfileView.vue'
import DashboardView from '@/views/admin/DashboardView.vue'
import MovieManagementView from '@/views/admin/MovieManagementView.vue'
import UserManagementView from '@/views/admin/UserManagementView.vue'
import CommentManageView from '@/views/admin/CommentManageView.vue'
import ContentManageView from '@/views/admin/ContentManageView.vue'
import TmdbManageView from '@/views/admin/TmdbManageView.vue'
import { useUserStore } from '@/stores/user'

const routes=[{path:'/',component:FrontLayout,children:[{path:'',name:'home',component:HomeView},{path:'movies',name:'movies',component:MovieListView},{path:'rankings',name:'rankings',component:RankingView},{path:'movies/:id',name:'movie-detail',component:MovieDetailView,props:true},{path:'movies/:id/cast',name:'movie-cast',component:MovieCastView,props:true},{path:'people/:type/:id',name:'person-detail',component:PersonDetailView,props:true},{path:'people/:type/:id/works/:sectionKey',name:'person-works',component:PersonWorksView,props:true},{path:'login',name:'login',component:LoginView},{path:'register',name:'register',component:RegisterView},{path:'recommendations',name:'recommendations',component:RecommendationView,meta:{requiresAuth:true}},{path:'profile',name:'profile',component:ProfileView,meta:{requiresAuth:true}}]},{path:'/admin',component:AdminLayout,meta:{requiresAdmin:true},children:[{path:'',name:'admin-dashboard',component:DashboardView},{path:'movies',name:'admin-movies',component:MovieManagementView},{path:'users',name:'admin-users',component:UserManagementView},{path:'comments',name:'admin-comments',component:CommentManageView},{path:'content',name:'admin-content',component:ContentManageView},{path:'tmdb',name:'admin-tmdb',component:TmdbManageView}]}]
if ('scrollRestoration' in window.history) window.history.scrollRestoration = 'manual'

function restoreWhenReady(position) {
  return new Promise((resolve) => {
    let attempts = 0
    const check = () => {
      const top = Number(position?.top || 0)
      const maxTop = Math.max(0, document.documentElement.scrollHeight - window.innerHeight)
      if (maxTop >= top || attempts >= 60) {
        resolve({ top: Math.min(top, maxTop), left: Number(position?.left || 0), behavior: 'auto' })
        return
      }
      attempts += 1
      window.setTimeout(check, 50)
    }
    window.requestAnimationFrame(check)
  })
}

const router=createRouter({
  history:createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    return savedPosition ? restoreWhenReady(savedPosition) : { top: 0, left: 0 }
  }
})
router.beforeEach((to) => {
  const store = useUserStore()
  const requiresAdmin = to.matched.some((record) => record.meta.requiresAdmin)
  const requiresAuth = requiresAdmin || to.matched.some((record) => record.meta.requiresAuth)
  if (requiresAuth && !store.token) return { path: '/login', query: { redirect: to.fullPath } }
  if (requiresAdmin && store.profile?.role !== 'ADMIN') return { path: '/' }
})
export default router

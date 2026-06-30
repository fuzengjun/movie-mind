import { createRouter, createWebHistory } from 'vue-router'
import FrontLayout from '@/layout/FrontLayout.vue'
import AdminLayout from '@/layout/AdminLayout.vue'
import HomeView from '@/views/front/HomeView.vue'
import MovieListView from '@/views/front/MovieListView.vue'
import MovieDetailView from '@/views/front/MovieDetailView.vue'
import MovieCastView from '@/views/front/MovieCastView.vue'
import PersonDetailView from '@/views/front/PersonDetailView.vue'
import PersonWorksView from '@/views/front/PersonWorksView.vue'
import LoginView from '@/views/front/LoginView.vue'
import RegisterView from '@/views/front/RegisterView.vue'
import RecommendationView from '@/views/front/RecommendationView.vue'
import DashboardView from '@/views/admin/DashboardView.vue'
import MovieManagementView from '@/views/admin/MovieManagementView.vue'
import UserManagementView from '@/views/admin/UserManagementView.vue'
import CommentManageView from '@/views/admin/CommentManageView.vue'
import ContentManageView from '@/views/admin/ContentManageView.vue'
import TmdbManageView from '@/views/admin/TmdbManageView.vue'
import { useUserStore } from '@/stores/user'

const routes=[{path:'/',component:FrontLayout,children:[{path:'',name:'home',component:HomeView},{path:'movies',name:'movies',component:MovieListView},{path:'movies/:id',name:'movie-detail',component:MovieDetailView,props:true},{path:'movies/:id/cast',name:'movie-cast',component:MovieCastView,props:true},{path:'people/:type/:id',name:'person-detail',component:PersonDetailView,props:true},{path:'people/:type/:id/works/:sectionKey',name:'person-works',component:PersonWorksView,props:true},{path:'login',name:'login',component:LoginView},{path:'register',name:'register',component:RegisterView},{path:'recommendations',name:'recommendations',component:RecommendationView,meta:{requiresAuth:true}}]},{path:'/admin',component:AdminLayout,meta:{requiresAdmin:true},children:[{path:'',name:'admin-dashboard',component:DashboardView},{path:'movies',name:'admin-movies',component:MovieManagementView},{path:'users',name:'admin-users',component:UserManagementView},{path:'comments',name:'admin-comments',component:CommentManageView},{path:'content',name:'admin-content',component:ContentManageView},{path:'tmdb',name:'admin-tmdb',component:TmdbManageView}]}]
const router=createRouter({history:createWebHistory(),routes})
router.beforeEach((to)=>{const store=useUserStore();if(to.matched.some(record=>record.meta.requiresAuth||record.meta.requiresAdmin)&&!store.token)return{path:'/login',query:{redirect:to.fullPath}}})
export default router

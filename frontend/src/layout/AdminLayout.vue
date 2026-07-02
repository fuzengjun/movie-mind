<template>
  <div class="page-shell pb-10">
    <header class="page-frame flex items-center justify-between py-5 md:py-7">
      <div>
        <p class="section-kicker text-xs">Movie Mind Admin</p>
        <h1 class="mt-2 text-2xl font-semibold tracking-tight md:text-3xl">运营与内容控制台</h1>
      </div>
      <div class="flex gap-2">
        <button class="pill-button" @click="$router.push('/')">返回前台</button>
        <button class="pill-button md:hidden" @click="drawerVisible = true">菜单</button>
      </div>
    </header>
    <div class="page-frame grid gap-6 md:grid-cols-[240px_minmax(0,1fr)]">
      <aside class="surface-card hidden h-fit p-4 md:block"><AdminNav /></aside>
      <main class="min-w-0"><router-view /></main>
    </div>
    <el-drawer v-model="drawerVisible" direction="ltr" size="78%" :with-header="false">
      <div class="space-y-5 px-2 py-6">
        <p class="section-kicker text-xs">Admin Menu</p>
        <AdminNav @navigate="drawerVisible = false" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { defineComponent, h, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'

const drawerVisible = ref(false)
const items = [
  ['/admin', '数据概览', '趋势、榜单与内容热度'],
  ['/admin/movies', '影片管理', '片库维护与上下架'],
  ['/admin/users', '用户管理', '账户状态与互动档案'],
  ['/admin/comments', '评论管理', '社区内容审核'],
  ['/admin/content', '基础资料', '分类、标签与人物资料'],
  ['/admin/tmdb', 'TMDB 导入', '外部影片数据同步']
]

const AdminNav = defineComponent({
  emits: ['navigate'],
  setup(_, { emit }) {
    const route = useRoute()
    return () => h('nav', { class: 'space-y-2' }, items.map((item) => h(RouterLink, {
      to: item[0],
      class: [
        'block rounded-2xl px-4 py-3 nav-link',
        route.path === item[0] ? '!text-[var(--text-primary)] bg-[rgba(239,123,69,0.12)]' : ''
      ],
      activeClass: '',
      onClick: () => emit('navigate')
    }, {
      default: () => [
        h('p', { class: 'font-medium' }, item[1]),
        h('p', { class: 'mt-1 text-xs text-[var(--text-muted)]' }, item[2])
      ]
    })))
  }
})
</script>

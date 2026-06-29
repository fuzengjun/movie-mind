<template>
  <div class="page-shell grid min-h-screen md:grid-cols-[240px_1fr]">
    <aside class="bg-brand-900 p-6 text-white">
      <h1 class="mb-8 text-2xl font-bold">管理后台</h1>
      <div class="space-y-3">
        <RouterLink class="block rounded-lg px-3 py-2 text-white no-underline hover:bg-white/10" to="/admin">
          数据概览
        </RouterLink>
        <RouterLink class="block rounded-lg px-3 py-2 text-white no-underline hover:bg-white/10" to="/admin/movies">
          影片管理
        </RouterLink>
        <RouterLink class="block rounded-lg px-3 py-2 text-white no-underline hover:bg-white/10" to="/admin/users">
          用户管理
        </RouterLink>
      </div>
    </aside>
    <main class="p-6">
      <router-view />
    </main>
  </div>
</template>

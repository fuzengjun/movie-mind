<template>
  <header class="sticky top-0 z-20 border-b border-white/50 bg-white/80 backdrop-blur">
    <div class="mx-auto flex max-w-7xl items-center justify-between px-4 py-4">
      <RouterLink class="text-xl font-bold text-brand-900 no-underline" to="/">
        Movie Mind
      </RouterLink>
      <nav class="flex items-center gap-4 text-sm">
        <RouterLink class="text-slate-700 no-underline hover:text-brand-700" to="/">首页</RouterLink>
        <RouterLink class="text-slate-700 no-underline hover:text-brand-700" to="/movies">影视库</RouterLink>
        <RouterLink class="text-slate-700 no-underline hover:text-brand-700" to="/admin">后台</RouterLink>
        <RouterLink class="rounded-full bg-brand-700 px-4 py-2 text-white no-underline" to="/login">
          登录
        </RouterLink>
      </nav>
    </div>
  </header>
</template>

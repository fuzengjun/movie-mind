<template>
  <div class="page-shell">
    <Navbar />
    <main class="mx-auto max-w-7xl px-4 py-8">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import Navbar from '@/components/Navbar.vue'
</script>

<template>
  <div class="page-shell pb-12">
    <Navbar />
    <main class="page-frame space-y-10 pb-10 pt-6 md:pt-8">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import Navbar from '@/components/Navbar.vue'
</script>

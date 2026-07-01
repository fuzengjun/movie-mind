<template>
  <div class="page-shell pb-12">
    <Navbar />
    <main class="page-frame space-y-10 pb-10 pt-6 md:pt-8">
      <router-view />
    </main>
    <BackToTop />
    <AiAssistant />
  </div>
</template>

<script setup>
import Navbar from '@/components/Navbar.vue'
import BackToTop from '@/components/BackToTop.vue'
import AiAssistant from '@/components/AiAssistant.vue'
</script>

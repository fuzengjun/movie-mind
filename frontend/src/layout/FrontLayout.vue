<template>
  <div class="page-shell" :class="{ 'pb-12': !isAuthPage }">
    <Navbar v-if="!isAuthPage" />
    <main :class="{ 'page-frame space-y-10 pb-10 pt-6 md:pt-8': !isAuthPage }">
      <router-view />
    </main>
    <BackToTop v-if="!isAuthPage" />
    <AiAssistant v-if="!isAuthPage" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Navbar from '@/components/Navbar.vue'
import BackToTop from '@/components/BackToTop.vue'
import AiAssistant from '@/components/AiAssistant.vue'

const route = useRoute()
const isAuthPage = computed(() => ['login', 'register'].includes(route.name))
</script>

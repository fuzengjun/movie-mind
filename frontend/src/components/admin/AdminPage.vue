<template>
  <section class="space-y-6">
    <header class="surface-card admin-page-head">
      <div><p class="section-kicker text-xs">{{ kicker }}</p>
        <h2>{{ title }}</h2>
        <p>{{ hint }}</p></div>
      <div class="admin-page-actions">
        <slot name="actions"/>
      </div>
    </header>
    <slot/>
  </section>
</template>
<script setup>defineProps({title: String, kicker: String, hint: String})</script>

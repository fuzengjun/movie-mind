<template>
  <div class="admin-pagination"><span>共 {{ total }} 条</span>
    <el-pagination background layout="prev, pager, next" :current-page="page" :page-size="10" :total="total"
                   @current-change="change"/>
  </div>
</template>
<script setup>defineProps({page: Number, total: Number});
const emit = defineEmits(['update:page', 'change']);

function change(v) {
  emit('update:page', v);
  emit('change')
}</script>

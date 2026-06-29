<template>
  <section class="mx-auto max-w-md glass-panel p-8">
    <h1 class="mb-6 text-2xl font-semibold">登录系统</h1>
    <el-form label-position="top">
      <el-form-item label="用户名">
        <el-input placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input placeholder="请输入密码" show-password type="password" />
      </el-form-item>
      <el-button class="w-full" type="primary">登录</el-button>
    </el-form>
  </section>
</template>

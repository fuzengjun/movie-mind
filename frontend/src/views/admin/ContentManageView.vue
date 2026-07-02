<template>
  <AdminPage title="基础资料" kicker="Metadata" hint="统一维护分类、标签、演员和导演。">
    <template #actions>
      <el-input v-model="keyword" clearable placeholder="搜索当前模块" @keyup.enter="load" />
      <button class="pill-button is-active" @click="openCreate">新增{{ current.label }}</button>
    </template>
    <div class="surface-card p-4">
      <el-tabs v-model="active" @tab-change="change">
        <el-tab-pane v-for="item in tabs" :key="item.name" :name="item.name" :label="item.label" />
      </el-tabs>
      <el-table :data="records" v-loading="loading">
        <el-table-column prop="name" :label="isPerson ? '姓名' : '名称'" min-width="180" />
        <el-table-column v-if="isPerson" prop="originalName" label="原名" min-width="160" />
        <el-table-column v-if="isPerson" prop="nationality" label="国籍" width="120" />
        <el-table-column v-else prop="description" label="说明" min-width="320" />
        <el-table-column v-if="!isPerson" label="状态" width="90">
          <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }"><el-button link @click="openEdit(row)">编辑</el-button><el-button link type="danger" @click="remove(row)">删除</el-button></template>
        </el-table-column>
      </el-table>
      <AdminPagination v-if="isPerson" v-model:page="pageNum" :total="total" @change="load" />
    </div>
    <el-dialog v-model="dialog" :title="`${form.id ? '编辑' : '新增'}${current.label}`" width="min(620px,92vw)">
      <el-form label-position="top">
        <template v-if="isPerson">
          <div class="admin-form-grid">
            <el-form-item label="姓名" required><el-input v-model="form.name" /></el-form-item>
            <el-form-item label="原名"><el-input v-model="form.originalName" /></el-form-item>
            <el-form-item label="性别"><el-select v-model="form.gender"><el-option label="未知" :value="0" /><el-option label="女" :value="1" /><el-option label="男" :value="2" /></el-select></el-form-item>
            <el-form-item label="出生日期"><el-date-picker v-model="form.birthday" value-format="YYYY-MM-DD" /></el-form-item>
            <el-form-item label="国籍"><el-input v-model="form.nationality" /></el-form-item>
            <el-form-item label="头像地址"><el-input v-model="form.profileUrl" /></el-form-item>
          </div>
          <el-form-item label="人物简介"><el-input v-model="form.biography" type="textarea" :rows="5" /></el-form-item>
        </template>
        <template v-else>
          <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="说明"><el-input v-model="form.description" type="textarea" :rows="4" /></el-form-item>
          <el-form-item label="状态"><el-radio-group v-model="form.status"><el-radio-button :value="1">启用</el-radio-button><el-radio-button :value="0">禁用</el-radio-button></el-radio-group></el-form-item>
        </template>
      </el-form>
      <template #footer><button class="pill-button" @click="dialog=false">取消</button><button class="pill-button is-active" @click="save">保存</button></template>
    </el-dialog>
  </AdminPage>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listDictionaries, saveDictionary, deleteDictionary, pagePeople, savePerson, deletePerson } from '@/api/adminManagement'
import AdminPage from '@/components/admin/AdminPage.vue'
import AdminPagination from '@/components/admin/AdminPagination.vue'

const tabs = [{ name:'categories', label:'分类' }, { name:'tags', label:'标签' }, { name:'actors', label:'演员' }, { name:'directors', label:'导演' }]
const active=ref('categories'), keyword=ref(''), records=ref([]), total=ref(0), pageNum=ref(1), loading=ref(false), dialog=ref(false), form=reactive({})
const current=computed(()=>tabs.find(item=>item.name===active.value))
const isPerson=computed(()=>['actors','directors'].includes(active.value))
const blank=()=>isPerson.value ? {id:null,name:'',originalName:'',gender:0,birthday:null,nationality:'',profileUrl:'',biography:''} : {id:null,name:'',description:'',status:1}

async function load(){loading.value=true;try{if(isPerson.value){const r=await pagePeople(active.value,{keyword:keyword.value,pageNum:pageNum.value,pageSize:10});records.value=r.data.records;total.value=r.data.total}else records.value=(await listDictionaries(active.value,{keyword:keyword.value})).data}finally{loading.value=false}}
function change(){keyword.value='';pageNum.value=1;load()}
function openCreate(){Object.assign(form,blank());dialog.value=true}
function openEdit(row){Object.assign(form,blank(),row);dialog.value=true}
async function save(){if(isPerson.value)await savePerson(active.value,form.id,form);else await saveDictionary(active.value,form.id,form);ElMessage.success('保存成功');dialog.value=false;load()}
async function remove(row){await ElMessageBox.confirm('确认删除该数据？','删除确认',{type:'warning'});if(isPerson.value)await deletePerson(active.value,row.id);else await deleteDictionary(active.value,row.id);ElMessage.success('已删除');load()}
onMounted(load)
</script>

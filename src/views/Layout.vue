<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  HomeFilled,
  Document,
  Menu,
  Food,
  Dish,
  DataLine,
  UserFilled,
  Bell,
  ArrowDown
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const isCollapse = ref(false)

// 菜单配置
const menuList = [
  { path: '/dashboard', title: '工作台', icon: HomeFilled },
  { path: '/order', title: '订单管理', icon: Document },
  { path: '/category', title: '分类管理', icon: Menu },
  { path: '/dish', title: '菜品管理', icon: Food },
  { path: '/setmeal', title: '套餐管理', icon: Dish },
  { path: '/statistics', title: '数据统计', icon: DataLine },
  { path: '/employee', title: '员工管理', icon: UserFilled }
]

const handleMenuSelect = (path) => {
  router.push(path)
}

const handleCommand = (command) => {
  if (command === 'logout') {
    // 退出登录逻辑
    console.log('退出登录')
  }
}
</script>

<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside class="sidebar" :width="isCollapse ? '64px' : '200px'">
      <div class="logo">
        <img src="@/assets/logo.png" alt="logo" class="logo-img" />
      </div>
      <el-menu
        :default-active="route.path"
        class="sidebar-menu"
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="#f2b6d4"
        text-color="#333333"
        active-text-color="#666666"
        @select="handleMenuSelect"
      >
        <el-menu-item v-for="item in menuList" :key="item.path" :index="item.path">
          <el-icon>
            <component :is="item.icon" />
          </el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-tag type="danger" effect="dark" size="large">营业中</el-tag>
        </div>
        <div class="header-right">
          <el-button type="primary" size="small">营业状态</el-button>
          <el-badge :value="99" class="message-badge" :max="99">
            <el-icon :size="24"><Bell /></el-icon>
          </el-badge>
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
              <span class="username">管理员</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-container {
  height: 100vh;
  width: 100vw;
}

.sidebar {
  background-color: #f3ecc8;
  transition: width 0.3s;
  height: 100vh;
  flex-shrink: 0;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f3ecc8;
  white-space: nowrap;
  overflow: hidden;
}

.logo-img {
  height: 40px;
  width: auto;
  object-fit: contain;
}

.sidebar-menu {
  border-right: none;
  background-color: #f3ecc8;
}

.sidebar-menu :deep(.el-menu-item) {
  color: #333333;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background-color:#f3ecc8;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  color: #409EFF;
  background-color: #fff;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.message-badge {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f5f5;
}

.username {
  font-size: 14px;
  color: #606266;
}

.main-content {
  background-color: #f5f5f5;
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}
</style>

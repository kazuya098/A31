import { createRouter, createWebHistory } from 'vue-router';
import Layout from '../components/Layout.vue'; // 引入 Layout 组件
import LoginView from '../views/auth/LoginView.vue'; // 引入 LoginView 组件
import RegisterView from '../views/auth/RegisterView.vue'; // 引入 RegisterView 组件

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: {
      title: '登录',
    },
  },
  {
    path: '/register',
    name: 'Register',
    component: RegisterView,
    meta: {
      title: '注册',
    },
  },
  {
    path: '/',
    name: 'Layout',
    component: Layout,
    redirect: '/dashboard', // 登录后重定向到仪表盘
    meta: {
      requiresAuth: true, // 需要认证
      title: '主页',
    },
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/DashboardView.vue'),
        meta: {
          title: '仪表盘',
        },
      },
      {
        path: '/upload',
        name: 'Upload',
        component: () => import('../views/recognition/UploadView.vue'),
        meta: {
          title: '上传识别',
        },
      },
      {
        path: '/records',
        name: 'RecordList',
        component: () => import('../views/records/RecordList.vue'),
        meta: {
          title: '识别记录',
        },
      },
      {
        path: '/individuals',
        name: 'IndividualList',
        component: () => import('../views/individuals/IndividualProfilePage.vue'),
        meta: {
          title: '个体管理',
        },
      },
      // {
      //   path: '/individuals/profile',
      //   name: 'IndividualProfile',
      //   component: () => import('../views/individuals/IndividualProfilePage.vue'),
      //   meta: {
      //     title: '个体档案',
      //   },
      // },
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('../views/profile/ProfileView.vue'),
        meta: {
          title: '个人中心',
        },
      },
      {
        path: '/report/:id',
        name: 'RecordReport',
        component: () => import('../views/report/Report.vue'),
        meta: {
          title: '识别报告',
        },
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// router.beforeEach((to, from, next) => {
//   document.title = to.meta.title ? `${to.meta.title} - 跨时域生物识别系统` : '跨时域生物识别系统';
//   const token = localStorage.getItem('token');
//   if (to.meta.requiresAuth && !token) {
//     next('/login');
//   } else {
//     next();
//   }
// });
// src/router/index.js
router.beforeEach((to, from) => {
  const token = localStorage.getItem('token');

  if (to.path === '/login' || to.path === '/register') {
    // 已登录状态下去登录页，直接跳首页
    if (token) {
      return '/';
    } else {
      return true;
    }
  } else {
    // 访问受保护页面（如设置、首页）
    if (!token) {
      return '/login'; // 没钥匙，踢回登录
    } else {
      return true; // 有钥匙，放行
    }
  }
});
export default router;

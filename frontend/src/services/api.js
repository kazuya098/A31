import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from '../router';

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api', // 从环境变量获取基础URL，默认为代理路径
  timeout: 5000, // 请求超时时间
});

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['X-Auth-Token'] = token; // 将token添加到请求头
      config.headers['ngrok-skip-browser-warning'] = '69420';
    }
    return config;
  },
  error => {
    console.log(error); // for debug
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data;
    // 根据实际业务逻辑判断请求是否成功，这里假设后端返回的data中有一个code字段
    if (res.code && res.code !== 200) {
      ElMessage.error(res.message || 'Error');

      // 401: Token失效或未登录
      if (res.code === 401) {
        // 清除token并跳转到登录页
        localStorage.removeItem('token');
        router.push('/login');
      }
      return Promise.reject(new Error(res.message || 'Error'));
    } else {
      return res;
    }
  },
  error => {
    console.log('err' + error); // for debug
    let message = error.message;
    if (error.response && error.response.data) {
      message = error.response.data.message || error.message;
      if (error.response.status === 401) {
        localStorage.removeItem('token');
        router.push('/login');
        ElMessage.error('认证失败，请重新登录');
        return Promise.reject(new Error('认证失败，请重新登录'));
      }
    }
    ElMessage.error(message);
    return Promise.reject(error);
  }
);

export default service;

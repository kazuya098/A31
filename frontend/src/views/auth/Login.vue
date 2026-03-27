<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <span>图像识别分析系统</span>
        </div>
      </template>
      <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-width="0px" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="用户名" prefix-icon="User"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input type="password" v-model="loginForm.password" placeholder="密码" prefix-icon="Lock" show-password></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitForm" class="login-button">登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { login as authLogin } from '../../services/modules/auth';

const router = useRouter();
const loginFormRef = ref(null);

const loginForm = reactive({
  username: '',
  password: '',
});

const loginRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
});

const submitForm = () => {
  loginFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const response = await authLogin(loginForm);
        localStorage.setItem('X-Auth-Token', response.token);
        ElMessage.success('登录成功');
        router.push('/');
      } catch (error) {
        // Error message already handled by axios interceptor
        console.error('Login failed:', error);
      }
    } else {
      console.log('error submit!!');
      return false;
    }
  });
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(to right, #4facfe 0%, #00f2fe 100%);
}

.login-card {
  width: 400px;
  max-width: 90%;
  border-radius: 10px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.card-header {
  text-align: center;
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.login-form {
  padding: 20px;
}

.login-button {
  width: 100%;
}
</style>

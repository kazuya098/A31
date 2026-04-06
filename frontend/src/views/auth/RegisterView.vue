<template>
  <div class="login-wrapper">
    <div class="page-ornament page-ornament--1" aria-hidden="true" />
    <div class="page-ornament page-ornament--2" aria-hidden="true" />
    <div class="brand-block">
      <p class="brand-tagline">跨时域生物识别系统</p>
      <p class="brand-desc">安全、高效的跨时域智能识别平台</p>
    </div>
    <footer class="login-footer">
      <span class="login-footer-text">安全认证 · 智能识别</span>
    </footer>
    <div class="login-panel">
      <div class="login-card">
        <div class="card-header">
          <span class="card-title">注册</span>
        </div>
        <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" label-width="0px" class="login-form">
          <el-form-item prop="username" class="form-item">
            <el-input
              v-model="registerForm.username"
              placeholder="用户名"
              prefix-icon="User"
              size="large"
              class="login-input"
            />
          </el-form-item>
          <el-form-item prop="password" class="form-item">
            <el-input
              type="password"
              v-model="registerForm.password"
              placeholder="密码"
              prefix-icon="Lock"
              show-password
              size="large"
              class="login-input"
            />
          </el-form-item>
          <el-form-item prop="confirmPassword" class="form-item">
            <el-input
              type="password"
              v-model="registerForm.confirmPassword"
              placeholder="确认密码"
              prefix-icon="Lock"
              show-password
              size="large"
              class="login-input"
            />
          </el-form-item>
          <el-form-item class="form-item form-item-submit">
            <el-button type="primary" @click="submitForm" class="login-button" size="large">
              注册
            </el-button>
          </el-form-item>
          <div class="login-hint">
            <span>已有账户？</span>
            <router-link to="/login" class="login-link">立即登录</router-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { register as authRegister } from '../../services/modules/auth';

const router = useRouter();
const registerFormRef = ref(null);

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
});

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

const registerRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名长度不能少于3位', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
});

const submitForm = () => {
  registerFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const response = await authRegister({
          username: registerForm.username,
          password: registerForm.password,
        });
        localStorage.setItem('X-Auth-Token', response.token);
        ElMessage.success('注册成功');
        router.push('/');
      } catch (error) {
        console.error('Register failed:', error);
        ElMessage.error('注册失败，用户名可能已存在');
      }
    }
  });
};
</script>

<style scoped>
.login-wrapper {
  --login-bg-overlay: linear-gradient(135deg, rgba(120, 53, 15, 0.32) 0%, rgba(30, 27, 75, 0.38) 50%, rgba(68, 64, 60, 0.35) 100%);
  --card-bg: rgba(30, 27, 24, 0.48);
  --card-border: rgba(255, 255, 255, 0.16);
  --card-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.35);
  --text-primary: #fafaf9;
  --text-muted: #a8a29e;
  --btn-bg: #b45309;
  --btn-hover: #d97706;
  --btn-glow: rgba(217, 119, 6, 0.45);
  --input-bg: rgba(255, 255, 255, 0.06);
  --input-border: rgba(255, 255, 255, 0.12);
  --accent-focus: #f59e0b;
  --accent-glow: rgba(245, 158, 11, 0.4);
  --motion-duration: 0.28s;
  --motion-ease: cubic-bezier(0.25, 0.46, 0.45, 0.94);
  display: flex;
  justify-content: flex-end;
  align-items: center;
  min-height: 100vh;
  min-height: 100dvh;
  width: 100%;
  overflow: hidden;
  background: url('@/assets/images/login/loginbg1.jpg') no-repeat center center;
  background-size: cover;
  position: relative;
}

.login-wrapper::before {
  content: '';
  position: absolute;
  inset: 0;
  background: var(--login-bg-overlay);
  z-index: 1;
}

.page-ornament {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.35;
  z-index: 0;
  pointer-events: none;
}

.page-ornament--1 {
  width: 320px;
  height: 320px;
  background: radial-gradient(circle, rgba(251, 191, 36, 0.5) 0%, transparent 70%);
  top: 15%;
  left: 10%;
}

.page-ornament--2 {
  width: 280px;
  height: 280px;
  background: radial-gradient(circle, rgba(217, 119, 6, 0.4) 0%, transparent 70%);
  bottom: 20%;
  left: 25%;
}

.brand-block {
  position: absolute;
  left: 14%;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2;
}

.brand-tagline {
  font-size: 3.85rem;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.02em;
  line-height: 1.25;
  text-shadow: 0 2px 20px rgba(0, 0, 0, 0.3);
  margin: 0;
}

.brand-desc {
  font-size: 1rem;
  color: var(--text-muted);
  margin: 12px 0 0;
  font-weight: 500;
  text-shadow: 0 1px 10px rgba(0, 0, 0, 0.25);
}

.login-footer {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 2;
}

.login-footer-text {
  font-size: 0.8rem;
  color: var(--text-muted);
  letter-spacing: 0.08em;
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.3);
}

.login-panel {
  width: 420px;
  max-width: 90vw;
  z-index: 2;
  margin-left: auto;
  margin-right: 12%;
  animation: fade-slide-up 0.6s var(--motion-ease) forwards;
}

.login-card {
  width: 100%;
  border-radius: 20px;
  overflow: hidden;
  background: var(--card-bg);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--card-border);
  box-shadow: var(--card-shadow);
}

.card-header {
  text-align: center;
  padding: 28px 32px 20px;
  margin-bottom: 8px;
  border-bottom: 1px solid var(--card-border);
}

.card-title {
  display: block;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.02em;
  line-height: 1.3;
}

.login-form {
  padding: 24px 32px 32px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.form-item {
  margin-bottom: 18px;
  animation: form-item-in 0.45s var(--motion-ease) backwards;
}

.form-item:nth-child(1) { animation-delay: 0.08s; }
.form-item:nth-child(2) { animation-delay: 0.16s; }
.form-item:nth-child(3) { animation-delay: 0.24s; }
.form-item-submit { animation-delay: 0.32s; margin-bottom: 0; }

.login-button {
  width: 100%;
  padding: 14px 24px;
  font-size: 1rem;
  font-weight: 600;
  background: var(--btn-bg);
  border: none;
  color: #fff;
  border-radius: 12px;
  cursor: pointer;
  transition: background-color var(--motion-duration) var(--motion-ease),
    box-shadow var(--motion-duration) var(--motion-ease),
    transform var(--motion-duration) var(--motion-ease);
  box-shadow: 0 4px 14px var(--btn-glow);
}

.login-button:hover {
  background: var(--btn-hover);
  box-shadow: 0 6px 20px var(--btn-glow);
  transform: translateY(-1px);
}

.login-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 10px 14px;
  background: var(--input-bg) !important;
  border: 1px solid var(--input-border) !important;
  box-shadow: none !important;
  transition: box-shadow var(--motion-duration) var(--motion-ease),
    border-color var(--motion-duration) var(--motion-ease);
}

.login-input :deep(.el-input__wrapper:hover) {
  border-color: rgba(255, 255, 255, 0.2) !important;
}

.login-input :deep(.el-input__wrapper.is-focused) {
  box-shadow: 0 0 0 2px var(--accent-glow) !important;
  border-color: var(--accent-focus) !important;
  background: rgba(255, 255, 255, 0.08) !important;
}

.login-input :deep(.el-input__inner) {
  color: var(--text-primary) !important;
}

.login-input :deep(.el-input__inner::placeholder) {
  color: var(--text-muted) !important;
}

.login-input :deep(.el-input__prefix .el-icon),
.login-input :deep(.el-input__suffix .el-icon) {
  color: var(--text-muted);
}

.login-hint {
  text-align: center;
  margin-top: 12px;
  font-size: 0.85rem;
  color: var(--text-muted);
}

.login-link {
  color: var(--accent-focus);
  text-decoration: none;
  font-weight: 600;
  transition: color var(--motion-duration) var(--motion-ease);
}

.login-link:hover {
  color: var(--btn-hover);
}

@keyframes fade-slide-up {
  from { opacity: 0; transform: translateY(36px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes form-item-in {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 768px) {
  .login-wrapper {
    flex-direction: column;
    justify-content: center;
    padding-bottom: 60px;
  }
  .brand-block {
    position: static;
    transform: none;
    text-align: center;
    padding: 24px 16px 16px;
  }
  .brand-tagline {
    font-size: 1.35rem;
  }
  .login-panel {
    width: 90%;
    margin-left: auto;
    margin-right: auto;
  }
}
</style>

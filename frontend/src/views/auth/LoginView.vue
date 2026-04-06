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
          <span class="card-title">账户登录</span>
          <span class="card-subtitle">欢迎回来，请登录您的账户</span>
        </div>
        <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-width="0px" class="login-form">
          <el-form-item prop="username" class="form-item">
            <el-input
              v-model="loginForm.username"
              placeholder="用户名"
              prefix-icon="User"
              size="large"
              class="login-input"
            />
          </el-form-item>
          <el-form-item prop="password" class="form-item">
            <el-input
              type="password"
              v-model="loginForm.password"
              placeholder="密码"
              prefix-icon="Lock"
              show-password
              size="large"
              class="login-input"
            />
          </el-form-item>
          <el-form-item class="form-item form-item-submit">
            <el-button type="primary" @click="submitForm" class="login-button" size="large">
              验证并登录
            </el-button>
          </el-form-item>
          <div class="register-hint">
            <span>没有账户？</span>
            <router-link to="/register" class="register-link">立即注册</router-link>
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
        localStorage.setItem('token', response.token);
        ElMessage.success('登录成功');
        router.push('/');
      } catch (error) {
        console.error('Login failed:', error);
      }
    }
  });
};
</script>

<style scoped>
/* ----- Design tokens: 暖色暗黑玻璃 Warm Dark Glassmorphism ----- */
.login-wrapper {
  --login-bg-overlay: linear-gradient(135deg, rgba(120, 53, 15, 0.32) 0%, rgba(30, 27, 75, 0.38) 50%, rgba(68, 64, 60, 0.35) 100%);
  --card-bg: rgba(30, 27, 24, 0.48);
  --card-border: rgba(255, 255, 255, 0.16);
  --card-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.35);
  --text-primary: #d0cdca;
  --text-muted: #a8a29e;
  --btn-bg: #b45309;
  --btn-hover: #d97706;
  --btn-glow: rgba(217, 119, 6, 0.45);
  --input-bg: transparent;
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
  animation: overlay-shift 12s ease-in-out infinite alternate;
}

/* 背景装饰光晕动效 */
.page-ornament {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.35;
  z-index: 0;
  animation: ornament-float 18s ease-in-out infinite;
  pointer-events: none;
}

.page-ornament--1 {
  width: 320px;
  height: 320px;
  background: radial-gradient(circle, rgba(251, 191, 36, 0.5) 0%, transparent 70%);
  top: 15%;
  left: 10%;
  animation-delay: 0s;
}

.page-ornament--2 {
  width: 280px;
  height: 280px;
  background: radial-gradient(circle, rgba(217, 119, 6, 0.4) 0%, transparent 70%);
  bottom: 20%;
  left: 25%;
  animation-delay: -6s;
}

@keyframes ornament-float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(20px, -15px) scale(1.05); }
}

@keyframes overlay-shift {
  0% { opacity: 0.95; }
  100% { opacity: 1; }
}

/* 左侧品牌区（往中间靠拢） */
.brand-block {
  position: absolute;
  left: 14%;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2;
  animation: brand-in 0.8s var(--motion-ease) 0.2s backwards;
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

@keyframes brand-in {
  from {
    opacity: 0;
    transform: translateY(-50%) translateX(-24px);
  }
  to {
    opacity: 1;
    transform: translateY(-50%) translateX(0);
  }
}

@keyframes brand-in-mobile {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-footer {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 2;
  animation: brand-in 0.8s var(--motion-ease) 0.5s backwards;
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
  display: flex;
  justify-content: center;
  align-items: center;
  box-sizing: border-box;
  z-index: 2;
  margin-left: auto;
  margin-right: 12%;
  animation: fade-slide-up 0.6s var(--motion-ease) forwards;
  cursor: pointer;
}

.login-card {
  width: 100%;
  min-height: 380px;
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  background: var(--card-bg);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--card-border);
  box-shadow: var(--card-shadow);
  padding: 0;
  transition: box-shadow var(--motion-duration) var(--motion-ease),
    border-color var(--motion-duration) var(--motion-ease);
}

.login-panel:hover .login-card {
  box-shadow: 0 32px 64px -16px rgba(0, 0, 0, 0.45);
  border-color: rgba(255, 255, 255, 0.22);
}

.card-header {
  text-align: center;
  padding: 36px 32px 20px;
  margin-bottom: 8px;
}

.card-title {
  display: block;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.02em;
  line-height: 1.3;
}

.card-subtitle {
  display: block;
  margin-top: 6px;
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--text-muted);
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

.login-button:active {
  transform: translateY(0);
  box-shadow: 0 2px 10px var(--btn-glow);
}

/* Element Plus input: 暗黑玻璃拟态输入框 */
.login-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 10px 14px;
  background-color: transparent !important;
  border: 1px solid var(--input-border) !important;
  box-shadow: none !important;
  transition: all var(--motion-duration) var(--motion-ease);
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

/* Entrance and form animations */
@keyframes fade-slide-up {
  from {
    opacity: 0;
    transform: translateY(36px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes form-item-in {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Respect reduced motion (accessibility) */
@media (prefers-reduced-motion: reduce) {
  .login-panel,
  .form-item,
  .brand-block,
  .login-footer,
  .page-ornament {
    animation: none;
  }
  .login-wrapper::before {
    animation: none;
  }
  .login-card,
  .login-button,
  .login-input :deep(.el-input__wrapper) {
    transition-duration: 0.01ms;
  }
  .login-button:hover {
    transform: none;
  }
}

/* Responsive */
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
    animation: brand-in-mobile 0.6s var(--motion-ease) backwards;
  }
  .brand-tagline {
    font-size: 1.35rem;
  }
  .brand-desc {
    font-size: 0.9rem;
  }
  .page-ornament {
    opacity: 0.2;
  }
  .login-panel {
    width: 90%;
    margin-left: auto;
    margin-right: auto;
  }
  .login-card {
    max-width: 400px;
  }
  .card-header {
    padding: 24px 24px 16px;
  }
  .login-form {
    padding: 20px 24px 28px;
  }
  .login-footer {
    bottom: 16px;
  }
}

@media (max-width: 480px) {
  .card-title {
    font-size: 1.25rem;
  }
  .card-subtitle {
    font-size: 0.85rem;
  }
}

.register-hint {
  text-align: center;
  margin-top: 12px;
  font-size: 0.85rem;
  color: var(--text-muted);
}

.register-link {
  color: var(--accent-focus);
  text-decoration: none;
  font-weight: 600;
  transition: color var(--motion-duration) var(--motion-ease);
}

.register-link:hover {
  color: var(--btn-hover);
}
</style>

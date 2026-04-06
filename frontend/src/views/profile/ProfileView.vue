<template>
  <div class="flex-1 flex flex-col p-8 bg-[#FDF9F3]/60 min-h-screen">
    <!-- Top Header Bar -->
    <header class="flex justify-between items-center mb-16 animate-fade-in-down">
      <div>
        <h1 class="text-4xl font-light tracking-tight text-gray-900 mb-2">
          设置 <span class="font-semibold text-orange-600">用户中心</span>
        </h1>
        <p class="text-gray-500 text-lg">管理您的账户信息与安全配置</p>
      </div>
      <div class="flex items-center space-x-6">
        <!-- Logout Button -->
        <button @click="handleLogout" class="bg-gray-900 text-white px-8 py-3 rounded-full text-sm font-medium transition-all hover:-translate-y-1 hover:shadow-xl hover:shadow-gray-900/20 active:translate-y-0 active:shadow-inner flex items-center">
          退出登录
        </button>
      </div>
    </header>



    <!-- Main Content Area -->
    <div class="flex-1 flex space-x-8 main-content">
      <!-- 左侧垂直导航栏 -->
      <nav class="side-nav bg-white/60 backdrop-blur-md rounded-2xl p-6 shadow-soft-blue w-fit h-fit shrink-0">


        <nav class="flex-grow">
          <ul>
            <li v-for="item in navItemsData" :key="item.id" class="mb-4 last:mb-0">
              <a href="#" @click.prevent="activeTabId = item.id"
                class="relative flex items-center p-3 rounded-lg hover:bg-orange-50 group transition-all duration-200"
                :class="{'bg-orange-100 text-orange-600 shadow-sm font-semibold': activeTabId === item.id, 'text-gray-700': activeTabId !== item.id}">
                <span class="mr-5 text-4xl transition-transform group-hover:scale-110">{{ item.icon }}</span>
                <span class="relative z-10 font-medium whitespace-nowrap">{{ item.label }}</span>
                <span v-if="item.badge" class="ml-auto px-2 py-0.5 text-[10px] font-bold rounded-full bg-red-500 text-white shadow-sm">{{ item.badge }}</span>
              </a>
            </li>
          </ul>
        </nav>
      </nav>

      <!-- 右侧内容区域 -->
        <div class="content-area flex-grow backdrop-blur-xl rounded-3xl p-8 shadow-soft-blue flex border border-white/50 bg-white/30">
          <div class="w-3/4 bg-white/40 rounded-l-3xl p-10 flex flex-col justify-start space-y-10">
            <template v-if="activeTabId === 'user-info'">
              <!-- Top section: Image and User Details -->
              <div class="flex items-start w-full">
                <!-- User Image (Left) -->
                <div class="w-40 h-40 rounded-lg overflow-hidden border-4 border-white shadow-lg flex-shrink-0 mr-12">
                  <img :src="currentUser.image" alt="User Avatar" class="w-full h-full object-cover">
                </div>

                <!-- User Information (Right) -->
                <div class="flex-grow space-y-6">
                  <div class="bg-white p-4 rounded-lg shadow-sm">
                    <div class="flex items-center mb-0">
                      <label class="w-32 text-lg text-gray-600 font-medium">用户名:</label>
                      <input v-if="isEditing" v-model="tempUser.username" class="flex-grow border border-gray-200 rounded-lg px-4 py-2 text-lg focus:outline-none focus:ring-2 focus:ring-orange-400 bg-white/60" />
                      <span v-else class="flex-grow text-lg font-semibold text-gray-800">{{ currentUser.username }}</span>
                    </div>
                  </div>

                  <div class="bg-white/60 backdrop-blur-sm p-5 rounded-xl border border-white/50 shadow-sm">
                    <div class="flex items-center mb-0">
                      <label class="w-32 text-lg text-gray-600 font-medium">权限:</label>
                      <input v-if="isEditing" v-model="tempUser.permission" class="flex-grow border border-gray-200 rounded-lg px-4 py-2 text-lg focus:outline-none focus:ring-2 focus:ring-orange-400 bg-white/60" />
                      <span v-else class="flex-grow text-lg font-semibold text-gray-800">{{ currentUser.permission }}</span>
                    </div>
                  </div>

                  <div class="bg-white/60 backdrop-blur-sm p-5 rounded-xl border border-white/50 shadow-sm">
                    <div class="flex items-center mb-0">
                      <label class="w-32 text-lg text-gray-600 font-medium">用户ID:</label>
                      <span class="flex-grow text-lg font-semibold text-gray-800">{{ currentUser.userId }}</span>
                    </div>
                  </div>

                  <div class="bg-white/60 backdrop-blur-sm p-5 rounded-xl border border-white/50 shadow-sm">
                    <div class="flex items-center mb-0">
                      <label class="w-32 text-lg text-gray-600 font-medium">绑定邮箱:</label>
                      <input v-if="isEditing" v-model="tempUser.email" class="flex-grow border border-gray-200 rounded-lg px-4 py-2 text-lg focus:outline-none focus:ring-2 focus:ring-orange-400 bg-white/60" />
                      <span v-else class="flex-grow text-lg font-semibold text-gray-800">{{ currentUser.email }} - <span class="text-emerald-500 font-medium tracking-tight">已验证</span></span>
                    </div>
                  </div>

                  <div class="bg-white/60 backdrop-blur-sm p-5 rounded-xl border border-white/50 shadow-sm">
                    <div class="flex items-center mb-0">
                      <label class="w-32 text-lg text-gray-600 font-medium">绑定手机:</label>
                      <input v-if="isEditing" v-model="tempUser.phone" class="flex-grow border border-gray-200 rounded-lg px-4 py-2 text-lg focus:outline-none focus:ring-2 focus:ring-orange-400 bg-white/60" />
                      <span v-else class="flex-grow text-lg font-semibold text-gray-800">{{ currentUser.phone }} - <span class="text-emerald-500 font-medium tracking-tight">已绑定</span></span>
                    </div>
                  </div>

                  <div class="bg-white/60 backdrop-blur-sm p-5 rounded-xl border border-white/50 shadow-sm">
                    <div class="flex items-center mb-0">
                      <label class="w-32 text-lg text-gray-600 font-medium">加入时间:</label>
                      <span class="flex-grow text-lg font-semibold text-gray-800">{{ currentUser.joinDate }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Data Update Timestamp -->
              <div class="w-full text-center text-gray-500 text-sm mt-auto mb-4">
                数据更新于 {{ lastUpdateTime }}
              </div>

              <!-- Buttons (Bottom Center) -->
              <div class="w-full flex justify-center space-x-4 pb-4">
                <button v-if="!isEditing" @click="startEdit"
                  @mousemove="handleEditButtonMouseMove" @mouseleave="handleEditButtonMouseLeave"
                  class="relative isolate overflow-hidden group px-10 py-4 mb-4 rounded-full text-white text-lg font-medium transition-all duration-300 ease-out focus:outline-none focus:ring-2 focus:ring-orange-500 focus:ring-offset-2 shadow-lg shadow-orange-500/30">
                  <!-- Gradient Background -->
                  <span class="absolute inset-0 bg-gradient-to-r from-orange-500 to-red-600 transition-transform duration-300 ease-out group-hover:scale-105"></span>
                  <!-- Dynamic Glow element -->
                  <span class="absolute -inset-px rounded-full bg-gradient-to-r from-orange-400 via-pink-400 to-purple-500 opacity-0 transition-opacity duration-500 group-hover:opacity-100 animate-spin-slow"
                        :style="glowStyle"></span>

                  <span class="relative z-10 flex items-center justify-center">
                    <span>编辑资料</span>
                    <span class="ml-2 transform transition-transform duration-300 ease-out group-hover:translate-x-1">
                      <el-icon><ArrowRight /></el-icon>
                    </span>
                    <!-- Bottom Underline Expand -->
                    <span class="absolute -bottom-1 left-0 w-full h-0.5 bg-transparent transform scale-x-0 group-hover:scale-x-100 transition-transform duration-300 ease-out origin-left"></span>
                  </span>
                </button>
                <template v-else>
                  <button @click="saveUserChanges"
                    @mousemove="handleSaveButtonMouseMove" @mouseleave="handleSaveButtonMouseLeave"
                    class="cta-btn-enter relative isolate overflow-hidden group px-10 py-4 mb-4 rounded-full text-white text-lg font-medium transition-all duration-300 ease-out focus:outline-none focus:ring-2 focus:ring-orange-500 focus:ring-offset-2 shadow-lg shadow-orange-500/30 mr-4">
                    <span class="absolute inset-0 bg-gradient-to-r from-orange-500 to-red-600 transition-transform duration-300 ease-out group-hover:scale-105"></span>
                    <span class="absolute -inset-px rounded-full bg-gradient-to-r from-orange-400 via-pink-400 to-purple-500 opacity-0 transition-opacity duration-500 group-hover:opacity-100"
                          :style="saveGlowStyle"></span>
                    <span class="relative z-10 flex items-center justify-center">
                      <span>保存更改</span>
                      <span class="ml-2 transform transition-transform duration-300 ease-out group-hover:translate-x-1">
                        <el-icon><ArrowRight /></el-icon>
                      </span>
                      <span class="absolute -bottom-1 left-0 w-full h-0.5 bg-transparent transform scale-x-0 group-hover:scale-x-100 transition-transform duration-300 ease-out origin-left"></span>
                    </span>
                  </button>
                  <button @click="resetUserChanges"
                    @mousemove="handleCancelButtonMouseMove" @mouseleave="handleCancelButtonMouseLeave"
                    class="cta-btn-enter relative isolate overflow-hidden group px-10 py-4 mb-4 rounded-full text-gray-600 text-lg font-medium transition-all duration-300 ease-out focus:outline-none focus:ring-2 focus:ring-gray-300 focus:ring-offset-2 shadow-lg shadow-gray-200/50">
                    <span class="absolute inset-0 bg-gradient-to-r from-gray-100 to-gray-200 transition-transform duration-300 ease-out group-hover:scale-105"></span>
                    <span class="absolute -inset-px rounded-full bg-gradient-to-r from-gray-200 via-gray-100 to-gray-200 opacity-0 transition-opacity duration-500 group-hover:opacity-100"
                          :style="cancelGlowStyle"></span>
                    <span class="relative z-10 flex items-center justify-center">
                      <span>取消重置</span>
                      <span class="ml-2 transform transition-transform duration-300 ease-out group-hover:translate-x-1">
                        <el-icon><ArrowRight /></el-icon>
                      </span>
                      <span class="absolute -bottom-1 left-0 w-full h-0.5 bg-transparent transform scale-x-0 group-hover:scale-x-100 transition-transform duration-300 ease-out origin-left"></span>
                    </span>
                  </button>
                </template>
              </div>
            </template>
            <template v-else-if="activeTabId === 'account-security'">
              <div class="w-full space-y-8 animate-fade-in">
                <!-- Section 1: Security Summary Cards -->
                <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
                  <div class="bg-white/60 backdrop-blur-md p-6 rounded-2xl border border-white/50 shadow-sm flex flex-col items-center text-center group hover:bg-white/80 transition-all duration-300">
                    <div class="w-16 h-16 bg-orange-100 rounded-full flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                      <span class="text-3xl">🛡️</span>
                    </div>
                    <h4 class="font-bold text-gray-800">账号等级</h4>
                    <p class="text-orange-600 font-extrabold text-xl mt-1">高安全</p>
                  </div>
                  <div class="bg-white/60 backdrop-blur-md p-6 rounded-2xl border border-white/50 shadow-sm flex flex-col items-center text-center group hover:bg-white/80 transition-all duration-300">
                    <div class="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                      <span class="text-3xl">📱</span>
                    </div>
                    <h4 class="font-bold text-gray-800">双重验证</h4>
                    <p class="text-gray-500 text-sm mt-1">未开启</p>
                    <button class="mt-2 text-blue-600 text-xs font-bold hover:underline">立即开启</button>
                  </div>
                  <div class="bg-white/60 backdrop-blur-md p-6 rounded-2xl border border-white/50 shadow-sm flex flex-col items-center text-center group hover:bg-white/80 transition-all duration-300">
                    <div class="w-16 h-16 bg-emerald-100 rounded-full flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
                      <span class="text-3xl">📧</span>
                    </div>
                    <h4 class="font-bold text-gray-800">邮箱绑定</h4>
                    <p class="text-emerald-600 text-sm mt-1 font-bold">已验证</p>
                    <p class="text-gray-400 text-[10px] mt-1">test***@email.com</p>
                  </div>
                </div>

                <!-- Section 2: Password Change (Main focus) -->
                <div class="bg-white/40 backdrop-blur-xl p-10 rounded-3xl border border-white/50 shadow-lg">
                  <div class="flex items-center mb-8">
                    <div class="p-3 bg-orange-500 rounded-2xl shadow-lg shadow-orange-500/20 mr-4">
                      <span class="text-2xl text-white">🔑</span>
                    </div>
                    <div>
                      <h3 class="text-2xl font-bold text-gray-800">修改登录密码</h3>
                      <p class="text-gray-500">为了您的账号安全，建议定期更换复杂的密码</p>
                    </div>
                  </div>

                  <form @submit.prevent="handlePasswordChange" class="space-y-6 max-w-2xl">
                    <div class="space-y-2">
                      <label for="oldPassword" class="block text-sm font-semibold text-gray-600 ml-1">当前密码</label>
                      <div class="relative group">
                        <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 group-focus-within:text-orange-500 transition-colors">🔒</span>
                        <input type="password" id="oldPassword" v-model="passwordForm.oldPassword" placeholder="请输入当前使用的密码"
                          class="block w-full pl-12 pr-4 py-4 border border-gray-100 rounded-2xl shadow-sm focus:ring-2 focus:ring-orange-500 focus:border-transparent text-lg bg-white/80 backdrop-blur-sm transition-all outline-none">
                      </div>
                      <p v-if="passwordErrors.oldPassword" class="text-xs text-red-500 ml-1 animate-pulse">{{ passwordErrors.oldPassword }}</p>
                    </div>

                    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div class="space-y-2">
                        <label for="newPassword" class="block text-sm font-semibold text-gray-600 ml-1">新密码</label>
                        <div class="relative group">
                          <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 group-focus-within:text-orange-500 transition-colors">✨</span>
                          <input type="password" id="newPassword" v-model="passwordForm.newPassword" placeholder="设置新密码"
                            class="block w-full pl-12 pr-4 py-4 border border-gray-100 rounded-2xl shadow-sm focus:ring-2 focus:ring-orange-500 focus:border-transparent text-lg bg-white/80 backdrop-blur-sm transition-all outline-none">
                        </div>
                        <p v-if="passwordErrors.newPassword" class="text-xs text-red-500 ml-1">{{ passwordErrors.newPassword }}</p>
                      </div>

                      <div class="space-y-2">
                        <label for="confirmNewPassword" class="block text-sm font-semibold text-gray-600 ml-1">确认新密码</label>
                        <div class="relative group">
                          <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 group-focus-within:text-orange-500 transition-colors">✅</span>
                          <input type="password" id="confirmNewPassword" v-model="passwordForm.confirmNewPassword" placeholder="再次输入新密码"
                            class="block w-full pl-12 pr-4 py-4 border border-gray-100 rounded-2xl shadow-sm focus:ring-2 focus:ring-orange-500 focus:border-transparent text-lg bg-white/80 backdrop-blur-sm transition-all outline-none">
                        </div>
                        <p v-if="passwordErrors.confirmNewPassword" class="text-xs text-red-500 ml-1">{{ passwordErrors.confirmNewPassword }}</p>
                      </div>
                    </div>

                    <div class="flex justify-start space-x-4 pt-4">
                      <button type="submit"
                        @mousemove="handlePwdSaveMouseMove" @mouseleave="handlePwdSaveMouseLeave"
                        class="cta-btn-enter relative isolate overflow-hidden group px-12 py-4 rounded-full text-white text-lg font-bold transition-all duration-300 ease-out focus:outline-none focus:ring-2 focus:ring-orange-500 focus:ring-offset-2 shadow-xl shadow-orange-500/20">
                        <span class="absolute inset-0 bg-gradient-to-r from-orange-500 via-red-500 to-orange-600 transition-transform duration-300 ease-out group-hover:scale-105"></span>
                        <span class="absolute -inset-px rounded-full bg-gradient-to-r from-orange-400 via-pink-400 to-purple-500 opacity-0 transition-opacity duration-500 group-hover:opacity-100"
                              :style="pwdSaveGlowStyle"></span>
                        <span class="relative z-10 flex items-center justify-center">
                          <span>更新密码</span>
                          <span class="ml-2 transform transition-transform duration-300 ease-out group-hover:translate-x-1">
                            <el-icon><ArrowRight /></el-icon>
                          </span>
                        </span>
                      </button>
                      <button type="button" @click="resetPasswordForm"
                        @mousemove="handlePwdCancelMouseMove" @mouseleave="handlePwdCancelMouseLeave"
                        class="cta-btn-enter relative isolate overflow-hidden group px-8 py-4 rounded-full text-gray-600 text-lg font-medium transition-all duration-300 ease-out hover:text-white">
                        <span class="absolute inset-0 bg-transparent border-2 border-gray-200 group-hover:bg-slate-500 group-hover:border-transparent rounded-full transition-all duration-300"></span>
                        <span class="absolute -inset-px rounded-full bg-gradient-to-r from-slate-400 via-gray-400 to-slate-500 opacity-0 transition-opacity duration-500 group-hover:opacity-100"
                              :style="pwdCancelGlowStyle"></span>
                        <span class="relative z-10">重置表单</span>
                      </button>
                    </div>
                  </form>
                </div>




              </div>
            </template>
            <template v-else-if="activeTabId === 'data-management'">
              <div class="w-full space-y-8">
                <h2 class="text-2xl font-bold text-gray-800">数据管理</h2>

                <!-- Data Overview Cards -->
                <div class="grid grid-cols-3 gap-6">
                  <div class="bg-white/60 backdrop-blur-sm p-6 rounded-xl border border-white/50 shadow-sm text-center">
                    <span class="text-3xl">📊</span>
                    <p class="mt-2 text-sm text-gray-500">识别记录</p>
                    <p class="text-2xl font-bold text-gray-800 mt-1">128 条</p>
                  </div>
                  <div class="bg-white/60 backdrop-blur-sm p-6 rounded-xl border border-white/50 shadow-sm text-center">
                    <span class="text-3xl">👥</span>
                    <p class="mt-2 text-sm text-gray-500">人员档案</p>
                    <p class="text-2xl font-bold text-gray-800 mt-1">42 份</p>
                  </div>
                  <div class="bg-white/60 backdrop-blur-sm p-6 rounded-xl border border-white/50 shadow-sm text-center">
                    <span class="text-3xl">💾</span>
                    <p class="mt-2 text-sm text-gray-500">存储占用</p>
                    <p class="text-2xl font-bold text-gray-800 mt-1">2.4 GB</p>
                  </div>
                </div>

                <!-- Restore Data Section -->
                <div class="bg-white/60 backdrop-blur-sm p-8 rounded-xl border border-white/50 shadow-sm">
                  <div class="flex items-start space-x-4">
                    <span class="text-4xl">🔄</span>
                    <div class="flex-grow">
                      <h3 class="text-xl font-semibold text-gray-800">恢复数据</h3>
                      <p class="text-gray-500 mt-1">将系统数据恢复到初始状态。此操作不可撤销，请谨慎执行。</p>
                    </div>
                  </div>

                  <!-- Warning Alert -->
                  <div class="mt-6 bg-amber-50 border-l-4 border-amber-400 p-4 rounded-r-lg">
                    <div class="flex items-center">
                      <span class="text-xl mr-2">⚠️</span>
                      <p class="text-amber-800 font-medium">警告：恢复数据将清除所有当前数据并恢复到初始状态。</p>
                    </div>
                  </div>

                  <!-- Confirmation checkbox -->
                  <label class="flex items-center mt-6 cursor-pointer select-none">
                    <input type="checkbox" v-model="restoreConfirmed" class="w-5 h-5 text-orange-500 rounded border-gray-300 focus:ring-orange-500 mr-3" />
                    <span class="text-gray-700">我已了解此操作的风险，确认恢复数据</span>
                  </label>

                  <!-- Restore Button -->
                  <div class="mt-6 flex items-center space-x-4">
                    <button @click="handleRestoreData"
                      :disabled="!restoreConfirmed || isRestoring"
                      @mousemove="handleRestoreMouseMove" @mouseleave="handleRestoreMouseLeave"
                      class="cta-btn-enter relative isolate overflow-hidden group px-10 py-4 rounded-full text-white text-lg font-medium transition-all duration-300 ease-out focus:outline-none focus:ring-2 focus:ring-orange-500 focus:ring-offset-2 shadow-lg shadow-orange-500/30 disabled:opacity-50 disabled:cursor-not-allowed disabled:shadow-none">
                      <span class="absolute inset-0 bg-gradient-to-r from-orange-500 to-red-600 transition-transform duration-300 ease-out group-hover:scale-105"></span>
                      <span class="absolute -inset-px rounded-full bg-gradient-to-r from-orange-400 via-pink-400 to-purple-500 opacity-0 transition-opacity duration-500 group-hover:opacity-100"
                            :style="restoreGlowStyle"></span>
                      <span class="relative z-10 flex items-center justify-center">
                        <span v-if="isRestoring" class="flex items-center">
                          <svg class="animate-spin -ml-1 mr-2 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path></svg>
                          恢复中...
                        </span>
                        <span v-else>恢复数据</span>
                        <span v-if="!isRestoring" class="ml-2 transform transition-transform duration-300 ease-out group-hover:translate-x-1">
                          <el-icon><ArrowRight /></el-icon>
                        </span>
                        <span class="absolute -bottom-1 left-0 w-full h-0.5 bg-transparent transform scale-x-0 group-hover:scale-x-100 transition-transform duration-300 ease-out origin-left"></span>
                      </span>
                    </button>

                    <!-- Success / Error Message -->
                    <transition name="fade">
                      <span v-if="restoreMessage" class="text-lg font-medium" :class="restoreSuccess ? 'text-emerald-600' : 'text-red-600'">
                        {{ restoreMessage }}
                      </span>
                    </transition>
                  </div>
                </div>
              </div>
            </template>
            <template v-else>
              <h3>{{ currentTabContent }}</h3>
            </template>
          </div>

          <!-- 右侧装饰性小盒子 -->
          <div class="flex-grow pl-4 flex flex-col space-y-6">
            <!-- 盒子 1: 未来实验室 (装饰性) -->
            <div class="flex-none aspect-square relative overflow-hidden rounded-2xl group cursor-default shadow-soft-blue border border-white/40">
              <img src="/images/tech_bg.png" class="absolute inset-0 w-full h-full object-cover transition-transform duration-700 group-hover:scale-110" alt="tech-bg">
              <div class="absolute inset-0 bg-gradient-to-t from-slate-900/80 via-transparent to-transparent"></div>
              <div class="absolute inset-0 backdrop-blur-[2px] group-hover:backdrop-blur-none transition-all duration-500"></div>
              <div class="absolute bottom-6 left-6 right-6">
                <div class="flex items-center space-x-2 mb-2">
                  <span class="w-2 h-2 bg-orange-400 rounded-full animate-pulse"></span>
                  <span class="text-xs font-bold text-orange-200 tracking-widest uppercase">Future Lab</span>
                </div>
                <h4 class="text-xl font-bold text-white mb-1">未来科技 · 智领未来</h4>
                <p class="text-white/70 text-sm">跨时域生物识别核心驱动中...</p>
              </div>
            </div>

            <!-- 盒子 2: 安全脉动 (装饰性) -->
            <div class="flex-none aspect-square relative overflow-hidden rounded-2xl bg-gradient-to-br from-orange-50/50 to-blue-50/50 p-6 shadow-soft-blue border border-white/60 flex flex-col justify-center items-center group">
              <div class="relative w-24 h-24 mb-6">
                <!-- 呼吸光效底层 -->
                <div class="absolute inset-0 bg-orange-200 rounded-full blur-2xl opacity-40 animate-pulse"></div>
                <img src="/images/security_icon.png" class="relative z-10 w-full h-full object-contain transition-all duration-500 group-hover:rotate-12 group-hover:scale-110" alt="security-icon">
              </div>
              <div class="text-center">
                <h4 class="text-lg font-bold text-slate-800 mb-2">账号防御系统已开启</h4>
                <p class="text-slate-500 text-sm italic px-4">"守护每一份信任，安全就在指尖。"</p>
              </div>
              <!-- 底部点缀 -->
              <div class="absolute bottom-4 flex space-x-1">
                <div v-for="i in 3" :key="i" class="w-1.5 h-1.5 rounded-full bg-orange-300/60" :style="{animationDelay: i*0.2 + 's'}" :class="'animate-bounce'"></div>
              </div>
            </div>
          </div>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { ArrowRight } from '@element-plus/icons-vue';
import { restoreData, getUserProfile, updatePassword } from '@/services/modules/user.js';
import { logout } from '@/services/modules/auth.js';
import { ElMessageBox, ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';

const navItemsData = [
            { id: 'user-info', label: '用户信息', icon: '👤', content: '', badge: '去完成资料' },
            { id: 'account-security', label: '账号安全', icon: '🛡️', content: '这是账号安全页面的详细内容。', badge: '高风险' },
            { id: 'data-management', label: '数据管理', icon: '🗄️', content: '这是数据管理页面的详细内容。' },
];

const activeTabId = ref(navItemsData[0].id); // 默认激活第一个

const setActiveTab = (id) => {
  activeTabId.value = id;
};

const currentTabContent = computed(() => {
  const activeItem = navItemsData.find(item => item.id === activeTabId.value);
  return activeItem ? activeItem.content : '';
});

const inactiveTabs = computed(() => {
  return navItemsData.filter(item => item.id !== activeTabId.value);
});

const router = useRouter();

// 用户信息模块逻辑
const currentUser = ref({
  image: 'https://api.dicebear.com/7.x/avataaars/svg?seed=user_profile', 
  username: '----',
  permission: 'USER',
  userId: '-',
  email: 'Not specified',
  phone: 'Not specified',
  joinDate: new Date().toISOString().split('T')[0],
});

const fetchUserProfile = async () => {
  try {
    const res = await getUserProfile();
    const data = res?.data || res;
    if (data) {
      currentUser.value = {
        ...currentUser.value, // Keep existing avatar/joinDate if missing in API
        userId: data.id,
        username: data.username,
        permission: data.role === 'ADMIN' ? '管理员' : '普通用户',
        // Optional placeholder values for missing backend fields
        email: data.username + '@email.com',
      };
      
      tempUser.value = { ...currentUser.value };
      lastUpdateTime.value = new Date().toLocaleString();
    }
  } catch (error) {
    console.error('Failed to fetch profile', error);
  }
};

onMounted(() => {
  fetchUserProfile();
});

const lastUpdateTime = ref(new Date().toLocaleString()); // 新增：数据更新时间戳

const tempUser = ref({
  username: currentUser.value.username,
  permission: currentUser.value.permission,
  email: currentUser.value.email,
  phone: currentUser.value.phone,
});

const handleLogout = async () => {
  try {
    await logout();
  } catch (e) {
    // Ignore error, force redirect
  }
  localStorage.removeItem('X-Auth-Token');
  ElMessage.success('已退出登录');
  router.push('/login');
};
const isEditing = ref(false);
const glowStyle = ref({});
const saveGlowStyle = ref({});
const cancelGlowStyle = ref({});
const pwdSaveGlowStyle = ref({});
const pwdCancelGlowStyle = ref({});

const createGlowHandler = (styleRef) => ({
  move: (e) => {
    const rect = e.currentTarget.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    styleRef.value = {
      background: `radial-gradient(circle at ${x}px ${y}px, rgba(255, 255, 255, 0.4) 0%, rgba(255, 255, 255, 0) 70%)`
    };
  },
  leave: () => { styleRef.value = {}; }
});

const editGlow = createGlowHandler(glowStyle);
const saveGlow = createGlowHandler(saveGlowStyle);
const cancelGlow = createGlowHandler(cancelGlowStyle);
const pwdSaveGlow = createGlowHandler(pwdSaveGlowStyle);
const pwdCancelGlow = createGlowHandler(pwdCancelGlowStyle);

const handleEditButtonMouseMove = (e) => editGlow.move(e);
const handleEditButtonMouseLeave = () => editGlow.leave();
const handleSaveButtonMouseMove = (e) => saveGlow.move(e);
const handleSaveButtonMouseLeave = () => saveGlow.leave();
const handleCancelButtonMouseMove = (e) => cancelGlow.move(e);
const handleCancelButtonMouseLeave = () => cancelGlow.leave();
const handlePwdSaveMouseMove = (e) => pwdSaveGlow.move(e);
const handlePwdSaveMouseLeave = () => pwdSaveGlow.leave();
const handlePwdCancelMouseMove = (e) => pwdCancelGlow.move(e);
const handlePwdCancelMouseLeave = () => pwdCancelGlow.leave();

// 数据管理模块逻辑
const restoreGlowStyle = ref({});
const restoreGlow = createGlowHandler(restoreGlowStyle);
const handleRestoreMouseMove = (e) => restoreGlow.move(e);
const handleRestoreMouseLeave = () => restoreGlow.leave();

const restoreConfirmed = ref(false);
const isRestoring = ref(false);
const restoreMessage = ref('');
const restoreSuccess = ref(false);

const handleRestoreData = async () => {
  if (!restoreConfirmed.value || isRestoring.value) return;

  try {
    await ElMessageBox.confirm(
      '此操作将恢复数据到初始状态，不可撤销。是否继续？',
      '确认恢复',
      {
        confirmButtonText: '确认恢复',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
  } catch {
    return; // User cancelled
  }

  isRestoring.value = true;
  restoreMessage.value = '';

  try {
    await restoreData();
    restoreSuccess.value = true;
    restoreMessage.value = '✅ 数据恢复成功！';
    ElMessage.success('数据恢复成功');
  } catch (error) {
    restoreSuccess.value = false;
    restoreMessage.value = '❌ 恢复失败，请重试';
  } finally {
    isRestoring.value = false;
    restoreConfirmed.value = false;
    setTimeout(() => { restoreMessage.value = ''; }, 5000);
  }
};

// 账号安全模块逻辑
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmNewPassword: '',
});

const securitySettings = ref({
  loginNotify: true,
  twoFactorEnabled: false,
});

const passwordErrors = ref({
  oldPassword: '',
  newPassword: '',
  confirmNewPassword: '',
});


const loginHistory = ref([
    { time: '2023-10-26 14:30', ip: '192.168.1.1', location: '上海市' },
    { time: '2023-10-25 09:15', ip: '10.0.0.5', location: '北京市' },
    { time: '2023-10-24 20:00', ip: '172.16.0.10', location: '深圳市' },
  ]);

  const securityScore = ref(85);
  const securitySuggestions = ref([
    '开启二次验证 (未开启)',
    '定期更换复杂密码',
    '绑定密保手机',
  ]);

const startEdit = () => {
  tempUser.value = { ...currentUser.value }; // 复制当前用户数据到临时数据
  isEditing.value = true;
};

    // 保存用户更改
    const saveUserChanges = () => {
      currentUser.value = { ...currentUser.value, ...tempUser.value }; // 保存临时数据到当前用户数据
      lastUpdateTime.value = new Date().toLocaleString(); // 更新时间戳
      isEditing.value = false;
      // 这里可以添加实际的API调用来保存用户数据
      alert('用户信息已保存！');
    };

    // 重置用户更改
    const resetUserChanges = () => {
      tempUser.value = { ...currentUser.value };
      isEditing.value = false;
    };

    // 处理密码修改
    const handlePasswordChange = async () => {
      // 清空之前的错误信息
      passwordErrors.value = {
        oldPassword: '',
        newPassword: '',
        confirmNewPassword: '',
      };

      let hasError = false;

      // 验证旧密码
      if (!passwordForm.value.oldPassword) {
        passwordErrors.value.oldPassword = '请输入旧密码';
        hasError = true;
      }

      // 验证新密码
      if (!passwordForm.value.newPassword) {
        passwordErrors.value.newPassword = '请输入新密码';
        hasError = true;
      } else if (passwordForm.value.newPassword.length < 6) {
        passwordErrors.value.newPassword = '新密码长度不能少于6位';
        hasError = true;
      }

      // 验证确认密码
      if (!passwordForm.value.confirmNewPassword) {
        passwordErrors.value.confirmNewPassword = '请确认新密码';
        hasError = true;
      } else if (passwordForm.value.newPassword !== passwordForm.value.confirmNewPassword) {
        passwordErrors.value.confirmNewPassword = '两次输入的新密码不一致';
        hasError = true;
      }

      if (hasError) {
        return; // 如果有错误，停止提交
      }

      // API Call
      try {
        await updatePassword({
          oldPassword: passwordForm.value.oldPassword,
          newPassword: passwordForm.value.newPassword
        });
        ElMessage.success('密码修改成功！');
        
        // 清空表单
        resetPasswordForm();
      } catch (e) {
        ElMessage.error(e.message || '原密码错误或修改失败');
      }
    };

    const resetPasswordForm = () => {
      passwordForm.value.oldPassword = '';
      passwordForm.value.newPassword = '';
      passwordForm.value.confirmNewPassword = '';
      passwordErrors.value = {
        oldPassword: '',
        newPassword: '',
        confirmNewPassword: '',
      };
    };

// 个人中心逻辑
</script>

<style scoped>
/* Slide-in blur entrance animation */
@keyframes ctaSlideIn {
  0% {
    opacity: 0;
    filter: blur(8px);
    transform: translateY(16px);
  }
  100% {
    opacity: 1;
    filter: blur(0);
    transform: translateY(0);
  }
}
.cta-btn-enter {
  animation: ctaSlideIn 0.5s cubic-bezier(0.22, 1, 0.36, 1) both;
}

/* Fade transition for messages */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.4s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

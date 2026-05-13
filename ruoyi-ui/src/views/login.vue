<template>
  <div class="login">
    <div class="login-shell">
      <section class="login-brand">
        <p class="brand-tag">TEACHING PLATFORM</p>
        <h1 class="brand-title">{{ title }}</h1>
        <p class="brand-desc">一站式智能教务管理平台</p>
      </section>
      <section class="login-panel">
        <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
          <h3 class="title">账号登录</h3>
          <p class="sub-title">欢迎回来，请输入账号信息</p>
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              type="text"
              auto-complete="off"
              placeholder="账号"
            >
              <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon" />
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              auto-complete="off"
              placeholder="密码"
              @keyup.enter.native="handleLogin"
            >
              <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
            </el-input>
          </el-form-item>
          <el-form-item prop="code" v-if="captchaEnabled" class="captcha-row">
            <el-input
              v-model="loginForm.code"
              auto-complete="off"
              placeholder="验证码"
              class="captcha-input"
              @keyup.enter.native="handleLogin"
            >
              <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
            </el-input>
            <div class="login-code">
              <img :src="codeUrl" @click="getCode" class="login-code-img"/>
            </div>
          </el-form-item>
          <div class="login-options">
            <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
            <router-link v-if="register" class="link-type" :to="'/register'">立即注册</router-link>
          </div>
          <el-form-item class="action-row">
            <el-button
              :loading="loading"
              size="medium"
              type="primary"
              class="login-btn"
              @click.native.prevent="handleLogin"
            >
              <span v-if="!loading">登 录</span>
              <span v-else>登 录 中...</span>
            </el-button>
          </el-form-item>
        </el-form>
      </section>
    </div>
    <!--  底部  -->
    <div class="el-login-footer">
      <span>{{ footerContent }}</span>
    </div>
  </div>
</template>

<script>
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from '@/utils/jsencrypt'
import defaultSettings from '@/settings'

export default {
  name: "Login",
  data() {
    return {
      title: process.env.VUE_APP_TITLE,
      footerContent: defaultSettings.footerContent,
      codeUrl: "",
      loginForm: {
        username: "admin",
        password: "admin123",
        rememberMe: false,
        code: "",
        uuid: ""
      },
      loginRules: {
        username: [
          { required: true, trigger: "blur", message: "请输入您的账号" }
        ],
        password: [
          { required: true, trigger: "blur", message: "请输入您的密码" }
        ],
        code: [{ required: true, trigger: "change", message: "请输入验证码" }]
      },
      loading: false,
      // 验证码开关
      captchaEnabled: true,
      // 注册开关
      register: false,
      redirect: undefined
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  created() {
    this.getCode()
    this.getCookie()
  },
  methods: {
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
        if (this.captchaEnabled) {
          this.codeUrl = "data:image/gif;base64," + res.img
          this.loginForm.uuid = res.uuid
        }
      })
    },
    getCookie() {
      const username = Cookies.get("username")
      const password = Cookies.get("password")
      const rememberMe = Cookies.get('rememberMe')
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      }
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, { expires: 30 })
            Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 })
            Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 })
          } else {
            Cookies.remove("username")
            Cookies.remove("password")
            Cookies.remove('rememberMe')
          }
          this.$store.dispatch("Login", this.loginForm).then(() => {
            const getters = this.$store.getters || {}
            const roles = getters.roles || []
            const identity = getters.identity
            const isStudent = roles.includes('student') || identity === 'student'
            const isTeacher = roles.includes('teacher') || identity === 'teacher'
            const defaultPath = isStudent ? "/studentIndex" : (isTeacher ? "/teacherIndex" : "/dashboard")
            this.$router.push({ path: this.redirect || defaultPath }).catch(() => {})
          }).catch(() => {
            this.loading = false
            if (this.captchaEnabled) {
              this.getCode()
            }
          })
        }
      })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.login {
  display: grid;
  place-items: center;
  justify-content: center;
  align-items: center;
  height: 100%;
  min-height: 100vh;
  padding: 28px 16px 62px;
  background:
    radial-gradient(circle at 10% 15%, rgba(34, 197, 94, 0.15), transparent 42%),
    radial-gradient(circle at 86% 80%, rgba(14, 165, 233, 0.2), transparent 45%),
    linear-gradient(140deg, #0f172a 0%, #1e3a5f 48%, #0b4a6f 100%);
}

.login-shell {
  width: min(960px, 100%);
  min-height: 560px;
  border-radius: 24px;
  overflow: hidden;
  border: 1px solid #dfe6f0;
  background: #ffffff;
  box-shadow: 0 24px 64px rgba(15, 23, 42, 0.12);
  display: grid;
  grid-template-columns: 1.05fr 1fr;
}

.login-brand {
  padding: 56px 48px;
  color: #f8fafc;
  background:
    linear-gradient(145deg, rgba(9, 31, 49, 0.95) 0%, rgba(28, 88, 123, 0.92) 55%, rgba(11, 74, 111, 0.9) 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.brand-tag {
  margin: 0 0 18px;
  font-size: 12px;
  letter-spacing: 2.4px;
  font-weight: 700;
  opacity: 0.8;
}

.brand-title {
  margin: 0;
  font-size: 40px;
  line-height: 1.12;
  letter-spacing: 1px;
  font-weight: 800;
}

.brand-desc {
  margin: 20px 0 0;
  max-width: 320px;
  font-size: 16px;
  line-height: 1.75;
  opacity: 0.9;
}

.login-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #f8fbff 0%, #edf4fb 100%);
}
.title {
  margin: 0 0 10px;
  text-align: left;
  color: #0f172a;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.login-form {
  width: min(400px, 100%);
  padding: 28px 34px 24px;
  z-index: 1;
}

.sub-title {
  margin: 0 0 26px;
  color: #64748b;
  font-size: 14px;
}

.login-form ::v-deep .el-input__inner {
  height: 44px;
  border-radius: 10px;
  border-color: #d3dbe7;
  background: #ffffff;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.login-form ::v-deep .el-input__inner:focus {
  border-color: #1d4ed8;
  box-shadow: 0 0 0 3px rgba(29, 78, 216, 0.12);
}

.captcha-row ::v-deep .el-form-item__content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.captcha-input {
  flex: 1;
}

.login-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 2px 0 22px;
}

.login-options .link-type {
  color: #1d4ed8;
  font-size: 13px;
}

.action-row {
  margin-bottom: 0;
}

.login-btn {
  width: 100%;
  height: 44px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, #1d4ed8 0%, #2563eb 100%);
  border-color: #1d4ed8;
}

.login-form .el-input {
  height: 44px;
  input {
    height: 44px;
  }
}

.input-icon {
  height: 44px;
  width: 16px;
  margin-left: 2px;
}

.login-code {
  width: 118px;
  height: 44px;
  flex-shrink: 0;
  img {
    cursor: pointer;
    vertical-align: middle;
    border-radius: 10px;
    border: 1px solid #d3dbe7;
  }
}

.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #64748b;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}
.login-code-img {
  height: 44px;
  width: 118px;
  object-fit: cover;
}

@media (max-width: 920px) {
  .login-shell {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .login-brand {
    padding: 30px 28px 26px;
  }

  .brand-title {
    font-size: 30px;
  }

  .brand-desc {
    margin-top: 14px;
    font-size: 14px;
    line-height: 1.65;
    max-width: none;
  }

  .login-panel {
    padding: 6px 0;
  }
}

@media (max-width: 480px) {
  .login {
    padding: 10px 10px 62px;
  }

  .login-form {
    padding: 22px 18px 18px;
  }

  .title {
    font-size: 24px;
  }

  .captcha-row ::v-deep .el-form-item__content {
    gap: 8px;
  }

  .login-code,
  .login-code-img {
    width: 106px;
  }
}
</style>

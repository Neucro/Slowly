<template>
  <div class="page-wrapper">
    <div class="container">
      <div class="header">
        <transition name="fade">
          <i class="el-icon-back back-to-email"
             v-show="showSecondStage"
             @click="backToEmail"></i>
        </transition>
        <a href="https://www.getslowly.com/en/"
           class="title-link">
          <span class="title">Slowly</span>
        </a>
        <locale-switch class="locale-list" />
      </div>
      <div class="form-wrapper">
        <transition-group :name="fadeName"
                          mode="out-in">
          <div class="content-wrapper"
               :key="1"
               v-show="!showSecondStage">
            <email-input v-model="email"
                         ref="emailInput" />
            <el-button class="login-button"
                       type="primary"
                       icon="el-icon-message"
                       v-loading.fullscreen.lock="fullscreenLoading"
                       @click.native="sendEmail(true)">{{$t('login_with_email')}}</el-button>
          </div>
          <div class="content-wrapper"
               :key="2"
               v-show="showSecondStage">
            <el-input v-model="passcode"
                      spellcheck="false"
                      :show-password="this.showPassword"
                      :placeholder="this.showPassword ? $t('input_password') : $t('input_code')" />
            <el-button class="login-button"
                       type="primary"
                       icon="el-icon-message"
                       v-loading.fullscreen.lock="fullscreenLoading"
                       @click.native="login">{{$t('sign_in')}}</el-button>
            <div @click="sendEmail(false)"
                 class="btn-send-passcode"
                 v-show="showPassword">{{$t('send_passcode')}}</div>
          </div>
        </transition-group>
      </div>
    </div>
    <version />
  </div>
</template>
<style lang="stylus" scoped>
.night-mode
  .page-wrapper
    background rgb(12, 11, 9)
    .title
      color rgb(75, 110, 130)
      text-shadow none
    .locale-list
      color rgb(75, 110, 130)
.page-wrapper
  overflow-x hidden
  position absolute
  top 0
  left 0
  right 0
  bottom 0
.container
  width 50%
  margin 10% auto 0 auto
.header
  margin-bottom 40px
  position relative
.title-link
  text-decoration none
.title
  color #66b1ff
  text-shadow 2px 2px 8px #66b1ff
  font-size 30px
.back-to-email
  display inline-block
  font-size 26px
  color #66b1ff
  position absolute
  margin-top 7px
  margin-left -50px
  cursor pointer
.locale-list
  cursor pointer
  color #66b1ff
  float right
  margin-top 12px
.form-wrapper
  position relative
  height 200px
.content-wrapper
  position absolute
  width 100%
.login-button
  margin 30px auto 0 auto
  width 100%
.btn-send-passcode
  margin-top 10px
  color #66b1ff
  cursor pointer
  font-size 14px
.tip-text
  text-align center
  margin-top 40px
.slide-out-enter-active, .slide-out-leave-active, .slide-in-enter-active, .slide-in-leave-active
  transition all 0.4s ease
.slide-out-enter
  transform translate(-100%, 0)
  opacity 0
.slide-in-enter
  transform translate(100%, 0)
  opacity 0
.slide-out-leave-to
  transform translate(100%, 0)
  opacity 0
.slide-in-leave-to
  transform translate(-100%, 0)
  opacity 0
</style>

<script>
import { validateEmail, showError, showSuccess } from "../util"
import { sendEmailPasscode, verifyPasscode, verifyPassword } from "../api"
import { setToken, getToken } from "../persist/account"
import EmailInput from "../components/EmailInput.vue"
import Version from "../components/Version.vue"
import LocaleSwitch from "../components/common/LocaleSwitch.vue"

export default {
  data() {
    return {
      email: "",
      passcode: "",
      fullscreenLoading: false,
      showPasscode: false,
      showPassword: false
    }
  },
  components: {
    EmailInput,
    Version,
    LocaleSwitch
  },
  computed: {
    fadeName() {
      return this.showSecondStage ? "slide-in" : "slide-out"
    },
    showSecondStage() {
      return this.showPassword || this.showPasscode
    }
  },
  methods: {
    sendEmail(checkPass) {
      if (!validateEmail(this.email)) {
        showError(this, this.$t("error_email"))
        return
      }

      this.fullscreenLoading = true
      sendEmailPasscode(this.email, checkPass)
        .then(response => {
          this.fullscreenLoading = false
          if (response.data && response.data.hasPassword) {
            this.showPassword = true
            this.showPasscode = false
            this.$refs.emailInput.save()
          } else if (response.data && response.data.success) {
            showSuccess(this, `${this.$t("send_code_to")}${this.email}`)
            this.showPassword = false
            this.showPasscode = true
            this.$refs.emailInput.save()
          }
        })
        .catch(err => {
          this.fullscreenLoading = false
          this.$errorHandler(err)
        })
    },
    login() {
      if (!this.passcode) {
        showError(
          this,
          this.showPassword ? this.$t("input_password") : this.$t("input_code")
        )
        return
      }
      this.fullscreenLoading = true
      const verifyFun = this.showPassword ? verifyPassword : verifyPasscode
      verifyFun(this.email, this.passcode)
        .then(response => {
          this.fullscreenLoading = false
          if (response && response.data && response.data.token) {
            setToken(response.data.token)
            this.$router.replace({
              name: "home"
            })
          }
        })
        .catch(err => {
          this.fullscreenLoading = false
          this.$errorHandler(err)
        })
    },
    backToEmail() {
      this.showPasscode = false
      this.showPassword = false
    }
  },
  mounted() {
    if (getToken()) {
      this.$router.replace({
        name: "home"
      })
    }
  }
}
</script>

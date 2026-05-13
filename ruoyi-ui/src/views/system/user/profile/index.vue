<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="6" :xs="24">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>个人信息</span>
          </div>
          <div>
            <div class="text-center">
              <userAvatar />
            </div>
            <ul class="list-group list-group-striped">
              <li
                v-for="item in profileItems"
                :key="item.label"
                class="list-group-item"
              >
                <svg-icon :icon-class="item.icon" />{{ item.label }}
                <div class="pull-right">{{ item.value || '-' }}</div>
              </li>
            </ul>
          </div>
        </el-card>
      </el-col>
      <el-col :span="18" :xs="24">
        <el-card>
          <div slot="header" class="clearfix">
            <span>基本资料</span>
          </div>
          <el-tabs v-model="selectedTab">
            <el-tab-pane label="基本资料" name="userinfo">
              <userInfo :user="user" />
            </el-tab-pane>
            <el-tab-pane label="修改密码" name="resetPwd">
              <resetPwd />
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import userAvatar from "./userAvatar"
import userInfo from "./userInfo"
import resetPwd from "./resetPwd"
import { getUserProfile } from "@/api/system/user"

export default {
  name: "Profile",
  components: { userAvatar, userInfo, resetPwd },
  data() {
    return {
      user: {},
      studentProfile: null,
      teacherProfile: null,
      roleGroup: {},
      postGroup: {},
      selectedTab: "userinfo"
    }
  },
  computed: {
    identityType() {
      if (this.studentProfile) {
        return "student"
      }
      if (this.teacherProfile) {
        return "teacher"
      }
      return "user"
    },
    profileItems() {
      if (this.identityType === "student") {
        return this.studentProfileItems
      }
      if (this.identityType === "teacher") {
        return this.teacherProfileItems
      }
      return this.userProfileItems
    },
    studentProfileItems() {
      const student = this.studentProfile || {}
      return [
        { icon: "education", label: "身份类型", value: "学生" },
        { icon: "user", label: "学生姓名", value: student.studentName || this.user.nickName },
        { icon: "number", label: "学号", value: student.studentNo || this.user.userName },
        { icon: "tree", label: "所属院系", value: this.getDeptName(student) },
        { icon: "phone", label: "联系电话", value: student.phone || this.user.phonenumber },
        { icon: "email", label: "邮箱", value: this.user.email },
        { icon: "date", label: "入档日期", value: student.createTime || this.user.createTime }
      ]
    },
    teacherProfileItems() {
      const teacher = this.teacherProfile || {}
      return [
        { icon: "education", label: "身份类型", value: "教师" },
        { icon: "user", label: "教师姓名", value: teacher.teacherName || this.user.nickName },
        { icon: "number", label: "工号", value: teacher.teacherNo || this.user.userName },
        { icon: "tree", label: "所属院系", value: this.getDeptName(teacher) },
        { icon: "post", label: "职称", value: teacher.title },
        { icon: "skill", label: "学历", value: teacher.education },
        { icon: "email", label: "邮箱", value: this.user.email },
        { icon: "date", label: "入档日期", value: teacher.createTime || this.user.createTime }
      ]
    },
    userProfileItems() {
      return [
        { icon: "user", label: "用户名称", value: this.user.userName },
        { icon: "phone", label: "手机号码", value: this.user.phonenumber },
        { icon: "email", label: "用户邮箱", value: this.user.email },
        { icon: "tree", label: "所属部门", value: this.systemDeptText },
        { icon: "peoples", label: "所属角色", value: this.roleGroup },
        { icon: "date", label: "创建日期", value: this.user.createTime }
      ]
    },
    systemDeptText() {
      if (!this.user.dept) {
        return ""
      }
      return [this.user.dept.deptName, this.postGroup].filter(Boolean).join(" / ")
    }
  },
  created() {
    const activeTab = this.$route.params && this.$route.params.activeTab
    if (activeTab) {
      this.selectedTab = activeTab
    }
    this.getUser()
  },
  methods: {
    getUser() {
      getUserProfile().then(response => {
        this.user = response.data
        this.roleGroup = response.roleGroup
        this.postGroup = response.postGroup
        this.studentProfile = response.studentProfile
        this.teacherProfile = response.teacherProfile
      })
    },
    getDeptName(profile) {
      return profile && profile.dept ? profile.dept.deptName : ""
    }
  }
}
</script>

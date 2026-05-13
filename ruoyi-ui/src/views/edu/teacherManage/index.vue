<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="工号" prop="teacherNo">
        <el-input
          v-model="queryParams.teacherNo"
          placeholder="请输入工号"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="姓名" prop="teacherName">
        <el-input
          v-model="queryParams.teacherName"
          placeholder="请输入姓名"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="性别" prop="gender">
        <el-select v-model="queryParams.gender" placeholder="请选择性别" clearable style="width: 200px">
          <el-option label="男" value="0"></el-option>
          <el-option label="女" value="1"></el-option>
          <el-option label="未知" value="2"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="院系" prop="deptId">
        <el-select v-model="queryParams.deptId" placeholder="请选择院系" clearable style="width: 200px">
          <el-option
            v-for="item in deptList"
            :key="item.deptId"
            :label="item.deptName"
            :value="item.deptId"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['edu:teacher:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['edu:teacher:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['edu:teacher:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="teacherList" @selection-change="handleSelectionChange" class="wrap-table" border fit>
      <el-table-column type="selection" width="45" align="center" />
      <el-table-column label="工号" align="center" prop="teacherNo" min-width="100" />
      <el-table-column label="姓名" align="center" prop="teacherName" min-width="85" />
      <el-table-column label="性别" align="center" prop="gender" width="65">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_user_sex" :value="scope.row.gender" />
        </template>
      </el-table-column>
      <el-table-column label="出生日期" align="center" prop="birthday" min-width="100">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.birthday, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="学历" align="center" prop="education" min-width="90" />
      <el-table-column label="职称" align="center" prop="title" min-width="90" />
      <el-table-column label="院系" align="center" prop="dept.deptName" min-width="130" />
      <el-table-column label="账号状态" align="center" width="85">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="getUserStatus(scope.row)" />
        </template>
      </el-table-column>
      <el-table-column label="最后登录时间" align="center" min-width="130">
        <template slot-scope="scope">
          <span>{{ parseTime(getLastLoginTime(scope.row)) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template slot-scope="scope">
          <div class="table-actions">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['edu:teacher:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['edu:teacher:remove']">删除</el-button>
          <el-button size="mini" type="text" icon="el-icon-key" @click="handleResetPwd(scope.row)" v-hasPermi="['edu:teacher:resetPwd']">重置密码</el-button>
          <el-button
            size="mini"
            type="text"
            :icon="getUserStatus(scope.row) === '0' ? 'el-icon-close' : 'el-icon-check'"
            :style="getUserStatus(scope.row) === '0' ? 'color: #f56c6c' : 'color: #67c23a'"
            @click="handleChangeStatus(scope.row)"
            v-hasPermi="['edu:teacher:edit']"
          >
            {{ getUserStatus(scope.row) === '0' ? '禁用' : '启用' }}
          </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="title" :visible.sync="open" width="650px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="工号" prop="teacherNo">
          <el-input v-model="form.teacherNo" placeholder="请输入工号" />
        </el-form-item>
        <el-form-item label="姓名" prop="teacherName">
          <el-input v-model="form.teacherName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" placeholder="请选择性别">
            <el-option label="男" value="0"></el-option>
            <el-option label="女" value="1"></el-option>
            <el-option label="未知" value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="出生日期" prop="birthday">
          <el-date-picker clearable v-model="form.birthday" type="date" value-format="yyyy-MM-dd" placeholder="请选择出生日期" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="学历" prop="education">
          <el-input v-model="form.education" placeholder="请输入学历" />
        </el-form-item>
        <el-form-item label="职称" prop="title">
          <el-input v-model="form.title" placeholder="请输入职称" />
        </el-form-item>
        <el-form-item label="院系" prop="deptId">
          <el-select v-model="form.deptId" placeholder="请选择院系" style="width: 100%;">
            <el-option
              v-for="item in deptList"
              :key="item.deptId"
              :label="item.deptName"
              :value="item.deptId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="简介" prop="introduction">
          <el-input v-model="form.introduction" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listTeacher,
  getTeacher,
  addTeacher,
  updateTeacher,
  delTeacher,
  resetTeacherPwd,
  changeTeacherStatus
} from '@/api/edu/teacher'
import { listDepartment } from '@/api/edu/department'

export default {
  name: 'TeacherManage',
  dicts: ['sys_user_sex', 'sys_normal_disable'],
  data() {
    return {
      loading: true,
      ids: [],
      multiple: true,
      showSearch: true,
      total: 0,
      teacherList: [],
      title: '',
      open: false,
      deptList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        teacherNo: null,
        teacherName: null,
        gender: null,
        deptId: null
      },
      form: {},
      rules: {
        teacherNo: [
          { required: true, message: '工号不能为空', trigger: 'blur' }
        ],
        teacherName: [
          { required: true, message: '姓名不能为空', trigger: 'blur' }
        ],
        deptId: [
          { required: true, message: '院系不能为空', trigger: 'change' }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.getDeptList()
  },
  methods: {
    getList() {
      this.loading = true
      listTeacher(this.queryParams).then(response => {
        this.teacherList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    getDeptList() {
      listDepartment().then(response => {
        this.deptList = response.rows || []
      })
    },
    getUserStatus(row) {
      if (row.params && row.params.userStatus) {
        return row.params.userStatus
      }
      return '0'
    },
    getLastLoginTime(row) {
      if (row.params && row.params.lastLoginTime) {
        return row.params.lastLoginTime
      }
      return null
    },
    reset() {
      this.form = {
        teacherId: null,
        teacherNo: null,
        teacherName: null,
        gender: '0',
        birthday: null,
        education: null,
        title: null,
        deptId: null,
        introduction: null,
        remark: null
      }
      this.resetForm('form')
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.teacherId)
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '添加教师'
    },
    handleUpdate(row) {
      this.reset()
      const teacherId = row.teacherId || this.ids[0]
      getTeacher(teacherId).then(response => {
        this.form = response.data || {}
        this.open = true
        this.title = '修改教师'
      })
    },
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.teacherId != null) {
          updateTeacher(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
        } else {
          addTeacher(this.form).then(() => {
            this.$modal.msgSuccess('新增成功')
            this.open = false
            this.getList()
          })
        }
      })
    },
    handleDelete(row) {
      const teacherIds = row.teacherId || this.ids
      this.$modal.confirm('是否确认删除教师编号为"' + teacherIds + '"的数据项？').then(() => {
        return delTeacher(teacherIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('edu/teacher/export', {
        ...this.queryParams
      }, 'teacher_' + new Date().getTime() + '.xlsx')
    },
    handleResetPwd(row) {
      this.$modal.confirm('是否确认重置教师"' + row.teacherName + '"的密码？重置后密码为默认密码。').then(() => {
        return resetTeacherPwd(row.teacherId)
      }).then(() => {
        this.$modal.msgSuccess('重置成功')
      }).catch(() => {})
    },
    handleChangeStatus(row) {
      const currentStatus = this.getUserStatus(row)
      const newStatus = currentStatus === '0' ? '1' : '0'
      const actionText = newStatus === '0' ? '启用' : '禁用'
      this.$modal.confirm('是否确认' + actionText + '教师"' + row.teacherName + '"的账号？').then(() => {
        return changeTeacherStatus(row.teacherId, newStatus)
      }).then(() => {
        row.params = row.params || {}
        row.params.userStatus = newStatus
        this.$modal.msgSuccess(actionText + '成功')
      }).catch(() => {})
    },
    cancel() {
      this.open = false
      this.reset()
    }
  }
}
</script>

<style scoped>
.wrap-table ::v-deep .el-table__cell .cell {
  white-space: normal;
  word-break: break-word;
  line-height: 1.4;
}

.wrap-table ::v-deep .el-table-column--selection .cell {
  padding-left: 0;
  padding-right: 0;
  white-space: nowrap;
  text-overflow: clip;
}

.wrap-table ::v-deep .el-button--text {
  margin: 0;
  padding: 0;
}

.table-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 4px 8px;
}
</style>

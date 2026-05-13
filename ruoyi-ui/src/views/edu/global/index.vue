<template>
  <div class="app-container">
    <el-card class="box-card" style="margin-bottom: 20px;">
      <div slot="header" class="clearfix">
        <span>学期管理</span>
        <el-button
          type="primary"
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          style="float: right; margin-top: -5px;"
        >新增学期</el-button>
      </div>

      <el-form :model="queryParams" ref="queryForm" inline="true" class="demo-form-inline">
        <el-form-item label="学期标识">
          <el-input
            v-model="queryParams.semesterValue"
            placeholder="请输入学期标识"
            clearable
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="学期名称">
          <el-input
            v-model="queryParams.semesterDesc"
            placeholder="请输入学期名称"
            clearable
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        v-loading="loading"
        :data="semesterList"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="学期标识" align="center" prop="semesterValue" width="140" />
        <el-table-column label="学期名称" align="center" prop="semesterDesc" />
        <el-table-column label="状态" align="center" prop="isCurrent" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isCurrent === 1 ? 'success' : 'info'">
              {{ scope.row.isCurrent === 1 ? '当前学期' : '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="160">
          <template slot-scope="scope">
            {{ parseTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="250">
          <template slot-scope="scope">
            <el-button
              size="mini"
              icon="el-icon-edit"
              @click="handleEdit(scope.row)"
            >修改</el-button>
            <el-button
              size="mini"
              icon="el-icon-delete"
              type="danger"
              @click="handleDelete(scope.row)"
              :disabled="scope.row.isCurrent === 1"
            >删除</el-button>
            <el-button
              size="mini"
              type="success"
              icon="el-icon-check"
              @click="handleSetCurrent(scope.row)"
              :disabled="scope.row.isCurrent === 1"
            >设为当前</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        layout="total, prev, pager, next, jumper"
        @pagination="getList"
      />
    </el-card>

    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>全局设置</span>
      </div>
      <el-form :model="settingsForm" label-width="150px">
        <el-form-item label="开启选课">
          <el-switch
            v-model="settingsForm.is_select_open"
            @change="handleSelectOpenChange"
          />
          <div class="form-item-desc">控制学生是否可以进行选课操作</div>
        </el-form-item>
        <el-form-item label="教师登分">
          <el-switch
            v-model="settingsForm.is_score_entry_open"
            @change="handleScoreEntryOpenChange"
          />
          <div class="form-item-desc">控制教师是否可以进行成绩录入操作</div>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="学期标识" prop="semesterValue">
          <el-input v-model="form.semesterValue" placeholder="请输入学期标识，如：2025-2026-1" />
        </el-form-item>
        <el-form-item label="学期名称" prop="semesterDesc">
          <el-input v-model="form.semesterDesc" placeholder="请输入学期名称，如：2025-2026学年第一学期" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
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
import { getCurrentSemester, listSemester, getSemester, addSemester, updateSemester, delSemester, setCurrentSemester, getGlobalSettings, setSelectOpen, setScoreEntryOpen } from "@/api/edu/global";

export default {
  name: "GlobalControl",
  data() {
    return {
      loading: true,
      semesterList: [],
      total: 0,
      title: "",
      open: false,
      ids: [],
      queryParams: {
        pageNum: 1,
        pageSize: 5,
        semesterValue: undefined,
        semesterDesc: undefined
      },
      form: {},
      settingsForm: {
        is_select_open: false,
        is_score_entry_open: false
      },
      rules: {
        semesterValue: [
          { required: true, message: "学期标识不能为空", trigger: "blur" },
          { max: 20, message: "学期标识长度不能超过20个字符", trigger: "blur" }
        ],
        semesterDesc: [
          { required: true, message: "学期名称不能为空", trigger: "blur" },
          { max: 100, message: "学期名称长度不能超过100个字符", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
    this.loadSettings();
  },
  methods: {
    getList() {
      this.loading = true;
      listSemester(this.queryParams).then(response => {
        this.semesterList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    loadSettings() {
      getGlobalSettings().then(response => {
        this.settingsForm = response.data;
      });
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.queryParams = {
        pageNum: 1,
        pageSize: 5,
        semesterValue: undefined,
        semesterDesc: undefined
      };
      this.getList();
    },
    handleAdd() {
      this.form = {};
      this.title = "新增学期";
      this.open = true;
    },
    handleEdit(row) {
      this.form = Object.assign({}, row);
      this.title = "修改学期";
      this.open = true;
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除该学期吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delSemester(row.id).then(response => {
          if (response.code === 200) {
            this.$modal.msgSuccess("删除成功");
            this.getList();
          }
        });
      });
    },
    handleSetCurrent(row) {
      this.$modal.confirm('确认将该学期设置为当前学期吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        setCurrentSemester(row.id).then(response => {
          if (response.code === 200) {
            this.$modal.msgSuccess("设置成功");
            this.getList();
          }
        });
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateSemester(this.form).then(response => {
              if (response.code === 200) {
                this.$modal.msgSuccess("修改成功");
                this.open = false;
                this.getList();
              }
            });
          } else {
            addSemester(this.form).then(response => {
              if (response.code === 200) {
                this.$modal.msgSuccess("新增成功");
                this.open = false;
                this.getList();
              }
            });
          }
        }
      });
    },
    cancel() {
      this.open = false;
      this.form = {};
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
    },
    handleSelectOpenChange(value) {
      setSelectOpen(value).then(response => {
        if (response.code === 200) {
          this.$modal.msgSuccess("设置成功");
        } else {
          this.settingsForm.is_select_open = !value;
        }
      }).catch(() => {
        this.settingsForm.is_select_open = !value;
      });
    },
    handleScoreEntryOpenChange(value) {
      setScoreEntryOpen(value).then(response => {
        if (response.code === 200) {
          this.$modal.msgSuccess("设置成功");
        } else {
          this.settingsForm.is_score_entry_open = !value;
        }
      }).catch(() => {
        this.settingsForm.is_score_entry_open = !value;
      });
    }
  }
};
</script>

<style scoped>
.box-card {
  margin-bottom: 0;
}

.form-item-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>

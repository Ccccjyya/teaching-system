<template>
  <div class="app-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>当前学期</span>
      </div>
      <div class="current-semester">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="当前学期" class="current-label">
              <el-tag type="success" size="large">{{ currentSemester.semesterDesc }}</el-tag>
              <span class="semester-value">{{ currentSemester.semesterValue }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-button type="primary" icon="el-icon-refresh" @click="refreshCurrent">刷新</el-button>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <el-card class="box-card" style="margin-top: 20px;">
      <div slot="header" class="clearfix">
        <span>学期管理</span>
        <el-button
          type="primary"
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['edu:semester:add']"
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
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="正常" value="normal"></el-option>
            <el-option label="当前" value="active"></el-option>
          </el-select>
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
        <el-table-column label="学期标识" align="center" prop="semesterValue" />
        <el-table-column label="学期名称" align="center" prop="semesterDesc" />
        <el-table-column label="状态" align="center" prop="status">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'warning'">
              {{ scope.row.status === 'active' ? '当前学期' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" />
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template slot-scope="scope">
            <el-button
              size="mini"
              icon="el-icon-edit"
              @click="handleEdit(scope.row)"
              v-hasPermi="['edu:semester:edit']"
              :disabled="scope.row.status === 'active'"
            >修改</el-button>
            <el-button
              size="mini"
              icon="el-icon-delete"
              @click="handleDelete(scope.row)"
              v-hasPermi="['edu:semester:remove']"
              :disabled="scope.row.status === 'active'"
            >删除</el-button>
            <el-button
              size="mini"
              type="success"
              icon="el-icon-check"
              @click="handleSwitch(scope.row)"
              v-hasPermi="['edu:semester:switch']"
              :disabled="scope.row.status === 'active'"
            >设为当前</el-button>
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
    </el-card>

    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="学期标识" prop="semesterValue">
          <el-input v-model="form.semesterValue" placeholder="请输入学期标识，如：2025-2026-1" />
        </el-form-item>
        <el-form-item label="学期名称" prop="semesterDesc">
          <el-input v-model="form.semesterDesc" placeholder="请输入学期名称，如：2025-2026学年第一学期" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="学期切换确认" :visible.sync="confirmDialog" width="400px" append-to-body>
      <div class="confirm-content">
        <p class="warning-icon">⚠️</p>
        <p class="confirm-title">确认切换学期？</p>
        <p class="confirm-desc">当前学期：<span class="highlight">{{ currentSemester.semesterDesc }}</span></p>
        <p class="confirm-desc">目标学期：<span class="highlight">{{ switchTargetSemester }}</span></p>
        <p class="confirm-note">切换后，当前学期标记将改变，所有历史数据保持不变。</p>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="confirmDialog = false">取 消</el-button>
        <el-button type="primary" @click="confirmSwitch">确 认</el-button>
      </div>
    </el-dialog>

    <el-dialog title="切换结果" :visible.sync="resultDialog" width="400px" append-to-body>
      <div class="result-content" :class="switchResult.success ? 'success' : 'error'">
        <p class="result-icon">{{ switchResult.success ? '✅' : '❌' }}</p>
        <p class="result-title">{{ switchResult.success ? '切换成功' : '切换失败' }}</p>
        <p v-if="switchResult.success" class="result-detail">
          已从 <span class="highlight">{{ switchResult.oldSemester }}</span> 切换到
          <span class="highlight">{{ switchResult.newSemester }}</span>
        </p>
        <p v-if="!switchResult.success" class="result-detail error-message">
          {{ switchResult.message }}
        </p>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="closeResult">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getCurrentSemester, listSemester, getSemester, addSemester, updateSemester, delSemester, switchSemester } from "@/api/edu/semester";

export default {
  name: "Semester",
  data() {
    return {
      loading: true,
      semesterList: [],
      total: 0,
      title: "",
      open: false,
      confirmDialog: false,
      resultDialog: false,
      ids: [],
      currentSemester: {
        semesterValue: "",
        semesterDesc: ""
      },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        semesterValue: undefined,
        semesterDesc: undefined,
        status: undefined
      },
      form: {},
      switchTargetSemester: "",
      switchResult: {
        success: false,
        message: "",
        oldSemester: "",
        newSemester: ""
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
    this.loadCurrentSemester();
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
    loadCurrentSemester() {
      getCurrentSemester().then(response => {
        this.currentSemester = response.data;
      });
    },
    refreshCurrent() {
      this.loadCurrentSemester();
      this.$modal.msgSuccess("刷新成功");
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        semesterValue: undefined,
        semesterDesc: undefined,
        status: undefined
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
        }).catch(error => {
          this.$modal.msgError(error.message);
        });
      });
    },
    handleSwitch(row) {
      this.switchTargetSemester = row.semesterDesc;
      this.confirmDialog = true;
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
    confirmSwitch() {
      this.confirmDialog = false;
      const targetSemester = this.semesterList.find(s => s.semesterDesc === this.switchTargetSemester);
      if (!targetSemester) {
        this.$modal.msgError("未找到目标学期");
        return;
      }
      this.$modal.loading("正在切换学期...");
      switchSemester({ newSemester: targetSemester.semesterValue }).then(response => {
        this.$modal.closeLoading();
        this.switchResult = {
          success: true,
          message: response.data.message,
          oldSemester: response.data.oldSemester,
          newSemester: response.data.newSemester
        };
        this.resultDialog = true;
        this.loadCurrentSemester();
        this.getList();
      }).catch(error => {
        this.$modal.closeLoading();
        this.switchResult = {
          success: false,
          message: error.message || "切换失败",
          oldSemester: "",
          newSemester: ""
        };
        this.resultDialog = true;
      });
    },
    closeResult() {
      this.resultDialog = false;
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
    }
  }
};
</script>

<style scoped>
.box-card {
  margin-bottom: 0;
}

.current-semester {
  padding: 20px 0;
}

.current-label {
  font-size: 16px;
}

.semester-value {
  margin-left: 10px;
  font-size: 14px;
  color: #666;
}

.confirm-content {
  text-align: center;
  padding: 20px;
}

.warning-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.confirm-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
}

.confirm-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.highlight {
  color: #409EFF;
  font-weight: bold;
}

.confirm-note {
  margin-top: 20px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 13px;
  color: #666;
}

.result-content {
  text-align: center;
  padding: 20px;
}

.result-content.success {
  color: #67c23a;
}

.result-content.error {
  color: #f56c6c;
}

.result-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.result-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 15px;
}

.result-detail {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.error-message {
  color: #f56c6c;
}
</style>
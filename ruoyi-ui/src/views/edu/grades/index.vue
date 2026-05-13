<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true">
      <el-form-item label="学年">
        <el-select v-model="queryParams.academicYear" placeholder="请选择学年" clearable>
          <el-option 
            v-for="year in getAcademicYearOptions()" 
            :key="year.value" 
            :label="year.label" 
            :value="year.value">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="学期">
        <el-select v-model="queryParams.semester" placeholder="请选择学期" clearable>
          <el-option 
            v-for="sem in getSemesterOptions()" 
            :key="sem.value" 
            :label="sem.label" 
            :value="sem.value">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="20" class="mb8">
      <el-col :span="6">
        <el-card class="box-card">
          <div class="text-center">
            <p class="text-muted">已获总学分</p>
            <p class="num">{{ statistics.totalCredit || 0 }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <div class="text-center">
            <p class="text-muted">平均GPA</p>
            <p class="num">{{ statistics.avgGpa || '0.00' }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <div class="text-center">
            <p class="text-muted">已修课程</p>
            <p class="num">{{ statistics.totalCourses || 0 }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <div class="text-center">
            <p class="text-muted">不及格门数</p>
            <p class="num text-danger">{{ statistics.failCount || 0 }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card title="成绩分布统计" class="mb8" v-if="hasStatistics">
      <el-row :gutter="10">
        <el-col :span="4" class="text-center">
          <div class="pie-chart" :style="pieChartStyle"></div>
        </el-col>
        <el-col :span="20">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="stat-item"><span class="dot excellent"></span> 优秀（90-100）：{{ statistics.excellentCount }}门</div>
              <div class="stat-item"><span class="dot good"></span> 良好（80-89）：{{ statistics.goodCount }}门</div>
              <div class="stat-item"><span class="dot medium"></span> 中等（70-79）：{{ statistics.mediumCount }}门</div>
            </el-col>
            <el-col :span="12">
              <div class="stat-item"><span class="dot pass"></span> 及格（60-69）：{{ statistics.passCount }}门</div>
              <div class="stat-item"><span class="dot fail"></span> 不及格（<60）：{{ statistics.failCount }}门</div>
            </el-col>
          </el-row>
        </el-col>
      </el-row>
    </el-card>

    <el-table v-loading="loading" :data="gradeList" border>
      <el-table-column label="课号" align="center" prop="courseNo" width="100" />
      <el-table-column label="课程名称" align="center" prop="courseName" />
      <el-table-column label="学分" align="center" prop="credit" width="80" />
      <el-table-column label="教师" align="center" prop="teacherName" width="100" />
      <el-table-column label="学年" align="center" prop="academicYear" width="120" />
      <el-table-column label="学期" align="center" prop="semesterName" width="100" />
      <el-table-column label="平时成绩" align="center" prop="usualScore" width="100">
        <template slot-scope="scope">
          <span>{{ scope.row.usualScore || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="考试成绩" align="center" prop="examScore" width="100">
        <template slot-scope="scope">
          <span>{{ scope.row.examScore || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="总评成绩" align="center" prop="totalScore" width="100">
        <template slot-scope="scope">
          <el-tag :type="getScoreType(scope.row.totalScore)">
            {{ scope.row.totalScore || '-' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="绩点" align="center" prop="gpa" width="80">
        <template slot-scope="scope">
          <span>{{ scope.row.gpa || '-' }}</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { queryGrades } from "@/api/edu/grades";
import { listSemester } from "@/api/edu/global";

export default {
  name: "Grades",
  data() {
    return {
      loading: true,
      gradeList: [],
      statistics: {},
      semesterList: [],
      queryParams: {
        academicYear: undefined,
        semester: undefined
      }
    };
  },
  computed: {
    hasStatistics() {
      return this.statistics.totalCourses && this.statistics.totalCourses > 0;
    },
    pieChartStyle() {
      const stats = this.statistics;
      const total = stats.totalCourses || 1;
      
      const excellent = stats.excellentCount || 0;
      const good = stats.goodCount || 0;
      const medium = stats.mediumCount || 0;
      const pass = stats.passCount || 0;
      const fail = stats.failCount || 0;
      
      const excellentAngle = (excellent / total) * 360;
      const goodAngle = (good / total) * 360;
      const mediumAngle = (medium / total) * 360;
      const passAngle = (pass / total) * 360;
      const failAngle = (fail / total) * 360;
      
      let angle = 0;
      const segments = [];
      
      if (excellent > 0) {
        segments.push(`#52c41a ${angle}deg`);
        angle += excellentAngle;
        segments.push(`#52c41a ${angle}deg`);
      }
      if (good > 0) {
        segments.push(`#1890ff ${angle}deg`);
        angle += goodAngle;
        segments.push(`#1890ff ${angle}deg`);
      }
      if (medium > 0) {
        segments.push(`#faad14 ${angle}deg`);
        angle += mediumAngle;
        segments.push(`#faad14 ${angle}deg`);
      }
      if (pass > 0) {
        segments.push(`#fa8c16 ${angle}deg`);
        angle += passAngle;
        segments.push(`#fa8c16 ${angle}deg`);
      }
      if (fail > 0) {
        segments.push(`#f5222d ${angle}deg`);
        angle += failAngle;
        segments.push(`#f5222d ${angle}deg`);
      }
      
      if (segments.length === 0) {
        segments.push(`#d9d9d9 0deg`);
        segments.push(`#d9d9d9 360deg`);
      }
      
      return {
        background: `conic-gradient(${segments.join(', ')})`
      };
    }
  },
  created() {
    this.getSemesterList();
  },
  methods: {
    getSemesterList() {
      listSemester({}).then(response => {
        this.semesterList = response.rows;
      }).catch(() => {
        this.semesterList = [];
      }).finally(() => {
        // 学期列表失败也不应阻断成绩查询
        this.getList();
      });
    },
    getList() {
      this.loading = true;
      queryGrades(this.queryParams.academicYear, this.queryParams.semester).then(response => {
        const data = response.data || {};
        this.gradeList = data.grades || [];
        this.statistics = data.statistics || {};
      }).catch(() => {
        this.gradeList = [];
        this.statistics = {};
      }).finally(() => {
        this.loading = false;
      });
    },
    handleQuery() {
      this.getList();
    },
    resetQuery() {
      this.queryParams.academicYear = undefined;
      this.queryParams.semester = undefined;
      this.getList();
    },
    getScoreType(score) {
      if (!score) return 'default';
      if (score >= 90) return 'success';
      if (score >= 80) return 'primary';
      if (score >= 60) return 'warning';
      return 'danger';
    },
    getAcademicYearOptions() {
      const years = new Set();
      this.semesterList.forEach(s => {
        const parts = s.semesterValue.split('-');
        if (parts.length >= 2) {
          years.add(parts[0] + '-' + parts[1]);
        }
      });
      return Array.from(years).sort().map(year => ({ label: year, value: year }));
    },
    getSemesterOptions() {
      if (!this.queryParams.academicYear) return [];
      return this.semesterList
        .filter(s => s.semesterValue.startsWith(this.queryParams.academicYear))
        .map(s => {
          const parts = s.semesterValue.split('-');
          const sem = parts[2];
          return {
            label: sem === '1' ? '秋季学期' : sem === '2' ? '春季学期' : sem,
            value: sem
          };
        });
    }
  }
};
</script>

<style scoped>
.box-card {
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.num {
  font-size: 24px;
  font-weight: bold;
  color: #1890ff;
}
.text-danger {
  color: #f5222d;
}
.text-muted {
  font-size: 12px;
  color: #999;
}
.stat-item {
  line-height: 2;
}
.dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 8px;
}
.dot.excellent { background: #52c41a; }
.dot.good { background: #1890ff; }
.dot.medium { background: #faad14; }
.dot.pass { background: #fa8c16; }
.dot.fail { background: #f5222d; }
.pie-chart {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  margin: 0 auto;
}
</style>

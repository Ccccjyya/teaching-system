package com.ruoyi.web.controller.edu;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.edu.domain.TeacherCourseApply;
import com.ruoyi.edu.dto.DashboardStatsDTO;
import com.ruoyi.edu.mapper.CourseMapper;
import com.ruoyi.edu.mapper.StudentMapper;
import com.ruoyi.edu.mapper.TeacherCourseApplyMapper;
import com.ruoyi.edu.mapper.TeacherMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/edu/dashboard")
public class DashboardController extends BaseController {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private TeacherCourseApplyMapper teacherCourseApplyMapper;

    @GetMapping("/stats")
    @Log(title = "首页统计", businessType = BusinessType.OTHER)
    public AjaxResult getStats() {
        int studentCount = studentMapper.countAll();
        int teacherCount = teacherMapper.countAll();
        int courseCount = courseMapper.countAll();
        TeacherCourseApply pendingApply = new TeacherCourseApply();
        pendingApply.setStats("pending");
        Long applyCount = teacherCourseApplyMapper.selectTeacherCourseApplyListCount(pendingApply);
        
        DashboardStatsDTO stats = new DashboardStatsDTO();
        stats.setStudentCount(studentCount);
        stats.setTeacherCount(teacherCount);
        stats.setCourseCount(courseCount);
        stats.setApplyCount(applyCount == null ? 0 : applyCount.intValue());

        return AjaxResult.success(stats);
    }
}

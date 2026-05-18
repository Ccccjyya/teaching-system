package com.ruoyi.web.controller.edu;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.edu.domain.Course;
import com.ruoyi.edu.domain.Department;
import com.ruoyi.edu.domain.Semester;
import com.ruoyi.edu.domain.Teacher;
import com.ruoyi.edu.domain.TeacherCourseApply;
import com.ruoyi.edu.service.ICourseService;
import com.ruoyi.edu.service.IDepartmentService;
import com.ruoyi.edu.service.ISemesterService;
import com.ruoyi.edu.service.ITeacherCourseApplyService;
import com.ruoyi.edu.service.ITeacherService;
import com.ruoyi.edu.vo.TeacherCourseVO;
import com.ruoyi.edu.vo.TeacherStatsVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/edu/teacher")
public class TeacherController extends BaseController {

    @Autowired
    private ITeacherService teacherService;

    @Autowired
    private ITeacherCourseApplyService teacherCourseApplyService;

    @Autowired
    private ICourseService courseService;

    @Autowired
    private ISemesterService semesterService;

    @Autowired
    private IDepartmentService departmentService;

    @Autowired
    private com.ruoyi.system.service.ISysUserService sysUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PreAuthorize("@ss.hasPermi('edu:teacher:list')")
    @GetMapping("/list")
    public TableDataInfo list(Teacher teacher) {
        startPage();
        List<Teacher> list = teacherService.selectTeacherList(teacher);
        for (Teacher t : list) {
            SysUser sysUser = findSysUser(t);
            if (sysUser != null) {
                t.getParams().put("userStatus", sysUser.getStatus());
                t.getParams().put("lastLoginTime", sysUser.getLoginDate());
            } else {
                t.getParams().put("userStatus", "0");
                t.getParams().put("lastLoginTime", null);
            }
        }
        return getDataTable(list);
    }

    @Log(title = "教师管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('edu:teacher:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, Teacher teacher) {
        List<Teacher> list = teacherService.selectTeacherList(teacher);
        ExcelUtil<Teacher> util = new ExcelUtil<Teacher>(Teacher.class);
        util.exportExcel(response, list, "教师数据");
    }

    @PreAuthorize("@ss.hasPermi('edu:teacher:query')")
    @GetMapping(value = "/{teacherId}")
    public AjaxResult getInfo(@PathVariable Long teacherId) {
        return success(teacherService.selectTeacherByTeacherId(teacherId));
    }

    @PreAuthorize("@ss.hasPermi('edu:teacher:add')")
    @Log(title = "教师管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody Teacher teacher) {
        teacher.setCreateBy(getUsername());
        return toAjax(teacherService.insertTeacher(teacher));
    }

    @PreAuthorize("@ss.hasPermi('edu:teacher:edit')")
    @Log(title = "教师管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody Teacher teacher) {
        teacher.setUpdateBy(getUsername());
        return toAjax(teacherService.updateTeacher(teacher));
    }

    @PreAuthorize("@ss.hasPermi('edu:teacher:remove')")
    @Log(title = "教师管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{teacherIds}")
    public AjaxResult remove(@PathVariable Long[] teacherIds) {
        return toAjax(teacherService.deleteTeacherByTeacherIds(teacherIds));
    }

    private SysUser findSysUser(Teacher teacher) {
        if (teacher.getUserId() != null) {
            SysUser user = sysUserService.selectUserById(teacher.getUserId());
            if (user != null) {
                return user;
            }
        }
        if (teacher.getTeacherNo() != null) {
            return sysUserService.selectUserByUserName(teacher.getTeacherNo());
        }
        return null;
    }

    @PreAuthorize("@ss.hasPermi('edu:teacher:resetPwd')")
    @Log(title = "教师管理", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd/{teacherId}")
    public AjaxResult resetPwd(@PathVariable Long teacherId) {
        Teacher teacher = teacherService.selectTeacherByTeacherId(teacherId);
        if (teacher == null) {
            return error("教师不存在");
        }

        SysUser sysUser = findSysUser(teacher);
        if (sysUser == null) {
            return error("系统用户不存在，请先创建教师账号");
        }

        String defaultPassword = "admin123";
        sysUser.setPassword(passwordEncoder.encode(defaultPassword));
        sysUser.setUpdateBy(getUsername());
        return toAjax(sysUserService.resetPwd(sysUser));
    }

    @PreAuthorize("@ss.hasPermi('edu:teacher:edit')")
    @Log(title = "教师管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus/{teacherId}")
    public AjaxResult changeStatus(@PathVariable Long teacherId, @RequestParam String status) {
        Teacher teacher = teacherService.selectTeacherByTeacherId(teacherId);
        if (teacher == null) {
            return error("教师不存在");
        }

        SysUser sysUser = findSysUser(teacher);
        if (sysUser == null) {
            return error("系统用户不存在，请先创建教师账号");
        }

        sysUser.setStatus(status);
        sysUser.setUpdateBy(getUsername());
        return toAjax(sysUserService.updateUserStatus(sysUser));
    }

    @PreAuthorize("@ss.hasPermi('edu:teacher:query')")
    @GetMapping("/getUserInfo/{teacherId}")
    public AjaxResult getUserInfo(@PathVariable Long teacherId) {
        Teacher teacher = teacherService.selectTeacherByTeacherId(teacherId);
        if (teacher == null) {
            return error("教师不存在");
        }

        SysUser sysUser = findSysUser(teacher);
        Map<String, Object> userInfo = new HashMap<>();
        if (sysUser == null) {
            userInfo.put("status", "0");
            userInfo.put("lastLoginTime", null);
        } else {
            userInfo.put("status", sysUser.getStatus());
            userInfo.put("lastLoginTime", sysUser.getLoginDate());
        }

        return success(userInfo);
    }

    @GetMapping("/stats")
    public AjaxResult getTeacherStats() {
        Teacher teacher = teacherService.selectTeacherByUserId(getUserId());
        if (teacher == null) {
            return error("教师信息不存在");
        }
        TeacherStatsVO stats = teacherService.getTeacherStats(teacher.getTeacherId());
        return success(stats);
    }

    @GetMapping("/courses")
    public TableDataInfo getTeacherCourses(String courseNo, String courseName, Boolean onlyCurrent) {
        startPage();
        Teacher teacher = teacherService.selectTeacherByUserId(getUserId());
        if (teacher == null) {
            return getDataTable(java.util.Collections.emptyList());
        }
        List<TeacherCourseVO> courses = teacherService.getTeacherCourses(teacher.getTeacherId(), onlyCurrent, courseNo, courseName);
        return getDataTable(courses);
    }

    @Log(title = "教师授课", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasTeacherIdentity()")
    @PostMapping("/courses/export")
    public void exportCourses(HttpServletResponse response, String courseNo, String courseName, Boolean onlyCurrent) {
        Teacher teacher = teacherService.selectTeacherByUserId(getUserId());
        if (teacher != null) {
            List<TeacherCourseVO> courses = teacherService.getTeacherCourses(teacher.getTeacherId(), onlyCurrent, courseNo, courseName);
            ExcelUtil<TeacherCourseVO> util = new ExcelUtil<TeacherCourseVO>(TeacherCourseVO.class);
            util.exportExcel(response, courses, "我的授课");
        }
    }

    @GetMapping("/apply/list")
    public TableDataInfo getTeacherApplyList(String xq, String km, String stats) {
        startPage();
        Teacher teacher = teacherService.selectTeacherByUserId(getUserId());
        if (teacher == null) {
            return getDataTable(java.util.Collections.emptyList());
        }
        TeacherCourseApply apply = new TeacherCourseApply();
        apply.setGh(teacher.getTeacherNo());
        apply.setXq(xq);
        apply.setKm(km);
        apply.setStats(stats);
        List<TeacherCourseApply> list = teacherCourseApplyService.selectTeacherCourseApplyList(apply);
        return getDataTable(list);
    }

    @Log(title = "教师开课申请", businessType = BusinessType.INSERT)
    @PostMapping("/apply")
    public AjaxResult submitApply(@RequestBody Map<String, Object> requestData) {
        Teacher teacher = teacherService.selectTeacherByUserId(getUserId());
        if (teacher == null) {
            return error("教师信息不存在");
        }

        String courseType = (String) requestData.get("courseType");
        String xq = (String) requestData.get("xq");
        if (StringUtils.isBlank(xq)) {
            return error("请选择学年学期");
        }

        TeacherCourseApply apply = new TeacherCourseApply();
        apply.setGh(teacher.getTeacherNo());
        apply.setTeacherName(teacher.getTeacherName());
        apply.setXq(xq);
        apply.setRemark((String) requestData.get("remark"));
        apply.setSchedule((String) requestData.get("schedule"));
        apply.setExpectedCapacity(requestData.get("expectedCapacity") != null
                ? Integer.parseInt(requestData.get("expectedCapacity").toString()) : null);

        if ("existing".equals(courseType)) {
            Long courseId = requestData.get("courseId") != null ? Long.parseLong(requestData.get("courseId").toString()) : null;
            if (courseId == null) {
                return error("请选择课程");
            }
            Course course = courseService.selectCourseById(courseId);
            if (course == null) {
                return error("课程不存在");
            }
            apply.setCourseId(courseId);
            apply.setKm(course.getCourseName());
            apply.setXf(course.getCredit().doubleValue());
            apply.setXs(course.getHours());
            apply.setYxhId(course.getDeptId());
        } else {
            apply.setKm((String) requestData.get("km"));
            apply.setXf(requestData.get("xf") != null ? Double.parseDouble(requestData.get("xf").toString()) : null);
            apply.setXs(requestData.get("xs") != null ? Integer.parseInt(requestData.get("xs").toString()) : null);
            apply.setYxhId(requestData.get("yxhId") != null ? Long.parseLong(requestData.get("yxhId").toString()) : null);
        }

        return toAjax(teacherCourseApplyService.insertTeacherCourseApply(apply));
    }

    @GetMapping("/apply/{id}")
    public AjaxResult getApplyInfo(@PathVariable Long id) {
        Teacher teacher = teacherService.selectTeacherByUserId(getUserId());
        if (teacher == null) {
            return error("教师信息不存在");
        }
        TeacherCourseApply apply = teacherCourseApplyService.selectTeacherCourseApplyById(id);
        if (apply != null && teacher.getTeacherNo().equals(apply.getGh())) {
            return success(apply);
        }
        return error("无权查看该申请");
    }

    @Log(title = "教师开课申请", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasTeacherIdentity()")
    @PostMapping("/apply/export")
    public void exportApply(HttpServletResponse response, String xq, String km, String stats) {
        Teacher teacher = teacherService.selectTeacherByUserId(getUserId());
        if (teacher != null) {
            TeacherCourseApply apply = new TeacherCourseApply();
            apply.setGh(teacher.getTeacherNo());
            apply.setXq(xq);
            apply.setKm(km);
            apply.setStats(stats);
            List<TeacherCourseApply> list = teacherCourseApplyService.selectTeacherCourseApplyList(apply);
            ExcelUtil<TeacherCourseApply> util = new ExcelUtil<TeacherCourseApply>(TeacherCourseApply.class);
            util.exportExcel(response, list, "我的开课申请");
        }
    }

    @PreAuthorize("@ss.hasTeacherIdentity()")
    @GetMapping("/semesters")
    public TableDataInfo getTeacherSemesters(Semester semester) {
        List<Semester> list = semesterService.selectSemesterList(semester);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasTeacherIdentity()")
    @GetMapping("/departments")
    public TableDataInfo getTeacherDepartments(Department department) {
        List<Department> list = departmentService.selectDepartmentList(department);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasTeacherIdentity()")
    @GetMapping("/courses/catalog")
    public TableDataInfo getTeacherCourseCatalog(Course course) {
        List<Course> list = courseService.selectCourseList(course);
        return getDataTable(list);
    }
}

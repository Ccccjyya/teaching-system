package com.ruoyi.web.controller.edu;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.edu.domain.Student;
import com.ruoyi.edu.service.IEnrollmentService;
import com.ruoyi.edu.service.IStudentService;
import com.ruoyi.edu.vo.CourseTableVO;
import com.ruoyi.edu.vo.GradeStatisticsVO;
import com.ruoyi.edu.vo.StudentGradeVO;
import com.ruoyi.system.service.ISysRoleService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/edu/student")
public class StudentController extends BaseController {

    @Autowired
    private IEnrollmentService enrollmentService;

    @Autowired
    private IStudentService studentService;

    @Autowired
    private com.ruoyi.system.service.ISysUserService sysUserService;

    @Autowired
    private ISysRoleService sysRoleService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Student getCurrentStudent() {
        String username = SecurityUtils.getUsername();
        return studentService.selectStudentByStudentNo(username);
    }

    @PreAuthorize("@ss.hasRole('student') or @ss.hasPermi('edu:student:queryGrades')")
    @GetMapping("/grades")
    public AjaxResult queryGrades(@RequestParam(required = false) String academicYear, @RequestParam(required = false) String semester) {
        Student student = getCurrentStudent();
        if (student == null) {
            return error("未找到学生信息");
        }

        List<StudentGradeVO> grades = enrollmentService.selectStudentGrades(student.getStudentId(), academicYear, semester);
        GradeStatisticsVO statistics = enrollmentService.calculateGradeStatistics(student.getStudentId(), academicYear, semester);

        Map<String, Object> result = new HashMap<>();
        result.put("grades", grades);
        result.put("statistics", statistics);

        return success(result);
    }

    @PreAuthorize("@ss.hasRole('student') or @ss.hasPermi('edu:student:courseTable')")
    @GetMapping("/courseTable")
    public AjaxResult courseTable(@RequestParam(required = false) String academicYear, @RequestParam(required = false) String semester) {
        Student student = getCurrentStudent();
        if (student == null) {
            return error("未找到学生信息");
        }

        List<CourseTableVO> courseTable = enrollmentService.selectStudentCourseTable(student.getStudentId(), academicYear, semester);
        return success(courseTable);
    }

    @PreAuthorize("@ss.hasRole('student') or @ss.hasPermi('edu:student:courseTable')")
    @GetMapping("/courseTable/semesters")
    public AjaxResult courseTableSemesters() {
        Student student = getCurrentStudent();
        if (student == null) {
            return error("未找到学生信息");
        }

        return success(enrollmentService.selectStudentEnrollmentSemesters(student.getStudentId()));
    }

    @PreAuthorize("@ss.hasRole('student') or @ss.hasPermi('edu:student:stats')")
    @GetMapping("/stats")
    public AjaxResult getStudentStats() {
        Student student = getCurrentStudent();
        if (student == null) {
            return error("未找到学生信息");
        }

        return success(enrollmentService.getStudentStats(student.getStudentId()));
    }

    @PreAuthorize("@ss.hasPermi('edu:student:list')")
    @GetMapping("/list")
    public TableDataInfo list(Student student) {
        startPage();
        List<Student> list = studentService.selectStudentList(student);
        for (Student s : list) {
            SysUser sysUser = findSysUser(s);
            if (sysUser != null) {
                s.getParams().put("userStatus", sysUser.getStatus());
                s.getParams().put("lastLoginTime", sysUser.getLoginDate());
            } else {
                s.getParams().put("userStatus", "0");
                s.getParams().put("lastLoginTime", null);
            }
        }
        return getDataTable(list);
    }

    @Log(title = "学生管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('edu:student:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, Student student) {
        List<Student> list = studentService.selectStudentList(student);
        ExcelUtil<Student> util = new ExcelUtil<Student>(Student.class);
        util.exportExcel(response, list, "学生数据");
    }

    @PreAuthorize("@ss.hasPermi('edu:student:query')")
    @GetMapping(value = "/{studentId}")
    public AjaxResult getInfo(@PathVariable Long studentId) {
        return success(studentService.selectStudentByStudentId(studentId));
    }

    @PreAuthorize("@ss.hasPermi('edu:student:add')")
    @Log(title = "学生管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody Student student) {
        student.setCreateBy(getUsername());
        return toAjax(studentService.insertStudent(student));
    }

    @PreAuthorize("@ss.hasPermi('edu:student:edit')")
    @Log(title = "学生管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody Student student) {
        student.setUpdateBy(getUsername());
        return toAjax(studentService.updateStudent(student));
    }

    @PreAuthorize("@ss.hasPermi('edu:student:remove')")
    @Log(title = "学生管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{studentIds}")
    public AjaxResult remove(@PathVariable Long[] studentIds) {
        return toAjax(studentService.deleteStudentByStudentIds(studentIds));
    }

    private SysUser findSysUser(Student student) {
        if (student.getUserId() != null) {
            SysUser user = sysUserService.selectUserById(student.getUserId());
            if (user != null) {
                return user;
            }
        }
        if (student.getStudentNo() != null) {
            return sysUserService.selectUserByUserName(student.getStudentNo());
        }
        return null;
    }

    @PreAuthorize("@ss.hasPermi('edu:student:resetPwd')")
    @Log(title = "学生管理", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd/{studentId}")
    public AjaxResult resetPwd(@PathVariable Long studentId) {
        Student student = studentService.selectStudentByStudentId(studentId);
        if (student == null) {
            return error("学生不存在");
        }

        SysUser sysUser = findSysUser(student);
        if (sysUser == null) {
            return error("系统用户不存在，请先创建学生账号");
        }

        String defaultPassword = "admin123";
        sysUser.setPassword(passwordEncoder.encode(defaultPassword));
        sysUser.setUpdateBy(getUsername());

        return toAjax(sysUserService.updateUser(sysUser));
    }

    @PreAuthorize("@ss.hasPermi('edu:student:edit')")
    @Log(title = "学生管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus/{studentId}")
    public AjaxResult changeStatus(@PathVariable Long studentId, @RequestParam String status) {
        Student student = studentService.selectStudentByStudentId(studentId);
        if (student == null) {
            return error("学生不存在");
        }

        SysUser sysUser = findSysUser(student);
        if (sysUser == null) {
            return error("系统用户不存在，请先创建学生账号");
        }

        sysUser.setStatus(status);
        sysUser.setUpdateBy(getUsername());

        return toAjax(sysUserService.updateUser(sysUser));
    }

    @PreAuthorize("@ss.hasPermi('edu:student:query')")
    @GetMapping("/getUserInfo/{studentId}")
    public AjaxResult getUserInfo(@PathVariable Long studentId) {
        Student student = studentService.selectStudentByStudentId(studentId);
        if (student == null) {
            return error("学生不存在");
        }

        SysUser sysUser = findSysUser(student);
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

    /**
     * 学生数据一致性校验
     */
    @PreAuthorize("@ss.hasPermi('edu:student:query')")
    @GetMapping("/checkConsistency")
    public AjaxResult checkConsistency() {
        List<Student> students = studentService.selectStudentList(new Student());
        List<Map<String, Object>> issues = new ArrayList<>();
        int totalStudents = students.size();
        int issueCount = 0;

        for (Student student : students) {
            Map<String, Object> issue = new HashMap<>();
            boolean hasIssue = false;
            issue.put("studentId", student.getStudentId());
            issue.put("studentNo", student.getStudentNo());
            issue.put("studentName", student.getStudentName());

            // 检查用户是否存在
            SysUser sysUser = findSysUser(student);
            if (sysUser == null) {
                issue.put("userMissing", true);
                hasIssue = true;
                issueCount++;
            } else {
                issue.put("userMissing", false);
                // 检查用户名是否一致
                if (!sysUser.getUserName().equals(student.getStudentNo())) {
                    issue.put("userNameMismatch", true);
                    issue.put("actualUserName", sysUser.getUserName());
                    hasIssue = true;
                }
                // 检查昵称是否一致
                if (!sysUser.getNickName().equals(student.getStudentName())) {
                    issue.put("nickNameMismatch", true);
                    issue.put("actualNickName", sysUser.getNickName());
                    hasIssue = true;
                }
            }

            // 检查user_id是否为空
            if (student.getUserId() == null) {
                issue.put("userIdMissing", true);
                hasIssue = true;
            }

            if (hasIssue) {
                issues.add(issue);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalStudents", totalStudents);
        result.put("issueCount", issueCount);
        result.put("issues", issues);
        return success(result);
    }

    /**
     * 修复学生数据一致性问题
     */
    @PreAuthorize("@ss.hasPermi('edu:student:edit')")
    @Log(title = "学生管理", businessType = BusinessType.UPDATE)
    @PutMapping("/fixConsistency")
    @Transactional
    public AjaxResult fixConsistency() {
        List<Student> students = studentService.selectStudentList(new Student());
        int fixedCount = 0;
        List<String> messages = new ArrayList<>();

        for (Student student : students) {
            try {
                SysUser sysUser = findSysUser(student);
                
                // 情况1: 用户不存在，创建用户
                if (sysUser == null) {
                    // 创建系统用户
                    SysUser newUser = new SysUser();
                    newUser.setUserName(student.getStudentNo());
                    newUser.setNickName(student.getStudentName());
                    newUser.setStatus("0");
                    newUser.setCreateBy(getUsername());
                    String defaultPassword = "admin123";
                    newUser.setPassword(passwordEncoder.encode(defaultPassword));
                    
                    sysUserService.insertUser(newUser);
                    
                    // 分配学生角色
                    com.ruoyi.common.core.domain.entity.SysRole roleQuery = new com.ruoyi.common.core.domain.entity.SysRole();
                    roleQuery.setRoleKey("student");
                    List<com.ruoyi.common.core.domain.entity.SysRole> roles = sysRoleService.selectRoleList(roleQuery);
                    Long studentRoleId = roles != null && !roles.isEmpty() ? roles.get(0).getRoleId() : 3L;
                    sysUserService.insertUserAuth(newUser.getUserId(), new Long[]{studentRoleId});
                    
                    // 更新学生的user_id
                    if (student.getUserId() == null || !student.getUserId().equals(newUser.getUserId())) {
                        student.setUserId(newUser.getUserId());
                        studentService.updateStudent(student);
                    }
                    
                    messages.add("已为学生 " + student.getStudentName() + " 创建账号");
                    fixedCount++;
                }
                // 情况2: 用户存在但数据不一致
                else {
                    boolean needUpdate = false;
                    
                    // 检查并修复用户名
                    if (!sysUser.getUserName().equals(student.getStudentNo())) {
                        sysUser.setUserName(student.getStudentNo());
                        needUpdate = true;
                    }
                    
                    // 检查并修复昵称
                    if (!sysUser.getNickName().equals(student.getStudentName())) {
                        sysUser.setNickName(student.getStudentName());
                        needUpdate = true;
                    }
                    
                    // 检查并修复学生的user_id
                    if (student.getUserId() == null || !student.getUserId().equals(sysUser.getUserId())) {
                        student.setUserId(sysUser.getUserId());
                        studentService.updateStudent(student);
                    }
                    
                    if (needUpdate) {
                        sysUser.setUpdateBy(getUsername());
                        sysUserService.updateUser(sysUser);
                        messages.add("已修复学生 " + student.getStudentName() + " 的账号信息");
                        fixedCount++;
                    }
                }
            } catch (Exception e) {
                messages.add("修复学生 " + student.getStudentName() + " 时出错: " + e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("fixedCount", fixedCount);
        result.put("messages", messages);
        return success(result);
    }
}

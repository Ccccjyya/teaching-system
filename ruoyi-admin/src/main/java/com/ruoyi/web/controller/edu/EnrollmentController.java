package com.ruoyi.web.controller.edu;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.edu.domain.Enrollment;
import com.ruoyi.edu.domain.Semester;
import com.ruoyi.edu.domain.Student;
import com.ruoyi.edu.domain.Teacher;
import com.ruoyi.edu.service.IEnrollmentService;
import com.ruoyi.edu.service.IGlobalSettingService;
import com.ruoyi.edu.service.ISemesterService;
import com.ruoyi.edu.service.IStudentService;
import com.ruoyi.edu.service.ITeacherService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 选课Controller
 * 
 * @author ruoyi
 * @date 2025-05-11
 */
@RestController
@RequestMapping("/edu/enrollment")
public class EnrollmentController extends BaseController
{
    @Autowired
    private IEnrollmentService enrollmentService;

    @Autowired
    private IStudentService studentService;

    @Autowired
    private ISemesterService semesterService;

    @Autowired
    private IGlobalSettingService globalSettingService;

    @Autowired
    private ITeacherService teacherService;

    /**
     * 查询选课列表
     */
    @PreAuthorize("@ss.hasPermi('edu:enrollment:list')")
    @GetMapping("/list")
    public TableDataInfo list(Enrollment enrollment)
    {
        startPage();
        List<Enrollment> list = enrollmentService.selectEnrollmentList(enrollment);
        return getDataTable(list);
    }

    /**
     * 查询学生已选课程（当前学期）
     */
    @PreAuthorize("@ss.hasRole('student') or @ss.hasPermi('edu:enrollment:query')")
    @GetMapping("/my")
    public AjaxResult myEnrollments()
    {
        // 获取当前登录用户的用户名（学号）
        String username = SecurityUtils.getUsername();
        // 根据用户名（学号）查询学生信息
        Student student = studentService.selectStudentByStudentNo(username);
        if (student == null) {
            return error("未找到学生信息");
        }
        
        // 获取当前学期
        Semester currentSemester = semesterService.selectCurrentSemester();
        if (currentSemester == null) {
            return success(new java.util.ArrayList<>());
        }
        String currentSemesterValue = currentSemester.getSemesterValue();
        
        // 获取学生所有选课记录
        List<Enrollment> allEnrollments = enrollmentService.selectEnrollmentByStudentId(student.getStudentId());
        
        // 过滤当前学期的选课记录
        List<Enrollment> currentSemesterEnrollments = allEnrollments.stream()
            .filter(enrollment -> isCurrentSemesterEnrollment(enrollment, currentSemesterValue))
            .collect(Collectors.toList());
        
        return success(currentSemesterEnrollments);
    }

    private boolean isCurrentSemesterEnrollment(Enrollment enrollment, String currentSemesterValue)
    {
        if (enrollment == null || currentSemesterValue == null) {
            return false;
        }
        return enrollment.getCourseOffering() != null
                && currentSemesterValue.equals(enrollment.getCourseOffering().getSemester());
    }

    /**
     * 导出选课列表
     */
    @PreAuthorize("@ss.hasPermi('edu:enrollment:export') or @ss.hasTeacherIdentity()")
    @Log(title = "选课", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Enrollment enrollment)
    {
        List<Enrollment> list = enrollmentService.selectEnrollmentList(enrollment);
        ExcelUtil<Enrollment> util = new ExcelUtil<Enrollment>(Enrollment.class);
        util.exportExcel(response, list, "选课数据");
    }

    /**
     * 获取选课详细信息
     */
    @PreAuthorize("@ss.hasPermi('edu:enrollment:query')")
    @GetMapping(value = "/{enrollmentId}")
    public AjaxResult getInfo(@PathVariable("enrollmentId") Long enrollmentId)
    {
        return success(enrollmentService.selectEnrollmentByEnrollmentId(enrollmentId));
    }

    /**
     * 学生选课
     */
    @PreAuthorize("@ss.hasRole('student') or @ss.hasPermi('edu:enrollment:add')")
    @Log(title = "学生选课", businessType = BusinessType.INSERT)
    @PostMapping("/studentEnroll")
    public AjaxResult studentEnroll(@RequestBody Enrollment enrollment)
    {
        // 获取当前登录用户的用户名（学号）
        String username = SecurityUtils.getUsername();
        // 根据用户名（学号）查询学生信息
        Student student = studentService.selectStudentByStudentNo(username);
        if (student == null) {
            return error("未找到学生信息，请先完善学生档案");
        }
        // 设置学生ID
        enrollment.setStudentId(student.getStudentId());
        return toAjax(enrollmentService.studentEnroll(enrollment));
    }

    /**
     * 新增选课
     */
    @PreAuthorize("@ss.hasPermi('edu:enrollment:add')")
    @Log(title = "选课", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Enrollment enrollment)
    {
        return toAjax(enrollmentService.insertEnrollment(enrollment));
    }

    /**
     * 修改选课
     */
    @PreAuthorize("@ss.hasPermi('edu:enrollment:edit')")
    @Log(title = "选课", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Enrollment enrollment)
    {
        return toAjax(enrollmentService.updateEnrollment(enrollment));
    }

    /**
     * 学生退课
     */
    @PreAuthorize("@ss.hasRole('student') or @ss.hasPermi('edu:enrollment:remove')")
    @Log(title = "学生退课", businessType = BusinessType.DELETE)
	@DeleteMapping("/studentDrop/{enrollmentId}")
    public AjaxResult studentDrop(@PathVariable Long enrollmentId)
    {
        return toAjax(enrollmentService.deleteEnrollmentByEnrollmentId(enrollmentId));
    }

    /**
     * 删除选课
     */
    @PreAuthorize("@ss.hasPermi('edu:enrollment:remove')")
    @Log(title = "选课", businessType = BusinessType.DELETE)
	@DeleteMapping("/{enrollmentIds}")
    public AjaxResult remove(@PathVariable Long[] enrollmentIds)
    {
        return toAjax(enrollmentService.deleteEnrollmentByEnrollmentIds(enrollmentIds));
    }

    /**
     * 查询某门课的学生名单
     */
    @GetMapping("/courseStudents/{offeringId}")
    public TableDataInfo getCourseStudents(@PathVariable Long offeringId, @RequestParam(required = false) String keyword)
    {
        startPage();
        List<Enrollment> list = enrollmentService.getCourseStudents(offeringId, keyword);
        return getDataTable(list);
    }

    /**
     * 保存单个学生成绩
     */
    @Log(title = "成绩录入", businessType = BusinessType.UPDATE)
    @PutMapping("/saveScore")
    public AjaxResult saveScore(@RequestBody Map<String, Object> scoreData)
    {
        if (!globalSettingService.isScoreEntryOpen())
        {
            return AjaxResult.error("目前未开启登分");
        }
        Long enrollmentId = Long.parseLong(scoreData.get("enrollmentId").toString());
        Integer usualScore = scoreData.get("usualScore") != null ? 
            Integer.parseInt(scoreData.get("usualScore").toString()) : null;
        Integer examScore = scoreData.get("examScore") != null ? 
            Integer.parseInt(scoreData.get("examScore").toString()) : null;
        Integer totalScore = scoreData.get("totalScore") != null ? 
            Integer.parseInt(scoreData.get("totalScore").toString()) : null;
        
        return toAjax(enrollmentService.saveSingleScore(enrollmentId, usualScore, examScore, totalScore));
    }

    /**
     * 批量保存学生成绩
     */
    @Log(title = "批量成绩录入", businessType = BusinessType.UPDATE)
    @PutMapping("/saveScores")
    public AjaxResult saveScores(@RequestBody List<Map<String, Object>> scoreList)
    {
        if (!globalSettingService.isScoreEntryOpen())
        {
            return AjaxResult.error("目前未开启登分");
        }
        return toAjax(enrollmentService.saveScores(scoreList));
    }
}

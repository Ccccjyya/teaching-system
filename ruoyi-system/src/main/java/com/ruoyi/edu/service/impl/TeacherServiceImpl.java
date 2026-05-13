package com.ruoyi.edu.service.impl;

import java.util.List;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.edu.mapper.TeacherMapper;
import com.ruoyi.edu.mapper.CourseOfferingMapper;
import com.ruoyi.edu.domain.Teacher;
import com.ruoyi.edu.domain.Semester;
import com.ruoyi.edu.service.ITeacherService;
import com.ruoyi.edu.service.ISemesterService;
import com.ruoyi.edu.vo.TeacherCourseVO;
import com.ruoyi.edu.vo.TeacherStatsVO;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.service.ISysUserService;

/**
 * 教师Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-05-11
 */
@Service
public class TeacherServiceImpl implements ITeacherService 
{
    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private CourseOfferingMapper courseOfferingMapper;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private ISemesterService semesterService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEACHER_ROLE_KEY = "teacher";

    private Long getTeacherRoleId()
    {
        SysRole role = new SysRole();
        role.setRoleKey(TEACHER_ROLE_KEY);
        List<SysRole> roles = sysRoleMapper.selectRoleList(role);
        if (roles != null && !roles.isEmpty())
        {
            return roles.get(0).getRoleId();
        }
        return 101L;
    }

    /**
     * 查询教师
     * 
     * @param teacherId 教师主键
     * @return 教师
     */
    @Override
    public Teacher selectTeacherByTeacherId(Long teacherId)
    {
        return teacherMapper.selectTeacherByTeacherId(teacherId);
    }

    /**
     * 根据用户ID查询教师
     * 
     * @param userId 用户ID
     * @return 教师
     */
    @Override
    public Teacher selectTeacherByUserId(Long userId)
    {
        return teacherMapper.selectTeacherByUserId(userId);
    }

    /**
     * 查询教师列表
     * 
     * @param teacher 教师
     * @return 教师
     */
    @Override
    public List<Teacher> selectTeacherList(Teacher teacher)
    {
        return teacherMapper.selectTeacherList(teacher);
    }

    /**
     * 新增教师
     * 
     * @param teacher 教师
     * @return 结果
     */
    @Override
    @Transactional
    public int insertTeacher(Teacher teacher)
    {
        // 1. 创建系统用户
        SysUser sysUser = new SysUser();
        sysUser.setUserName(teacher.getTeacherNo());
        sysUser.setNickName(teacher.getTeacherName());
        sysUser.setStatus("0");
        sysUser.setCreateBy(teacher.getCreateBy());
        
        // 初始密码：默认密码 admin123
        String defaultPassword = "admin123";
        sysUser.setPassword(passwordEncoder.encode(defaultPassword));
        
        // 2. 插入系统用户
        sysUserService.insertUser(sysUser);
        
        // 3. 分配教师角色
        sysUserService.insertUserAuth(sysUser.getUserId(), new Long[]{getTeacherRoleId()});
        
        // 4. 设置教师的user_id
        teacher.setUserId(sysUser.getUserId());
        
        // 5. 插入教师记录
        return teacherMapper.insertTeacher(teacher);
    }

    /**
     * 修改教师
     * 
     * @param teacher 教师
     * @return 结果
     */
    @Override
    public int updateTeacher(Teacher teacher)
    {
        return teacherMapper.updateTeacher(teacher);
    }

    /**
     * 批量删除教师
     * 
     * @param teacherIds 需要删除的教师主键
     * @return 结果
     */
    @Override
    public int deleteTeacherByTeacherIds(Long[] teacherIds)
    {
        return teacherMapper.deleteTeacherByTeacherIds(teacherIds);
    }

    /**
     * 删除教师信息
     * 
     * @param teacherId 教师主键
     * @return 结果
     */
    @Override
    public int deleteTeacherByTeacherId(Long teacherId)
    {
        return teacherMapper.deleteTeacherByTeacherId(teacherId);
    }

    @Override
    public Teacher selectTeacherByGh(String gh) {
        return teacherMapper.selectTeacherByGh(gh);
    }

    @Override
    public TeacherStatsVO getTeacherStats(Long teacherId) {
        TeacherStatsVO stats = new TeacherStatsVO();
        
        Semester currentSemester = semesterService.selectCurrentSemester();
        String semesterValue = currentSemester != null ? currentSemester.getSemesterValue() : null;
        String semesterDesc = currentSemester != null ? currentSemester.getSemesterDesc() : "暂无";
        
        stats.setCurrentSemester(semesterDesc);
        
        int courseCount = courseOfferingMapper.countTeacherCourses(teacherId, semesterValue);
        stats.setCourseCount(courseCount);
        
        int studentCount = courseOfferingMapper.countTeacherStudents(teacherId, semesterValue);
        stats.setStudentCount(studentCount);
        
        int pendingScoreCount = courseOfferingMapper.countPendingScoreCourses(teacherId, semesterValue);
        stats.setPendingScoreCount(pendingScoreCount);
        
        return stats;
    }

    @Override
    public List<TeacherCourseVO> getTeacherCourses(Long teacherId) {
        return getTeacherCourses(teacherId, null, null, null);
    }

    @Override
    public List<TeacherCourseVO> getTeacherCourses(Long teacherId, String courseNo, String courseName) {
        return getTeacherCourses(teacherId, null, courseNo, courseName);
    }

    @Override
    public List<TeacherCourseVO> getTeacherCourses(Long teacherId, Boolean onlyCurrent, String courseNo, String courseName) {
        String semesterValue = null;
        String excludeSemester = null;
        if (onlyCurrent != null) {
            Semester currentSemester = semesterService.selectCurrentSemester();
            String currentSemesterValue = currentSemester != null ? currentSemester.getSemesterValue() : null;
            if (onlyCurrent) {
                semesterValue = currentSemesterValue;
            } else {
                excludeSemester = currentSemesterValue;
            }
        }

        List<TeacherCourseVO> courses = courseOfferingMapper.selectTeacherCourses(teacherId, semesterValue, excludeSemester, courseNo, courseName);

        for (TeacherCourseVO course : courses) {
            int pendingCount = courseOfferingMapper.countPendingScoreStudents(course.getOfferingId());
            course.setNeedScoreEntry(pendingCount > 0);
        }

        return courses;
    }
}

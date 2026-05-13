package com.ruoyi.edu.service.impl;

import java.util.List;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.edu.mapper.StudentMapper;
import com.ruoyi.edu.domain.Student;
import com.ruoyi.edu.service.IStudentService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.service.ISysRoleService;
import com.ruoyi.system.mapper.SysRoleMapper;

/**
 * 学生Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-05-11
 */
@Service
public class StudentServiceImpl implements IStudentService 
{
    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private ISysRoleService sysRoleService;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 学生角色的role_key
     */
    private static final String STUDENT_ROLE_KEY = "student";

    /**
     * 查询学生
     * 
     * @param studentId 学生主键
     * @return 学生
     */
    @Override
    public Student selectStudentByStudentId(Long studentId)
    {
        return studentMapper.selectStudentByStudentId(studentId);
    }

    /**
     * 根据学号查询学生
     * 
     * @param studentNo 学号
     * @return 学生
     */
    @Override
    public Student selectStudentByStudentNo(String studentNo)
    {
        return studentMapper.selectStudentByStudentNo(studentNo);
    }

    /**
     * 根据用户ID查询学生
     * 
     * @param userId 用户ID
     * @return 学生
     */
    @Override
    public Student selectStudentByUserId(Long userId)
    {
        return studentMapper.selectStudentByUserId(userId);
    }

    /**
     * 查询学生列表
     * 
     * @param student 学生
     * @return 学生
     */
    @Override
    public List<Student> selectStudentList(Student student)
    {
        return studentMapper.selectStudentList(student);
    }

    /**
     * 获取学生角色ID
     * 
     * @return 学生角色ID
     */
    private Long getStudentRoleId()
    {
        SysRole role = new SysRole();
        role.setRoleKey(STUDENT_ROLE_KEY);
        List<SysRole> roles = sysRoleMapper.selectRoleList(role);
        if (roles != null && !roles.isEmpty())
        {
            return roles.get(0).getRoleId();
        }
        // 兜底：使用角色ID 3
        return 3L;
    }

    /**
     * 新增学生
     * 
     * @param student 学生
     * @return 结果
     */
    @Override
    @Transactional
    public int insertStudent(Student student)
    {
        // 1. 创建系统用户
        SysUser sysUser = new SysUser();
        sysUser.setUserName(student.getStudentNo());
        sysUser.setNickName(student.getStudentName());
        sysUser.setStatus("0");
        sysUser.setCreateBy(student.getCreateBy());
        
        // 初始密码：默认密码 admin123
        String defaultPassword = "admin123";
        sysUser.setPassword(passwordEncoder.encode(defaultPassword));
        
        // 2. 插入系统用户
        sysUserService.insertUser(sysUser);
        
        // 3. 分配学生角色（通过role_key动态获取角色ID）
        Long studentRoleId = getStudentRoleId();
        sysUserService.insertUserAuth(sysUser.getUserId(), new Long[]{studentRoleId});
        
        // 4. 设置学生的user_id
        student.setUserId(sysUser.getUserId());
        
        // 5. 插入学生记录
        return studentMapper.insertStudent(student);
    }

    /**
     * 修改学生
     * 
     * @param student 学生
     * @return 结果
     */
    @Override
    @Transactional
    public int updateStudent(Student student)
    {
        // 如果学生姓名变更，同时更新系统用户的昵称
        if (student.getUserId() != null)
        {
            Student oldStudent = studentMapper.selectStudentByStudentId(student.getStudentId());
            if (oldStudent != null && !oldStudent.getStudentName().equals(student.getStudentName()))
            {
                SysUser sysUser = sysUserService.selectUserById(student.getUserId());
                if (sysUser != null)
                {
                    sysUser.setNickName(student.getStudentName());
                    sysUserService.updateUser(sysUser);
                }
            }
        }
        return studentMapper.updateStudent(student);
    }

    /**
     * 批量删除学生
     * 
     * @param studentIds 需要删除的学生主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteStudentByStudentIds(Long[] studentIds)
    {
        // 1. 先删除关联的系统用户和角色
        for (Long studentId : studentIds)
        {
            deleteStudentUserAndRole(studentId);
        }
        // 2. 删除学生记录
        return studentMapper.deleteStudentByStudentIds(studentIds);
    }

    /**
     * 删除学生信息
     * 
     * @param studentId 学生主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteStudentByStudentId(Long studentId)
    {
        // 1. 先删除关联的系统用户和角色
        deleteStudentUserAndRole(studentId);
        // 2. 删除学生记录
        return studentMapper.deleteStudentByStudentId(studentId);
    }

    /**
     * 删除学生关联的系统用户和角色
     * 
     * @param studentId 学生ID
     */
    private void deleteStudentUserAndRole(Long studentId)
    {
        Student student = studentMapper.selectStudentByStudentId(studentId);
        if (student != null && student.getUserId() != null)
        {
            // 删除系统用户时会同步清理用户角色/岗位关联
            sysUserService.deleteUserById(student.getUserId());
        }
    }
}

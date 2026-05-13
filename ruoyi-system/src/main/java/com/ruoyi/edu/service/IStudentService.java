package com.ruoyi.edu.service;

import java.util.List;
import com.ruoyi.edu.domain.Student;

/**
 * 学生Service接口
 * 
 * @author ruoyi
 * @date 2025-05-11
 */
public interface IStudentService 
{
    /**
     * 查询学生
     * 
     * @param studentId 学生主键
     * @return 学生
     */
    public Student selectStudentByStudentId(Long studentId);

    /**
     * 根据学号查询学生
     * 
     * @param studentNo 学号
     * @return 学生
     */
    public Student selectStudentByStudentNo(String studentNo);

    /**
     * 根据用户ID查询学生
     * 
     * @param userId 用户ID
     * @return 学生
     */
    public Student selectStudentByUserId(Long userId);

    /**
     * 查询学生列表
     * 
     * @param student 学生
     * @return 学生集合
     */
    public List<Student> selectStudentList(Student student);

    /**
     * 新增学生
     * 
     * @param student 学生
     * @return 结果
     */
    public int insertStudent(Student student);

    /**
     * 修改学生
     * 
     * @param student 学生
     * @return 结果
     */
    public int updateStudent(Student student);

    /**
     * 批量删除学生
     * 
     * @param studentIds 需要删除的学生主键集合
     * @return 结果
     */
    public int deleteStudentByStudentIds(Long[] studentIds);

    /**
     * 删除学生信息
     * 
     * @param studentId 学生主键
     * @return 结果
     */
    public int deleteStudentByStudentId(Long studentId);
}

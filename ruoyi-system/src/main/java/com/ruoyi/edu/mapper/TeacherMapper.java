package com.ruoyi.edu.mapper;

import java.util.List;
import com.ruoyi.edu.domain.Teacher;

/**
 * 教师Mapper接口
 * 
 * @author ruoyi
 * @date 2025-05-11
 */
public interface TeacherMapper 
{
    /**
     * 查询教师
     * 
     * @param teacherId 教师主键
     * @return 教师
     */
    public Teacher selectTeacherByTeacherId(Long teacherId);

    /**
     * 根据用户ID查询教师
     * 
     * @param userId 用户ID
     * @return 教师
     */
    public Teacher selectTeacherByUserId(Long userId);

    /**
     * 查询教师列表
     * 
     * @param teacher 教师
     * @return 教师集合
     */
    public List<Teacher> selectTeacherList(Teacher teacher);

    /**
     * 新增教师
     * 
     * @param teacher 教师
     * @return 结果
     */
    public int insertTeacher(Teacher teacher);

    /**
     * 修改教师
     * 
     * @param teacher 教师
     * @return 结果
     */
    public int updateTeacher(Teacher teacher);

    /**
     * 删除教师
     * 
     * @param teacherId 教师主键
     * @return 结果
     */
    public int deleteTeacherByTeacherId(Long teacherId);

    /**
     * 批量删除教师
     * 
     * @param teacherIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTeacherByTeacherIds(Long[] teacherIds);

    /**
     * 统计教师总数
     * 
     * @return 教师总数
     */
    public int countAll();

    /**
     * 根据工号查询教师
     * 
     * @param gh 工号
     * @return 教师
     */
    public Teacher selectTeacherByGh(String gh);
}

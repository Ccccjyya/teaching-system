package com.ruoyi.edu.service;

import java.util.List;
import com.ruoyi.edu.domain.Teacher;
import com.ruoyi.edu.vo.TeacherCourseVO;
import com.ruoyi.edu.vo.TeacherStatsVO;

/**
 * 教师Service接口
 */
public interface ITeacherService {

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
     * 批量删除教师
     *
     * @param teacherIds 需要删除的教师主键集合
     * @return 结果
     */
    public int deleteTeacherByTeacherIds(Long[] teacherIds);

    /**
     * 删除教师信息
     *
     * @param teacherId 教师主键
     * @return 结果
     */
    public int deleteTeacherByTeacherId(Long teacherId);

    /**
     * 根据工号查询教师
     *
     * @param gh 工号
     * @return 教师
     */
    public Teacher selectTeacherByGh(String gh);

    /**
     * 获取教师统计数据
     *
     * @param teacherId 教师ID
     * @return 统计数据
     */
    public TeacherStatsVO getTeacherStats(Long teacherId);

    /**
     * 获取教师当前学期授课列表
     *
     * @param teacherId 教师ID
     * @return 授课列表
     */
    public List<TeacherCourseVO> getTeacherCourses(Long teacherId);

    /**
     * 获取教师授课列表（分页）
     *
     * @param teacherId 教师ID
     * @param courseNo 课程号
     * @param courseName 课程名称
     * @return 授课列表
     */
    public List<TeacherCourseVO> getTeacherCourses(Long teacherId, String courseNo, String courseName);

    /**
     * 获取教师授课列表（分页）
     *
     * @param teacherId 教师ID
     * @param onlyCurrent 是否只查询当前学期
     * @param courseNo 课程号
     * @param courseName 课程名称
     * @return 授课列表
     */
    public List<TeacherCourseVO> getTeacherCourses(Long teacherId, Boolean onlyCurrent, String courseNo, String courseName);
}
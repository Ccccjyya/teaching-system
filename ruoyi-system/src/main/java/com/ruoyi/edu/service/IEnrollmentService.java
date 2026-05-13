package com.ruoyi.edu.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.edu.domain.Enrollment;
import com.ruoyi.edu.vo.CourseTableVO;
import com.ruoyi.edu.vo.GradeStatisticsVO;
import com.ruoyi.edu.vo.StudentGradeVO;

/**
 * 选课Service接口
 * 
 * @author ruoyi
 * @date 2025-05-11
 */
public interface IEnrollmentService 
{
    /**
     * 查询选课
     * 
     * @param enrollmentId 选课主键
     * @return 选课
     */
    public Enrollment selectEnrollmentByEnrollmentId(Long enrollmentId);

    /**
     * 查询选课列表
     * 
     * @param enrollment 选课
     * @return 选课集合
     */
    public List<Enrollment> selectEnrollmentList(Enrollment enrollment);

    /**
     * 查询学生已选课程
     * 
     * @param studentId 学生ID
     * @return 选课集合
     */
    public List<Enrollment> selectEnrollmentByStudentId(Long studentId);

    /**
     * 新增选课
     * 
     * @param enrollment 选课
     * @return 结果
     */
    public int insertEnrollment(Enrollment enrollment);

    /**
     * 学生选课（带时间冲突检测）
     * 
     * @param enrollment 选课
     * @return 结果
     */
    public int studentEnroll(Enrollment enrollment);

    /**
     * 修改选课
     * 
     * @param enrollment 选课
     * @return 结果
     */
    public int updateEnrollment(Enrollment enrollment);

    /**
     * 批量删除选课
     * 
     * @param enrollmentIds 需要删除的选课主键集合
     * @return 结果
     */
    public int deleteEnrollmentByEnrollmentIds(Long[] enrollmentIds);

    /**
     * 删除选课信息（退课）
     * 
     * @param enrollmentId 选课主键
     * @return 结果
     */
    public int deleteEnrollmentByEnrollmentId(Long enrollmentId);

    /**
     * 查询学生成绩列表
     * 
     * @param studentId 学生ID
     * @param academicYear 学年（可选）
     * @param semester 学期（可选）
     * @return 成绩列表
     */
    public List<StudentGradeVO> selectStudentGrades(Long studentId, String academicYear, String semester);

    /**
     * 计算成绩统计信息
     * 
     * @param studentId 学生ID
     * @param academicYear 学年（可选）
     * @param semester 学期（可选）
     * @return 统计信息
     */
    public GradeStatisticsVO calculateGradeStatistics(Long studentId, String academicYear, String semester);

    /**
     * 查询学生课表
     * 
     * @param studentId 学生ID
     * @param semester 学期（可选）
     * @return 课表列表
     */
    public List<CourseTableVO> selectStudentCourseTable(Long studentId, String academicYear, String semester);

    /**
     * 查询学生有选课记录的学期
     *
     * @param studentId 学生ID
     * @return 学期值列表
     */
    public List<String> selectStudentEnrollmentSemesters(Long studentId);

    /**
     * 获取学生统计数据（已选课程数、已修学分）
     *
     * @param studentId 学生ID
     * @return 统计数据 Map，包含 courseCount（已选课程数）和 creditCount（已修学分）
     */
    public Map<String, Object> getStudentStats(Long studentId);

    /**
     * 查询某门课的学生名单
     *
     * @param offeringId 开课ID
     * @return 学生选课列表
     */
    public List<Enrollment> getCourseStudents(Long offeringId);

    /**
     * 查询某门课的学生名单（带搜索关键词）
     *
     * @param offeringId 开课ID
     * @param keyword 搜索关键词
     * @return 学生选课列表
     */
    public List<Enrollment> getCourseStudents(Long offeringId, String keyword);

    /**
     * 保存单个学生的成绩
     *
     * @param enrollmentId 选课ID
     * @param usualScore 平时成绩
     * @param examScore 考试成绩
     * @param totalScore 总评成绩
     * @return 更新结果
     */
    public int saveSingleScore(Long enrollmentId, Integer usualScore, Integer examScore, Integer totalScore);

    /**
     * 批量保存学生成绩
     *
     * @param scoreList 成绩列表
     * @return 更新结果
     */
    public int saveScores(List<Map<String, Object>> scoreList);
}

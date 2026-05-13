package com.ruoyi.edu.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.edu.domain.Enrollment;
import com.ruoyi.edu.vo.CourseTableVO;
import com.ruoyi.edu.vo.StudentGradeVO;

/**
 * 选课Mapper接口
 * 
 * @author ruoyi
 * @date 2025-05-11
 */
public interface EnrollmentMapper 
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
     * 根据学生ID查询已选课程
     * 
     * @param studentId 学生ID
     * @return 选课集合
     */
    public List<Enrollment> selectEnrollmentByStudentId(Long studentId);

    /**
     * 根据学生ID和开课ID查询有效选课
     *
     * @param studentId 学生ID
     * @param offeringId 开课ID
     * @return 选课
     */
    public Enrollment selectActiveEnrollmentByStudentIdAndOfferingId(@Param("studentId") Long studentId, @Param("offeringId") Long offeringId);

    /**
     * 新增选课
     * 
     * @param enrollment 选课
     * @return 结果
     */
    public int insertEnrollment(Enrollment enrollment);

    /**
     * 修改选课
     * 
     * @param enrollment 选课
     * @return 结果
     */
    public int updateEnrollment(Enrollment enrollment);

    /**
     * 删除选课
     * 
     * @param enrollmentId 选课主键
     * @return 结果
     */
    public int deleteEnrollmentByEnrollmentId(Long enrollmentId);

    /**
     * 批量删除选课
     * 
     * @param enrollmentIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteEnrollmentByEnrollmentIds(Long[] enrollmentIds);

    public int countBySemester(String semester);

    public int deleteBySemester(String semester);

    /**
     * 查询学生成绩列表
     */
    public List<StudentGradeVO> selectStudentGrades(@Param("studentId") Long studentId, @Param("academicYear") String academicYear, @Param("semester") String semester);

    /**
     * 查询学生课表
     */
    public List<CourseTableVO> selectStudentCourseTable(@Param("studentId") Long studentId, @Param("academicYear") String academicYear, @Param("semester") String semester);

    /**
     * 查询学生有选课记录的学期
     */
    public List<String> selectStudentEnrollmentSemesters(@Param("studentId") Long studentId);

    /**
     * 查询当前学期学生已选课程
     */
    public List<Enrollment> selectCurrentSemesterEnrollments(@Param("studentId") Long studentId);

    /**
     * 计算学生已修学分（成绩 >= 60 的课程学分总和）
     */
    public int sumCompletedCredits(@Param("studentId") Long studentId);

    /**
     * 查询某门课的学生名单（按开课ID）
     */
    public List<Enrollment> selectCourseStudents(@Param("offeringId") Long offeringId);

    /**
     * 查询某门课的学生名单（按开课ID和搜索关键词）
     */
    public List<Enrollment> selectCourseStudentsByKeyword(@Param("offeringId") Long offeringId, @Param("keyword") String keyword);
}

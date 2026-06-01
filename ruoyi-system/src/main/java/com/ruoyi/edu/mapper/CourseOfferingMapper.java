package com.ruoyi.edu.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.edu.domain.CourseOffering;
import com.ruoyi.edu.vo.TeacherCourseVO;

/**
 * 开课Mapper接口
 * 
 * @author ruoyi
 * @date 2025-05-11
 */
public interface CourseOfferingMapper 
{
    /**
     * 查询开课
     * 
     * @param offeringId 开课主键
     * @return 开课
     */
    public CourseOffering selectCourseOfferingByOfferingId(Long offeringId);

    /**
     * 查询开课并锁定对应行，用于选课容量校验
     *
     * @param offeringId 开课主键
     * @return 开课
     */
    public CourseOffering selectCourseOfferingByOfferingIdForUpdate(Long offeringId);

    /**
     * 查询开课列表
     * 
     * @param courseOffering 开课
     * @return 开课集合
     */
    public List<CourseOffering> selectCourseOfferingList(CourseOffering courseOffering);

    /**
     * 新增开课
     * 
     * @param courseOffering 开课
     * @return 结果
     */
    public int insertCourseOffering(CourseOffering courseOffering);

    /**
     * 修改开课
     * 
     * @param courseOffering 开课
     * @return 结果
     */
    public int updateCourseOffering(CourseOffering courseOffering);

    /**
     * 删除开课
     * 
     * @param offeringId 开课主键
     * @return 结果
     */
    public int deleteCourseOfferingByOfferingId(Long offeringId);

    /**
     * 批量删除开课
     * 
     * @param offeringIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCourseOfferingByOfferingIds(Long[] offeringIds);

    public int countBySemester(String semester);

    public int deleteBySemester(String semester);

    /**
     * 统计教师本学期授课门数
     */
    public int countTeacherCourses(@Param("teacherId") Long teacherId, @Param("semester") String semester);

    /**
     * 统计教师本学期授课学生总数
     */
    public int countTeacherStudents(@Param("teacherId") Long teacherId, @Param("semester") String semester);

    /**
     * 统计教师待登分课程数
     */
    public int countPendingScoreCourses(@Param("teacherId") Long teacherId, @Param("semester") String semester);

    /**
     * 查询教师授课列表
     */
    public List<TeacherCourseVO> selectTeacherCourses(@Param("teacherId") Long teacherId, @Param("semester") String semester);

    /**
     * 查询教师授课列表（带搜索条件）
     */
    public List<TeacherCourseVO> selectTeacherCourses(@Param("teacherId") Long teacherId, @Param("semester") String semester,
                                                       @Param("courseNo") String courseNo, @Param("courseName") String courseName);

    /**
     * 查询教师授课列表（带搜索条件和排除学期）
     */
    public List<TeacherCourseVO> selectTeacherCourses(@Param("teacherId") Long teacherId, @Param("semester") String semester,
                                                       @Param("excludeSemester") String excludeSemester,
                                                       @Param("courseNo") String courseNo, @Param("courseName") String courseName);

    /**
     * 统计某门课程待登分学生数
     */
    public int countPendingScoreStudents(@Param("offeringId") Long offeringId);

    /**
     * 根据课程ID查询开课列表
     */
    public List<CourseOffering> selectCourseOfferingListByCourseId(@Param("courseId") Long courseId);
}

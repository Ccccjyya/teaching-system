package com.ruoyi.edu.mapper;

import java.util.List;
import com.ruoyi.edu.domain.Course;

public interface CourseMapper {

    public List<Course> selectCourseList(Course course);

    public Course selectCourseById(Long id);

    public Course selectCourseByKh(String kh);

    public int insertCourse(Course course);

    public int updateCourse(Course course);

    public int deleteCourseById(Long id);

    public int deleteCourseByIds(Long[] ids);

    public int countAll();
}
package com.ruoyi.edu.service;

import java.util.List;
import com.ruoyi.edu.domain.Course;

public interface ICourseService {

    public List<Course> selectCourseList(Course course);

    public Course selectCourseById(Long id);

    public int insertCourse(Course course);

    public int updateCourse(Course course);

    public int deleteCourseById(Long id);

    public int deleteCourseByIds(Long[] ids);
}
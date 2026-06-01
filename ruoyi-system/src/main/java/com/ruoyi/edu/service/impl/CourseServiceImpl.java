package com.ruoyi.edu.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.edu.domain.Course;
import com.ruoyi.edu.mapper.CourseMapper;
import com.ruoyi.edu.mapper.CourseOfferingMapper;
import com.ruoyi.edu.service.ICourseService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;

@Service
public class CourseServiceImpl implements ICourseService {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private CourseOfferingMapper courseOfferingMapper;

    @Override
    public List<Course> selectCourseList(Course course) {
        return courseMapper.selectCourseList(course);
    }

    @Override
    public Course selectCourseById(Long courseId) {
        return courseMapper.selectCourseById(courseId);
    }

    @Override
    public int insertCourse(Course course) {
        Course existCourse = courseMapper.selectCourseByKh(course.getCourseNo());
        if (existCourse != null) {
            throw new ServiceException("课程号已存在");
        }
        course.setCreateTime(DateUtils.getNowDate());
        return courseMapper.insertCourse(course);
    }

    @Override
    public int updateCourse(Course course) {
        Course existCourse = courseMapper.selectCourseByKh(course.getCourseNo());
        if (existCourse != null && !existCourse.getCourseId().equals(course.getCourseId())) {
            throw new ServiceException("课程号已存在");
        }
        course.setUpdateTime(DateUtils.getNowDate());
        return courseMapper.updateCourse(course);
    }

    @Override
    public int deleteCourseById(Long courseId) {
        checkCourseOfferingReference(courseId);
        return courseMapper.deleteCourseById(courseId);
    }

    @Override
    public int deleteCourseByIds(Long[] courseIds) {
        for (Long courseId : courseIds) {
            checkCourseOfferingReference(courseId);
        }
        return courseMapper.deleteCourseByIds(courseIds);
    }

    private void checkCourseOfferingReference(Long courseId) {
        if (courseOfferingMapper.countByCourseId(courseId) > 0) {
            throw new ServiceException("该课程已有开课记录，不能删除");
        }
    }
}

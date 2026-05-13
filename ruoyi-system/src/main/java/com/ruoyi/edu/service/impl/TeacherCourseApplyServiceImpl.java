package com.ruoyi.edu.service.impl;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.edu.domain.TeacherCourseApply;
import com.ruoyi.edu.domain.Course;
import com.ruoyi.edu.domain.CourseOffering;
import com.ruoyi.edu.domain.Department;
import com.ruoyi.edu.mapper.TeacherCourseApplyMapper;
import com.ruoyi.edu.mapper.CourseMapper;
import com.ruoyi.edu.mapper.CourseOfferingMapper;
import com.ruoyi.edu.mapper.DepartmentMapper;
import com.ruoyi.edu.mapper.TeacherMapper;
import com.ruoyi.edu.service.ITeacherCourseApplyService;
import com.ruoyi.edu.dto.ApplyCommitDTO;
import com.ruoyi.edu.dto.ApplyRefuseDTO;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;

@Service
public class TeacherCourseApplyServiceImpl implements ITeacherCourseApplyService {

    @Autowired
    private TeacherCourseApplyMapper teacherCourseApplyMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private CourseOfferingMapper courseOfferingMapper;

    @Autowired
    private DepartmentMapper deptMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Override
    public List<TeacherCourseApply> selectTeacherCourseApplyList(TeacherCourseApply apply) {
        return teacherCourseApplyMapper.selectTeacherCourseApplyList(apply);
    }

    @Override
    public TeacherCourseApply selectTeacherCourseApplyById(Long id) {
        return teacherCourseApplyMapper.selectTeacherCourseApplyById(id);
    }

    @Override
    public int insertTeacherCourseApply(TeacherCourseApply apply) {
        apply.setStats("pending");
        apply.setCreateTime(DateUtils.getNowDate());
        return teacherCourseApplyMapper.insertTeacherCourseApply(apply);
    }

    @Override
    public int updateTeacherCourseApply(TeacherCourseApply apply) {
        apply.setUpdateTime(DateUtils.getNowDate());
        return teacherCourseApplyMapper.updateTeacherCourseApply(apply);
    }

    @Override
    public int deleteTeacherCourseApplyById(Long id) {
        return teacherCourseApplyMapper.deleteTeacherCourseApplyById(id);
    }

    @Override
    public int deleteTeacherCourseApplyByIds(Long[] ids) {
        return teacherCourseApplyMapper.deleteTeacherCourseApplyByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int applyCommit(ApplyCommitDTO dto) {
        TeacherCourseApply apply = teacherCourseApplyMapper.selectTeacherCourseApplyById(dto.getId());
        if (apply == null) {
            throw new ServiceException("申请不存在");
        }

        if (!"pending".equals(apply.getStats())) {
            throw new ServiceException("该申请已处理，无法重复审核");
        }

        Long courseId = apply.getCourseId();
        Course course;

        if (courseId != null) {
            course = courseMapper.selectCourseById(courseId);
            if (course == null) {
                throw new ServiceException("关联的课程不存在");
            }
        } else {
            Course existCourse = courseMapper.selectCourseByKh(apply.getKm());
            if (existCourse != null) {
                throw new ServiceException("课程名已存在，无法重复开课");
            }

            course = new Course();
            String deptCode = "";
            if (apply.getYxhId() != null) {
                var dept = deptMapper.selectDepartmentById(apply.getYxhId());
                if (dept != null && StringUtils.isNotBlank(dept.getDeptCode())) {
                    deptCode = dept.getDeptCode();
                }
            }
            String generatedCourseNo = StringUtils.isNotBlank(dto.getCourseNo()) ? dto.getCourseNo() : deptCode + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            course.setCourseNo(generatedCourseNo);
            course.setCourseName(apply.getKm());
            course.setCredit(apply.getXf().intValue());
            course.setHours(dto.getHours() != null ? dto.getHours() : (apply.getXs() != null ? apply.getXs() : apply.getXf().intValue() * 16));
            course.setDeptId(apply.getYxhId());
            course.setCreateBy("admin");
            course.setCreateTime(DateUtils.getNowDate());
            courseMapper.insertCourse(course);
        }

        CourseOffering offering = new CourseOffering();
        var teacher = teacherMapper.selectTeacherByGh(apply.getGh());
        if (teacher == null) {
            throw new ServiceException("教师不存在，工号: " + apply.getGh());
        }
        offering.setCourseId(course.getCourseId());
        offering.setCourseNo(course.getCourseNo());
        offering.setSemester(apply.getXq());
        offering.setTeacherId(teacher.getTeacherId());
        offering.setSchedule(dto.getSchedule());
        offering.setLocation(dto.getLocation());
        offering.setMaxCapacity(dto.getMaxCapacity());
        offering.setCreateBy("admin");
        offering.setCreateTime(DateUtils.getNowDate());
        courseOfferingMapper.insertCourseOffering(offering);

        TeacherCourseApply updateApply = new TeacherCourseApply();
        updateApply.setId(dto.getId());
        updateApply.setStats("approved");
        updateApply.setUpdateBy("admin");
        updateApply.setUpdateTime(DateUtils.getNowDate());
        teacherCourseApplyMapper.updateApplyStatus(updateApply);

        return 1;
    }

    @Override
    public int applyRefuse(ApplyRefuseDTO dto) {
        TeacherCourseApply apply = teacherCourseApplyMapper.selectTeacherCourseApplyById(dto.getId());
        if (apply == null) {
            throw new ServiceException("申请不存在");
        }
        if (!"pending".equals(apply.getStats())) {
            throw new ServiceException("该申请已处理，无法重复审核");
        }

        TeacherCourseApply updateApply = new TeacherCourseApply();
        updateApply.setId(dto.getId());
        updateApply.setStats("rejected");
        updateApply.setRefuseReason(dto.getRefuseReason());
        updateApply.setUpdateBy("admin");
        updateApply.setUpdateTime(DateUtils.getNowDate());
        return teacherCourseApplyMapper.updateApplyStatus(updateApply);
    }
}

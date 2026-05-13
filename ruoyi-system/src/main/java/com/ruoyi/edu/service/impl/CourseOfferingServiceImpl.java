package com.ruoyi.edu.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.edu.mapper.CourseOfferingMapper;
import com.ruoyi.edu.domain.CourseOffering;
import com.ruoyi.edu.service.ICourseOfferingService;

/**
 * 开课Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-05-11
 */
@Service
public class CourseOfferingServiceImpl implements ICourseOfferingService 
{
    @Autowired
    private CourseOfferingMapper courseOfferingMapper;

    /**
     * 查询开课
     * 
     * @param offeringId 开课主键
     * @return 开课
     */
    @Override
    public CourseOffering selectCourseOfferingByOfferingId(Long offeringId)
    {
        return courseOfferingMapper.selectCourseOfferingByOfferingId(offeringId);
    }

    /**
     * 查询开课列表
     * 
     * @param courseOffering 开课
     * @return 开课
     */
    @Override
    public List<CourseOffering> selectCourseOfferingList(CourseOffering courseOffering)
    {
        return courseOfferingMapper.selectCourseOfferingList(courseOffering);
    }

    /**
     * 新增开课
     * 
     * @param courseOffering 开课
     * @return 结果
     */
    @Override
    public int insertCourseOffering(CourseOffering courseOffering)
    {
        return courseOfferingMapper.insertCourseOffering(courseOffering);
    }

    /**
     * 修改开课
     * 
     * @param courseOffering 开课
     * @return 结果
     */
    @Override
    public int updateCourseOffering(CourseOffering courseOffering)
    {
        return courseOfferingMapper.updateCourseOffering(courseOffering);
    }

    /**
     * 批量删除开课
     * 
     * @param offeringIds 需要删除的开课主键
     * @return 结果
     */
    @Override
    public int deleteCourseOfferingByOfferingIds(Long[] offeringIds)
    {
        return courseOfferingMapper.deleteCourseOfferingByOfferingIds(offeringIds);
    }

    /**
     * 删除开课信息
     * 
     * @param offeringId 开课主键
     * @return 结果
     */
    @Override
    public int deleteCourseOfferingByOfferingId(Long offeringId)
    {
        return courseOfferingMapper.deleteCourseOfferingByOfferingId(offeringId);
    }
}

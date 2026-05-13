package com.ruoyi.edu.service;

import java.util.List;
import com.ruoyi.edu.domain.CourseOffering;

/**
 * 开课Service接口
 * 
 * @author ruoyi
 * @date 2025-05-11
 */
public interface ICourseOfferingService 
{
    /**
     * 查询开课
     * 
     * @param offeringId 开课主键
     * @return 开课
     */
    public CourseOffering selectCourseOfferingByOfferingId(Long offeringId);

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
     * 批量删除开课
     * 
     * @param offeringIds 需要删除的开课主键集合
     * @return 结果
     */
    public int deleteCourseOfferingByOfferingIds(Long[] offeringIds);

    /**
     * 删除开课信息
     * 
     * @param offeringId 开课主键
     * @return 结果
     */
    public int deleteCourseOfferingByOfferingId(Long offeringId);
}

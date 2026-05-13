package com.ruoyi.web.controller.edu;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.edu.domain.CourseOffering;
import com.ruoyi.edu.domain.Semester;
import com.ruoyi.edu.service.ICourseOfferingService;
import com.ruoyi.edu.service.ISemesterService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 开课Controller
 * 
 * @author ruoyi
 * @date 2025-05-11
 */
@RestController
@RequestMapping("/edu/offering")
public class CourseOfferingController extends BaseController
{
    @Autowired
    private ICourseOfferingService courseOfferingService;
    
    @Autowired
    private ISemesterService semesterService;

    /**
     * 查询开课列表（管理端用，不限制学期）
     */
    @PreAuthorize("@ss.hasPermi('edu:offering:list')")
    @GetMapping("/admin/list")
    public TableDataInfo adminList(CourseOffering courseOffering)
    {
        courseOffering.getParams().put("adminList", true);
        startPage();
        List<CourseOffering> list = courseOfferingService.selectCourseOfferingList(courseOffering);
        return getDataTable(list);
    }

    /**
     * 查询开课列表（学生选课用）
     */
    @PreAuthorize("@ss.hasRole('student') or @ss.hasPermi('edu:offering:list')")
    @GetMapping("/list")
    public TableDataInfo list(CourseOffering courseOffering)
    {
        // 检查是否是管理员模式（不限制学期）
        Boolean adminMode = courseOffering.getParams().get("adminMode") != null 
                ? Boolean.valueOf(courseOffering.getParams().get("adminMode").toString()) 
                : false;
        
        if (!adminMode)
        {
            // 学生端查询强制只返回当前学期课程（例如 2024-2025-1）
            Semester currentSemester = semesterService.selectCurrentSemester();
            if (currentSemester != null && currentSemester.getSemesterValue() != null)
            {
                courseOffering.getParams().put("currentSemester", currentSemester.getSemesterValue());
            }
            startPage();
            List<CourseOffering> list = courseOfferingService.selectCourseOfferingList(courseOffering);
            return getDataTable(list);
        }
        else
        {
            // 管理员模式：不限制学期，返回所有数据
            courseOffering.getParams().put("adminList", true);
            startPage();
            List<CourseOffering> list = courseOfferingService.selectCourseOfferingList(courseOffering);
            return getDataTable(list);
        }
    }

    /**
     * 导出开课列表
     */
    @PreAuthorize("@ss.hasPermi('edu:offering:export')")
    @Log(title = "开课", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CourseOffering courseOffering)
    {
        List<CourseOffering> list = courseOfferingService.selectCourseOfferingList(courseOffering);
        ExcelUtil<CourseOffering> util = new ExcelUtil<CourseOffering>(CourseOffering.class);
        util.exportExcel(response, list, "开课数据");
    }

    /**
     * 获取开课详细信息
     */
    @PreAuthorize("@ss.hasRole('student') or @ss.hasPermi('edu:offering:query')")
    @GetMapping(value = "/{offeringId}")
    public AjaxResult getInfo(@PathVariable("offeringId") Long offeringId)
    {
        return success(courseOfferingService.selectCourseOfferingByOfferingId(offeringId));
    }

    /**
     * 新增开课
     */
    @PreAuthorize("@ss.hasPermi('edu:offering:add')")
    @Log(title = "开课", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CourseOffering courseOffering)
    {
        return toAjax(courseOfferingService.insertCourseOffering(courseOffering));
    }

    /**
     * 修改开课
     */
    @PreAuthorize("@ss.hasPermi('edu:offering:edit')")
    @Log(title = "开课", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CourseOffering courseOffering)
    {
        return toAjax(courseOfferingService.updateCourseOffering(courseOffering));
    }

    /**
     * 删除开课
     */
    @PreAuthorize("@ss.hasPermi('edu:offering:remove')")
    @Log(title = "开课", businessType = BusinessType.DELETE)
	@DeleteMapping("/{offeringIds}")
    public AjaxResult remove(@PathVariable Long[] offeringIds)
    {
        return toAjax(courseOfferingService.deleteCourseOfferingByOfferingIds(offeringIds));
    }
}

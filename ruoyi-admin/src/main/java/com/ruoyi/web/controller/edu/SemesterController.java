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
import com.ruoyi.edu.domain.Semester;
import com.ruoyi.edu.service.ISemesterService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 学期管理Controller
 * 
 * @author ruoyi
 * @date 2025-05-11
 */
@RestController
@RequestMapping("/edu/semester")
public class SemesterController extends BaseController
{
    @Autowired
    private ISemesterService semesterService;

    /**
     * 获取当前学期
     */
    @GetMapping("/current")
    public AjaxResult getCurrentSemester()
    {
        return success(semesterService.selectCurrentSemester());
    }

    /**
     * 查询学期管理列表
     */
    @PreAuthorize("@ss.hasPermi('edu:semester:list')")
    @GetMapping("/list")
    public TableDataInfo list(Semester semester)
    {
        startPage();
        List<Semester> list = semesterService.selectSemesterList(semester);
        return getDataTable(list);
    }

    /**
     * 导出学期管理列表
     */
    @PreAuthorize("@ss.hasPermi('edu:semester:export')")
    @Log(title = "学期管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Semester semester)
    {
        List<Semester> list = semesterService.selectSemesterList(semester);
        ExcelUtil<Semester> util = new ExcelUtil<Semester>(Semester.class);
        util.exportExcel(response, list, "学期管理数据");
    }

    /**
     * 获取学期管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('edu:semester:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(semesterService.selectSemesterById(id));
    }

    /**
     * 新增学期管理
     */
    @PreAuthorize("@ss.hasPermi('edu:semester:add')")
    @Log(title = "学期管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Semester semester)
    {
        semester.setCreateBy(getUsername());
        return toAjax(semesterService.insertSemester(semester));
    }

    /**
     * 修改学期管理
     */
    @PreAuthorize("@ss.hasPermi('edu:semester:edit')")
    @Log(title = "学期管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Semester semester)
    {
        semester.setUpdateBy(getUsername());
        return toAjax(semesterService.updateSemester(semester));
    }

    /**
     * 删除学期管理
     */
    @PreAuthorize("@ss.hasPermi('edu:semester:remove')")
    @Log(title = "学期管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(semesterService.deleteSemesterByIds(ids));
    }

    /**
     * 设置当前学期
     */
    @PreAuthorize("hasRole('admin')")
    @Log(title = "设置当前学期", businessType = BusinessType.UPDATE)
    @PostMapping("/setCurrent/{id}")
    public AjaxResult setCurrentSemester(@PathVariable Long id)
    {
        semesterService.setCurrentSemester(id);
        return success();
    }
}

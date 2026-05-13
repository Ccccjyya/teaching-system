package com.ruoyi.web.controller.edu;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.edu.domain.Department;
import com.ruoyi.edu.service.IDepartmentService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/edu/department")
@Validated
public class DepartmentController extends BaseController {

    @Autowired
    private IDepartmentService departmentService;

    @PreAuthorize("@ss.hasPermi('edu:department:list')")
    @GetMapping("/list")
    public TableDataInfo list(Department department) {
        startPage();
        List<Department> list = departmentService.selectDepartmentList(department);
        return getDataTable(list);
    }

    @Log(title = "院系管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('edu:department:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, Department department) {
        List<Department> list = departmentService.selectDepartmentList(department);
        ExcelUtil<Department> util = new ExcelUtil<Department>(Department.class);
        util.exportExcel(response, list, "院系数据");
    }

    @PreAuthorize("@ss.hasPermi('edu:department:query')")
    @GetMapping(value = "/{deptId}")
    public AjaxResult getInfo(@PathVariable Long deptId) {
        return success(departmentService.selectDepartmentById(deptId));
    }

    @PreAuthorize("@ss.hasPermi('edu:department:add')")
    @Log(title = "院系管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody Department department) {
        return toAjax(departmentService.insertDepartment(department));
    }

    @PreAuthorize("@ss.hasPermi('edu:department:edit')")
    @Log(title = "院系管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody Department department) {
        return toAjax(departmentService.updateDepartment(department));
    }

    @PreAuthorize("@ss.hasPermi('edu:department:remove')")
    @Log(title = "院系管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deptIds}")
    public AjaxResult remove(@PathVariable Long[] deptIds) {
        return toAjax(departmentService.deleteDepartmentByIds(deptIds));
    }
}
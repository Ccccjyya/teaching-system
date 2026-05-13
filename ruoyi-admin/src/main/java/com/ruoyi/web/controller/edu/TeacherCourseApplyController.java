package com.ruoyi.web.controller.edu;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.edu.domain.TeacherCourseApply;
import com.ruoyi.edu.service.ITeacherCourseApplyService;
import com.ruoyi.edu.dto.ApplyCommitDTO;
import com.ruoyi.edu.dto.ApplyRefuseDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/edu/apply")
@Validated
public class TeacherCourseApplyController extends BaseController {

    @Autowired
    private ITeacherCourseApplyService teacherCourseApplyService;

    @PreAuthorize("@ss.hasPermi('edu:apply:list')")
    @GetMapping("/list")
    public TableDataInfo list(TeacherCourseApply apply) {
        startPage();
        List<TeacherCourseApply> list = teacherCourseApplyService.selectTeacherCourseApplyList(apply);
        return getDataTable(list);
    }

    @Log(title = "开课申请", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('edu:apply:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, TeacherCourseApply apply) {
        List<TeacherCourseApply> list = teacherCourseApplyService.selectTeacherCourseApplyList(apply);
        ExcelUtil<TeacherCourseApply> util = new ExcelUtil<TeacherCourseApply>(TeacherCourseApply.class);
        util.exportExcel(response, list, "开课申请数据");
    }

    @PreAuthorize("@ss.hasPermi('edu:apply:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(teacherCourseApplyService.selectTeacherCourseApplyById(id));
    }

    @PreAuthorize("@ss.hasPermi('edu:apply:add')")
    @Log(title = "开课申请", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody TeacherCourseApply apply) {
        return toAjax(teacherCourseApplyService.insertTeacherCourseApply(apply));
    }

    @PreAuthorize("@ss.hasPermi('edu:apply:edit')")
    @Log(title = "开课申请", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody TeacherCourseApply apply) {
        return toAjax(teacherCourseApplyService.updateTeacherCourseApply(apply));
    }

    @PreAuthorize("@ss.hasPermi('edu:apply:remove')")
    @Log(title = "开课申请", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(teacherCourseApplyService.deleteTeacherCourseApplyByIds(ids));
    }

    @PreAuthorize("@ss.hasPermi('edu:apply:commit')")
    @Log(title = "开课申请审核", businessType = BusinessType.UPDATE)
    @PostMapping("/commit")
    public AjaxResult commit(@Validated @RequestBody ApplyCommitDTO dto) {
        return toAjax(teacherCourseApplyService.applyCommit(dto));
    }

    @PreAuthorize("@ss.hasPermi('edu:apply:refuse')")
    @Log(title = "开课申请拒绝", businessType = BusinessType.UPDATE)
    @PostMapping("/refuse")
    public AjaxResult refuse(@Validated @RequestBody ApplyRefuseDTO dto) {
        return toAjax(teacherCourseApplyService.applyRefuse(dto));
    }
}
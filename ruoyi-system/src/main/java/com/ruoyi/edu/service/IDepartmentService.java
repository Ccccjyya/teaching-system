package com.ruoyi.edu.service;

import com.ruoyi.edu.domain.Department;
import java.util.List;

public interface IDepartmentService {

    public Department selectDepartmentById(Long deptId);

    public List<Department> selectDepartmentList(Department department);

    public int insertDepartment(Department department);

    public int updateDepartment(Department department);

    public int deleteDepartmentById(Long deptId);

    public int deleteDepartmentByIds(Long[] deptIds);

}
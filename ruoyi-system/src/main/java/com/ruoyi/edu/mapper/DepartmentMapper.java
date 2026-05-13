package com.ruoyi.edu.mapper;

import java.util.List;
import com.ruoyi.edu.domain.Department;

public interface DepartmentMapper {

    public Department selectDepartmentById(Long deptId);

    public List<Department> selectDepartmentList(Department department);

    public int insertDepartment(Department department);

    public int updateDepartment(Department department);

    public int deleteDepartmentById(Long deptId);

    public int deleteDepartmentByIds(Long[] deptIds);
}
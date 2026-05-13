package com.ruoyi.edu.service.impl;

import java.util.List;
import com.ruoyi.edu.domain.Department;
import com.ruoyi.edu.mapper.DepartmentMapper;
import com.ruoyi.edu.service.IDepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DepartmentServiceImpl implements IDepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    public Department selectDepartmentById(Long deptId) {
        return departmentMapper.selectDepartmentById(deptId);
    }

    @Override
    public List<Department> selectDepartmentList(Department department) {
        return departmentMapper.selectDepartmentList(department);
    }

    @Override
    public int insertDepartment(Department department) {
        return departmentMapper.insertDepartment(department);
    }

    @Override
    public int updateDepartment(Department department) {
        return departmentMapper.updateDepartment(department);
    }

    @Override
    public int deleteDepartmentById(Long deptId) {
        return departmentMapper.deleteDepartmentById(deptId);
    }

    @Override
    public int deleteDepartmentByIds(Long[] deptIds) {
        return departmentMapper.deleteDepartmentByIds(deptIds);
    }

}
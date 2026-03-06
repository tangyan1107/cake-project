package com.cake.service;

import com.cake.dto.EmployeeDTO;
import com.cake.dto.EmployeeLoginDTO;
import com.cake.dto.EmployeePageQueryDTO;
import com.cake.entity.Employee;
import com.cake.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工
     * @param employeeDTO
     */
    void save(EmployeeDTO employeeDTO);

    /**
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 启用,禁用员工账号
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);
}

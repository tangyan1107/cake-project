package com.cake.service;

import com.cake.dto.EmployeeLoginDTO;
import com.cake.entity.Employee;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

}

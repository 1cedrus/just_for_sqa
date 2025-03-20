package com.restaurent.manager.mapper;

import com.restaurent.manager.dto.request.employee.EmployeeRequest;
import com.restaurent.manager.dto.request.employee.EmployeeUpdateInformationRequest;
import com.restaurent.manager.dto.request.employee.EmployeeUpdateRequest;
import com.restaurent.manager.dto.response.EmployeeResponse;
import com.restaurent.manager.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    Employee toEmployee(EmployeeRequest request);
    EmployeeResponse toEmployeeResponse(Employee employee);
    void updateRestaurant(@MappingTarget Employee employee, EmployeeUpdateInformationRequest employeeUpdateRequest);
}

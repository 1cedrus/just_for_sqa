package com.restaurent.manager.controller;

import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.request.employee.EmployeeRequest;
import com.restaurent.manager.dto.request.employee.EmployeeUpdateInformationRequest;
import com.restaurent.manager.dto.request.employee.EmployeeUpdateRequest;
import com.restaurent.manager.dto.response.ApiResponse;
import com.restaurent.manager.dto.response.EmployeeResponse;
import com.restaurent.manager.service.IEmployeeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@FieldDefaults(makeFinal = true)
@RequiredArgsConstructor
@RequestMapping(value = "/api/employee")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {
    IEmployeeService employeeService;

    @PostMapping("/create")
    public ApiResponse<EmployeeResponse> createEmployee(@RequestBody @Valid EmployeeRequest req){
        return ApiResponse.<EmployeeResponse>builder()
                .result(employeeService.createEmployee(req))
                .build();
    }
    @PutMapping("/{employeeId}")
    public ApiResponse<EmployeeResponse> updateEmployee(@PathVariable Long employeeId,@RequestBody @Valid EmployeeUpdateInformationRequest req){
        return ApiResponse.<EmployeeResponse>builder()
                .result(employeeService.updateEmployee(employeeId,req))
                .build();
    }
    @DeleteMapping("/delete/{employeeId}")
    public ApiResponse<Void> deleteEmployee(@PathVariable Long employeeId){
        employeeService.deleteEmployee(employeeId);
        return ApiResponse.<Void>builder()
                .message("Delete success")
                .build();
    }
    @GetMapping("/{employeeId}")
    public ApiResponse<EmployeeResponse> getEmployeeById(@PathVariable Long employeeId){
        return ApiResponse.<EmployeeResponse>builder()
                .result(employeeService.findEmployeeByIdConvertDTO(employeeId))
                .build();
    }
    @GetMapping("/restaurant/{accountId}")
    public ApiResponse<PagingResult<EmployeeResponse>> getEmployeesInRestaurantByAccountId(@PathVariable Long accountId, @RequestParam(value = "page", defaultValue = "1") int pageIndex, @RequestParam(value = "size",defaultValue = "10") int size,
                                                                         @RequestParam(value = "query",defaultValue = "") String query){
        Pageable pageable = PageRequest.of(pageIndex - 1,size);
        return  ApiResponse.<PagingResult<EmployeeResponse>>builder()
                .result(employeeService.findEmployeesByAccountId(accountId, pageable, query))
                .build();
    }
    @PutMapping(value = "/{employeeId}/change-password/{newPassword}")
    public ApiResponse<Void> changePasswordByEmployeeId(@PathVariable Long employeeId, @PathVariable String newPassword){
        employeeService.updateEmployeePassword(employeeId,newPassword);
        return ApiResponse.<Void>builder().build();
    }
}

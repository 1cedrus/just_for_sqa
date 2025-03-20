package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.RoleRequest;
import com.restaurent.manager.dto.response.RoleResponse;
import com.restaurent.manager.entity.Role;

import java.util.List;

public interface IRoleService {
    RoleResponse createRole(RoleRequest request);
    List<RoleResponse> getRoles();
    List<RoleResponse> getRolesInRestaurant();
    RoleResponse findRoleByName(String name);
    Role findByRoleName(String name);
}

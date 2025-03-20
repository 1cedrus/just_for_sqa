package com.restaurent.manager.mapper;

import com.restaurent.manager.dto.request.RoleRequest;
import com.restaurent.manager.dto.response.RoleResponse;
import com.restaurent.manager.entity.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    Role toRole(RoleRequest request);
    RoleResponse toRoleResponse(Role role);
}

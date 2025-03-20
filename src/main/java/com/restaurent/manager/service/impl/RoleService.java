package com.restaurent.manager.service.impl;

import com.restaurent.manager.dto.request.RoleRequest;
import com.restaurent.manager.dto.response.RoleResponse;
import com.restaurent.manager.entity.Role;
import com.restaurent.manager.enums.RoleSystem;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.RoleMapper;
import com.restaurent.manager.repository.RoleRepository;
import com.restaurent.manager.service.IRoleService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@AllArgsConstructor
public class RoleService implements IRoleService {
    RoleRepository roleRepository;
    RoleMapper roleMapper;
    @Override
    public RoleResponse createRole(RoleRequest request) {
        if(roleRepository.existsByName(request.getName())){
            throw new AppException(ErrorCode.ROLE_EXISTED);
        }
        Role role = roleRepository.save(roleMapper.toRole(request));
        return roleMapper.toRoleResponse(role);
    }

    @Override
    public List<RoleResponse> getRoles() {
        return roleRepository.findAll().stream().map(roleMapper::toRoleResponse).toList();
    }
    @Override
    public List<RoleResponse> getRolesInRestaurant() {
        List<RoleResponse> roles = new ArrayList<>();
        roles.add(findRoleByName(RoleSystem.CHEF.name()));
        roles.add(findRoleByName(RoleSystem.WAITER.name()));
        roles.add(findRoleByName(RoleSystem.HOSTESS.name()));
        return roles;
    }

    @Override
    public RoleResponse findRoleByName(String name) {
        return roleMapper.toRoleResponse(roleRepository.findByName(name)
                .orElseThrow(
                        () -> new AppException(ErrorCode.ROLE_NOT_EXISTED)
                ));
    }

    @Override
    public Role findByRoleName(String name) {
        return roleRepository.findByName(name).orElseThrow(
                () -> new AppException(ErrorCode.ROLE_NOT_EXISTED)
        );
    }
}

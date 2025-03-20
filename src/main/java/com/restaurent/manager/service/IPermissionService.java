package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.PermissionRequest;
import com.restaurent.manager.dto.response.PermissionResponse;
import com.restaurent.manager.entity.Permission;

import java.util.List;

public interface IPermissionService {
    PermissionResponse createPermission(PermissionRequest request);
    List<PermissionResponse> getPermissions();
    PermissionResponse updatePermission(Long permissionId,PermissionRequest request);
    Permission findPermissionById(Long permissionId);
}

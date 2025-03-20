package com.restaurent.manager.repository;

import com.restaurent.manager.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PermissionRepository extends JpaRepository<Permission,Long> {
}

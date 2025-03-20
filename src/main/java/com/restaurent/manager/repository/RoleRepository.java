package com.restaurent.manager.repository;

import com.restaurent.manager.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {
    boolean existsByName(String roleName);
    Optional<Role> findByName(String name);
}

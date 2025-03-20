package com.restaurent.manager.repository;

import com.restaurent.manager.entity.TableType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TableTypeRepository extends JpaRepository<TableType,Long> {
    Optional<TableType> findByName(String name);
}

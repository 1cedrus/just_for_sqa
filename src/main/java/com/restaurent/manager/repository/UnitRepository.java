package com.restaurent.manager.repository;

import com.restaurent.manager.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UnitRepository extends JpaRepository<Unit,Long> {
    List<Unit> getUnitsByAccount_Id(Long accountId);
}

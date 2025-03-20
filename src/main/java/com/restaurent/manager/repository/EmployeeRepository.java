package com.restaurent.manager.repository;

import com.restaurent.manager.entity.Employee;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee,Long> {
    List<Employee> findByRestaurant_IdAndEmployeeNameContaining(Long restaurantId, String query ,Pageable pageable);
    int countByRestaurant_IdAndEmployeeNameContaining(Long restaurantId, String query);
    Optional<Employee> findByUsernameAndRestaurant_Id(String userName, Long restaurantId);
    boolean existsByUsernameAndRestaurant_Id(String userName,Long restaurantId);
}

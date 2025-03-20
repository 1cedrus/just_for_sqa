package com.restaurent.manager.repository;

import com.restaurent.manager.entity.ScheduleDish;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleDishRepository extends JpaRepository<ScheduleDish, Long> {
    List<ScheduleDish> findBySchedule_Id(Long scheduleId);
}

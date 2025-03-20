package com.restaurent.manager.repository;

import com.restaurent.manager.entity.Package;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PackageRepository extends JpaRepository<Package,Long> {
    Optional<Package> findByPackName(String packName);
    List<Package> findByPricePerMonthGreaterThan(double pricePerMonth);
}

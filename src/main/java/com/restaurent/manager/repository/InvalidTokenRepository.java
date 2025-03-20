package com.restaurent.manager.repository;

import com.restaurent.manager.entity.InvalidToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidTokenRepository extends JpaRepository<InvalidToken,String> {
    boolean existsById(String id);
}

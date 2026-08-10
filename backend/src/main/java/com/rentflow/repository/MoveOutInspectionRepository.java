package com.rentflow.repository;

import com.rentflow.entity.MoveOutInspection;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.UUID;


public interface MoveOutInspectionRepository extends JpaRepository<MoveOutInspection, UUID> {
}

package com.fitnexus.server.repository.gym;

import com.fitnexus.server.entity.gym.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    boolean existsByName(String name);
}

package com.fitnexus.server.repository.gym;

import com.fitnexus.server.entity.gym.GymEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GymEquipmentRepository extends JpaRepository<GymEquipment,Long> {
}

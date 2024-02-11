package com.fitnexus.server.repository.trainer;

import com.fitnexus.server.entity.trainer.TrainerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerTypeRepository extends JpaRepository<TrainerType, Long> {

    TrainerType findTrainerTypeByTypeName(String typeName);
}

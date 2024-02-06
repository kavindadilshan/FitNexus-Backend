package com.fitnexus.server.repository.businessprofile;

import com.fitnexus.server.entity.businessprofile.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacilityRepository extends JpaRepository<Facility,Long> {

    boolean existsByName(String name);
}

package com.fitnexus.server.repository.advertisement;

import com.fitnexus.server.entity.advertisement.Advertisement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement,Long> {

    List<Advertisement> findAdvertisementsByVisible(boolean isVisible);
}

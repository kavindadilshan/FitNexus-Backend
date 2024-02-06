package com.fitnexus.server.service;

import com.fitnexus.server.dto.businessprofile.FacilityDTO;
import com.fitnexus.server.entity.businessprofile.Facility;
import com.fitnexus.server.entity.businessprofile.LocationFacility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FacilityService {
    void createFacility(FacilityDTO facilityDTO);

    void createFacilityList(List<FacilityDTO> facilityDTOList);

    void updateFacilityList(List<FacilityDTO> facilityDTOList);

    FacilityDTO getDTO(Facility facility);

    List<FacilityDTO> getFacilityDTOList(List<Facility> facilities);

    Facility getById(long id);

    List<Facility> getFacilityList(List<Long> idList);

    List<FacilityDTO> getAllFacilities();

    Page<FacilityDTO> getFacilityPage(Pageable pageable);

    List<FacilityDTO> getFacilityDTOListFromLocationFacilityList(List<LocationFacility> locationFacilities);
}

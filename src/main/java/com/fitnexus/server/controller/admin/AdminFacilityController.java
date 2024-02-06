package com.fitnexus.server.controller.admin;

import com.fitnexus.server.dto.businessprofile.FacilityDTO;
import com.fitnexus.server.dto.common.CommonResponse;
import com.fitnexus.server.service.FacilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/admin/facility")
public class AdminFacilityController {

    private final FacilityService facilityService;

    @PostMapping(value = "/create")
    public ResponseEntity createFacility(@RequestBody FacilityDTO dto) {
        log.info("Create facility : \nfacility dto: {}", dto);
        facilityService.createFacility(dto);
        log.info("Response : Facility created successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Facility created successfully"));
    }

    @PostMapping(value = "/create/list")
    public ResponseEntity createMultipleFacilities(@RequestBody List<FacilityDTO> dtoList) {
        log.info("Create multiple facilities : \nfacility dto list: {}", dtoList);
        facilityService.createFacilityList(dtoList);
        log.info("Response : Facility list created successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Facility list created successfully"));
    }

    @PutMapping(value = "/update/list")
    public ResponseEntity updateMultipleFacilities(@RequestBody List<FacilityDTO> dtoList) {
        log.info("Update multiple facilities : \nfacility dto list: {}", dtoList);
        facilityService.updateFacilityList(dtoList);
        log.info("Response : Facility list updated successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Facility list updated successfully"));
    }

    @GetMapping(value = "/all")
    public ResponseEntity getAllFacilities() {
        log.info("Get all facilities");
        List<FacilityDTO> allFacilities = facilityService.getAllFacilities();
        log.info("Response : All facility list");
        return ResponseEntity.ok(new CommonResponse<>(true, allFacilities));
    }

    @GetMapping(value = "/all/page")
    public ResponseEntity getAllFacilityPage(Pageable pageable) {
        log.info("Get facility page: \npage request- {}", pageable);
        Page<FacilityDTO> facilityPage = facilityService.getFacilityPage(pageable);
        log.info("Response : All facility page");
        return ResponseEntity.ok(new CommonResponse<>(true, facilityPage));
    }
}

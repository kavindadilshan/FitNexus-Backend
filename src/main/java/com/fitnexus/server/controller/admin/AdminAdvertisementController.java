package com.fitnexus.server.controller.admin;

import com.fitnexus.server.dto.advertisement.AdvertisementDTO;
import com.fitnexus.server.dto.common.CommonResponse;
import com.fitnexus.server.service.AdvertisementService;
import com.fitnexus.server.util.CustomGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/admin/advertisement")
public class AdminAdvertisementController {

    private final AdvertisementService advertisementService;

    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @PostMapping(value = "/create")
    public ResponseEntity createAdvertisement(@RequestBody AdvertisementDTO dto, @RequestHeader(name = "Authorization") String token) {
        String username = getUsername(token);
        log.info("Create advertisement: {}, \tusername: {}", dto, username);
        advertisementService.createAdvertisement(dto, username);
        log.info("Response : Advertisement created successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Advertisement created successfully"));
    }

    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @PutMapping(value = "/update")
    public ResponseEntity updateAdvertisement(@RequestBody AdvertisementDTO dto, @RequestHeader(name = "Authorization") String token) {
        String username = getUsername(token);
        log.info("Update advertisement: {}, \tusername: {}", dto, username);
        advertisementService.updateAdvertisement(dto, username);
        log.info("Response : Advertisement updated successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Advertisement updated successfully"));
    }

    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @PutMapping(value = "/update/visibility")
    public ResponseEntity updateAdvertisementVisibility(@RequestBody AdvertisementDTO dto, @RequestHeader(name = "Authorization") String token) {
        String username = getUsername(token);
        log.info("Update advertisement visibility: {}, \tusername: {}", dto, username);
        advertisementService.updateAdvertisementVisibility(dto, username);
        log.info("Response : Advertisement visibility updated successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Advertisement visibility updated successfully"));
    }

    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity deleteAdvertisement(@PathVariable("id") long id, @RequestHeader(name = "Authorization") String token) {
        String username = getUsername(token);
        log.info("Delete advertisement: {}, \tusername: {}", id, username);
        advertisementService.deleteAdvertisement(id);
        log.info("Response : Advertisement deleted successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Advertisement deleted successfully"));
    }

    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @GetMapping(value = "/all")
    public ResponseEntity getAllAdvertisements(Pageable pageable) {
        log.info("Get all advertisements page request: {}", pageable);
        Page<AdvertisementDTO> allAdvertisements = advertisementService.getAllAdvertisements(pageable);
        log.info("Advertisements page");
        return ResponseEntity.ok(new CommonResponse<>(true, allAdvertisements));
    }

    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @GetMapping(value = "/{id}")
    public ResponseEntity getAllAdvertisements(@PathVariable("id") long id) {
        log.info("Get advertisement by id: {}", id);
        AdvertisementDTO advertisementById = advertisementService.getAdvertisementById(id);
        log.info("Advertisement by id");
        return ResponseEntity.ok(new CommonResponse<>(true, advertisementById));
    }

    private String getUsername(String token) {
        return CustomGenerator.getJsonObjectFromJwt(token).getString("user_name");
    }
}

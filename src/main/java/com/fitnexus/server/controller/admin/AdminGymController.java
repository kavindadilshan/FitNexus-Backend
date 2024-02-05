package com.fitnexus.server.controller.admin;

import com.fitnexus.server.dto.classes.ClassNameIdDTO;
import com.fitnexus.server.dto.common.CommonResponse;
import com.fitnexus.server.dto.gym.GymDTO;
import com.fitnexus.server.dto.gym.GymSearchDTO;
import com.fitnexus.server.service.GymService;
import com.fitnexus.server.util.CustomGenerator;
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
@RequestMapping(value = "/admin/gym")
public class AdminGymController {

    private final GymService gymService;

    @PostMapping(value = "/create")
    public ResponseEntity createGym(@RequestBody GymDTO dto, @RequestHeader(name = "Authorization") String token) {
        String username = getUsername(token);
        log.info("Create gym: \ngym dto: {} \nusername: {} ", dto, username);
        gymService.createGym(dto, username);
        log.info("Response : Gym created successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Gym created successfully"));
    }

    @PutMapping(value = "/update")
    public ResponseEntity updateGym(@RequestBody GymDTO dto, @RequestHeader(name = "Authorization") String token) {
        String username = getUsername(token);
        log.info("Update gym: \ngym dto: {} \nusername: {} ", dto, username);
        gymService.updateGym(dto, username);
        log.info("Response : Gym updated successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Gym updated successfully"));
    }

    @GetMapping(value = "/all")
    public ResponseEntity getAllGyms(Pageable pageable) {
        log.info("Get all gyms: \npage request : {}", pageable);
        Page<GymDTO> gymPage = gymService.getAllGyms(pageable);
        log.info("Response : Gym page");
        return ResponseEntity.ok(new CommonResponse<>(true, gymPage));
    }

    @GetMapping(value = "/all/by-profile/{id}")
    public ResponseEntity getGymsByProfile(@PathVariable("id") long id, Pageable pageable) {
        log.info("Get gyms by business profile: \nprofile id: {} \npageRequest: {}", id, pageable);
        Page<GymDTO> gymPage = gymService.getGymsByBusinessProfile(id, pageable);
        log.info("Response : gym page");
        return ResponseEntity.ok(new CommonResponse<>(true, gymPage));
    }

    @GetMapping(value = "/by-id/{id}")
    public ResponseEntity getGymById(@PathVariable("id") long id) {
        log.info("Get gym by id : \ngym id: {}", id);
        GymDTO gymDTO = gymService.getById(id);
        log.info("Response : Gym by id");
        return ResponseEntity.ok(new CommonResponse<>(true, gymDTO));
    }

    @PostMapping(value = "/search")
    public ResponseEntity searchAllGyms(@RequestBody GymSearchDTO dto, Pageable pageable) {
        log.info("Search all gyms: \ntext: {} \npageRequest: {}", dto.getText(), pageable);
        Page<GymDTO> gymPage = gymService.searchGyms(dto.getText(), pageable);
        log.info("Response : gym page");
        return ResponseEntity.ok(new CommonResponse<>(true, gymPage));
    }

    @PostMapping(value = "/search/by-profile/{id}")
    public ResponseEntity searchGymsByProfile(@PathVariable("id") long id, @RequestBody GymSearchDTO dto, Pageable pageable) {
        log.info("Search gym by profile: \nprofile id: {} \ntext: {} \npage request: {}", id, dto.getText(), pageable);
        Page<GymDTO> gymPage = gymService.searchGymsByBusinessProfile(id, dto.getText(), pageable);
        log.info("Response : gym page");
        return ResponseEntity.ok(new CommonResponse<>(true, gymPage));
    }

    @GetMapping(value = "/all/profile/{id}")
    public ResponseEntity getAllGymsForBusinessProfile(@PathVariable long id) {
        log.info("Get all gyms for business profile : \nbusiness profile id: {}", id);
        List<ClassNameIdDTO> gyms = gymService.getAllGymsForBusinessProfile(id);
        log.info("Response : Gym name id list");
        return ResponseEntity.ok(new CommonResponse<>(true, gyms));
    }


    private String getUsername(String token) {
        return CustomGenerator.getJsonObjectFromJwt(token).getString("user_name");
    }
}

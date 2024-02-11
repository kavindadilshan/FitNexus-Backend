package com.fitnexus.server.controller.admin;

import com.fitnexus.server.dto.common.CommonResponse;
import com.fitnexus.server.dto.common.SearchDTO;
import com.fitnexus.server.dto.instructor.*;
import com.fitnexus.server.dto.instructor.*;
import com.fitnexus.server.dto.instructpackage.PackageForInstructorDTO;
import com.fitnexus.server.service.InstructorService;
import com.fitnexus.server.util.CustomGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/admin/instructor")
public class AdminInstructorController {

    private final InstructorService instructorService;

    @GetMapping(value = "/all/{id}")
    public ResponseEntity getAllInstructorsForBusinessProfile(@PathVariable("id") long id) {
        log.info("Get all instructors for business profile: \nbusiness profile id: {}", id);
        List<InstructorNameIdDTO> result = instructorService.getInstructorsForBusinessProfile(id);
        log.info("Response : Instructor name id list");
        return ResponseEntity.ok(new CommonResponse<>(true, result));
    }

    @GetMapping(value = "/rating/{id}")
    public ResponseEntity getInstructorRatings(@PathVariable long id, Pageable pageable) {
        log.info("Get instructor ratings : \ninstructor id : " + id);
        Page<InstructorRatingDTO> instructorRatings = instructorService.getInstructorRatings(id, pageable);
        log.info("Response : Instructor rating page");
        return ResponseEntity.ok(new CommonResponse<>(true, instructorRatings));
    }

    @PostMapping(value = "/package/type")
    public ResponseEntity createInstructorPackageType(@RequestBody InstructorPackageTypeDTO dto, @RequestHeader(name = "Authorization") String token) {
        log.info("Create instructor package type : \ninstructor package type DTO : {}", dto);
        String updatingUsername = getUsername(token);
        instructorService.createInstructorPackageType(dto, updatingUsername);
        log.info("Response : Instructor package type created successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Instructor package type created successfully"));
    }

    @PutMapping(value = "/package/type")
    public ResponseEntity updateInstructorPackageType(@RequestBody InstructorPackageTypeDTO dto, @RequestHeader(name = "Authorization") String token) {
        log.info("Update instructor package type : \ninstructor package type DTO : {}", dto);
        String updatingUsername = getUsername(token);
        instructorService.updateInstructorPackageType(dto, updatingUsername);
        log.info("Response : Instructor package type updated successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Instructor package type updated successfully"));
    }

    @GetMapping(value = "/package/type")
    public ResponseEntity getInstructorPackageTypes(Pageable pageable) {
        log.info("Get instructor package type page");
        Page<InstructorPackageTypeDTO> instructorPackageTypes = instructorService.getInstructorPackageTypes(pageable);
        log.info("Response : instructor package type page");
        return ResponseEntity.ok(new CommonResponse<>(true, instructorPackageTypes));
    }

    @GetMapping(value = "/package/types")
    public ResponseEntity getInstructorPackageTypes() {
        log.info("Get all instructor package types");
        List<InstructorPackageTypeDTO> instructorPackageTypes = instructorService.getInstructorPackageTypes();
        log.info("Response : Instructor package types list");
        return ResponseEntity.ok(new CommonResponse<>(true, instructorPackageTypes));
    }

    @PostMapping(value = "/package/type/search")
    public ResponseEntity searchInstructorPackageTypes(@RequestBody Map<String, String> data, Pageable pageable) {
        log.info("searchInstructorPackageTypes : data - {}", data.get("data"));
        Page<InstructorPackageTypeDTO> instructorPackageTypes = instructorService.searchInstructorPackageTypes(data.get("data"), pageable);
        log.info("instructor package types - {}", instructorPackageTypes.getContent());
        return ResponseEntity.ok(new CommonResponse<>(true, instructorPackageTypes));
    }

    @PostMapping(value = "/package")
    public ResponseEntity createInstructorPackage(@RequestBody InstructorPackageDTO packageDTO, @RequestParam long instructorId, @RequestHeader(name = "Authorization") String token) {
        log.info("Create instructor package : \ninstructor id: {} \npackage DTO: {} ", instructorId, packageDTO);
        String updatingUsername = getUsername(token);
        instructorService.createInstructorPackage(packageDTO, instructorId, updatingUsername);
        log.info("Response : Instructor package created successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Instructor package created successfully"));
    }

    @PutMapping(value = "/package")
    public ResponseEntity updateInstructorPackage(@RequestBody InstructorPackageDTO packageDTO, @RequestHeader(name = "Authorization") String token) {
        log.info("Update instructor package :  \npackage DTO: {} ", packageDTO);
        String updatingUsername = getUsername(token);
        instructorService.updateInstructorPackage(packageDTO, updatingUsername);
        log.info("Response - Instructor package updated successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Instructor package updated successfully"));
    }

    @PostMapping(value = "/package/search")
    public ResponseEntity searchInstructorPackages(@RequestBody Map<String, String> data, Pageable pageable) {
        log.info("Search instructor packages : \ntext - {}", data.get("data"));
        Page<InstructorPackageDTO> pacakges = instructorService.searchInstructorPackages(data.get("data"), pageable);
        log.info("Response : Instructor package search page");
        return ResponseEntity.ok(new CommonResponse<>(true, pacakges));
    }

    @GetMapping(value = "/package")
    public ResponseEntity getAllInstructorPackages(Pageable pageable) {
        log.info("Get all instructor packages");
        Page<InstructorPackageDTO> packages = instructorService.getAllInstructorPackages(pageable);
        log.info("Response : Instructor package page");
        return ResponseEntity.ok(new CommonResponse<>(true, packages));
    }

    @GetMapping(value = "/profile/packages/{id}")
    public ResponseEntity getAllInstructorPackagesForBusinessProfile(@PathVariable("id") long id, Pageable pageable) {
        log.info("Get all instructor packages by business profile : \nbusiness profile id: {}", id);
        Page<InstructorPackageDTO> result = instructorService.getAllInstructorPackagesForBusinessProfile(id, pageable);
        log.info("Response : Instructor package page");
        return ResponseEntity.ok(new CommonResponse<>(true, result));
    }

    @GetMapping(value = "/package/{id}")
    public ResponseEntity getInstructorPackageByID(@PathVariable long id) {
        log.info("Get instructor package by id : \ninstructor package id : " + id);
        InstructorPackageDTO instructorPackage = instructorService.getInstructorPackageById(id);
        log.info("Response : Instructor package by id");
        return ResponseEntity.ok(new CommonResponse<>(true, instructorPackage));
    }

    @GetMapping(value = "/packages/{id}")
    public ResponseEntity getPackagesForInstructor(@PathVariable("id") long id, Pageable pageable) {
        log.info("Get instructor packages by instructor - \ninstructor id : " + id);
        Page<PackageForInstructorDTO> packagesForInstructor = instructorService.getPackagesForInstructor(id, pageable);
        log.info("Response - instructor packages page");
        return ResponseEntity.ok(new CommonResponse<>(true, packagesForInstructor));
    }

    @PostMapping(value = "/packages/{id}/search")
    public ResponseEntity searchPackagesForInstructor(@PathVariable("id") long id, @RequestBody SearchDTO searchDTO, Pageable pageable) {
        log.info("Search packages for instructor : \nid: {} \ntext: {} \npage request : {}", id, searchDTO.getText(), pageable);
        Page<PackageForInstructorDTO> packages = instructorService.searchPackagesForInstructor(id, searchDTO.getText(), pageable);
        log.info("Response - instructor package page");
        return ResponseEntity.ok(new CommonResponse<>(true, packages));
    }

    @GetMapping(value = "/all")
    public ResponseEntity getAllInstructors(Pageable pageable, @RequestHeader(name = "Authorization") String token) {
        String username = getUsername(token);
        log.info("Get all instructors : \nusername: {} \npage request: {} ", username, pageable);
        Page<InstructorDTO> allInstructors = instructorService.getAllInstructors(username, pageable);
        log.info("Response - instructor page");
        return ResponseEntity.ok(new CommonResponse<>(true, allInstructors));
    }

    @PostMapping(value = "/search")
    public ResponseEntity searchInstructors(@RequestBody SearchDTO searchDTO, Pageable pageable) {
        log.info("Search instructors : \ntext: {} \npage request : {}", searchDTO.getText(), pageable);
        Page<InstructorDTO> allInstructors = instructorService.searchInstructors(searchDTO.getText(), pageable);
        log.info("Response - instructor page");
        return ResponseEntity.ok(new CommonResponse<>(true, allInstructors));
    }

    @PostMapping(value = "/type/create")
    public ResponseEntity createInstructorType(@RequestParam(value = "name") String name) {
        log.info("Create instructor type : \nname : {}", name);
        instructorService.createInstructorType(name);
        log.info("Response : instructor type created successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "instructor type created successfully"));
    }

    @PutMapping(value = "/type/update/{id}")
    public ResponseEntity updateInstructorType(@PathVariable("id") long id, @RequestParam(value = "name") String name) {
        log.info("Update instructor type : \nid : {} \nname: {}", id, name);
        instructorService.updateInstructorType(id, name);
        log.info("Response : instructor type updated successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "instructor type updated successfully"));
    }

    @DeleteMapping(value = "/type/remove/{id}")
    public ResponseEntity removeInstructorType(@PathVariable("id") long id) {
        log.info("Remove instructor type : \nid : {}", id);
        instructorService.removeInstructorType(id);
        log.info("Response : instructor type removed successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "instructor type removed successfully"));
    }

    @GetMapping(value = "/type/all")
    public ResponseEntity getAllInstructorTypes(Pageable pageable) {
        log.info("Get all instructor types page");
        Page<Map<String, Object>> allInstructorTypes = instructorService.getAllInstructorTypes(pageable);
        log.info("Response : instructor types page");
        return ResponseEntity.ok(new CommonResponse<>(true, allInstructorTypes));
    }

    @PostMapping(value = "/type/search")
    public ResponseEntity searchInstructorType(@RequestBody Map<String, String> data, Pageable pageable) {
        log.info("Search instructor type : \nname : {}", data.get("name"));
        Page<Map<String, Object>> instructorTypes = instructorService.searchInstructorType(data.get("name"), pageable);
        log.info("Response : instructor type search page");
        return ResponseEntity.ok(new CommonResponse<>(true, instructorTypes));
    }

    private String getUsername(String token) {
        return CustomGenerator.getJsonObjectFromJwt(token).getString("user_name");
    }
}
